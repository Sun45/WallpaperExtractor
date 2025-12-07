package cn.sun45_.wallpaperextractor.monitor;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Steam Workshop日志文件监听器
 *
 * <p>负责监控Steam日志文件的变化，实时检测Workshop项目的订阅状态变化</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>定时检测指定日志文件的变化（每秒检测一次）</li>
 *   <li>当文件内容发生变化时解析并通知监听器</li>
 *   <li>支持基于时间戳的日志过滤</li>
 *   <li>使用缓存机制避免重复通知相同的内容</li>
 *   <li>支持多次启动和停止监听</li>
 * </ul>
 *
 * <p>工作原理：</p>
 * <ul>
 *   <li>通过定时任务定期检查文件修改时间</li>
 *   <li>使用{@link LogAnalyzer}解析日志内容</li>
 *   <li>比较新旧数据，只在内容真正变化时通知监听器</li>
 *   <li>使用原子引用确保线程安全</li>
 * </ul>
 *
 * @see LogAnalyzer 日志分析器
 * @see FileChangeListener 文件变化监听器接口
 */
public class FileWatcher {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = Logger.getLogger(FileWatcher.class.getName());

    /**
     * 文件变化监听器
     */
    private final FileChangeListener listener;

    /**
     * 定时任务调度器
     */
    private volatile ScheduledExecutorService scheduler;

    /**
     * 起始时间（用于过滤日志）
     */
    private String startTime;

    /**
     * Steam路径原子引用（线程安全）
     */
    private final AtomicReference<String> steamPathRef = new AtomicReference<>();

    /**
     * 文件最后修改时间
     */
    private long lastModifiedTime = 0L;

    /**
     * 缓存的工作坊数据原子引用（线程安全）
     */
    private final AtomicReference<List<WorkshopData>> cacheModelsRef = new AtomicReference<>();

    /**
     * 监听器运行状态标志
     */
    private volatile boolean running = false;

    /**
     * 构造文件监听器
     *
     * @param listener  文件变化监听器，不能为null
     * @param startTime 起始时间，用于过滤日志，可以为null
     */
    public FileWatcher(FileChangeListener listener, String startTime) {
        this.listener = Objects.requireNonNull(listener, ResourceManager.getString("error.file.change.listener.null"));
        this.startTime = startTime;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, ResourceManager.getString("thread.file.watcher"));
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 开始监听指定路径下的日志文件
     *
     * <p>启动文件监听任务，如果已有监听任务运行，会先停止之前的任务</p>
     *
     * @param steamPath Steam安装路径（包含Steam.exe的目录）
     * @throws IllegalArgumentException 如果steamPath为null或空
     */
    public synchronized void startWatching(String steamPath) {
        if (steamPath == null || steamPath.trim().isEmpty()) {
            throw new IllegalArgumentException(ResourceManager.getString("error.steam.path.null.or.empty"));
        }

        // 停止当前监听
        stopWatching();

        // 更新路径和状态
        steamPathRef.set(steamPath.trim());
        running = true;

        // 启动新的监听任务
        scheduler.scheduleAtFixedRate(this::detectFile, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 更新起始时间
     *
     * @param newTime 新的起始时间，格式为"yyyy-MM-dd HH:mm:ss"
     * @throws IllegalArgumentException 如果newTime为null
     */
    public synchronized void updateStartTime(String newTime) {
        if (newTime == null) {
            throw new IllegalArgumentException(ResourceManager.getString("error.start.time.null"));
        }
        this.startTime = newTime;
        // 清空缓存以重新过滤日志
        lastModifiedTime = 0L;
        cacheModelsRef.set(null);
    }

    /**
     * 检测文件变化（线程安全）
     *
     * <p>定时检测日志文件的变化，当文件内容发生变化时提取Workshop数据并通知监听器</p>
     *
     * @see Constants#LOG_DIRECTORY 日志目录相对路径
     * @see Constants#LOG_FILENAME 日志文件名
     */
    private synchronized void detectFile() {
        if (!running) {
            return;
        }

        String steamPath = steamPathRef.get();
        if (steamPath == null) {
            return;
        }

        // 获取文件路径
        Path filePath = Paths.get(steamPath, Constants.LOG_DIRECTORY, Constants.LOG_FILENAME);

        try {
            // 获取文件的当前修改时间
            long currentModifiedTime = Files.getLastModifiedTime(filePath).toMillis();

            // 如果修改时间没有变化，直接返回
            if (currentModifiedTime == lastModifiedTime) {
                return;
            }

            // 更新修改时间
            lastModifiedTime = currentModifiedTime;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, ResourceManager.getString("error.get.file.modify.time.error"), e);
            return;
        }

        // 读取文件并提取ID列表
        List<String> newContent = readFileByLine(filePath);
        List<WorkshopData> newModels = newContent != null ?
                LogAnalyzer.analyzeLog(newContent, startTime) : null;

        // 比较ID列表
        List<WorkshopData> oldModels = cacheModelsRef.get();
        if (oldModels == null || newModels == null) {
            if (oldModels != newModels) {
                cacheModelsRef.set(newModels);
                listener.onFileChanged(newModels);
            }
            return;
        }

        if (oldModels.size() != newModels.size()) {
            cacheModelsRef.set(newModels);
            listener.onFileChanged(newModels);
            return;
        }

        // 比较ID内容
        for (int i = 0; i < newModels.size(); i++) {
            if (!Objects.equals(oldModels.get(i), newModels.get(i))) {
                cacheModelsRef.set(newModels);
                listener.onFileChanged(newModels);
                return;
            }
        }
    }

    /**
     * 读取文件内容
     *
     * <p>读取指定路径的文件内容，按行返回字符串列表</p>
     *
     * <p>实现细节：</p>
     * <ul>
     *   <li>检查文件是否存在，不存在返回null</li>
     *   <li>检查文件是否可读，不可读返回null</li>
     *   <li>使用UTF-8编码读取文件所有行</li>
     *   <li>捕获并记录读取异常，返回null</li>
     * </ul>
     *
     * @param filePath 文件路径
     * @return 文件内容列表（每行一个字符串），如果文件不存在、不可读或读取失败返回null
     */
    private List<String> readFileByLine(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                return null;
            }

            if (!Files.isReadable(filePath)) {
                return null;
            }

            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, ResourceManager.getString("error.read.file.lines.error"), e);
            return null;
        }
    }

    /**
     * 停止监听
     */
    public synchronized void stopWatching() {
        if (!running) {
            return;
        }

        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, ResourceManager.getString("thread.file.watcher"));
                thread.setDaemon(true);
                return thread;
            });
        }
        cacheModelsRef.set(null);
    }

    /**
     * 日志文件变化监听器接口
     */
    public interface FileChangeListener {
        /**
         * 当监听的日志文件发生变化时调用
         *
         * @param workshopData 筛选后的workshopModel列表
         */
        void onFileChanged(List<WorkshopData> workshopData);
    }
}
