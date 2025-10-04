/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.service.file_handeling.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

@Service
@Slf4j
public class WatermarkServiceImpl implements WatermarkService {

    @Value("${app.watermark.default-text:Exclusive Product}")
    private String defaultWatermarkText;

    @Override
    public BufferedImage applyWatermark(BufferedImage originalImage, String watermarkText) {
        return applyWatermark(originalImage, watermarkText, WatermarkConfig.subtleConfig());
    }

    @Override
    public BufferedImage applyWatermark(BufferedImage originalImage, String watermarkText, WatermarkConfig config) {
        if (originalImage == null || watermarkText == null || watermarkText.trim().isEmpty()) {
            return originalImage;
        }

        String text = watermarkText.trim().isEmpty() ? defaultWatermarkText : watermarkText;

        BufferedImage watermarkedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = watermarkedImage.createGraphics();

        try {
            g2d.drawImage(originalImage, 0, 0, null);

            setupGraphicsForWatermark(g2d);

            Font font = new Font(config.getFontName(), config.getFontStyle(),
                    calculateOptimalFontSize(originalImage, text, config));
            g2d.setFont(font);

            FontMetrics metrics = g2d.getFontMetrics(font);
            int textWidth = metrics.stringWidth(text);
            int textHeight = metrics.getHeight();

            Point position = calculateWatermarkPosition(originalImage, textWidth, textHeight, config);

            drawWatermarkWithOutline(g2d, text, position.x, position.y, config.getOpacity());

            log.debug("Applied watermark '{}' at position ({}, {})", text, position.x, position.y);

        } finally {
            g2d.dispose();
        }

        return watermarkedImage;
    }

    private void setupGraphicsForWatermark(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private int calculateOptimalFontSize(BufferedImage image, String text, WatermarkConfig config) {
        int imageWidth = image.getWidth();
        int baseFontSize = config.getFontSize();

        if (imageWidth < 300) {
            return Math.max(8, baseFontSize - 2);
        } else if (imageWidth < 600) {
            return baseFontSize;
        } else if (imageWidth < 1200) {
            return baseFontSize + 2;
        } else {
            return baseFontSize + 4;
        }
    }

    private Point calculateWatermarkPosition(BufferedImage image, int textWidth, int textHeight, WatermarkConfig config) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int padding = config.getPadding();

        return switch (config.getPosition().toLowerCase()) {
            case "top-left" -> new Point(padding, textHeight + padding);
            case "top-right" -> new Point(imageWidth - textWidth - padding, textHeight + padding);
            case "top-center" -> new Point((imageWidth - textWidth) / 2, textHeight + padding);
            case "center-left" -> new Point(padding, (imageHeight + textHeight) / 2);
            case "center" -> new Point((imageWidth - textWidth) / 2, (imageHeight + textHeight) / 2);
            case "center-right" -> new Point(imageWidth - textWidth - padding, (imageHeight + textHeight) / 2);
            case "bottom-left" -> new Point(padding, imageHeight - padding);
            case "bottom-center" -> new Point((imageWidth - textWidth) / 2, imageHeight - padding);
            default -> new Point(imageWidth - textWidth - padding, imageHeight - padding);
        };
    }

    private void drawWatermarkWithOutline(Graphics2D g2d, String text, int x, int y, float opacity) {
        Font font = g2d.getFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Shape textShape = gv.getOutline(x, y);

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        g2d.setColor(new Color(0, 0, 0, Math.round(255 * opacity * 0.3f)));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(textShape);

        g2d.setColor(new Color(255, 255, 255, Math.round(255 * opacity)));
        g2d.fill(textShape);

        g2d.setComposite(originalComposite);
    }
}