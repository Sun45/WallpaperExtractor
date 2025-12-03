package cn.sun45_.wallpaperextractor.utils;

import cn.sun45_.wallpaperextractor.Constants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 文件操作工具类
 *
 * <p>提供文件拷贝、目录操作等常用工具方法</p>
 */
public class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    /**
     * 拷贝源目录中的第一个视频文件到目标目录
     *
     * <p>从源目录拷贝视频文件到目标目录，只拷贝指定扩展名的视频文件</p>
     *
     * @param sourceDir 源目录路径（包含视频文件的目录）
     * @param targetDir 目标目录路径（视频文件将被拷贝到此目录）
     * @throws IOException       如果发生I/O错误或拷贝失败
     * @throws SecurityException 如果没有文件访问权限
     * @see Constants#VIDEO_EXTENSIONS 支持的视频文件扩展名
     */
    public static void copyVideoFiles(Path sourceDir, Path targetDir) throws IOException {
        if (sourceDir == null || targetDir == null) {
            throw new IOException("源目录或目标目录不能为null");
        }

        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IOException("文件夹不存在");
        }

        Files.createDirectories(targetDir);

        boolean copied = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path sourceFile : stream) {
                if (!Files.isDirectory(sourceFile)) {
                    String fileName = sourceFile.getFileName().toString().toLowerCase();
                    if (java.util.Arrays.stream(Constants.VIDEO_EXTENSIONS).anyMatch(fileName::endsWith)) {
                        Path targetFile = targetDir.resolve(sourceFile.getFileName());
                        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        copied = true;
                        break; // 只拷贝第一个匹配的视频文件
                    }
                }
            }
        }

        if (!copied) {
            throw new IOException("视频文件不存在");
        }
    }

    /**
     * 拷贝整个目录到目标路径，保持源目录结构
     *
     * <p>递归拷贝源目录及其所有子目录和文件到目标路径</p>
     *
     * @param sourceDir 源目录路径（要拷贝的目录）
     * @param targetDir 目标目录路径（源目录将被拷贝到此目录下）
     * @throws IOException 如果发生I/O错误或拷贝失败
     * @see StandardCopyOption#REPLACE_EXISTING 覆盖已存在文件
     */
    public static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (sourceDir == null || targetDir == null) {
            throw new IOException("源目录或目标目录不能为null");
        }

        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IOException("文件夹不存在");
        }

        // 创建目标目录（包含源目录名的子目录）
        Path targetPath = targetDir.resolve(sourceDir.getFileName());
        Files.createDirectories(targetPath);

        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream.forEach(source -> {
                try {
                    if (!source.equals(sourceDir)) {  // 跳过源目录本身
                        // 计算相对路径
                        Path relativePath = sourceDir.relativize(source);
                        Path destination = targetPath.resolve(relativePath);

                        // 如果是目录则创建，否则拷贝文件
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * 安全删除目录及其内容
     *
     * @param dirPath 要删除的目录路径
     * @return 如果删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(Path dirPath) {
        if (dirPath == null) {
            return false;
        }

        if (!Files.exists(dirPath)) {
            return false;
        }

        if (!Files.isDirectory(dirPath)) {
            return false;
        }

        try {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "删除文件失败: " + path, ex);
                        }
                    });
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "删除目录失败: " + dirPath, ex);
            return false;
        }
    }
}
