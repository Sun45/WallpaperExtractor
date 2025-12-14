package cn.sun45_.wallpaperextractor.controller;

import cn.sun45_.wallpaperextractor.Constants;
import cn.sun45_.wallpaperextractor.WallpaperExtractorApp;
import cn.sun45_.wallpaperextractor.component.IconAnimation;
import cn.sun45_.wallpaperextractor.utils.AppConfig;
import cn.sun45_.wallpaperextractor.utils.ResourceManager;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * 设置界面控制器
 *
 * <p>负责管理应用程序的设置界面，主要处理Steam安装路径的配置和验证</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>界面初始化和项目描述显示</li>
 *   <li>拖拽功能设置（支持拖拽文件夹到界面）</li>
 *   <li>Steam路径选择（通过文件选择器或拖拽）</li>
 *   <li>Steam路径验证（检查日志文件是否存在）</li>
 *   <li>配置保存和界面切换</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <ul>
 *   <li>点击拖拽区域打开文件选择器选择Steam安装目录</li>
 *   <li>或直接拖拽Steam安装目录到拖拽区域</li>
 *   <li>系统会自动验证路径有效性并保存配置</li>
 * </ul>
 *
 * @see AppConfig 应用程序配置管理
 * @see WallpaperExtractorApp 主应用程序类
 */
public class SettingsController {
    @FXML
    private StackPane dragArea;
    @FXML
    public Label hintLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Hyperlink githubLink;

    @FXML
    private Canvas iconCanvas;

    @FXML
    private ImageView appIcon;

    private IconAnimation iconAnimation;

    @FXML
    public void initialize() {
        descriptionLabel.setText(Constants.PROJECT_DESCRIPTION);
        setupDragAndDrop();
        setupIconAnimation();
    }

    private void setupIconAnimation() {
        // 初始化IconAnimation
        iconAnimation = new IconAnimation(iconCanvas);
    }

    @FXML
    protected void onGithubLinkClicked() {
        try {
            Desktop.getDesktop().browse(new java.net.URI("https://github.com/Sun45/WallpaperExtractor"));
        } catch (Exception e) {
            // 如果打开浏览器失败，可以显示错误信息
            hintLabel.setText(ResourceManager.getFormattedString("status.github.open.error.format", e.getMessage()));
            hintLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    protected void onIconClicked() {
        iconAnimation.startAnimation();
    }

    private void setupDragAndDrop() {
        // 拖拽进入区域
        dragArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // 拖拽释放
        dragArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (!files.isEmpty()) {
                    File file = files.get(0);
                    if (file.isDirectory()) {
                        handleSelectedPath(file.getAbsolutePath());
                        success = true;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    protected void onDragAreaClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(ResourceManager.getString("dialog.select.steam.path"));

        File selectedDirectory = directoryChooser.showDialog(WallpaperExtractorApp.primaryStage);
        if (selectedDirectory != null) {
            handleSelectedPath(selectedDirectory.getAbsolutePath());
        }
    }

    private void handleSelectedPath(String steamPath) {
        // 验证路径是否为有效的Steam安装路径
        if (isValidSteamPath(steamPath)) {
            // 保存路径
            AppConfig.saveSteamPath(steamPath);

            // 切换到主界面
            switchToMainView();
        } else {
            hintLabel.setText(ResourceManager.getString("status.steam.log.not.found"));
            hintLabel.setTextFill(Color.RED);
        }
    }

    private boolean isValidSteamPath(String steamPath) {
        // 简单的Steam路径验证
        File steamDir = new File(steamPath);
        if (!steamDir.exists() || !steamDir.isDirectory()) {
            return false;
        }

        String logPath = Paths.get(steamPath, "logs", "workshop_log.txt").toString();
        return new File(logPath).exists();
    }

    private void switchToMainView() {
        WallpaperExtractorApp.application.showMainScene();
    }
}
