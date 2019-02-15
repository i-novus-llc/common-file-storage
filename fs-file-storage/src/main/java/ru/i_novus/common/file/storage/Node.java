package ru.i_novus.common.file.storage;

import lombok.Getter;

import java.util.Date;

/**
 * User: iryabov
 * Date: 21.05.13
 * Time: 20:16
 */
@Getter
public class Node {
    private String name;
    private String path;
    private Date lasModified;
    private boolean isFolder;
    private String type;

    public Node(String name, String path, Date lasModified, boolean folder) {
        this.name = name;
        this.path = path;
        this.lasModified = lasModified;
        isFolder = folder;
        int idx = name.lastIndexOf('.');
        if (idx > 0 && idx < (name.length() - 1)) {
            type = name.substring(idx + 1);
        }
    }
}
