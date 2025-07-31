package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.service.ImageProcessingService;
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

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png", "webp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MIN_DIMENSION = 50;

    @Override
    public ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config) {
        validateImageFile(file);

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            }

            ImageMetadata metadata = ImageMetadata.builder()
                    .width(originalImage.getWidth())
                    .height(originalImage.getHeight())
                    .originalFormat(getFileExtension(file.getOriginalFilename()))
                    .originalSize(file.getSize())
                    .build();

            BufferedImage processedImage = resizeImage(originalImage, config);

            byte[] imageData = compressImage(processedImage, config);
            String contentType = "image/" + config.getOutputFormat();
            String fileExtension = "." + config.getOutputFormat();

            return ProcessedImageResult.builder()
                    .imageData(imageData)
                    .contentType(contentType)
                    .fileExtension(fileExtension)
                    .metadata(metadata)
                    .fileSizeBytes(imageData.length)
                    .build();

        } catch (IOException e) {
            log.error("Failed to process image: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image");
        }
    }

    @Override
    public ProcessedImageResult processBrandLogo(MultipartFile file) {
        return processImage(file, ImageProcessingConfig.brandLogo());
    }

    @Override
    public ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail) {
        ImageProcessingConfig config = isThumbnail ?
                ImageProcessingConfig.productThumbnail() :
                ImageProcessingConfig.productCatalog();
        return processImage(file, config);
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
        return Arrays.asList(SUPPORTED_FORMATS).contains(extension);
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

    private Dimension calculateNewDimensions(
            int originalWidth,
            int originalHeight,
            int maxWidth,
            int maxHeight,
            boolean maintainAspectRatio) {
        if (!maintainAspectRatio) {
            return new Dimension(Math.min(originalWidth, maxWidth), Math.min(originalHeight, maxHeight));
        }

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return new Dimension(originalWidth, originalHeight);
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return new Dimension(newWidth, newHeight);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, ImageProcessingConfig config) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth < MIN_DIMENSION || originalHeight < MIN_DIMENSION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Image dimensions must be at least " + MIN_DIMENSION + "x" + MIN_DIMENSION + " pixels");
        }

        Dimension newDimension = calculateNewDimensions(
                originalWidth, originalHeight,
                config.getMaxWidth(), config.getMaxHeight(),
                config.isMaintainAspectRatio()
        );

        if (newDimension.width == originalWidth && newDimension.height == originalHeight) {
            return originalImage;
        }

        BufferedImage resizedImage = new BufferedImage(
                newDimension.width, newDimension.height, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newDimension.width, newDimension.height);
        g2d.drawImage(originalImage, 0, 0, newDimension.width, newDimension.height, null);
        g2d.dispose();

        return resizedImage;
    }

    private byte[] compressToWebP(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] compressImage(BufferedImage image, ImageProcessingConfig config) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if ("webp".equals(config.getOutputFormat())) {
            return compressToWebP(image, config.getQuality());
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
                param.setCompressionQuality(config.getQuality());
            }

            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return byteArrayOutputStream.toByteArray();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}