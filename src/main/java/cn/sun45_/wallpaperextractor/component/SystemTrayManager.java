package cn.sun45_.wallpaperextractor.component;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;
import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 系统托盘管理器
 *
 * <p>负责管理应用程序的系统托盘图标、菜单和动画功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>系统托盘图标的创建和显示</li>
 *   <li>托盘菜单的配置和管理</li>
 *   <li>托盘图标的动画效果控制</li>
 *   <li>用户与托盘图标的交互处理</li>
 * </ul>
 */
public class SystemTrayManager {
    private static final Logger LOGGER = Logger.getLogger(SystemTrayManager.class.getName());

    private final Stage primaryStage;
    private final Runnable startCopyCallback;
    private FXTrayIcon trayIcon;
    private AwtTrayAnimation trayAnimation;
    private MenuItem startCopyMenuItem;

    /**
     * 构造函数
     *
     * @param primaryStage      主舞台引用
     * @param startCopyCallback 开始拷贝操作的回调函数，当用户点击托盘菜单中的开始拷贝时触发
     */
    public SystemTrayManager(Stage primaryStage, Runnable startCopyCallback) {
        this.primaryStage = primaryStage;
        this.startCopyCallback = startCopyCallback;
    }

    /**
     * 初始化系统托盘管理器
     *
     * <p>执行系统托盘图标的创建、菜单配置和事件处理器的设置</p>
     */
    public void initialize() {
        try {
            setupSystemTrayIcon();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, ResourceManager.getString("error.tray.manager.init.failed"), e);
        }
    }

    /**
     * 设置系统托盘图标
     */
    private void setupSystemTrayIcon() {
        try {
            // 使用ResourceManager统一获取应用图标
            Image appIcon = ResourceManager.getApplicationIcon();
            if (appIcon == null) {
                throw new IllegalStateException(ResourceManager.getString("error.load.app.icon.failed"));
            }

            // 创建系统托盘图标实例
            trayIcon = new FXTrayIcon(primaryStage, appIcon);
            trayIcon.setTrayIconTooltip(Constants.NAME);

            // 配置托盘菜单项
            configureTrayMenuItems();

            // 初始化托盘动画控制器
            initializeTrayAnimation();

            // 配置托盘图标点击事件
            configureTrayIconClickHandler();

            // 显示系统托盘图标
            trayIcon.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, ResourceManager.getString("error.setup.tray.icon.failed"), e);
        }
    }

    /**
     * 配置托盘菜单项
     */
    private void configureTrayMenuItems() {
        // 添加开始拷贝菜单项，显示实时拷贝数量
        trayIcon.addMenuItem(
                ResourceManager.getFormattedString("tray.start.copy", 0),
                event -> {
                    if (startCopyCallback != null) {
                        startCopyCallback.run();
                    }
                }
        );
        startCopyMenuItem = trayIcon.getMenuItem(0);

        // 添加分隔线
        trayIcon.addSeparator();

        // 添加退出菜单项
        trayIcon.addExitItem(ResourceManager.getString("tray.exit"));
    }

    /**
     * 初始化托盘动画控制器
     *
     * <p>获取底层AWT TrayIcon实例并创建动画控制器</p>
     */
    private void initializeTrayAnimation() {
        // 获取底层AWT TrayIcon用于精确动画控制
        TrayIcon awtTrayIcon = trayIcon.getRestricted().getTrayIcon();
        trayAnimation = new AwtTrayAnimation(awtTrayIcon);
    }

    /**
     * 配置托盘图标点击事件
     *
     * <p>为托盘图标添加鼠标点击事件监听器，处理左键点击事件</p>
     *
     * <p>右键点击自动显示菜单，左键点击用于恢复和激活应用程序窗口</p>
     */
    private void configureTrayIconClickHandler() {
        TrayIcon awtTrayIcon = trayIcon.getRestricted().getTrayIcon();
        awtTrayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                // 只处理左键点击事件，右键点击自动显示菜单
                if (event.getButton() == MouseEvent.BUTTON1) {
                    handleTrayIconLeftClick();
                }
            }
        });
    }

    /**
     * 处理托盘图标左键点击
     */
    private void handleTrayIconLeftClick() {
        Platform.runLater(() -> {
            try {
                // 如果窗口被最小化，先恢复窗口状态
                if (primaryStage.isIconified()) {
                    primaryStage.setIconified(false);
                }

                // 确保窗口可见
                if (!primaryStage.isShowing()) {
                    primaryStage.show();
                }

                // 将窗口置于前台并获取焦点
                primaryStage.toFront();
                primaryStage.requestFocus();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, ResourceManager.getString("error.tray.icon.click.failed"), e);
            }
        });
    }

    /**
     * 开始托盘动画
     *
     * <p>启动系统托盘图标的动画效果，显示波纹扩散动画</p>
     */
    public void startTrayAnimation() {
        if (trayAnimation != null) {
            trayAnimation.startAnimation();
        }
    }

    /**
     * 停止托盘动画
     *
     * <p>停止系统托盘图标的动画效果，恢复为默认图标</p>
     */
    public void stopTrayAnimation() {
        if (trayAnimation != null) {
            trayAnimation.stopAnimation();
        }
    }

    /**
     * 更新拷贝数量显示
     *
     * <p>更新系统托盘菜单中显示的拷贝数量</p>
     *
     * @param count 当前已完成的拷贝数量
     */
    public void updateCopyCount(int count) {
        if (startCopyMenuItem != null) {
            String updatedLabel = ResourceManager.getFormattedString("tray.start.copy", count);
            startCopyMenuItem.setLabel(updatedLabel);
        }
    }

    /**
     * 清理资源
     *
     * <p>释放系统托盘管理器占用的所有资源，包括动画控制器和托盘图标</p>
     *
     * <p>在应用程序退出前必须调用此方法以确保资源正确释放</p>
     */
    public void dispose() {
        if (trayAnimation != null) {
            trayAnimation.dispose();
            trayAnimation = null;
        }

        if (trayIcon != null) {
            trayIcon.hide();
            trayIcon = null;
        }
    }
}
