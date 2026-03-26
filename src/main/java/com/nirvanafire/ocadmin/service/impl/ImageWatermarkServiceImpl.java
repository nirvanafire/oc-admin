package com.nirvanafire.ocadmin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class ImageWatermarkServiceImpl {

    public byte[] addWatermark(byte[] imageData, String watermarkText) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = watermarkedImage.createGraphics();

            // 绘制原图
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置水印字体和颜色
            Font font = new Font("Arial", Font.BOLD, Math.max(width / 30, 12));
            g2d.setFont(font);
            g2d.setColor(new Color(255, 255, 255, 128));

            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 计算水印位置（右下角，留出边距）
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(watermarkText);
            int textHeight = fontMetrics.getHeight();
            int padding = 20;
            int x = width - textWidth - padding;
            int y = height - padding;

            // 绘制半透明背景
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRect(x - 10, y - textHeight, textWidth + 20, textHeight + 10);

            // 绘制水印文字
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.drawString(watermarkText, x, y);

            g2d.dispose();

            // 输出为PNG格式（支持透明度）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(watermarkedImage, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("添加水印失败", e);
            return imageData;
        }
    }
}
