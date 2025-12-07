package cn.sun45_.wallpaperextractor.component;

import cn.sun45_.wallpaperextractor.model.WorkshopListItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Workshop列表项自定义单元格组件
 *
 * <p>用于在ListView中显示Workshop项目的详细信息，提供丰富的UI交互功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示Workshop项目ID、订阅状态和时间戳</li>
 *   <li>实时显示文件拷贝状态（未拷贝/拷贝中/成功/失败）</li>
 *   <li>提供打开本地文件夹和创意工坊网页的快捷操作</li>
 *   <li>支持根据订阅状态和拷贝状态动态调整样式</li>
 *   <li>实现现代化的扁平化UI设计</li>
 * </ul>
 *
 * <p>UI布局结构：</p>
 * <ul>
 *   <li>左侧：项目ID + 状态和时间信息</li>
 *   <li>中间：自适应间隔区域</li>
 *   <li>右侧：拷贝状态标签 + 操作按钮</li>
 * </ul>
 *
 * <p>样式特性：</p>
 * <ul>
 *   <li>订阅状态：蓝色主题（已订阅） vs 灰色主题（已取消订阅）</li>
 *   <li>拷贝状态：不同颜色标识不同状态</li>
 *   <li>按钮交互：悬停和点击效果</li>
 *   <li>阴影效果：卡片式设计</li>
 * </ul>
 *
 * @see WorkshopListItem Workshop列表项数据模型
 * @see ListCell JavaFX列表单元格基类
 */
public class WorkshopListCell extends ListCell<WorkshopListItem> {
    /**
     * 根容器，用于设置外边距
     */
    private final HBox rootContainer;

    /**
     * 内容容器，包含所有UI组件
     */
    private final HBox contentContainer;

    /**
     * Workshop项目ID标签
     */
    private final Label idLabel;

    /**
     * 订阅状态标签
     */
    private final Label statusLabel;

    /**
     * 拷贝状态标签
     */
    private final Label copyStatusLabel;

    /**
     * 时间戳标签
     */
    private final Label timeLabel;

    /**
     * 打开文件夹按钮
     */
    private final Button openFolderButton;

    /**
     * 打开网页按钮
     */
    private final Button openWebButton;

    /**
     * 间隔区域，用于弹性布局
     */
    private final Region spacer;

    /**
     * 单元格事件监听器
     */
    private final WorkshopListCellListener listener;

    /**
     * Workshop列表项操作监听器接口
     *
     * <p>定义列表项中按钮点击事件的回调方法</p>
     *
     * <p>实现此接口的类可以接收列表项中的操作事件：</p>
     * <ul>
     *   <li>打开本地Workshop文件夹</li>
     *   <li>打开Steam创意工坊网页</li>
     * </ul>
     */
    public interface WorkshopListCellListener {
        /**
         * 打开Workshop文件夹回调方法
         *
         * @param workshopId Workshop项目ID
         */
        void onOpenWorkshopFolder(String workshopId);

        /**
         * 打开Workshop网页回调方法
         *
         * @param workshopId Workshop项目ID
         */
        void onOpenWorkshopWebPage(String workshopId);
    }

    /**
     * 默认构造函数
     *
     * <p>创建一个不带监听器的列表单元格</p>
     */
    public WorkshopListCell() {
        this(null);
    }

    /**
     * 带监听器的构造函数
     *
     * <p>创建一个带有操作监听器的列表单元格</p>
     *
     * @param listener 列表项操作监听器，可以为null
     */
    public WorkshopListCell(WorkshopListCellListener listener) {
        this.listener = listener;
        // 移除固定高度限制，让内容自适应
        setPrefHeight(Region.USE_COMPUTED_SIZE);
        setMinHeight(Region.USE_COMPUTED_SIZE);

        // 移除选中效果并设置列表项间距
        setStyle("-fx-background-color: transparent; -fx-padding: 4px 0;");

        // 创建ID标签
        idLabel = new Label();
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        idLabel.setMaxWidth(250);
        idLabel.setEllipsisString("...");

        // 创建状态标签
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");

        // 创建时间标签
        timeLabel = new Label();
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        // 创建状态和时间水平容器
        HBox statusTimeContainer = new HBox(statusLabel, timeLabel);
        statusTimeContainer.setSpacing(10);
        statusTimeContainer.setStyle("-fx-alignment: center-left;");

        // 创建左侧信息容器
        VBox leftInfoContainer = new VBox(idLabel, statusTimeContainer);
        leftInfoContainer.setSpacing(5);

        // 创建拷贝状态标签 - 突出显示
        copyStatusLabel = new Label();
        copyStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 6px 12px; " +
                "-fx-background-radius: 6px; -fx-border-radius: 6px;");
        copyStatusLabel.setMinWidth(90);
        copyStatusLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // 创建带图标的按钮
        FontIcon folderIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        folderIcon.setIconColor(Color.WHITE);
        folderIcon.setIconSize(12);

        FontIcon webIcon = new FontIcon(FontAwesomeSolid.EXTERNAL_LINK_ALT);
        webIcon.setIconColor(Color.WHITE);
        webIcon.setIconSize(12);

        openFolderButton = new Button("", folderIcon);
        openWebButton = new Button("", webIcon);

        // 设置按钮样式 - 使用现代扁平化设计
        String buttonStyle = "-fx-font-size: 11px; -fx-padding: 8px 12px; " +
                "-fx-background-color: #007bff; " +
                "-fx-text-fill: white; -fx-background-radius: 6px; " +
                "-fx-cursor: hand; -fx-border: none;";

        String buttonHoverStyle = "-fx-background-color: #0056b3; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,123,255,0.3), 4, 0.3, 0, 2);";

        String buttonPressedStyle = "-fx-background-color: #004085;";

        openFolderButton.setStyle(buttonStyle);
        openWebButton.setStyle(buttonStyle);

        // 添加按钮交互效果
        openFolderButton.setOnMouseEntered(e -> openFolderButton.setStyle(buttonStyle + buttonHoverStyle));
        openFolderButton.setOnMouseExited(e -> openFolderButton.setStyle(buttonStyle));
        openFolderButton.setOnMousePressed(e -> openFolderButton.setStyle(buttonStyle + buttonPressedStyle));
        openFolderButton.setOnMouseReleased(e -> openFolderButton.setStyle(buttonStyle + buttonHoverStyle));

        openWebButton.setOnMouseEntered(e -> openWebButton.setStyle(buttonStyle + buttonHoverStyle));
        openWebButton.setOnMouseExited(e -> openWebButton.setStyle(buttonStyle));
        openWebButton.setOnMousePressed(e -> openWebButton.setStyle(buttonStyle + buttonPressedStyle));
        openWebButton.setOnMouseReleased(e -> openWebButton.setStyle(buttonStyle + buttonHoverStyle));

        // 设置按钮提示文本
        openFolderButton.setTooltip(new javafx.scene.control.Tooltip("在资源管理器中打开"));
        openWebButton.setTooltip(new javafx.scene.control.Tooltip("在创意工坊中打开"));

        // 创建右侧容器（拷贝状态 + 按钮）
        HBox rightContainer = new HBox(copyStatusLabel, openFolderButton, openWebButton);
        rightContainer.setSpacing(8);
        rightContainer.setStyle("-fx-alignment: center-right;");

        // 创建间隔区域
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 创建主容器 - 调整外边距和间距
        contentContainer = new HBox(leftInfoContainer, spacer, rightContainer);
        contentContainer.setSpacing(8);
        contentContainer.setStyle("-fx-padding: 6px 12px; " +
                "-fx-alignment: center-left; " +
                "-fx-background-radius: 6px; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-color: #ffffff; " +
                "-fx-border-color: #e9ecef; " +
                "-fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0, 0, 1); ");
        // 移除最大高度限制
        contentContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // 创建外部容器用于设置边距
        rootContainer = new HBox(contentContainer);
        rootContainer.setStyle("-fx-padding: 0 3px;");
        HBox.setHgrow(contentContainer, Priority.ALWAYS);

        // 设置按钮点击事件
        setupButtonActions();
    }

    /**
     * 更新列表项内容
     *
     * <p>当列表项数据发生变化时调用，负责更新所有UI组件的显示内容</p>
     *
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>如果项为空，清除显示内容</li>
     *   <li>如果项不为空，更新所有标签和按钮的显示</li>
     *   <li>根据订阅状态设置不同的颜色主题</li>
     *   <li>根据拷贝状态设置不同的样式和颜色</li>
     *   <li>设置按钮的用户数据以便事件处理</li>
     * </ul>
     *
     * @param item  要显示的Workshop列表项数据
     * @param empty 指示该项是否为空
     */
    @Override
    protected void updateItem(WorkshopListItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            // 清除显示内容
            setGraphic(null);
            setText(null);
        } else {
            // 更新文本内容
            idLabel.setText("创意工坊 ID: " + item.getId());
            statusLabel.setText("状态: " + item.getSubscribeStatus());
            timeLabel.setText("时间: " + item.getTime());
            copyStatusLabel.setText(item.getCopyStatusText());

            // 设置状态文本颜色
            if (item.isSubscribed()) {
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #1e88e5;");
                contentContainer.setStyle(contentContainer.getStyle() + " -fx-background-color: #e3f2fd; -fx-border-color: #bbdefb;");
            } else {
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #78909c;");
                contentContainer.setStyle(contentContainer.getStyle() + " -fx-background-color: #eceff1; -fx-border-color: #cfd8dc;");
            }

            // 根据拷贝状态设置拷贝状态标签颜色和背景
            switch (item.getCopyStatus()) {
                case NOT_COPIED:
                    copyStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #7f8c8d; " +
                            "-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1px;");
                    break;
                case COPYING:
                    copyStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #f39c12; " +
                            "-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-width: 1px;");
                    break;
                case SUCCESS:
                    copyStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #27ae60; " +
                            "-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-width: 1px;");
                    break;
                case FAILED:
                    copyStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #e74c3c; " +
                            "-fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; -fx-border-width: 1px;");
                    break;
            }

            // 设置按钮的用户数据
            openFolderButton.setUserData(item);
            openWebButton.setUserData(item);

            setGraphic(rootContainer);
        }
    }

    private void setupButtonActions() {
        openFolderButton.setOnAction(event -> {
            WorkshopListItem item = (WorkshopListItem) openFolderButton.getUserData();
            if (item != null && listener != null) {
                listener.onOpenWorkshopFolder(item.getId());
            }
        });

        openWebButton.setOnAction(event -> {
            WorkshopListItem item = (WorkshopListItem) openWebButton.getUserData();
            if (item != null && listener != null) {
                listener.onOpenWorkshopWebPage(item.getId());
            }
        });
    }
}
