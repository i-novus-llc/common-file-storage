package ru.i_novus.common.file.storage.api;

import java.io.InputStream;

/**
 * Created by tnurdinov on 15.05.2017.
 */
public interface FileStorage {

    /**
     * Get content of file s input stream
     *
     * @param path Path to file in file storage
     * @return content of file
     */
    InputStream getContent(String path);

    /**
     * Save the file in the file storage with specified name.
     * All additional folders will be created. Returns full path, which can differ from simple file name.
     *
     * @param content File content
     * @param name Simple name of file
     * @return Full path to saved file in file storage
     */
    String saveContent(InputStream content, String name);

    /**
     * Remove file from file storage
     *
     * @param path Path to file in file storage
     */
    void removeContent(String path);
}
