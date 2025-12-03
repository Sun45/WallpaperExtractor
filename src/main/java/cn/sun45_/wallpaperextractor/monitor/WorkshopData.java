package cn.sun45_.wallpaperextractor.monitor;

import java.util.Objects;

/**
 * Workshop数据模型类
 *
 * <p>表示从Steam日志中解析出的Workshop项目信息，包含项目ID、时间戳和订阅状态</p>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>存储从日志文件中提取的Workshop项目信息</li>
 *   <li>作为数据传递对象在文件监听器和界面控制器之间传递</li>
 *   <li>支持基于ID和时间的相等性比较</li>
 * </ul>
 *
 * <p>数据来源：通过{@link LogAnalyzer}从Steam日志文件中解析得到</p>
 *
 * @see LogAnalyzer 日志分析器
 */
public class WorkshopData {
    /**
     * Workshop项目ID
     */
    private String workshopId;

    /**
     * 订阅/取消订阅时间戳，格式为 yyyy-MM-dd HH:mm:ss
     */
    private String timestamp;

    /**
     * 订阅状态：true表示已订阅，false表示已取消订阅
     */
    private boolean subscribed;

    /**
     * 获取Workshop项目ID
     *
     * @return Workshop项目ID字符串
     */
    public String getId() {
        return workshopId;
    }

    /**
     * 设置Workshop项目ID
     *
     * @param workshopId Workshop项目ID
     */
    public void setId(String workshopId) {
        this.workshopId = workshopId;
    }

    /**
     * 获取订阅/取消订阅时间戳
     *
     * @return 时间戳字符串，格式为 yyyy-MM-dd HH:mm:ss
     */
    public String getTime() {
        return timestamp;
    }

    /**
     * 设置订阅/取消订阅时间戳
     *
     * @param timestamp 时间戳字符串，格式为 yyyy-MM-dd HH:mm:ss
     */
    public void setTime(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 检查订阅状态
     *
     * @return true表示已订阅，false表示已取消订阅
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * 设置订阅状态
     *
     * @param subscribed true表示已订阅，false表示已取消订阅
     */
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * 比较两个WorkshopData对象是否相等
     *
     * <p>基于项目ID、时间戳和订阅状态进行相等性比较</p>
     *
     * @param o 要比较的对象
     * @return 如果对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkshopData that = (WorkshopData) o;
        return subscribed == that.subscribed && Objects.equals(workshopId, that.workshopId) && Objects.equals(timestamp, that.timestamp);
    }

    /**
     * 计算对象的哈希值
     *
     * <p>基于项目ID、时间戳和订阅状态计算哈希值</p>
     *
     * @return 对象的哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(workshopId, timestamp, subscribed);
    }
}
