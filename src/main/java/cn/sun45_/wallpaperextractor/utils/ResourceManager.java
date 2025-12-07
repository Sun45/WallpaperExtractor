package cn.sun45_.wallpaperextractor.utils;

import java.util.ResourceBundle;

/**
 * 资源管理工具类
 *
 * <p>集中管理应用程序中的所有字符串资源，提供统一的字符串获取接口</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>加载和管理messages.properties资源包</li>
 *   <li>提供类型安全的字符串获取方法</li>
 *   <li>支持格式化字符串</li>
 *   <li>便于国际化支持</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <pre>
 * // 获取简单字符串
 * String title = ResourceManager.getString("app.title");
 *
 * // 获取格式化字符串
 * String message = ResourceManager.getFormattedString("status.copy.completed.format", 5, 2);
 * </pre>
 */
public class ResourceManager {

    private static final ResourceBundle BUNDLE;

    static {
        BUNDLE = ResourceBundle.getBundle("cn/sun45_/wallpaperextractor/messages");
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
