package cn.sun45_.wallpaperextractor.controller;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.WallpaperExtractorApp;
import cn.sun45_.wallpaperextractor.component.WorkshopListCell;
import cn.sun45_.wallpaperextractor.model.WorkshopListItem;
import cn.sun45_.wallpaperextractor.monitor.FileWatcher;
import cn.sun45_.wallpaperextractor.monitor.WorkshopData;
import cn.sun45_.wallpaperextractor.service.CopyService;
import cn.sun45_.wallpaperextractor.service.WorkshopDataService;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;
import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.TimePicker;
import javafx.application.Platform;
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
import java.util.List;

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
     * 起始时间过滤器
     */
    private String startTimeFilter;

    /**
     * Workshop数据服务
     */
    private WorkshopDataService dataService;

    /**
     * 文件拷贝服务
     */
    private CopyService copyService;

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
        setStatusMessage(ResourceManager.getString("status.waiting.for.steam.config"));
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
        startTimeFilter = formatDateTime(today, now);

        // 初始化数据服务
        dataService = new WorkshopDataService();
        copyService = new CopyService(dataService);

        // 设置数据变化监听器
        dataService.setOnDataChangedListener(count -> {
            Platform.runLater(() -> {
                if (count > 0) {
                    countLabel.setText(ResourceManager.getFormattedString("count.label.format", count));
                    int actualCopyCount = dataService.calculateActualCopyCount();
                    if (WallpaperExtractorApp.application != null) {
                        WallpaperExtractorApp.application.updateCopyCount(actualCopyCount);
                    }
                } else {
                    countLabel.setText(ResourceManager.getString("count.label.zero"));
                    if (WallpaperExtractorApp.application != null) {
                        WallpaperExtractorApp.application.updateCopyCount(0);
                    }
                }
            });
        });

        // 使用数据服务的ObservableList绑定ListView
        idListView.setItems(dataService.getWorkshopItemsList());

        // 设置自定义的ListCell
        idListView.setCellFactory(param -> new WorkshopListCell(this));

        // 初始化数量显示标签
        countLabel.setText(ResourceManager.getString("count.label.zero"));

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
        fileWatcher = new FileWatcher(this, startTimeFilter);
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
     * 处理时间更新事件
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
    protected void handleTimeUpdate() {
        LocalDate selectedDate = calendarPicker.getValue();
        LocalTime selectedTime = timePicker.getTime();
        if (selectedDate != null && selectedTime != null) {
            startTimeFilter = formatDateTime(selectedDate, selectedTime);
            fileWatcher.updateStartTime(startTimeFilter);
            setStatusMessage(ResourceManager.getFormattedString("status.set.start.time.format", selectedDate, selectedTime));
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
        // 使用数据服务处理数据更新，监听器会自动更新界面
        dataService.updateWorkshopItems(workshopDataList);
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
                setStatusMessage(ResourceManager.getString("status.listening.steam.log"));
            } catch (Exception e) {
                setStatusMessage(ResourceManager.getFormattedString("status.listening.failed", e.getMessage()));
            }
        } else {
            setStatusMessage(ResourceManager.getString("status.please.configure.steam.path"));
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
        setStatusMessage(ResourceManager.getString("status.listening.stopped"));
    }

    /**
     * 格式化日期时间为字符串
     */
    private String formatDateTime(LocalDate date, LocalTime time) {
        return date.format(dateFormatter) + " " + time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * 开始拷贝操作 - 供系统托盘菜单调用
     */
    public void startCopying() {
        Platform.runLater(() -> {
            // 模拟点击拷贝按钮
            if (copyButton != null) {
                copyButton.fire();
            }
        });
    }

    /**
     * 设置状态消息（线程安全）
     *
     * <p>使用Platform.runLater()确保UI操作在JavaFX应用线程中执行</p>
     */
    private void setStatusMessage(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
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
                    setStatusMessage(ResourceManager.getFormattedString("status.folder.not.exists", folderPath));
                }
            } catch (IOException e) {
                setStatusMessage(ResourceManager.getFormattedString("status.open.folder.failed", e.getMessage()));
            }
        } else {
            setStatusMessage(ResourceManager.getString("status.please.configure.steam.path"));
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
            setStatusMessage(ResourceManager.getFormattedString("status.open.webpage.failed", e.getMessage()));
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
        try {
            // 验证拷贝条件并获取配置
            String copyPath = copyPathTextField.getText();
            boolean copyVideoMode = copyVideoRadioButton.isSelected();
            CopyService.CopyConfig copyConfig = copyService.validateCopyConditions(copyPath, copyVideoMode);

            // 保存配置
            AppConfig.saveCopyPath(copyConfig.getCopyPath());
            AppConfig.saveCopyMode(copyConfig.isCopyVideoMode());

            // 准备拷贝
            copyButton.setDisable(true);
            setStatusMessage(ResourceManager.getString("status.copy.started"));

            // 启动系统托盘动画
            if (WallpaperExtractorApp.application != null) {
                WallpaperExtractorApp.application.startTrayAnimation();
            }

            // 重置非成功项的拷贝状态
            dataService.resetNonSuccessCopyStatus();

            // 开始拷贝
            copyService.startCopy(copyConfig,
                    (successCount, failedCount) -> {
                        // 拷贝完成回调
                        Platform.runLater(() -> {
                            copyButton.setDisable(false);
                            String message = ResourceManager.getFormattedString("status.copy.completed.format", successCount, failedCount);
                            setStatusMessage(message);

                            // 停止系统托盘动画
                            if (WallpaperExtractorApp.application != null) {
                                WallpaperExtractorApp.application.stopTrayAnimation();
                            }

                            // 更新系统托盘菜单中的拷贝数量
                            int actualCopyCount = dataService.calculateActualCopyCount();
                            if (WallpaperExtractorApp.application != null) {
                                WallpaperExtractorApp.application.updateCopyCount(actualCopyCount);
                            }
                        });
                    });

        } catch (IllegalArgumentException e) {
            // 参数异常，通常是用户输入错误或配置问题，直接显示异常消息
            setStatusMessage(e.getMessage());
            copyButton.setDisable(false);
        } catch (Exception e) {
            // 其他异常，显示通用的拷贝失败消息
            setStatusMessage(ResourceManager.getFormattedString("status.copy.failed", e.getMessage()));
            copyButton.setDisable(false);
        }
    }
}
