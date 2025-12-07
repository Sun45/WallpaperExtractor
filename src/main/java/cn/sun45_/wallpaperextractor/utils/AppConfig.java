package cn.sun45_.wallpaperextractor.utils;

import cn.sun45_.wallpaperextractor.Constants;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * 应用程序配置管理类
 *
 * <p>使用Java Preferences API管理应用程序配置，提供线程安全的配置读写操作</p>
 */
public class AppConfig {
    private static final Preferences PREFS = Preferences.userNodeForPackage(FileUtils.class);

    /**
     * 清空所有配置项
     *
     * @throws RuntimeException 如果清空配置失败
     */
    public static void clearPrefs() {
        try {
            PREFS.clear();
        } catch (BackingStoreException e) {
            String errorMsg = ResourceManager.getString("error.clear.config.failed");
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 保存Steam安装路径
     *
     * @param path Steam安装路径
     */
    public static void saveSteamPath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            PREFS.put(Constants.STEAM_PATH_KEY, path.trim());
        }
    }

    /**
     * 获取Steam安装路径
     *
     * @return Steam安装路径，如果未设置则返回null
     */
    public static String getSteamPath() {
        return PREFS.get(Constants.STEAM_PATH_KEY, null);
    }

    /**
     * 保存拷贝目标路径
     *
     * @param path 拷贝目标路径
     */
    public static void saveCopyPath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            PREFS.put(Constants.COPY_PATH_KEY, path.trim());
        }
    }

    /**
     * 获取拷贝目标路径
     *
     * @return 拷贝目标路径，如果未设置则返回null
     */
    public static String getCopyPath() {
        return PREFS.get(Constants.COPY_PATH_KEY, null);
    }

    /**
     * 保存拷贝模式
     *
     * @param copyVideoMode true表示拷贝视频文件模式，false表示拷贝目录模式
     */
    public static void saveCopyMode(boolean copyVideoMode) {
        PREFS.putBoolean(Constants.COPY_MODE_KEY, copyVideoMode);
    }

    /**
     * 获取拷贝模式
     *
     * @return true表示拷贝视频文件模式，false表示拷贝目录模式（默认值为true）
     */
    public static boolean getCopyMode() {
        return PREFS.getBoolean(Constants.COPY_MODE_KEY, true);
    }
}

