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
package com.singhtwenty2.commerce_service.service.file_handeling;

import java.awt.image.BufferedImage;

public interface WatermarkService {

    BufferedImage applyWatermark(BufferedImage originalImage, String watermarkText);

    BufferedImage applyWatermark(BufferedImage originalImage, String watermarkText, WatermarkConfig config);

    class WatermarkConfig {
        private float opacity = 0.3f;
        private int fontSize = 12;
        private String position = "bottom-right";
        private int padding = 10;
        private String fontName = "Arial";
        private int fontStyle = java.awt.Font.PLAIN;

        public static WatermarkConfig defaultConfig() {
            return new WatermarkConfig();
        }

        public static WatermarkConfig subtleConfig() {
            WatermarkConfig config = new WatermarkConfig();
            config.opacity = 0.2f;
            config.fontSize = 10;
            config.padding = 8;
            return config;
        }

        public float getOpacity() {
            return opacity;
        }

        public WatermarkConfig setOpacity(float opacity) {
            this.opacity = Math.max(0.1f, Math.min(1.0f, opacity));
            return this;
        }

        public int getFontSize() {
            return fontSize;
        }

        public WatermarkConfig setFontSize(int fontSize) {
            this.fontSize = Math.max(8, Math.min(72, fontSize));
            return this;
        }

        public String getPosition() {
            return position;
        }

        public WatermarkConfig setPosition(String position) {
            this.position = position;
            return this;
        }

        public int getPadding() {
            return padding;
        }

        public WatermarkConfig setPadding(int padding) {
            this.padding = Math.max(0, padding);
            return this;
        }

        public String getFontName() {
            return fontName;
        }

        public WatermarkConfig setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public int getFontStyle() {
            return fontStyle;
        }

        public WatermarkConfig setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }
    }
}