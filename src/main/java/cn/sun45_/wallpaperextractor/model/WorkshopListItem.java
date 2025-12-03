package cn.sun45_.wallpaperextractor.model;

import cn.sun45_.wallpaperextractor.monitor.WorkshopData;

/**
 * Workshop列表项数据模型
 *
 * <p>用于在ListView中展示Workshop项目信息，包含订阅状态、拷贝状态和操作按钮</p>
 *
 * <p>功能特性：</p>
 * <ul>
 *   <li>封装WorkshopData基础数据</li>
 *   <li>管理拷贝状态和状态消息</li>
 *   <li>提供订阅状态和拷贝状态的显示文本</li>
 *   <li>支持CSS样式类动态切换</li>
 * </ul>
 */
public class WorkshopListItem {
    /**
     * Workshop基础数据
     */
    private final WorkshopData workshopData;

    /**
     * 拷贝状态
     */
    private CopyStatus copyStatus = CopyStatus.NOT_COPIED;

    /**
     * 拷贝状态消息
     */
    private String copyStatusMessage = "";

    /**
     * 构造函数
     *
     * @param workshopData Workshop基础数据
     */
    public WorkshopListItem(WorkshopData workshopData) {
        this.workshopData = workshopData;
    }

    /**
     * 拷贝状态枚举
     *
     * <p>定义文件拷贝过程中的各种状态</p>
     */
    public enum CopyStatus {
        /**
         * 未拷贝状态
         */
        NOT_COPIED("未拷贝"),

        /**
         * 拷贝进行中状态
         */
        COPYING("拷贝中"),

        /**
         * 拷贝成功状态
         */
        SUCCESS("拷贝成功"),

        /**
         * 拷贝失败状态
         */
        FAILED("拷贝失败");

        /**
         * 状态显示名称
         */
        private final String displayName;

        /**
         * 枚举构造函数
         *
         * @param displayName 状态显示名称
         */
        CopyStatus(String displayName) {
            this.displayName = displayName;
        }

        /**
         * 获取状态显示名称
         *
         * @return 状态显示名称
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 获取Workshop项目ID
     *
     * @return Workshop项目ID
     */
    public String getId() {
        return workshopData.getId();
    }

    /**
     * 获取订阅/取消订阅时间
     *
     * @return 时间字符串，格式为 yyyy-MM-dd HH:mm:ss
     */
    public String getTime() {
        return workshopData.getTime();
    }

    /**
     * 检查是否已订阅
     *
     * @return true表示已订阅，false表示已取消订阅
     */
    public boolean isSubscribed() {
        return workshopData.isSubscribed();
    }

    /**
     * 获取Workshop基础数据
     *
     * @return WorkshopData对象
     */
    public WorkshopData getWorkshopData() {
        return workshopData;
    }

    /**
     * 获取订阅状态文本
     *
     * @return "已订阅" 或 "已取消订阅"
     */
    public String getSubscribeStatus() {
        return isSubscribed() ? "已订阅" : "已取消订阅";
    }

    /**
     * 获取订阅状态样式类
     *
     * @return CSS样式类名，用于界面显示
     */
    public String getSubscribeStyleClass() {
        return isSubscribed() ? "subscribe-status-subscribed" : "subscribe-status-unsubscribed";
    }

    /**
     * 获取拷贝状态
     *
     * @return 拷贝状态枚举值
     */
    public CopyStatus getCopyStatus() {
        return copyStatus;
    }

    /**
     * 设置拷贝状态
     *
     * @param copyStatus        拷贝状态枚举值，不能为null
     * @param copyStatusMessage 拷贝状态详细消息（可选）
     */
    public void setCopyStatus(CopyStatus copyStatus, String... copyStatusMessage) {
        if (copyStatus == null) {
            throw new IllegalArgumentException("copyStatus参数不能为null");
        }
        
        this.copyStatus = copyStatus;
        this.copyStatusMessage = (copyStatusMessage != null && copyStatusMessage.length > 0) 
            ? copyStatusMessage[0] 
            : "";
    }

    /**
     * 获取拷贝状态文本
     *
     * <p>包含状态和详细消息的组合文本</p>
     *
     * @return 拷贝状态显示文本
     */
    public String getCopyStatusText() {
        if (copyStatusMessage != null && !copyStatusMessage.isEmpty()) {
            return copyStatus.getDisplayName() + ": " + copyStatusMessage;
        }
        return copyStatus.getDisplayName();
    }
}
