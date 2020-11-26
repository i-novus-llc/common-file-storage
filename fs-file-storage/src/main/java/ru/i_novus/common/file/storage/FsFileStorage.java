package ru.i_novus.common.file.storage;

import ru.i_novus.common.file.storage.api.FileStorage;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * User: RMakhmutov
 * Date: 13.02.12
 * Time: 17:56
 */
public interface FsFileStorage extends FileStorage
{
    String saveContentWithFullPath(InputStream content, String path);

    /**
     *
     * Save the file in the file storage with specified name taking into account the default name encoding (ISO_8859_1).
     *
     * @param content File content
     * @param name Simple name of file
     * @param pathIds Entity identifiers that are used to form a unique path in the file system
     *                <br>(e.g. add34cb3-5dcc-4fd3-86cb-907263965f10/../b4e45887-86a8-440f-9c19-bf456c9bcb13/file_name)
     * @return Full path (file name is Base64 encoded) to saved file in file storage
     */
    String saveUploadedFileContent(
            InputStream content,
            String name,
            UUID... pathIds
    );

    /**
     *
     * Save the file in the file storage with specified name taking into account the name encoding.
     *
     * @param content File content
     * @param name Simple name of file
     * @param charset File name encoding
     * @param pathIds Entity identifiers that are used to form a unique path in the file system
     *                <br>(e.g. add34cb3-5dcc-4fd3-86cb-907263965f10/../b4e45887-86a8-440f-9c19-bf456c9bcb13/file_name)
     * @return Full path (file name is Base64 encoded) to saved file in file storage
     */
    String saveUploadedFileContent(
            InputStream content,
            String name,
            Charset charset,
            UUID... pathIds
    );
}
