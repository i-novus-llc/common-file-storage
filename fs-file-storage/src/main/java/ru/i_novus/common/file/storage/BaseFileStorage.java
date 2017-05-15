package ru.i_novus.common.file.storage;

import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public abstract class BaseFileStorage implements FsFileStorage
{
	@Value ("${fileStorage.root}")
	private String root;

	private FileManager fileManager;

	protected abstract String getWorkspaceName();

	protected abstract String getSpaceName();

    @Override
	public InputStream getContent(String path)
	{
		return getFileManager().getContent(path);
	}

	@Override
	public String saveContent(InputStream content, String path)
	{
        Calendar calendar = Calendar.getInstance();
        String separator = "/";
        String fullPath = new StringBuilder().append(calendar.get(Calendar.YEAR)).append(separator)
                .append(calendar.get(Calendar.MONTH) + 1).append(separator).append(calendar.get(Calendar.DATE))
                .append(separator).append(calendar.get(Calendar.HOUR_OF_DAY)).append(separator).append(path).toString();

        getFileManager().saveContent(content, fullPath);
        return fullPath;
	}

    @Override
    public String saveContentWithFullPath(InputStream content, String path)
    {
        getFileManager().saveContent(content, path);
        return path;
    }

	@Override
	public void removeContent(String name)
	{
		getFileManager().removeContent(name);
	}

    public boolean isExistContent(String name)
    {
       return getFileManager().isFileExist(name);
    }

    public List<Node> getChildrenOf(String path)
    {
        return getFileManager().getChildrenOf(path);
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    private synchronized FileManager getFileManager()
	{
		if (fileManager == null)
		{
			fileManager = new FileManager(root, getWorkspaceName(), getSpaceName());
		}
		return fileManager;
	}
}
