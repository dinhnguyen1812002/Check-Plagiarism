package org.checkplagiarism.reporting.jsonfactory;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides Methods for creating directories.
 */
public class DirectoryManager {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryManager.class);


    public static File createDirectory(String path, String name, File file, File submissionRoot) throws IOException {
        File directory;
        String fileFullPath = file.getPath();
        String submissionRootPath = submissionRoot.getPath();
        String filePathWithoutRootName = fileFullPath.substring(submissionRootPath.length());
        String outputRootDirectory = Path.of(path, name).toString();
        if ("".equals(filePathWithoutRootName)) {
            directory = new File(Path.of(outputRootDirectory, name).toString());
        } else {
            directory = new File(outputRootDirectory + filePathWithoutRootName);
        }
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create dir.");
        }
        return directory;
    }

    public static File createDirectory(String path, String name) throws IOException {
        File directory = new File(path.concat(File.separator).concat(name));
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create dir.");
        }
        return directory;
    }

    public static void createDirectory(String path) throws IOException {
        createDirectory(path, "");
    }

    public static void deleteDirectory(String path) {
        try (var f = Files.walk(Path.of(path))) {
            f.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            logger.error("Could not delete folder " + path, e);
        }
    }

    /**
     * Zip the directory identified by the given path
     * @param path The path that identifies the directory to zip
     * @return True if zip was successful, false otherwise
     */
    public static boolean zipDirectory(String path) {
        Path p = Path.of(path);
        String zipName = path + ".zip";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName))) {

            Files.walkFileTree(p, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = p.relativize(file);
                    zos.putNextEntry(new ZipEntry(targetFile.toString()));

                    byte[] bytes = Files.readAllBytes(file);
                    zos.write(bytes, 0, bytes.length);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    logger.error("Unable to zip " + file, exc);
                    throw exc;
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            deleteDirectory(zipName);
            return false;
        }
        logger.info("Successfully zipped report files: {}", zipName);

        return true;
    }
}
