package ru.i_novus.common.file.storage;

import java.io.InputStream;

/**
 * User: RMakhmutov
 * Date: 13.02.12
 * Time: 17:56
 */
public interface FileStorage
{
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
     * Save the file in the file storage in the specified path
     *
     * @param content File content
     * @param path Full path to the file in the file storage
     * @return Full path to saved file in the file storage
     */
    String saveContentWithFullPath(InputStream content, String path);

    /**
     * Remove file from file storage
     *
     * @param path Path to file in file storage
     */
	void removeContent(String path);
}
