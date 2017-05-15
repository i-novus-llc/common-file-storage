package ru.i_novus.common.file.storage;

import java.util.Date;

/**
 * User: iryabov
 * Date: 21.05.13
 * Time: 20:16
 */
public class Node
{
    private String name;
    private String path;
    private Date lasModified;
    private boolean isFolder;
    private String type;

    public Node(String name, String path, Date lasModified, boolean folder)
    {
        this.name = name;
        this.path = path;
        this.lasModified = lasModified;
        isFolder = folder;
        int idx = name.lastIndexOf(".");
        if (idx > 0)
        {
            type = name.substring(name.indexOf("."));
        }
    }

    public String getName()
    {
        return name;
    }

    public String getPath()
    {
        return path;
    }

    public Date getLasModified()
    {
        return lasModified;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public String getType()
    {
        return type;
    }
}
