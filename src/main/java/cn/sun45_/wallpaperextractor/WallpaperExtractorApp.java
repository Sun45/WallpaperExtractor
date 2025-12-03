package cn.sun45_.wallpaperextractor;

import cn.sun45_.wallpaperextractor.controller.MainController;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wallpaper Extractor 主应用程序类
 *
 * <p>负责管理应用程序的生命周期、界面切换和资源管理</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>应用程序初始化和启动</li>
 *   <li>界面加载和场景管理</li>
 *   <li>应用程序图标设置</li>
 *   <li>主界面和设置界面的切换</li>
 *   <li>应用程序关闭时的资源清理</li>
 * </ul>
 *
 * @see Application JavaFX应用程序基类
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
        // 加载设置界面
        FXMLLoader fxmlLoader = new FXMLLoader(WallpaperExtractorApp.class.getResource("settings-view.fxml"));
        settingsScene = new Scene(fxmlLoader.load(), 500, 400);

        // 加载主界面并获取控制器引用
        fxmlLoader = new FXMLLoader(WallpaperExtractorApp.class.getResource("main-view.fxml"));
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
            Image icon = new Image(getClass().getResourceAsStream("icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "无法加载应用程序图标: ", e);
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
        primaryStage.setTitle(Constants.NAME + " - 设置");
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
