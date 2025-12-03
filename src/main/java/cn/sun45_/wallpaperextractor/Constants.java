package cn.sun45_.wallpaperextractor;

import cn.sun45_.wallpaperextractor.monitor.FileWatcher;
import cn.sun45_.wallpaperextractor.utils.FileUtils;

/**
 * 应用程序常量定义类
 *
 * <p>集中管理应用程序中使用的所有常量，包括配置键名、文件路径、应用程序信息等</p>
 *
 * <p>主要常量分类：</p>
 * <ul>
 *   <li>应用程序信息：名称、描述等</li>
 *   <li>配置键名：用于Preferences API的键值</li>
 *   <li>文件扩展名：支持的视频文件格式</li>
 *   <li>Steam相关路径：日志目录、文件名、Workshop内容路径</li>
 * </ul>
 *
 * <p>设计目的：</p>
 * <ul>
 *   <li>统一管理常量，避免魔法数字和字符串</li>
 *   <li>提高代码可读性和可维护性</li>
 *   <li>便于配置修改和国际化支持</li>
 * </ul>
 *
 * @see java.util.prefs.Preferences 配置存储实现
 * @see cn.sun45_.wallpaperextractor.utils.AppConfig 配置管理类
 * @see cn.sun45_.wallpaperextractor.monitor.FileWatcher 文件监听器
 * @see cn.sun45_.wallpaperextractor.utils.FileUtils 文件操作工具类
 */
public class Constants {
    /**
     * 应用程序信息
     */
    public static final String PROJECT_NAME = "WallpaperExtractor";
    public static final String NAME = "Wallpaper Extractor：壁纸提取器";
    public static final String NAME_SIMPLE = "壁纸提取器";

    /**
     * 配置键名
     */
    public static final String STEAM_PATH_KEY = "steam_path";
    public static final String COPY_PATH_KEY = "copy_path";
    public static final String COPY_MODE_KEY = "copy_mode";

    /**
     * 支持的视频文件扩展名
     *
     * @see FileUtils#copyVideoFiles 使用此常量的方法
     */
    public static final String[] VIDEO_EXTENSIONS = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv"};

    /**
     * Steam日志目录（相对于Steam安装目录）
     *
     * @see FileWatcher 使用此常量的类
     */
    public static final String LOG_DIRECTORY = "logs";

    /**
     * Steam Workshop日志文件名
     *
     * @see FileWatcher 使用此常量的类
     */
    public static final String LOG_FILENAME = "workshop_log.txt";

    /**
     * Steam Workshop内容路径（相对于Steam安装目录）
     * 431960是Wallpaper Engine的App ID
     */
    public static final String WORKSHOP_CONTENT_PATH = "steamapps\\workshop\\content\\431960";

    /**
     * 项目说明
     */
    public static final String PROJECT_DESCRIPTION = "本工具为壁纸引擎辅助工具，包含如下功能：\n" +
            "• 自动监控壁纸订阅状态变化\n" +
            "• 智能拷贝并自动清理源文件\n" +
            "• 支持视频文件和整体文件夹两种拷贝模式";
}
