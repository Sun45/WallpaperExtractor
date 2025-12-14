package cn.sun45_.wallpaperextractor;

import cn.sun45_.wallpaperextractor.component.SystemTrayManager;
import cn.sun45_.wallpaperextractor.controller.MainController;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 壁纸提取器应用程序主类
 *
 * <p>负责管理应用程序的生命周期、界面切换和系统托盘功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>应用程序初始化和启动</li>
 *   <li>界面加载和场景管理</li>
 *   <li>系统托盘图标和动画管理</li>
 *   <li>主界面和设置界面的切换</li>
 *   <li>应用程序关闭时的资源清理</li>
 * </ul>
 */
public class WallpaperExtractorApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(WallpaperExtractorApp.class.getName());

    /**
     * 应用程序实例引用
     */
    public static WallpaperExtractorApp application;

    /**
     * 主舞台引用
     */
    public static Stage primaryStage;

    /**
     * 系统托盘管理器
     */
    private SystemTrayManager trayManager;

    /**
     * 设置界面场景
     */
    private Scene settingsScene;

    /**
     * 主界面场景
     */
    private Scene mainScene;

    /**
     * 主控制器引用
     */
    private MainController mainController;

    /**
     * 应用程序初始化方法
     *
     * <p>在应用程序启动前执行，设置应用程序实例引用</p>
     *
     * @throws Exception 如果初始化过程中发生异常
     */
    @Override
    public void init() throws Exception {
        application = this;
        super.init();
    }

    /**
     * 更新系统托盘菜单中的拷贝数量显示
     *
     * <p>供外部调用的公开方法，用于更新系统托盘菜单中显示的拷贝数量</p>
     *
     * @param copyCount 执行拷贝的数量
     */
    public void updateCopyCount(int copyCount) {
        if (trayManager != null) {
            trayManager.updateCopyCount(copyCount);
        }
    }

    /**
     * 启动系统托盘动画
     *
     * <p>供外部调用的公开方法，用于启动系统托盘动画</p>
     */
    public void startTrayAnimation() {
        if (trayManager != null) {
            trayManager.startTrayAnimation();
        }
    }

    /**
     * 停止系统托盘动画
     *
     * <p>供外部调用的公开方法，用于停止系统托盘动画</p>
     */
    public void stopTrayAnimation() {
        if (trayManager != null) {
            trayManager.stopTrayAnimation();
        }
    }

    /**
     * 应用程序启动方法
     *
     * <p>JavaFX应用程序的主入口点，负责初始化界面和启动应用程序</p>
     *
     * @param primaryStage 主舞台对象
     * @throws IOException 如果界面加载失败
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        // 初始化应用程序
        initializeApplication();

        // 设置应用程序图标
        setApplicationIcon();

        // 初始化系统托盘管理器
        trayManager = new SystemTrayManager(primaryStage, () -> {
            if (mainController != null) {
                mainController.startCopying();
            }
        });
        trayManager.initialize();

        // 设置窗口不可调整大小
        primaryStage.setResizable(false);

        // 显示主窗口
        primaryStage.show();
    }

    /**
     * 初始化应用程序界面和数据
     *
     * <p>加载所有界面并根据配置决定初始显示的界面</p>
     *
     * @throws IOException 如果界面加载失败
     */
    private void initializeApplication() throws IOException {
        // 加载界面
        loadScenes();

        // 根据配置决定显示哪个界面
        determineInitialScene();
    }

    /**
     * 加载所有界面
     *
     * <p>加载设置界面和主界面，并获取主控制器引用</p>
     *
     * @throws IOException 如果界面加载失败
     */
    private void loadScenes() throws IOException {
        // 加载资源包
        ResourceBundle bundle = ResourceBundle.getBundle("cn/sun45_/wallpaperextractor/messages", Locale.CHINA);

        // 加载设置界面
        FXMLLoader fxmlLoader = new FXMLLoader(WallpaperExtractorApp.class.getResource("settings-view.fxml"), bundle);
        settingsScene = new Scene(fxmlLoader.load(), 500, 460);

        // 加载主界面并获取控制器引用
        fxmlLoader = new FXMLLoader(WallpaperExtractorApp.class.getResource("main-view.fxml"), bundle);
        mainScene = new Scene(fxmlLoader.load(), 800, 800);
        mainController = fxmlLoader.getController();
    }

    /**
     * 根据配置决定初始显示的界面
     *
     * <p>检查是否已配置Steam路径，如果有则显示主界面，否则显示设置界面</p>
     */
    private void determineInitialScene() {
        String steamPath = AppConfig.getSteamPath();
        if (steamPath != null && !steamPath.isEmpty()) {
            // 有保存的路径，直接显示默认界面
            showMainScene();
        } else {
            // 没有保存的路径，显示设置界面
            showSettingsScene();
        }
    }

    /**
     * 设置应用程序图标
     *
     * <p>从资源文件加载应用程序图标并设置到主舞台</p>
     */
    private void setApplicationIcon() {
        try {
            // 使用ResourceManager统一获取JavaFX格式的应用图标
            Image icon = ResourceManager.getApplicationIconFx();
            if (icon != null) {
                primaryStage.getIcons().add(icon);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, ResourceManager.getString("error.load.app.icon.failed"), e);
        }
    }

    /**
     * 应用程序停止方法
     *
     * <p>在应用程序关闭时执行，确保后台进程正确关闭</p>
     *
     * @throws Exception 如果停止过程中发生异常
     */
    @Override
    public void stop() throws Exception {
        // 停止文件监视器，确保后台进程正确关闭
        if (mainController != null) {
            mainController.stopWatching();
        }

        // 清理系统托盘资源
        if (trayManager != null) {
            trayManager.dispose();
        }
        super.stop();
    }

    /**
     * 显示设置界面
     *
     * <p>切换到设置界面，停止文件监听并更新窗口标题</p>
     */
    public void showSettingsScene() {
        // 停止监听（如果正在监听）
        if (mainController != null) {
            mainController.stopWatching();
        }
        primaryStage.setTitle(Constants.NAME + ResourceManager.getString("settings.window.title.suffix"));
        primaryStage.setScene(settingsScene);
    }

    /**
     * 显示主界面
     *
     * <p>切换到主界面，启动文件监听并更新窗口标题</p>
     */
    public void showMainScene() {
        primaryStage.setTitle(Constants.NAME);
        primaryStage.setScene(mainScene);
        // 启动监听
        if (mainController != null) {
            mainController.startWatching();
        }
    }
}
