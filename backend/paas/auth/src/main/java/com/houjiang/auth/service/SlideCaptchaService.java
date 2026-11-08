package com.houjiang.auth.service;

import com.houjiang.commons.config.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 滑动拼图验证码服务。
 *
 * <p>为商城 PC/uniapp 登录提供「验证2」滑动拼图能力：
 * 生成随机背景图与缺口坐标（{@code /generate}），并在用户拖动完成后
 * 校验横向落点（{@code /verifyGenerateCaptcha}）。答案以 imgToken 为键
 * 存放 Redis，5 分钟有效，验证通过后立即失效，防止重放。</p>
 *
 * @author Amos
 */
@Slf4j
@Service
public class SlideCaptchaService {

    /** 背景图宽度（与前端 SlideVerify 配置一致） */
    private static final int WIDTH = 320;
    /** 背景图高度（与前端 SlideVerify 配置一致） */
    private static final int HEIGHT = 200;
    /** 拼图块边长（与前端 SlideVerify 配置一致） */
    private static final int BLOCK_SIZE = 60;
    /** 横向落点允许误差（像素，与前端 tolerance 一致） */
    private static final int TOLERANCE = 5;
    /** 验证码有效期（秒） */
    private static final long EXPIRE_SECONDS = 300;
    /** Redis 键前缀 */
    private static final String REDIS_PREFIX = "SLIDE_CAPTCHA_";

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private RedisCache redisCache;

    /**
     * 生成滑动拼图验证码。
     *
     * <p>随机生成缺口坐标，绘制合成背景图（渐变+几何图形，避免依赖服务器字体/图片资源），
     * 并将答案（targetX）写入 Redis。</p>
     *
     * @return 包含 bgImage（PNG Base64，无 data: 前缀）、imgToken、targetX、targetY 的 Map
     */
    public Map<String, Object> generate() {
        // 缺口横向范围与前端 generateRandomPosition 保持一致，保证拼图块可拖动到达
        int minX = (int) (BLOCK_SIZE * 1.5);
        int maxX = WIDTH - (int) (BLOCK_SIZE * 1.5);
        int minY = 20;
        int maxY = HEIGHT - BLOCK_SIZE - 20;
        int targetX = random.nextInt(maxX - minX + 1) + minX;
        int targetY = random.nextInt(maxY - minY + 1) + minY;

        String imgToken = UUID.randomUUID().toString().replace("-", "");
        String bgImage = drawBackground();

        redisCache.setCacheObject(REDIS_PREFIX + imgToken, String.valueOf(targetX));
        redisCache.expire(REDIS_PREFIX + imgToken, EXPIRE_SECONDS, TimeUnit.SECONDS);

        Map<String, Object> data = new HashMap<>();
        data.put("bgImage", bgImage);
        data.put("imgToken", imgToken);
        data.put("targetX", targetX);
        data.put("targetY", targetY);
        return data;
    }

    /**
     * 校验滑动拼图验证码。
     *
     * @param imgToken 生成接口下发的验证码 token
     * @param targetX  用户拖动后拼图块的横向位置
     * @return 是否在允许误差内；token 不存在/已过期视为失败
     */
    public boolean verify(String imgToken, int targetX) {
        if (imgToken == null || imgToken.isBlank()) {
            return false;
        }
        Object stored = redisCache.getCacheObject(REDIS_PREFIX + imgToken);
        if (stored == null) {
            log.warn("滑动验证码 token 不存在或已过期: {}", imgToken);
            return false;
        }
        int expected;
        try {
            expected = Integer.parseInt(String.valueOf(stored));
        } catch (NumberFormatException e) {
            log.error("滑动验证码 Redis 值解析失败: {}", stored);
            return false;
        }
        boolean ok = Math.abs(expected - targetX) <= TOLERANCE;
        if (ok) {
            // 一次性使用，验证通过后立即删除，防止重放
            redisCache.deleteObject(REDIS_PREFIX + imgToken);
        }
        return ok;
    }

    /**
     * 使用 Java2D 绘制合成背景图（320x200 PNG）。
     *
     * <p>由渐变底色、随机圆形、波浪带和噪点构成，色彩从预设调色板中随机选取，
     * 保证每次生成的图片都不同且不依赖外部资源。</p>
     *
     * @return PNG 图片的 Base64 字符串（无 data: 前缀）
     */
    private String drawBackground() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 预设调色板：[渐变起色, 渐变止色, 图形色1, 图形色2, 图形色3]
            Color[][] palettes = {
                {new Color(238, 243, 255), new Color(255, 223, 226), new Color(255, 138, 138), new Color(124, 167, 255), new Color(123, 216, 196)},
                {new Color(224, 242, 241), new Color(254, 241, 219), new Color(244, 162, 97), new Color(72, 149, 239), new Color(122, 193, 153)},
                {new Color(240, 234, 255), new Color(222, 243, 255), new Color(157, 123, 255), new Color(94, 196, 255), new Color(255, 154, 158)},
                {new Color(255, 240, 222), new Color(226, 244, 222), new Color(250, 176, 92), new Color(126, 192, 105), new Color(96, 165, 250)},
                {new Color(226, 232, 240), new Color(240, 228, 236), new Color(100, 116, 139), new Color(196, 132, 168), new Color(94, 176, 192)}
            };
            Color[] palette = palettes[random.nextInt(palettes.length)];

            // 渐变底色
            g.setPaint(new GradientPaint(0, 0, palette[0], WIDTH, HEIGHT, palette[1]));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // 随机半透明圆形
            for (int i = 0; i < 3; i++) {
                Color c = palette[2 + random.nextInt(3)];
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 140 + random.nextInt(60)));
                int d = 60 + random.nextInt(90);
                g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), d, d);
            }

            // 波浪带
            Color wave = palette[2 + random.nextInt(3)];
            g.setColor(new Color(wave.getRed(), wave.getGreen(), wave.getBlue(), 170));
            int baseY = HEIGHT / 2 + random.nextInt(HEIGHT / 3);
            g.setStroke(new BasicStroke(14f));
            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
            path.moveTo(0, baseY);
            path.curveTo(WIDTH * 0.3, baseY - 60, WIDTH * 0.6, baseY + 60, WIDTH, baseY - 30);
            g.draw(path);

            // 细碎噪点纹理，增加图片差异性
            for (int i = 0; i < 60; i++) {
                Color c = palette[2 + random.nextInt(3)];
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 90));
                g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 3, 3);
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("滑动验证码背景图生成失败", e);
            throw new IllegalStateException("滑动验证码背景图生成失败", e);
        }
    }
}
