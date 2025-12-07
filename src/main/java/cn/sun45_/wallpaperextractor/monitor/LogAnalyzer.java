package cn.sun45_.wallpaperextractor.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam Workshop日志分析器
 *
 * <p>负责解析Steam日志文件，提取Wallpaper Engine (AppID 431960) 的Workshop项目订阅状态变化信息</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>使用正则表达式解析Steam日志文件内容</li>
 *   <li>提取订阅、取消订阅和未使用项移除的Workshop项目ID</li>
 *   <li>支持基于时间戳的日志过滤，只处理指定时间之后的记录</li>
 *   <li>实现基于ID的状态覆盖机制，确保每个ID只保留最新的状态</li>
 *   <li>自动过滤非Wallpaper Engine相关的日志记录</li>
 * </ul>
 *
 * <p>解析规则：</p>
 * <ul>
 *   <li>只处理包含 "[AppID 431960]" 的日志行</li>
 *   <li>识别三种类型的Workshop变更记录：
 *     <ul>
 *       <li>订阅项：Detected workshop change : added subscribed item {ID}</li>
 *       <li>取消订阅项：Detected workshop change : removing unsubscribed item {ID}</li>
 *       <li>未使用项：Detected workshop change : removing unused item {ID}</li>
 *     </ul>
 *   </li>
 *   <li>提取时间戳格式：[yyyy-MM-dd HH:mm:ss]</li>
 * </ul>
 *
 * <p>状态管理：当同一个Workshop ID出现新的订阅状态时，会覆盖旧的记录，确保返回的数据反映最新的状态</p>
 *
 * @see WorkshopData Workshop数据模型
 * @see FileWatcher 文件监听器（调用本分析器）
 */
public class LogAnalyzer {
    /**
     * 时间戳正则表达式模式 - 匹配格式如 [2023-12-02 10:30:45]
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\]");

    /**
     * 未使用项移除日志正则表达式模式
     */
    private static final Pattern UNUSED_PATTERN = Pattern.compile("Detected workshop change : removing unused item (\\d+)");

    /**
     * 未知项移除日志正则表达式模式
     */
    private static final Pattern UNKNOWN_PATTERN = Pattern.compile("Detected workshop change : removing unknown item (\\d+)");

    /**
     * 订阅项日志正则表达式模式
     */
    private static final Pattern SUBSCRIBE_PATTERN = Pattern.compile("Detected workshop change : added subscribed item (\\d+)");

    /**
     * 取消订阅项日志正则表达式模式
     */
    private static final Pattern UNSUBSCRIBE_PATTERN = Pattern.compile("Detected workshop change : removing unsubscribed item (\\d+)");

    /**
     * 分析Steam日志内容，提取Wallpaper Engine (AppID 431960)的Workshop项目状态变化记录
     *
     * <p>处理四种类型的Workshop变更记录：</p>
     * <ul>
     *   <li>订阅项：Detected workshop change : added subscribed item {ID}</li>
     *   <li>取消订阅项：Detected workshop change : removing unsubscribed item {ID}</li>
     *   <li>未使用项移除：Detected workshop change : removing unused item {ID}</li>
     *   <li>未知项移除：Detected workshop change : removing unknown item {ID}</li>
     * </ul>
     *
     * <p>实现基于ID的状态覆盖机制：当同一个Workshop ID出现新的订阅状态时，会覆盖旧的记录，
     * 确保返回的数据反映每个ID的最新状态。未使用项和未知项会从结果中移除对应ID的记录。</p>
     *
     * @param logContent 日志内容列表，每行包含时间戳和日志信息
     * @param startTime  起始时间字符串，格式为"yyyy-MM-dd HH:mm:ss"，用于过滤日志时间
     * @return 筛选后的WorkshopData列表，每个ID只保留最新的订阅状态
     */
    public static List<WorkshopData> analyzeLog(List<String> logContent, String startTime) {
        if (logContent == null || logContent.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用HashMap存储WorkshopModel，以ID为key，提高查找性能
        Map<String, WorkshopData> modelMap = new HashMap<>();

        // 时间过滤标志：如果startTime为null或空，则处理所有日志
        boolean shouldFilterByTime = startTime != null && !startTime.isEmpty();

        for (String line : logContent) {
            // 检查AppID是否为431960
            if (!line.contains("[AppID 431960]")) {
                continue;
            }

            // 提取时间戳
            Matcher timeMatcher = TIME_PATTERN.matcher(line);
            if (!timeMatcher.find()) {
                continue; // 没有有效时间戳，跳过该行
            }

            String logTime = timeMatcher.group(1);

            // 时间过滤：如果设置了起始时间，只处理时间大于等于起始时间的日志
            if (shouldFilterByTime && logTime.compareTo(startTime) < 0) {
                continue;
            }

            // 判断日志类型并提取相关信息
            String itemId = null;
            Boolean subscribeStatus = null;

            // 未使用项 - 从map中移除对应的记录
            Matcher unusedMatcher = UNUSED_PATTERN.matcher(line);
            if (unusedMatcher.find()) {
                itemId = unusedMatcher.group(1);
                modelMap.remove(itemId);
                continue;
            }

            // 未知项 - 从map中移除对应的记录
            Matcher unknownMatcher = UNKNOWN_PATTERN.matcher(line);
            if (unknownMatcher.find()) {
                itemId = unknownMatcher.group(1);
                modelMap.remove(itemId);
                continue;
            }

            // 1. 订阅项
            Matcher subscribeMatcher = SUBSCRIBE_PATTERN.matcher(line);
            if (subscribeMatcher.find()) {
                itemId = subscribeMatcher.group(1);
                subscribeStatus = true;
            }

            // 2. 取消订阅项
            Matcher unsubscribeMatcher = UNSUBSCRIBE_PATTERN.matcher(line);
            if (itemId == null && unsubscribeMatcher.find()) {
                itemId = unsubscribeMatcher.group(1);
                subscribeStatus = false;
            }

            // 如果成功提取到ID和状态，则处理状态覆盖逻辑
            if (itemId != null && subscribeStatus != null) {
                WorkshopData existingModel = modelMap.get(itemId);

                if (existingModel != null) {
                    // 找到相同ID的记录，直接更新时间和订阅状态
                    existingModel.setTime(logTime);
                    existingModel.setSubscribed(subscribeStatus);
                } else {
                    // 如果没有找到相同ID的记录，则创建新记录
                    WorkshopData newModel = new WorkshopData();
                    newModel.setId(itemId);
                    newModel.setTime(logTime);
                    newModel.setSubscribed(subscribeStatus);
                    modelMap.put(itemId, newModel);
                }
            }
        }

        // 将HashMap的值转换为List返回
        return new ArrayList<>(modelMap.values());
    }
}
