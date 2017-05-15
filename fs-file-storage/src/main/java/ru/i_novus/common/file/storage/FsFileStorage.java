package ru.i_novus.common.file.storage;

import java.io.InputStream;

/**
 * User: RMakhmutov
 * Date: 13.02.12
 * Time: 17:56
 */
public interface FsFileStorage extends FileStorage
{
    String saveContentWithFullPath(InputStream content, String path);
}
