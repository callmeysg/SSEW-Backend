package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.service.file_handeling.ImageProcessingService;
import com.singhtwenty2.commerce_service.service.file_handeling.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ImageDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final WatermarkService watermarkService;

    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png", "webp", "gif", "bmp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MIN_DIMENSION = 50;
    private static final int MAX_DIMENSION = 5000;

    @Override
    public ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config) {
        return processImage(file, config, null);
    }

    @Override
    public ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config, String watermarkText) {
        validateImageFile(file);

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file - could not read image data");
            }

            log.debug("Processing image - Original: {}x{}, Size: {} bytes",
                    originalImage.getWidth(), originalImage.getHeight(), file.getSize());

            ImageMetadata metadata = ImageMetadata.builder()
                    .width(originalImage.getWidth())
                    .height(originalImage.getHeight())
                    .originalFormat(getFileExtension(file.getOriginalFilename()))
                    .originalSize(file.getSize())
                    .build();

            BufferedImage processedImage = resizeImageGracefully(originalImage, config);

            if (watermarkText != null && !watermarkText.trim().isEmpty()) {
                processedImage = watermarkService.applyWatermark(processedImage, watermarkText);
                log.debug("Applied watermark to image: {}", watermarkText);
            }

            byte[] imageData = compressImageWithBetterQuality(processedImage, config);
            String contentType = "image/" + config.getOutputFormat();
            String fileExtension = "." + config.getOutputFormat();

            log.debug("Image processed successfully - Final: {}x{}, Size: {} bytes",
                    processedImage.getWidth(), processedImage.getHeight(), imageData.length);

            return ProcessedImageResult.builder()
                    .imageData(imageData)
                    .contentType(contentType)
                    .fileExtension(fileExtension)
                    .metadata(metadata)
                    .fileSizeBytes(imageData.length)
                    .build();

        } catch (IOException e) {
            log.error("Failed to process image: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image: " + e.getMessage());
        }
    }

    @Override
    public ProcessedImageResult processManufacturerLogo(MultipartFile file) {
        return processImage(file, ImageProcessingConfig.brandLogo(), null);
    }

    @Override
    public ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail) {
        return processProductImage(file, isThumbnail, null);
    }

    @Override
    public ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail, String watermarkText) {
        ImageProcessingConfig config = isThumbnail ?
                ImageProcessingConfig.productThumbnail() :
                ImageProcessingConfig.productCatalog();
        return processImage(file, config, watermarkText);
    }

    @Override
    public boolean isValidImageFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        boolean isValidFormat = Arrays.asList(SUPPORTED_FORMATS).contains(extension);

        if (isValidFormat) {
            try {
                BufferedImage testImage = ImageIO.read(file.getInputStream());
                return testImage != null;
            } catch (IOException e) {
                log.warn("File has valid extension but is not a valid image: {}", originalFilename);
                return false;
            }
        }

        return false;
    }

    @Override
    public ImageMetadata extractImageMetadata(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            }

            return ImageMetadata.builder()
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .originalFormat(getFileExtension(file.getOriginalFilename()))
                    .originalSize(file.getSize())
                    .build();
        } catch (IOException e) {
            log.error("Failed to extract image metadata: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to extract image metadata");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File size exceeds maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }

        if (!isValidImageFormat(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported image format. Supported formats: " + Arrays.toString(SUPPORTED_FORMATS));
        }
    }

    private Dimension calculateOptimalDimensions(
            int originalWidth,
            int originalHeight,
            int maxWidth,
            int maxHeight,
            boolean maintainAspectRatio) {

        if (originalWidth > MAX_DIMENSION || originalHeight > MAX_DIMENSION) {
            log.warn("Image dimensions exceed safety limit: {}x{}", originalWidth, originalHeight);
        }

        if (!maintainAspectRatio) {
            return new Dimension(Math.min(originalWidth, maxWidth), Math.min(originalHeight, maxHeight));
        }

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return new Dimension(originalWidth, originalHeight);
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) Math.round(originalWidth * ratio);
        int newHeight = (int) Math.round(originalHeight * ratio);

        newWidth = Math.max(newWidth, MIN_DIMENSION);
        newHeight = Math.max(newHeight, MIN_DIMENSION);

        return new Dimension(newWidth, newHeight);
    }

    private BufferedImage resizeImageGracefully(BufferedImage originalImage, ImageProcessingConfig config) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth < MIN_DIMENSION || originalHeight < MIN_DIMENSION) {
            log.warn("Image dimensions are smaller than recommended: {}x{} (minimum: {}x{})",
                    originalWidth, originalHeight, MIN_DIMENSION, MIN_DIMENSION);
        }

        Dimension newDimension = calculateOptimalDimensions(
                originalWidth, originalHeight,
                config.getMaxWidth(), config.getMaxHeight(),
                config.isMaintainAspectRatio()
        );

        if (newDimension.width == originalWidth && newDimension.height == originalHeight) {
            return originalImage;
        }

        log.debug("Resizing image from {}x{} to {}x{}",
                originalWidth, originalHeight, newDimension.width, newDimension.height);

        BufferedImage resizedImage = new BufferedImage(
                newDimension.width, newDimension.height, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newDimension.width, newDimension.height);

        g2d.drawImage(originalImage, 0, 0, newDimension.width, newDimension.height, null);
        g2d.dispose();

        return resizedImage;
    }

    private byte[] compressImageWithBetterQuality(BufferedImage image, ImageProcessingConfig config) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if ("webp".equals(config.getOutputFormat())) {
            return compressToWebPFallback(image, config.getQuality());
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(config.getOutputFormat());
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found for format: " + config.getOutputFormat());
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(byteArrayOutputStream)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                float actualQuality = adjustQualityByFormat(config.getOutputFormat(), config.getQuality());
                param.setCompressionQuality(actualQuality);

                log.debug("Using compression quality: {} for format: {}", actualQuality, config.getOutputFormat());
            }

            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        byte[] result = byteArrayOutputStream.toByteArray();
        log.debug("Compressed image size: {} bytes (quality: {})", result.length, config.getQuality());

        return result;
    }

    private float adjustQualityByFormat(String format, float requestedQuality) {
        return switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> Math.max(0.8f, requestedQuality);
            case "png" -> 1.0f;
            default -> Math.max(0.85f, requestedQuality);
        };
    }

    private byte[] compressToWebPFallback(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        boolean written = ImageIO.write(image, "png", byteArrayOutputStream);
        if (!written) {
            throw new IOException("Failed to write image in PNG format");
        }

        byte[] result = byteArrayOutputStream.toByteArray();
        log.debug("WebP fallback (PNG) image size: {} bytes", result.length);

        return result;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}