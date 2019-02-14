package ru.i_novus.common.file.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FileManager {

    private String space;

    public FileManager(String root, String workspace, String space) {
        if (root == null) {
            throw new IllegalArgumentException("root argument for FileManager cannot be null");
        }
        this.space = root;
        if (workspace != null)
            this.space = Paths.get(root, workspace).toString();
        if (space != null)
            this.space = Paths.get(this.space, space).toString();
    }

    private Path resolveFile(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null, cannot resolve file");
        }
        return Paths.get(space, path);
    }

    public InputStream getContent(String path) {
        Path target = resolveFile(path);

        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            logger.error("Cannot get file '{}' content", target, e);
            throw new UncheckedIOException(e);
        }
    }

    public void saveContent(InputStream content, String path) {
        Path target = resolveFile(path);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Cannot save data as file content '{}'", target, e);
            throw new UncheckedIOException(e);
        }
    }

    public boolean isFileExist(String path) {
        Path target = resolveFile(path);
        return Files.exists(target);
    }

    public void removeContent(String path) {
        Path target = resolveFile(path);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            logger.warn("Cannot delete file '{}'", path, e);
        }
    }

    public List<Node> getChildrenOf(String path) {
        Path target = resolveFile(path);
        try {
            return Files.walk(target, 1).map(
                    file -> {
                        FileTime lastModified = null;
                        try {
                            lastModified = Files.getLastModifiedTime(file);
                        } catch (IOException e) {
                            logger.warn("Cannot get file '{}' last modified datetime", file, e);
                        }

                        return new Node(
                                file.getFileName().toString(),
                                file.toString(),
                                lastModified == null ? null : new Date(lastModified.toMillis()),
                                Files.isDirectory(file));
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Cannot list children of '{}' path", path, e);
            return Collections.emptyList();
        }
    }
}