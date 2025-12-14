package cn.sun45_.wallpaperextractor.component;

import cn.sun45_.wallpaperextractor.utils.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * 系统托盘动画控制器
 *
 * <p>直接使用AWT TrayIcon实现动画</p>
 *
 * <p>功能特性：</p>
 * <ul>
 *   <li>使用应用图标作为动画背景</li>
 *   <li>圆形波纹扩散动画效果</li>
 *   <li>高性能的帧生成和播放</li>
 *   <li>完整的资源管理和错误处理</li>
 * </ul>
 */
public class AwtTrayAnimation {
    private static final Logger LOGGER = Logger.getLogger(AwtTrayAnimation.class.getName());

    // 动画配置常量
    private static final int DEFAULT_FRAME_RATE_MILLISECONDS = 50;  // 默认帧率（毫秒）
    private static final int ANIMATION_FRAME_COUNT = 16;           // 动画总帧数
    private static final int ICON_SIZE_PIXELS = 32;                // 图标尺寸（像素）

    // 动画状态
    private final TrayIcon trayIcon;
    private final List<Image> animationFrames;
    private int currentFrameIndex = 0;
    private Timer animationTimer;
    private int frameRateMilliseconds = DEFAULT_FRAME_RATE_MILLISECONDS;
    private volatile boolean isAnimationRunning = false;

    /**
     * 构造函数
     *
     * @param trayIcon 系统托盘图标实例，不能为null
     */
    public AwtTrayAnimation(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
        this.animationFrames = generateAnimationFrames();
    }

    /**
     * 生成动画帧序列
     *
     * @return 不可修改的动画帧列表，如果生成失败返回空列表
     */
    private List<Image> generateAnimationFrames() {
        List<Image> frames = new ArrayList<>(ANIMATION_FRAME_COUNT);

        for (int frameIndex = 0; frameIndex < ANIMATION_FRAME_COUNT; frameIndex++) {
            double animationProgress = ((double) (frameIndex + 1)) / ANIMATION_FRAME_COUNT;
            BufferedImage frame = createAnimationFrame(animationProgress);
            if (frame != null) {
                frames.add(frame);
            }
        }

        return Collections.unmodifiableList(frames);
    }

    /**
     * 创建单帧动画图像
     *
     * @param progress 动画进度（0.0到1.0）
     * @return 动画帧图像，如果创建失败返回null
     */
    private BufferedImage createAnimationFrame(double progress) {
        try {
            BufferedImage frame = new BufferedImage(ICON_SIZE_PIXELS, ICON_SIZE_PIXELS, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = frame.createGraphics();

            try {
                // 配置图形渲染质量
                configureGraphicsQuality(graphics);

                // 绘制动画帧内容
                drawAnimationFrame(graphics, ICON_SIZE_PIXELS, progress);
            } finally {
                graphics.dispose(); // 确保图形资源被释放
            }

            return frame;
        } catch (Exception e) {
            LOGGER.warning(ResourceManager.getString("error.animation.frame.create.failed") + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 配置图形渲染质量设置
     */
    private void configureGraphicsQuality(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /**
     * 绘制单帧动画内容
     *
     * @param graphics 图形上下文
     * @param size     画布尺寸
     * @param progress 动画进度
     */
    private void drawAnimationFrame(Graphics2D graphics, int size, double progress) {
        // 清空画布为透明背景
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, size, size);

        // 绘制应用图标背景
        drawApplicationIconBackground(graphics, size);

        // 绘制波纹动画效果
        drawRippleAnimationEffect(graphics, size, progress);
    }

    /**
     * 绘制应用图标背景
     *
     * <p>从资源管理器获取应用图标并绘制到指定位置</p>
     *
     * <p>如果应用图标加载失败，则跳过背景绘制</p>
     */
    private void drawApplicationIconBackground(Graphics2D graphics, int size) {
        Image appIcon = ResourceManager.getApplicationIcon();
        if (appIcon != null) {
            graphics.drawImage(appIcon, 0, 0, size, size, null);
        }
    }

    /**
     * 绘制波纹动画效果
     *
     * <p>根据动画进度绘制波纹扩散效果</p>
     *
     * <p>波纹从图标中心向外扩散，半径随动画进度线性增长</p>
     *
     * @param graphics 图形上下文
     * @param size     画布尺寸
     * @param progress 动画进度（0.0到1.0）
     */
    private void drawRippleAnimationEffect(Graphics2D graphics, int size, double progress) {
        double center = size / 2.0;

        // 设置波纹样式
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(2f));

        // 计算波纹半径（基于动画进度）
        double rippleRadius = center * Math.sqrt(2) * progress;

        // 绘制波纹圆环
        graphics.drawOval(
                (int) (center - rippleRadius),
                (int) (center - rippleRadius),
                (int) (rippleRadius * 2),
                (int) (rippleRadius * 2)
        );
    }

    /**
     * 开始播放动画
     */
    public void startAnimation() {
        if (isAnimationRunning || animationFrames.isEmpty()) {
            return;
        }

        isAnimationRunning = true;

        // 停止现有的定时器
        if (animationTimer != null) {
            animationTimer.cancel();
        }

        // 创建新的定时器播放动画
        animationTimer = new Timer("SystemTrayAnimation", true);
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                advanceToNextFrame();
            }
        }, 0, frameRateMilliseconds);
    }

    /**
     * 前进到下一帧动画
     */
    private void advanceToNextFrame() {
        if (trayIcon != null && !animationFrames.isEmpty()) {
            currentFrameIndex = (currentFrameIndex + 1) % animationFrames.size();
            try {
                trayIcon.setImage(animationFrames.get(currentFrameIndex));
            } catch (Exception e) {
                LOGGER.warning(ResourceManager.getString("error.tray.icon.set.failed") + ": " + e.getMessage());
                stopAnimation();
            }
        }
    }

    /**
     * 停止播放动画并显示应用图标
     */
    public void stopAnimation() {
        isAnimationRunning = false;

        // 停止定时器
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }

        // 显示应用图标
        if (trayIcon != null) {
            Image appIcon = ResourceManager.getApplicationIcon();
            trayIcon.setImage(appIcon);
        }
    }

    /**
     * 检查动画是否正在播放
     *
     * @return true如果动画正在播放，false否则
     */
    public boolean isAnimationRunning() {
        return isAnimationRunning;
    }

    /**
     * 设置动画帧率
     *
     * @param frameRateMilliseconds 帧率（毫秒）
     */
    public void setFrameRate(int frameRateMilliseconds) {
        this.frameRateMilliseconds = frameRateMilliseconds;

        // 如果动画正在播放，重新启动以应用新的帧率
        if (isAnimationRunning) {
            stopAnimation();
            startAnimation();
        }
    }

    /**
     * 释放动画资源
     *
     * <p>停止动画播放并清理相关资源，确保无内存泄漏</p>
     */
    public void dispose() {
        stopAnimation();
    }
}
