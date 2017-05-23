package ru.i_novus.common.file.storage.pg;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import ru.i_novus.common.file.storage.api.FileStorage;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

/**
 * Created by rgogolev on 18.05.2017.
 */

public class PgFileStorage implements FileStorage{


    private JdbcTemplate jdbcTemplate;

    private String space;

    private DataSource dataSource;

    public PgFileStorage(DataSource dataSource, String space){
        this.space=space;
        this.dataSource=dataSource;
        this.jdbcTemplate= new JdbcTemplate(this.dataSource);
        createSchema();
        createTable();
    }

    @Override
    public InputStream getContent(String path) {
        String getFileContentSql = "select content from file_storage."+space+" where id='"+path+"';";
        final DefaultLobHandler lobHandler = new DefaultLobHandler();
        lobHandler.setWrapAsLob(true);
        List file = jdbcTemplate.query(getFileContentSql, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                Blob blob = rs.getBlob(1);
                byte[] bytes = blob.getBytes(1, (int) blob.length());
                return bytes;
            }
        });
        return new ByteArrayInputStream((byte[]) file.get(0));
    }

    @Override
    public String saveContent(InputStream content, String name) {
        DefaultLobHandler lobHandler = new DefaultLobHandler();
        lobHandler.setWrapAsLob(true);
        try {
            jdbcTemplate.update(
                    "INSERT INTO file_storage." + space + " (id, content) VALUES (?, ?)",
                    new Object[]{
                            name,
                            new SqlLobValue(IOUtils.toByteArray(content), lobHandler)
                    },
                    new int[]{Types.VARCHAR, Types.BLOB});

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return name;
    }

    @Override
    public void removeContent(String path) {
        String deleteStatement = "DELETE FROM file_storage."+space+" WHERE id=?";
        jdbcTemplate.update(deleteStatement,path);
    }

    private void createSchema(){
        String schemaIsCreated = "select EXISTS (select * from pg_catalog.pg_namespace where nspname='file_storage');";
        String schemaCreate = "CREATE SCHEMA file_storage;";

        Boolean exist = jdbcTemplate.queryForObject(
                schemaIsCreated, Boolean.class);
        if(!exist){
            jdbcTemplate.batchUpdate(schemaCreate);
        }
    }

    private void createTable(){
        String tableIsCreated = "select exists(select * from pg_tables where tablename='"+space+"' and schemaname='file_storage');";
        String tableCreate = "CREATE TABLE file_storage."+space+
                "            (" +
                "                id character varying NOT NULL, " +
                "                content oid NOT NULL, " +
                "                CONSTRAINT document_pk PRIMARY KEY (id)" +
                "            )" +
                "            WITH (" +
                "            OIDS=FALSE" +
                "            );";

        Boolean exist = jdbcTemplate.queryForObject(
                tableIsCreated, Boolean.class);
        if(!exist){
            jdbcTemplate.batchUpdate(tableCreate);
        }
    }
}
