package cn.sun45_.wallpaperextractor.controller;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.WallpaperExtractorApp;
import cn.sun45_.wallpaperextractor.component.WorkshopListCell;
import cn.sun45_.wallpaperextractor.model.WorkshopListItem;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import cn.sun45_.wallpaperextractor.monitor.FileWatcher;
import cn.sun45_.wallpaperextractor.utils.FileUtils;
import cn.sun45_.wallpaperextractor.monitor.WorkshopData;
import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.TimePicker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 主界面控制器
 *
 * <p>负责管理应用程序的主界面，处理Workshop项目的监听、显示和文件拷贝操作</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>界面初始化和组件设置</li>
 *   <li>Workshop日志文件监听管理</li>
 *   <li>Workshop项目列表的显示和更新</li>
 *   <li>文件拷贝操作（视频文件或整个目录）</li>
 *   <li>时间设置和过滤功能</li>
 *   <li>状态消息显示和用户交互</li>
 * </ul>
 *
 * <p>核心组件：</p>
 * <ul>
 *   <li>Workshop项目列表视图</li>
 *   <li>时间选择器（日期和时间）</li>
 *   <li>拷贝模式选择（视频文件/整个目录）</li>
 *   <li>拷贝路径设置</li>
 *   <li>状态显示标签</li>
 * </ul>
 *
 * <p>实现接口：</p>
 * <ul>
 *   <li>{@link FileWatcher.FileChangeListener} - 文件变化监听器</li>
 *   <li>{@link WorkshopListCell.WorkshopListCellListener} - 列表项操作监听器</li>
 * </ul>
 *
 * @see FileWatcher 文件监听器
 * @see WorkshopListCell 自定义列表单元格
 * @see WorkshopListItem Workshop列表项数据模型
 */
public class MainController implements FileWatcher.FileChangeListener, WorkshopListCell.WorkshopListCellListener {
    /**
     * 设置按钮
     */
    @FXML
    private Button settingsButton;

    /**
     * 日期选择器
     */
    @FXML
    private CalendarPicker calendarPicker;

    /**
     * 时间选择器
     */
    @FXML
    private TimePicker timePicker;

    /**
     * 更新时间按钮
     */
    @FXML
    private Button updateTimeButton;

    /**
     * Workshop项目列表视图
     */
    @FXML
    private ListView<WorkshopListItem> idListView;

    /**
     * 状态显示标签
     */
    @FXML
    private Label statusLabel;

    /**
     * 数量显示标签
     */
    @FXML
    private Label countLabel;

    /**
     * 拷贝视频文件单选按钮
     */
    @FXML
    private RadioButton copyVideoRadioButton;

    /**
     * 拷贝目录单选按钮
     */
    @FXML
    private RadioButton copyDirectoryRadioButton;

    /**
     * 拷贝按钮
     */
    @FXML
    private Button copyButton;

    /**
     * 拷贝路径输入框
     */
    @FXML
    private TextField copyPathTextField;

    /**
     * 拷贝模式选择组
     */
    private ToggleGroup copyModeGroup;

    /**
     * 文件监听器实例
     */
    private FileWatcher fileWatcher;

    /**
     * 当前设置的起始时间
     */
    private String currentTime;

    /**
     * Workshop项目列表（ObservableList，支持数据绑定）
     */
    private ObservableList<WorkshopListItem> workshopItemsList;

    /**
     * Workshop项目映射表（用于快速查找）
     */
    private Map<String, WorkshopListItem> workshopItemsMap;

    /**
     * 日期格式化器
     */
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // 初始化UI组件
        initializeUI();

        // 初始化数据
        initializeData();

        // 设置初始状态
        setStatusMessage("等待Steam路径配置...");
    }

    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        // 设置默认时间为当前日期和时间
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        calendarPicker.setValue(today);
        timePicker.setValue(now);
        currentTime = formatDateTime(today, now);

        // 初始化ObservableList和Map用于ListView
        workshopItemsList = FXCollections.observableArrayList();
        workshopItemsMap = new HashMap<>();
        idListView.setItems(workshopItemsList);

        // 设置自定义的ListCell
        idListView.setCellFactory(param -> new WorkshopListCell(this));

        // 初始化数量显示标签
        countLabel.setText("0个");

        // 初始化拷贝模式选择
        copyModeGroup = new ToggleGroup();
        copyVideoRadioButton.setToggleGroup(copyModeGroup);
        copyDirectoryRadioButton.setToggleGroup(copyModeGroup);

        // 加载保存的拷贝模式
        boolean savedCopyMode = AppConfig.getCopyMode();
        if (savedCopyMode) {
            copyVideoRadioButton.setSelected(true);
        } else {
            copyDirectoryRadioButton.setSelected(true);
        }

        // 加载保存的拷贝路径
        String savedCopyPath = AppConfig.getCopyPath();
        if (savedCopyPath != null && !savedCopyPath.isEmpty()) {
            copyPathTextField.setText(savedCopyPath);
        } else {
            // 如果没有保存的路径，使用默认路径
            String defaultPath = Paths.get(System.getProperty("user.home"), "Desktop", "WallpaperExtractor").toString();
            copyPathTextField.setText(defaultPath);
        }
    }

    /**
     * 初始化数据
     */
    private void initializeData() {
        // 初始化FileWatcher
        fileWatcher = new FileWatcher(this, currentTime);
    }

    /**
     * 设置按钮点击事件处理
     *
     * <p>切换到设置界面，用于配置Steam路径等参数</p>
     */
    @FXML
    public void onSettingClick() {
        WallpaperExtractorApp.application.showSettingsScene();
    }

    /**
     * 更新时间按钮点击事件处理
     *
     * <p>根据用户选择的日期和时间更新文件监听的起始时间</p>
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取日历选择器和时间选择器的值</li>
     *   <li>格式化日期时间为字符串格式</li>
     *   <li>更新文件监听器的起始时间</li>
     *   <li>显示状态消息通知用户</li>
     * </ul>
     */
    @FXML
    protected void onUpdateTimeClick() {
        LocalDate selectedDate = calendarPicker.getValue();
        LocalTime selectedTime = timePicker.getTime();
        if (selectedDate != null && selectedTime != null) {
            currentTime = formatDateTime(selectedDate, selectedTime);
            fileWatcher.updateStartTime(currentTime);
            setStatusMessage("设置自动检测起始时间: " + selectedDate + " " + selectedTime);
        }
    }

    /**
     * 文件变化监听回调方法
     *
     * <p>当文件监听器检测到日志文件变化时调用此方法</p>
     *
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>在JavaFX应用线程中更新界面</li>
     *   <li>如果有新的Workshop数据，更新列表显示</li>
     *   <li>如果没有数据，清空列表显示</li>
     *   <li>更新项目数量显示标签</li>
     * </ul>
     *
     * @param workshopDataList 从日志文件中解析出的Workshop数据列表
     */
    @Override
    public void onFileChanged(List<WorkshopData> workshopDataList) {
        Platform.runLater(() -> {
            if (workshopDataList != null && !workshopDataList.isEmpty()) {
                updateWorkshopItems(workshopDataList);
                countLabel.setText(workshopDataList.size() + "个");
            } else {
                clearWorkshopItems();
                countLabel.setText("0个");
            }
        });
    }

    /**
     * 更新workshop项目列表
     *
     * @param workshopDataList 新的workshop模型列表
     */
    private void updateWorkshopItems(List<WorkshopData> workshopDataList) {
        // 使用Set来跟踪需要保留的项目ID
        Set<String> currentIds = new HashSet<>();

        for (WorkshopData workshopData : workshopDataList) {
            String itemId = workshopData.getId();
            currentIds.add(itemId);

            WorkshopListItem existingItem = workshopItemsMap.get(itemId);

            if (existingItem != null) {
                // 更新现有项目
                WorkshopData existingModel = existingItem.getWorkshopData();
                existingModel.setTime(workshopData.getTime());
                existingModel.setSubscribed(workshopData.isSubscribed());
            } else {
                // 创建新项目并添加到Map和List
                WorkshopListItem newItem = new WorkshopListItem(workshopData);
                workshopItemsMap.put(itemId, newItem);
                workshopItemsList.add(newItem);
            }
        }

        // 移除不再存在的项目
        removeObsoleteItems(currentIds);
    }

    /**
     * 移除不再存在的项目
     *
     * @param currentIds 当前有效的项目ID集合
     */
    private void removeObsoleteItems(Set<String> currentIds) {
        // 创建需要移除的项目列表
        List<WorkshopListItem> itemsToRemove = new ArrayList<>();

        for (WorkshopListItem item : workshopItemsList) {
            if (!currentIds.contains(item.getId())) {
                itemsToRemove.add(item);
            }
        }

        // 批量移除项目
        workshopItemsList.removeAll(itemsToRemove);
        for (WorkshopListItem item : itemsToRemove) {
            workshopItemsMap.remove(item.getId());
        }
    }

    /**
     * 清空workshop项目列表
     */
    private void clearWorkshopItems() {
        workshopItemsList.clear();
        workshopItemsMap.clear();
    }

    /**
     * 启动文件监听（当主界面显示时调用）
     */
    public void startWatching() {
        if (fileWatcher == null) {
            return;
        }
        String steamPath = AppConfig.getSteamPath();
        if (steamPath != null && !steamPath.isEmpty()) {
            try {
                // 确保先停止之前的监听
                fileWatcher.stopWatching();
                fileWatcher.startWatching(steamPath);
                setStatusMessage("正在监听Steam日志文件...");
            } catch (Exception e) {
                setStatusMessage("监听失败: " + e.getMessage());
            }
        } else {
            setStatusMessage("请先配置Steam路径");
        }
    }

    /**
     * 停止文件监听（当主界面隐藏时调用）
     */
    public void stopWatching() {
        if (fileWatcher == null) {
            return;
        }
        fileWatcher.stopWatching();
        setStatusMessage("监听已停止");
    }

    /**
     * 格式化日期时间为字符串
     */
    private String formatDateTime(LocalDate date, LocalTime time) {
        return date.format(dateFormatter) + " " + time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * 设置状态消息
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    /**
     * Workshop列表项监听器接口实现 - 打开Workshop文件夹
     *
     * <p>当用户在列表项中点击"打开文件夹"按钮时调用此方法</p>
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查Steam路径是否已配置</li>
     *   <li>构建Workshop文件夹的完整路径</li>
     *   <li>使用系统默认文件管理器打开文件夹</li>
     *   <li>处理路径不存在或打开失败的情况</li>
     * </ul>
     *
     * @param workshopId Workshop项目ID
     */
    @Override
    public void onOpenWorkshopFolder(String workshopId) {
        String steamPath = AppConfig.getSteamPath();
        if (steamPath != null && !steamPath.isEmpty()) {
            try {
                Path folderPath = Paths.get(steamPath, Constants.WORKSHOP_CONTENT_PATH, workshopId);
                if (Files.exists(folderPath)) {
                    Desktop.getDesktop().open(folderPath.toFile());
                } else {
                    setStatusMessage("文件夹不存在: " + folderPath);
                }
            } catch (IOException e) {
                setStatusMessage("打开文件夹失败: " + e.getMessage());
            }
        } else {
            setStatusMessage("请先配置Steam路径");
        }
    }

    /**
     * Workshop列表项监听器接口实现 - 打开Workshop网页
     *
     * <p>当用户在列表项中点击"打开网页"按钮时调用此方法</p>
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>构建Steam社区Workshop页面的URL</li>
     *   <li>使用系统默认浏览器打开网页</li>
     *   <li>处理URL格式错误或打开失败的情况</li>
     * </ul>
     *
     * @param workshopId Workshop项目ID
     */
    @Override
    public void onOpenWorkshopWebPage(String workshopId) {
        try {
            String url = "https://steamcommunity.com/sharedfiles/filedetails/?id=" + workshopId;
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            setStatusMessage("打开网页失败: " + e.getMessage());
        }
    }

    /**
     * 拷贝按钮点击事件处理
     *
     * <p>启动Workshop项目的文件拷贝操作</p>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>验证拷贝条件是否满足（Steam路径、项目列表、目标路径）</li>
     *   <li>准备拷贝任务（保存配置、禁用按钮、重置状态）</li>
     *   <li>启动异步拷贝任务</li>
     * </ol>
     *
     * <p>拷贝模式：</p>
     * <ul>
     *   <li>拷贝视频文件模式：只拷贝Workshop目录中的第一个视频文件</li>
     *   <li>拷贝目录模式：拷贝整个Workshop目录结构</li>
     * </ul>
     */
    @FXML
    protected void onCopyButtonClick() {
        // 验证拷贝条件,并获取拷贝配置
        CopyConfig copyConfig = validateCopyConditions();
        if (copyConfig == null) {
            return;
        }

        // 准备拷贝
        prepareCopyTask(copyConfig);

        // 开始拷贝
        startCopyTask(copyConfig);
    }

    /**
     * 验证拷贝条件是否满足,并获取拷贝配置
     *
     * @return 拷贝配置对象，如果配置无效返回null
     */
    private CopyConfig validateCopyConditions() {
        String steamPath = AppConfig.getSteamPath();
        if (steamPath == null || steamPath.isEmpty()) {
            setStatusMessage("请先配置Steam路径");
            return null;
        }

        if (workshopItemsList.isEmpty()) {
            setStatusMessage("没有可拷贝的Workshop项目");
            return null;
        }

        String copyPath = copyPathTextField.getText();
        if (copyPath == null || copyPath.trim().isEmpty()) {
            setStatusMessage("请输入拷贝目标路径");
            return null;
        }

        boolean copyVideoMode = copyVideoRadioButton.isSelected();
        try {
            Path targetDir = Paths.get(copyPath);
            Files.createDirectories(targetDir);
            return new CopyConfig(copyVideoMode, copyPath, targetDir);
        } catch (IOException e) {
            setStatusMessage("创建目标目录失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 准备拷贝
     *
     * @param copyConfig 拷贝配置
     */
    private void prepareCopyTask(CopyConfig copyConfig) {
        // 保存拷贝路径和拷贝模式
        AppConfig.saveCopyPath(copyConfig.getCopyPath());
        AppConfig.saveCopyMode(copyConfig.isCopyVideoMode());

        // 禁用拷贝按钮，防止重复点击
        copyButton.setDisable(true);
        setStatusMessage("开始拷贝...");

        // 重置非成功项的拷贝状态
        for (WorkshopListItem item : workshopItemsList) {
            if (item.getCopyStatus() != WorkshopListItem.CopyStatus.SUCCESS) {
                item.setCopyStatus(WorkshopListItem.CopyStatus.NOT_COPIED);
            }
        }
    }

    /**
     * 开始拷贝任务
     *
     * @param copyConfig 拷贝配置
     */
    private void startCopyTask(CopyConfig copyConfig) {
        Task<Void> copyTask = createCopyTask(copyConfig);
        Thread copyThread = new Thread(copyTask);
        copyThread.setDaemon(true);
        copyThread.start();
    }

    /**
     * 创建拷贝任务
     *
     * @param copyConfig 拷贝配置
     * @return 拷贝任务对象
     */
    private Task<Void> createCopyTask(CopyConfig copyConfig) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                return executeCopyOperation(copyConfig);
            }
        };
    }

    /**
     * 执行拷贝操作
     *
     * @param copyConfig 拷贝配置
     * @return 总是返回null
     */
    private Void executeCopyOperation(CopyConfig copyConfig) {
        int totalCount = workshopItemsList.size();
        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < totalCount; i++) {
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

        // 更新最终状态
        updateFinalCopyStatus(successCount, failedCount);
        return null;
    }

    /**
     * 拷贝单个项目
     *
     * @param item       要拷贝的项目
     * @param copyConfig 拷贝配置
     * @throws IOException 如果拷贝失败，抛出包含详细错误信息的异常
     */
    private void copyItem(WorkshopListItem item, CopyConfig copyConfig) throws IOException {
        String steamPath = AppConfig.getSteamPath();
        Path sourceDir = Paths.get(steamPath, Constants.WORKSHOP_CONTENT_PATH, item.getId());

        try {
            if (copyConfig.isCopyVideoMode()) {
                FileUtils.copyVideoFiles(sourceDir, copyConfig.getTargetDir());
            } else {
                FileUtils.copyDirectory(sourceDir, copyConfig.getTargetDir());
            }
            // 拷贝成功后删除源目录
            FileUtils.deleteDirectory(sourceDir);
        } catch (IOException e) {
            // 重新抛出异常，让调用方处理
            throw e;
        }
    }

    /**
     * 更新项目拷贝状态
     *
     * @param item    要更新的项目
     * @param index   项目索引
     * @param status  新的状态
     * @param message 状态消息
     */
    private void updateItemCopyStatus(WorkshopListItem item, int index, WorkshopListItem.CopyStatus status, String message) {
        Platform.runLater(() -> {
            item.setCopyStatus(status, message);
            workshopItemsList.set(index, item);
        });
    }

    /**
     * 更新最终拷贝状态
     *
     * @param successCount 成功数量
     * @param failedCount  失败数量
     */
    private void updateFinalCopyStatus(int successCount, int failedCount) {
        Platform.runLater(() -> {
            copyButton.setDisable(false);
            String message = String.format("拷贝完成: 成功%d个, 失败%d个",
                    successCount, failedCount);
            setStatusMessage(message);
        });
    }

    /**
     * 拷贝配置类
     *
     * <p>封装文件拷贝操作的相关配置参数</p>
     *
     * <p>主要用途：</p>
     * <ul>
     *   <li>统一管理拷贝操作的配置参数</li>
     *   <li>提供类型安全的配置访问</li>
     *   <li>简化拷贝任务参数的传递</li>
     * </ul>
     *
     * <p>配置参数：</p>
     * <ul>
     *   <li>拷贝模式：视频文件模式或目录模式</li>
     *   <li>拷贝路径：用户指定的目标路径</li>
     *   <li>目标目录：解析后的目标目录路径对象</li>
     * </ul>
     */
    private static class CopyConfig {
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
         * @param copyPath 拷贝目标路径字符串
         * @param targetDir 拷贝目标目录路径对象
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
}
