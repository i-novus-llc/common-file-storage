package ru.i_novus.common.file.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class FileManager
{
	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

	private File space;

	public FileManager(String root, String workspace, String space)
	{
        if (root == null)
        {
            throw new IllegalArgumentException("root argument for FileManager cannot be null");
        }
		this.space = new File(root);
		if (workspace != null)
			this.space = new File(root, workspace);
		if (space != null)
			this.space = new File(this.space, space);
	}

	private File resolveFile(String path)
	{
        if (path == null)
        {
            throw new IllegalArgumentException("path is null, cannot resolve file");
        }
		return new File(space, path);
	}

	public InputStream getContent(String path)
	{
		File target = resolveFile(path);
		Closer closer = new Closer();
		try
		{
			FileInputStream fis = closer.register(new FileInputStream(target));
			return closer.register(new BufferedInputStream(fis));
		}
		catch (FileNotFoundException e)
		{
			closer.rethrow(e);
			closer.close();
			throw new IllegalStateException();//unreachable due to closer
		}
	}

	public void saveContent(InputStream content, String path)
	{
		File target = resolveFile(path);
		new File(target.getParent()).mkdirs();
		Closer closer = new Closer();
		try
		{
			FileOutputStream fos = closer.register(new FileOutputStream(target));
			BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
			int b;

			while ((b = content.read()) != -1)
			{
				bos.write(b);
			}
		}
		catch (IOException e)
		{
			closer.rethrow(e);
		}
		finally
		{
			closer.close();
		}
	}

    public boolean isFileExist(String path)
    {
        File target =resolveFile(path);
        return target.exists();
    }

	public void removeContent(String path)
	{
		File target = resolveFile(path);
        if (target.exists())
        {
            target.delete();
        }
	}

    public List<Node> getChildrenOf(String path)
    {
        List<Node> children = new LinkedList<>();
        File target = resolveFile(path);
        File[] files = target.listFiles();
        if (files != null)
        {
            for (File child : files)
            {
                children.add(new Node(child.getName(), path + "/" + child.getName(), new Date(child.lastModified())
                        , child.isDirectory()));
            }
        }
        return children;
    }

	class Closer//todo use com.google.common.io.Closer instead
	{
		private Deque<Closeable> stack = new ArrayDeque<>();

		private Throwable error;

		public <T extends Closeable> T register(T closeable)
		{
			stack.push(closeable);
			return closeable;
		}

		public void close()
		{
			while (!stack.isEmpty())
			{
				Closeable resource = stack.pop();
				try
				{
					resource.close();
				}
				catch (IOException e)
				{
					if (error == null)
						error = e;
					else
						logger.error(e.getMessage(), e);
				}
			}
			if (error != null)
				throw new RuntimeException(error);
		}

		public void rethrow(IOException e)
		{
			error = e;
		}
	}
}