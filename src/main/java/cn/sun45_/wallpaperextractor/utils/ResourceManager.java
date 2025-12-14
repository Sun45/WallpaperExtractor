package cn.sun45_.wallpaperextractor.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 资源管理工具类
 *
 * <p>集中管理应用程序中的所有资源，提供统一的资源获取接口</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>加载和管理messages.properties字符串资源包</li>
 *   <li>提供类型安全的字符串获取方法</li>
 *   <li>支持格式化字符串</li>
 *   <li>管理应用图标等图像资源</li>
 *   <li>便于国际化支持</li>
 * </ul>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>所有方法都是静态的，便于全局访问</li>
 *   <li>提供统一的错误处理和日志记录</li>
 *   <li>支持多种资源格式（字符串、图像等）</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <pre>
 * // 获取简单字符串
 * String title = ResourceManager.getString("app.title");
 *
 * // 获取格式化字符串
 * String message = ResourceManager.getFormattedString("status.copy.completed.format", 5, 2);
 *
 * // 获取应用图标
 * Image appIcon = ResourceManager.getApplicationIcon();
 * </pre>
 */
public class ResourceManager {
    private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());
    private static final ResourceBundle BUNDLE;

    // 应用图标资源路径
    private static final String APPLICATION_ICON_RESOURCE_PATH = "/cn/sun45_/wallpaperextractor/icon.png";

    static {
        BUNDLE = ResourceBundle.getBundle("cn/sun45_/wallpaperextractor/messages");
    }

    /**
     * 获取应用图标（AWT格式）
     *
     * <p>从类路径加载应用图标资源，支持AWT图形界面使用</p>
     *
     * @return 应用图标图像，如果加载失败返回null
     */
    public static Image getApplicationIcon() {
        try {
            InputStream iconStream = ResourceManager.class.getResourceAsStream(APPLICATION_ICON_RESOURCE_PATH);
            if (iconStream != null) {
                return ImageIO.read(iconStream);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, ResourceManager.getString("error.load.app.icon.failed.console") + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取应用图标（JavaFX格式）
     *
     * <p>从类路径加载应用图标资源，支持JavaFX界面使用</p>
     *
     * @return JavaFX格式的应用图标图像，如果加载失败返回null
     */
    public static javafx.scene.image.Image getApplicationIconFx() {
        try {
            InputStream iconStream = ResourceManager.class.getResourceAsStream(APPLICATION_ICON_RESOURCE_PATH);
            if (iconStream != null) {
                return new javafx.scene.image.Image(iconStream);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, ResourceManager.getString("error.load.app.icon.fx.failed.console") + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取字符串
     *
     * @param key 字符串键
     * @return 对应的字符串值
     */
    public static String getString(String key) {
        return BUNDLE.getString(key);
    }

    /**
     * 获取格式化字符串
     *
     * @param key  字符串键
     * @param args 格式化参数
     * @return 格式化后的字符串
     */
    public static String getFormattedString(String key, Object... args) {
        String pattern = BUNDLE.getString(key);
        return String.format(pattern, args);
    }

    /**
     * 检查是否包含指定的键
     *
     * @param key 字符串键
     * @return 如果包含返回true，否则返回false
     */
    public static boolean containsKey(String key) {
        return BUNDLE.containsKey(key);
    }
}
