package ru.i_novus.common.cassandra.file.storage;

import com.datastax.driver.core.*;
import lombok.extern.slf4j.Slf4j;
import ru.i_novus.common.file.storage.api.FileStorage;
import ru.i_novus.common.file.storage.api.exception.EmptyFileException;
import ru.i_novus.common.file.storage.api.exception.NotFoundException;

import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by tnurdinov on 15.05.2017.
 */
@Slf4j
public class CassandraFileStorage implements FileStorage {

    private int chunkSize;

    private String space;

    private List<String> cassandraAddresses;

    private String replication;

    private Session session;

    private PreparedStatement insertChunk;

    private PreparedStatement updateMetadata;

    private PreparedStatement selectMetadata;

    private PreparedStatement selectChunk;

    private PreparedStatement deleteChunks;

    private Cluster cluster;

    public CassandraFileStorage(String space, List<String> cassandraAddresses, String replication,  Integer chunkSize) {
        this.replication = replication != null ? replication : "{ 'class' : 'SimpleStrategy', 'replication_factor' : 1 }";
        try {
            cluster = Cluster.builder()
                    .addContactPoints(cassandraAddresses.toArray(new String[0]))
                    .build();
            session = cluster.connect();
        } catch (RuntimeException e) {
            logger.error("Cannot build cluster", e);
            if (cluster != null) {
                cluster.close();
            }
            throw e;
        }
        this.space = space;
        this.cassandraAddresses = cassandraAddresses;
        init();
        insertChunk = session.prepare("insert into file_storage." + space + "(name, chunk_num, data, metadata) values(?, ?, ?, ?);");
        updateMetadata = session.prepare("update file_storage." + space + " set metadata = ? where name = ? and chunk_num = 1;");
        selectChunk = session.prepare("select * from file_storage." + space + " where name = ? and chunk_num = ?;");
        selectMetadata = session.prepare("select metadata from file_storage." + space + " where name = ? and chunk_num = 1;");
        deleteChunks = session.prepare("delete from file_storage." + space + " where name = ? and chunk_num in ?");
        this.chunkSize = chunkSize != null ? (chunkSize * 1024) : 102400 ;
    }

    private void init(){
        if(!keySpaceExists()) {
            createKeySpace();
        }
        if(!tableExists()) {
            createTable();
        }
    }

    @PreDestroy
    public void destroy(){
        if(cluster != null) {
            cluster.close();
        }
    }

    @Override
    public InputStream getContent(String path) {
        ResultSet rs = session.execute(selectChunk.bind(path, 1));
        if (!rs.iterator().hasNext())
            throw new NotFoundException();
        Row row = rs.one();
        Map<String, Integer> metadata = row.getMap("metadata", String.class, Integer.class);
        Integer chunkCount = metadata.get("chunkCount");
        Integer objectSize = metadata.get("objectSize");
        if(chunkCount == 1) {
            ByteBuffer byteBuffer = row.getBytes("data");
            return getData(byteBuffer);
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[objectSize]);
        bb.put(row.getBytes("data"));
        for (int i = 2; i<=chunkCount; i++) {
            bb.put(session.execute(selectChunk.bind(path, i)).one().getBytes("data"));
        }

        return getData(bb);
    }

    @Override
    public String saveContent(InputStream content, String name) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
            byteBuffer.position(0);
            boolean done = false;
            int nChunksWritten = 0;
            int nBytesWritten = 0;
            int firstChunkSize = 0;
            while (!done){
                int nBytesRead = readFully(content, byteBuffer.array(), 0, chunkSize);
                if (nBytesRead > 0) {
                    byteBuffer.limit(nBytesRead);
                    if(nChunksWritten + 1 == 1){
                        firstChunkSize = byteBuffer.limit();
                    }
                    logger.debug("WRITE " + (nChunksWritten + 1) + " size=" + byteBuffer.limit());
                    writeChunk(name, (nChunksWritten+1), byteBuffer);
                    nBytesWritten += byteBuffer.limit();
                    nChunksWritten++;
                }
                else {
                    done = true;
                }
            }

            if (nChunksWritten == 0)
                throw new EmptyFileException();

            writeMetadata(name, firstChunkSize, nChunksWritten, nBytesWritten);
            return name;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void removeContent(String path) {
        ResultSet rs = session.execute(selectMetadata.bind(path));
        if (!rs.iterator().hasNext())
            return;
        Row row = rs.one();
        Map<String, Integer> metadata = row.getMap("metadata", String.class, Integer.class);
        Integer count = metadata.get("chunkCount");
        List<Integer> chunks = new ArrayList<>(count);
        for (int i = 1; i<=count; i++){
            chunks.add(i);
        }
        session.execute(deleteChunks.bind(path, chunks));
    }

    private InputStream getData(ByteBuffer byteBuffer) {
        try (InputStream is = new ByteArrayInputStream(byteBuffer.array())){
            return is;
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    private int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int total = 0;
        for (;;) {
            int got = in.read(b, off + total, len - total);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            }
            else {
                total += got;
                if (total == len)
                    return total;
            }
        }
    }

    private void writeChunk(String name, Integer chunkNumber, ByteBuffer data) {
        Map<String, Integer> metadata = new HashMap<>();
        metadata.put("chunkSize", data.limit());
        session.execute(insertChunk.bind(name, chunkNumber, data, metadata));
    }

    private void writeMetadata(String name, int chunkSize, int chunkCount, int objectSize){
        Map<String, Integer> metadata = new HashMap<>();
        metadata.put("chunkCount", chunkCount);
        metadata.put("objectSize", objectSize);
        metadata.put("chunkSize", chunkSize);
        session.execute(updateMetadata.bind(metadata, name));
    }
    private boolean keySpaceExists(){
        ResultSet rows = session.execute("select keyspace_name from system_schema.keyspaces where keyspace_name = 'file_storage'");
        return rows.iterator().hasNext();
    }

    private boolean tableExists(){
        ResultSet rows = session.execute("select table_name from system_schema.tables where keyspace_name = 'file_storage' and table_name = '" +space+"'");
        return rows.iterator().hasNext();
    }
    private void createKeySpace(){
        logger.info("creating file_storage keyspace");
        session.execute("CREATE KEYSPACE file_storage  WITH REPLICATION = " + replication);
    }

    private void createTable(){
        logger.info("creating file_storage."+ space + " table");
        session.execute("CREATE TABLE file_storage."+ space +" (" +
                "  name text," +
                "  chunk_num int, " +
                "  data blob, " +
                "  metadata map<text, int>, " +
                "  PRIMARY KEY ((name, chunk_num)) " +
                ");");
    }
}
