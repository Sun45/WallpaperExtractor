package cn.sun45_.wallpaperextractor.component;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * 图标动画组件
 *
 * <p>负责管理应用程序图标的动画效果，实现类似Android代码中的圆形和三角形组合动画效果。</p>
 *
 * <p>动画效果分为两个对称阶段：</p>
 * <ul>
 *   <li><b>正向动画（0-1.1秒）</b>：圆形淡入后，圆形缩小同时两个三角形向不同方向旋转</li>
 *   <li><b>反向动画（1.1-2.2秒）</b>：反向播放动画，实现平滑的淡出效果</li>
 * </ul>
 *
 * <p>技术特点：</p>
 * <ul>
 *   <li>使用JTS Topology Suite库实现几何图形的并集操作</li>
 *   <li>两个三角形分别顺时针和逆时针旋转，形成交错效果</li>
 *   <li>使用Interpolator.EASE_BOTH实现平滑的动画插值</li>
 *   <li>支持完整的正向和反向动画播放</li>
 * </ul>
 *
 * @see javafx.animation.AnimationTimer
 * @see org.locationtech.jts.geom.Geometry
 */
public class IconAnimation {
    private final Canvas canvas;
    private AnimationTimer animationTimer;
    private boolean isAnimating = false;

    // 常量定义
    private static final double BASE_SCALE_FACTOR = 0.48;
    private static final double FADE_DURATION = 0.25;
    private static final double ANIM_DURATION = 0.85;
    private static final Color STROKE_COLOR = Color.rgb(0x88, 0x88, 0x88);

    // 可复用的几何工厂
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final GeometricShapeFactory shapeFactory = new GeometricShapeFactory(geometryFactory);

    // 预计算的固定值
    private double centerX;
    private double centerY;
    private double baseSize;
    private double hexagonVerticalOffset;
    private double hexagonHorizontalOffset;
    private double scaleEnd;
    private double scaleDelta;

    /**
     * 构造函数
     *
     * @param canvas 用于绘制动画的画布
     */
    public IconAnimation(Canvas canvas) {
        this.canvas = canvas;
        // 初始化形状工厂
        shapeFactory.setNumPoints(32); // 设置圆形精度
        // 预计算固定值
        precomputeFixedValues();
    }

    /**
     * 预计算动画中不会变化的固定值
     */
    private void precomputeFixedValues() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        centerX = canvasWidth / 2;
        centerY = canvasHeight / 2;
        baseSize = Math.min(canvasWidth, canvasHeight);

        // 计算六边形（三角形）固定尺寸
        double hexagonSize = BASE_SCALE_FACTOR * baseSize;
        hexagonVerticalOffset = hexagonSize / 2;
        hexagonHorizontalOffset = hexagonVerticalOffset * Math.sqrt(3);

        // 计算圆形尺寸变化的固定参数
        scaleEnd = BASE_SCALE_FACTOR / Math.sqrt(3);
        scaleDelta = BASE_SCALE_FACTOR - scaleEnd;
    }

    /**
     * 开始播放图标动画
     *
     * <p>动画分为两个对称阶段：</p>
     * <ul>
     *   <li><b>正向动画（0-1.1秒）</b>：圆形淡入后，圆形缩小同时两个三角形向不同方向旋转</li>
     *   <li><b>反向动画（1.1-2.2秒）</b>：反向播放动画，实现平滑的淡出效果</li>
     * </ul>
     *
     * <p>动画总时长为2.2秒，包含完整的正向和反向播放。</p>
     */
    public void startAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        isAnimating = true;
        final long startTime = System.nanoTime();
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedSeconds = (now - startTime) / 1_000_000_000.0;
                final double totalDuration = FADE_DURATION + ANIM_DURATION; // 单次动画总时长

                boolean isFadePhase = false;
                double animationProgress;

                // 第一阶段：正向动画（0-1.1秒）
                if (elapsedSeconds < totalDuration) {
                    if (elapsedSeconds < FADE_DURATION) {
                        // 淡入阶段：圆形透明度变化
                        isFadePhase = true;
                        animationProgress = elapsedSeconds / FADE_DURATION;
                    } else {
                        // 组合动画阶段：圆形缩小 + 三角形旋转
                        animationProgress = (elapsedSeconds - FADE_DURATION) / ANIM_DURATION;
                    }
                }
                // 第二阶段：反向动画（1.1-2.2秒）
                else if (elapsedSeconds < totalDuration * 2) {
                    elapsedSeconds -= totalDuration;
                    if (elapsedSeconds < ANIM_DURATION) {
                        // 反向组合动画
                        animationProgress = (ANIM_DURATION - elapsedSeconds) / ANIM_DURATION;
                    } else {
                        // 反向淡出阶段
                        isFadePhase = true;
                        animationProgress = (FADE_DURATION - elapsedSeconds + ANIM_DURATION) / FADE_DURATION;
                    }
                }
                // 动画结束
                else {
                    animationProgress = 0;
                    this.stop();
                    isAnimating = false;
                }

                drawAnimation(isFadePhase, animationProgress);
            }
        };

        animationTimer.start();
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            isAnimating = false;
        }
    }

    /**
     * 检查是否正在动画中
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * 根据动画阶段和进度绘制相应的动画效果
     *
     * @param isFadePhase       是否为淡入/淡出阶段
     * @param animationProgress 动画进度（0.0到1.0）
     */
    private void drawAnimation(boolean isFadePhase, double animationProgress) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        // 清空画布
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (animationProgress == 0) {
            return;
        }

        if (isFadePhase) {
            // 淡入/淡出阶段：圆形透明度变化
            drawFadeAnimation(graphicsContext, animationProgress);
        } else {
            // 组合动画阶段：圆形缩小 + 三角形旋转
            drawCombinedAnimation(graphicsContext, animationProgress);
        }
    }

    /**
     * 绘制圆形淡入/淡出动画
     *
     * @param graphicsContext   图形上下文
     * @param animationProgress 动画进度
     */
    private void drawFadeAnimation(GraphicsContext graphicsContext, double animationProgress) {
        double circleSize = BASE_SCALE_FACTOR * baseSize;
        double radius = circleSize;

        // 计算透明度（0x00到0xff）
        int alphaValue = (int) (animationProgress * 0xff);
        if (alphaValue > 0xff) {
            alphaValue = 0xff;
        }

        // 设置描边样式：灰色带透明度
        graphicsContext.setStroke(Color.rgb(0x88, 0x88, 0x88, alphaValue / 255.0));
        graphicsContext.setLineWidth(animationProgress * 2);

        // 绘制圆形
        graphicsContext.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * 绘制圆形和三角形组合动画
     *
     * @param graphicsContext   图形上下文
     * @param animationProgress 动画进度
     */
    private void drawCombinedAnimation(GraphicsContext graphicsContext, double animationProgress) {
        // 设置描边样式：固定灰色，线宽2像素
        graphicsContext.setStroke(STROKE_COLOR);
        graphicsContext.setLineWidth(2);

        // 计算圆形尺寸变化（使用预计算参数）
        double circleSize = (BASE_SCALE_FACTOR - scaleDelta * animationProgress) * baseSize;
        double circleRadius = circleSize;

        // 计算六边形（三角形）尺寸（使用预计算参数）
        double hexagonSize = BASE_SCALE_FACTOR * baseSize;

        // 保存当前画布状态
        graphicsContext.save();

        // 应用平滑旋转插值
        double smoothedProgress = Interpolator.EASE_BOTH.interpolate(0.0, 1.0, animationProgress);
        double rotationAngle = 180 * smoothedProgress;

        // 创建并绘制组合几何体（使用预计算参数）
        Geometry combinedGeometry = createUnionGeometry(centerX, centerY, circleRadius,
                hexagonSize, hexagonHorizontalOffset,
                hexagonVerticalOffset, rotationAngle);
        String svgPath = convertJTSGeometryToSVGPath(combinedGeometry);

        graphicsContext.beginPath();
        graphicsContext.appendSVGPath(svgPath);
        graphicsContext.stroke();

        // 恢复画布状态
        graphicsContext.restore();
    }

    /**
     * 创建圆形和两个旋转三角形的并集几何体
     *
     * @param centerX                 中心点X坐标
     * @param centerY                 中心点Y坐标
     * @param circleRadius            圆形半径
     * @param hexagonSize             六边形（三角形）尺寸
     * @param hexagonHorizontalOffset 六边形水平偏移量
     * @param hexagonVerticalOffset   六边形垂直偏移量
     * @param rotationAngle           旋转角度（度）
     * @return 合并后的几何体
     */
    private Geometry createUnionGeometry(double centerX, double centerY, double circleRadius,
                                         double hexagonSize, double hexagonHorizontalOffset,
                                         double hexagonVerticalOffset, double rotationAngle) {
        // 创建圆形几何体（使用已初始化的工厂）
        shapeFactory.setCentre(new Coordinate(centerX, centerY));
        shapeFactory.setSize(circleRadius * 2);
        Geometry circleGeometry = shapeFactory.createCircle();

        // 创建两个三角形
        Geometry triangle1Geometry = createTriangleGeometry(centerX, centerY, hexagonSize,
                hexagonHorizontalOffset, hexagonVerticalOffset,
                true); // 第一个三角形
        Geometry triangle2Geometry = createTriangleGeometry(centerX, centerY, hexagonSize,
                hexagonHorizontalOffset, hexagonVerticalOffset,
                false); // 第二个三角形

        // 对三角形应用旋转
        Geometry rotatedTriangle1 = rotateGeometry(triangle1Geometry, centerX, centerY, rotationAngle);
        Geometry rotatedTriangle2 = rotateGeometry(triangle2Geometry, centerX, centerY, -rotationAngle);

        // 执行并集操作：圆形 ∪ 顺时针旋转的三角形1 ∪ 逆时针旋转的三角形2
        return circleGeometry.union(rotatedTriangle1).union(rotatedTriangle2);
    }

    /**
     * 创建三角形几何体
     *
     * @param centerX                 中心点X坐标
     * @param centerY                 中心点Y坐标
     * @param hexagonSize             六边形（三角形）尺寸
     * @param hexagonHorizontalOffset 六边形水平偏移量
     * @param hexagonVerticalOffset   六边形垂直偏移量
     * @param isFirstTriangle         是否为第一个三角形
     * @return 三角形几何体
     */
    private Geometry createTriangleGeometry(double centerX, double centerY, double hexagonSize,
                                            double hexagonHorizontalOffset, double hexagonVerticalOffset,
                                            boolean isFirstTriangle) {
        Coordinate[] triangleCoordinates;

        if (isFirstTriangle) {
            // 第一个三角形（顶点1, 3, 5：顶部、右下、左下）
            triangleCoordinates = new Coordinate[]{
                    new Coordinate(centerX, centerY - hexagonSize),
                    new Coordinate(centerX + hexagonHorizontalOffset, centerY + hexagonVerticalOffset),
                    new Coordinate(centerX - hexagonHorizontalOffset, centerY + hexagonVerticalOffset),
                    new Coordinate(centerX, centerY - hexagonSize) // 闭合多边形
            };
        } else {
            // 第二个三角形（顶点2, 4, 6：右上、底部、左上）
            triangleCoordinates = new Coordinate[]{
                    new Coordinate(centerX + hexagonHorizontalOffset, centerY - hexagonVerticalOffset),
                    new Coordinate(centerX, centerY + hexagonSize),
                    new Coordinate(centerX - hexagonHorizontalOffset, centerY - hexagonVerticalOffset),
                    new Coordinate(centerX + hexagonHorizontalOffset, centerY - hexagonVerticalOffset) // 闭合多边形
            };
        }

        return geometryFactory.createPolygon(triangleCoordinates);
    }

    /**
     * 对几何体应用旋转变换
     *
     * @param geometry     要旋转的几何体
     * @param centerX      旋转中心X坐标
     * @param centerY      旋转中心Y坐标
     * @param angleDegrees 旋转角度（度）
     * @return 旋转后的几何体
     */
    private Geometry rotateGeometry(Geometry geometry, double centerX, double centerY, double angleDegrees) {
        AffineTransformation rotation = AffineTransformation.rotationInstance(
                Math.toRadians(angleDegrees), centerX, centerY);
        return rotation.transform(geometry);
    }

    /**
     * 将JTS Geometry对象转换为SVG路径字符串
     *
     * @param geometry JTS几何体对象
     * @return SVG路径字符串
     */
    private String convertJTSGeometryToSVGPath(Geometry geometry) {
        Path javaFXPath = convertJTSGeometryToFXPath(geometry);
        // 将JavaFX Path转换为SVG路径字符串
        StringBuilder svgPathBuilder = new StringBuilder();
        for (PathElement pathElement : javaFXPath.getElements()) {
            if (pathElement instanceof MoveTo) {
                MoveTo moveTo = (MoveTo) pathElement;
                svgPathBuilder.append("M ").append(moveTo.getX()).append(" ").append(moveTo.getY()).append(" ");
            } else if (pathElement instanceof LineTo) {
                LineTo lineTo = (LineTo) pathElement;
                svgPathBuilder.append("L ").append(lineTo.getX()).append(" ").append(lineTo.getY()).append(" ");
            } else if (pathElement instanceof ClosePath) {
                svgPathBuilder.append("Z ");
            }
        }
        return svgPathBuilder.toString();
    }

    /**
     * 将JTS Geometry对象转换为JavaFX Path对象
     *
     * @param geometry JTS几何体对象
     * @return JavaFX Path对象
     */
    private Path convertJTSGeometryToFXPath(Geometry geometry) {
        Path javaFXPath = new Path();
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
            if (coordinates.length > 0) {
                // 移动到第一个坐标点
                javaFXPath.getElements().add(new MoveTo(coordinates[0].x, coordinates[0].y));
                // 添加线段连接到其他坐标点
                for (int i = 1; i < coordinates.length; i++) {
                    javaFXPath.getElements().add(new LineTo(coordinates[i].x, coordinates[i].y));
                }
                // 闭合路径
                javaFXPath.getElements().add(new ClosePath());
            }
        }
        return javaFXPath;
    }
}
