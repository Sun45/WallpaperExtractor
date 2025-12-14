package cn.sun45_.wallpaperextractor.service;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.model.WorkshopListItem;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import cn.sun45_.wallpaperextractor.utils.FileUtils;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

/**
 * 文件拷贝服务类
 *
 * <p>负责处理Workshop项目的文件拷贝操作，与UI控制器解耦，实现业务逻辑与界面逻辑的分离</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>执行文件拷贝任务</li>
 *   <li>管理拷贝状态和进度</li>
 *   <li>处理拷贝过程中的错误</li>
 *   <li>提供拷贝结果回调</li>
 *   <li>支持异步拷贝操作</li>
 *   <li>支持拷贝任务取消</li>
 * </ul>
 *
 * <p>设计模式：</p>
 * <ul>
 *   <li>服务模式：封装拷贝业务逻辑</li>
 *   <li>观察者模式：通过回调函数通知状态变化</li>
 *   <li>命令模式：使用Task封装拷贝操作</li>
 * </ul>
 *
 * <p>依赖关系：</p>
 * <ul>
 *   <li>依赖WorkshopDataService获取项目数据</li>
 *   <li>依赖FileUtils执行实际的文件操作</li>
 *   <li>依赖AppConfig获取配置信息</li>
 *   <li>被MainController调用执行拷贝操作</li>
 * </ul>
 *
 * <p>线程安全：</p>
 * <ul>
 *   <li>使用JavaFX Task确保UI线程安全</li>
 *   <li>拷贝操作在后台线程执行</li>
 *   <li>状态更新通过Platform.runLater()确保线程安全</li>
 * </ul>
 */
public class CopyService {

    private final WorkshopDataService dataService;
    private Task<Void> currentCopyTask;

    /**
     * 构造函数
     *
     * @param dataService Workshop数据服务
     */
    public CopyService(WorkshopDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 拷贝配置类
     *
     * <p>封装文件拷贝操作的相关配置参数，提供类型安全的配置访问</p>
     *
     * <p>主要配置项：</p>
     * <ul>
     *   <li>拷贝模式：视频文件模式或目录模式</li>
     *   <li>拷贝路径：用户指定的目标路径字符串</li>
     *   <li>目标目录：解析后的目标目录路径对象</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>
     * CopyConfig config = new CopyConfig(true, "/path/to/target", Paths.get("/path/to/target"));
     * </pre>
     */
    public static class CopyConfig {
        /**
         * 拷贝模式：true表示拷贝视频文件模式，false表示拷贝目录模式
         */
        private final boolean copyVideoMode;

        /**
         * 拷贝目标路径字符串
         */
        private final String copyPath;

        /**
         * 拷贝目标目录路径对象
         */
        private final Path targetDir;

        /**
         * 构造函数
         *
         * @param copyVideoMode 拷贝模式：true表示拷贝视频文件模式，false表示拷贝目录模式
         * @param copyPath      拷贝目标路径字符串
         * @param targetDir     拷贝目标目录路径对象
         */
        public CopyConfig(boolean copyVideoMode, String copyPath, Path targetDir) {
            this.copyVideoMode = copyVideoMode;
            this.copyPath = copyPath;
            this.targetDir = targetDir;
        }

        /**
         * 获取拷贝模式
         *
         * @return true表示拷贝视频文件模式，false表示拷贝目录模式
         */
        public boolean isCopyVideoMode() {
            return copyVideoMode;
        }

        /**
         * 获取拷贝目标路径
         *
         * @return 拷贝目标路径字符串
         */
        public String getCopyPath() {
            return copyPath;
        }

        /**
         * 获取拷贝目标目录
         *
         * @return 拷贝目标目录路径对象
         */
        public Path getTargetDir() {
            return targetDir;
        }
    }

    /**
     * 验证拷贝条件是否满足，并获取拷贝配置
     *
     * @param copyPath      拷贝目标路径
     * @param copyVideoMode 是否拷贝视频文件模式
     * @return 拷贝配置对象，如果配置无效返回null
     * @throws IOException 如果创建目标目录失败
     */
    public CopyConfig validateCopyConditions(String copyPath, boolean copyVideoMode) throws IOException {
        String steamPath = AppConfig.getSteamPath();
        if (steamPath == null || steamPath.isEmpty()) {
            throw new IllegalArgumentException(ResourceManager.getString("status.please.configure.steam.path"));
        }

        if (dataService.getTotalCount() == 0) {
            throw new IllegalArgumentException(ResourceManager.getString("status.no.workshop.items"));
        }

        if (copyPath == null || copyPath.trim().isEmpty()) {
            throw new IllegalArgumentException(ResourceManager.getString("status.please.enter.copy.path"));
        }

        Path targetDir = Paths.get(copyPath);
        Files.createDirectories(targetDir);
        return new CopyConfig(copyVideoMode, copyPath, targetDir);
    }

    /**
     * 开始拷贝操作
     *
     * @param copyConfig 拷贝配置
     * @param onComplete 拷贝完成回调（成功数量，失败数量）
     * @return 拷贝任务
     */
    public Task<Void> startCopy(CopyConfig copyConfig,
                                BiConsumer<Integer, Integer> onComplete) {

        if (currentCopyTask != null && currentCopyTask.isRunning()) {
            throw new IllegalStateException(ResourceManager.getString("error.copy.task.running"));
        }

        currentCopyTask = createCopyTask(copyConfig, onComplete);
        Thread copyThread = new Thread(currentCopyTask);
        copyThread.setDaemon(true);
        copyThread.start();

        return currentCopyTask;
    }

    /**
     * 取消当前拷贝任务
     */
    public void cancelCopy() {
        if (currentCopyTask != null && currentCopyTask.isRunning()) {
            currentCopyTask.cancel();
        }
    }

    /**
     * 检查是否有正在运行的拷贝任务
     *
     * @return 是否有正在运行的拷贝任务
     */
    public boolean isCopyRunning() {
        return currentCopyTask != null && currentCopyTask.isRunning();
    }

    /**
     * 创建拷贝任务
     *
     * <p>创建一个异步任务来执行文件拷贝操作，支持任务取消和进度回调</p>
     *
     * <p>任务执行流程：</p>
     * <ol>
     *   <li>遍历所有Workshop项目</li>
     *   <li>跳过已拷贝成功的项目</li>
     *   <li>更新项目状态为拷贝中</li>
     *   <li>执行文件拷贝操作</li>
     *   <li>更新项目状态为成功或失败</li>
     *   <li>触发相应的回调函数</li>
     * </ol>
     *
     * @param copyConfig 拷贝配置
     * @param onComplete 拷贝完成回调
     * @return 创建的拷贝任务
     */
    private Task<Void> createCopyTask(CopyConfig copyConfig,
                                      BiConsumer<Integer, Integer> onComplete) {
        return new Task<Void>() {
            private int successCount = 0;
            private int failedCount = 0;

            @Override
            protected Void call() throws Exception {
                var workshopItemsList = dataService.getWorkshopItemsList();
                int totalCount = workshopItemsList.size();

                for (int i = 0; i < totalCount; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    WorkshopListItem item = workshopItemsList.get(i);

                    // 跳过已经拷贝成功的项
                    if (item.getCopyStatus() == WorkshopListItem.CopyStatus.SUCCESS) {
                        continue;
                    }

                    // 更新当前项状态为拷贝中
                    updateItemCopyStatus(item, i, WorkshopListItem.CopyStatus.COPYING, "");

                    try {
                        copyItem(item, copyConfig);
                        successCount++;

                        // 更新当前项状态为成功
                        updateItemCopyStatus(item, i, WorkshopListItem.CopyStatus.SUCCESS, "");
                    } catch (Exception e) {
                        failedCount++;
                        updateItemCopyStatus(item, i, WorkshopListItem.CopyStatus.FAILED, e.getMessage());
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                if (onComplete != null) {
                    onComplete.accept(successCount, failedCount);
                }
            }

            @Override
            protected void failed() {
                if (onComplete != null) {
                    onComplete.accept(0, dataService.getTotalCount());
                }
            }
        };
    }

    /**
     * 拷贝单个Workshop项目
     *
     * <p>根据拷贝配置执行具体的文件拷贝操作</p>
     *
     * <p>拷贝模式：</p>
     * <ul>
     *   <li>视频文件模式：只拷贝Workshop目录中的第一个视频文件</li>
     *   <li>目录模式：拷贝整个Workshop目录结构</li>
     * </ul>
     *
     * <p>拷贝完成后会自动删除源目录</p>
     *
     * @param item       要拷贝的Workshop项目
     * @param copyConfig 拷贝配置
     * @throws IOException 如果拷贝操作失败
     */
    private void copyItem(WorkshopListItem item, CopyConfig copyConfig) throws IOException {
        String steamPath = AppConfig.getSteamPath();
        Path sourceDir = Paths.get(steamPath, Constants.WORKSHOP_CONTENT_PATH, item.getId());

        if (copyConfig.isCopyVideoMode()) {
            FileUtils.copyVideoFiles(sourceDir, copyConfig.getTargetDir());
        } else {
            FileUtils.copyDirectory(sourceDir, copyConfig.getTargetDir());
        }

        // 拷贝成功后删除源目录
        FileUtils.deleteDirectory(sourceDir);
    }

    /**
     * 更新项目拷贝状态
     *
     * <p>在JavaFX应用线程中安全地更新项目的拷贝状态，并强制触发UI刷新</p>
     *
     * @param item    要更新的Workshop项目
     * @param index   项目索引
     * @param status  新的拷贝状态
     * @param message 状态消息（用于失败状态）
     */
    private void updateItemCopyStatus(WorkshopListItem item, int index, WorkshopListItem.CopyStatus status, String message) {
        Platform.runLater(() -> {
            item.setCopyStatus(status, message);
            // 强制刷新列表项，触发UI更新
            var workshopItemsList = dataService.getWorkshopItemsList();
            if (index >= 0 && index < workshopItemsList.size()) {
                workshopItemsList.set(index, item);
            }
        });
    }
}
