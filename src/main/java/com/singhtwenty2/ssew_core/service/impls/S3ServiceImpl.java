package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.temp-bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private static final String BRAND_FOLDER = "brands";
    private static final String PRODUCT_FOLDER = "products";
    private static final String THUMBNAIL_SUBFOLDER = "thumbnails";
    private static final String CATALOG_SUBFOLDER = "catalog";

    @Override
    public void initializeS3Bucket() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            try {
                s3Client.headBucket(headBucketRequest);
                log.info("Bucket {} already exists", bucketName);
            } catch (NoSuchBucketException e) {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .createBucketConfiguration(CreateBucketConfiguration.builder()
                                .locationConstraint(BucketLocationConstraint.fromValue(region))
                                .build())
                        .build();
                s3Client.createBucket(createBucketRequest);
                log.info("Bucket {} created successfully", bucketName);
            }

            configureBucketPolicies();
            createFolderStructure();

        } catch (Exception e) {
            log.error("Failed to initialize S3 bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize S3 bucket", e);
        }
    }

    @Override
    public ImageUploadResult uploadBrandLogo(ProcessedImageResult processedImage, String brandSlug) {
        try {
            String objectKey = generateBrandLogoKey(brandSlug, processedImage.getFileExtension());

            Map<String, String> metadata = Map.of(
                    "entity-type", "brand",
                    "entity-id", brandSlug,
                    "image-type", "logo",
                    "upload-timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "original-format", processedImage.getMetadata().getOriginalFormat(),
                    "original-size", String.valueOf(processedImage.getMetadata().getOriginalSize()),
                    "processed-width", String.valueOf(processedImage.getMetadata().getWidth()),
                    "processed-height", String.valueOf(processedImage.getMetadata().getHeight())
            );

            return uploadImageToS3(objectKey, processedImage, metadata);

        } catch (Exception e) {
            log.error("Failed to upload brand logo for brand {}: {}", brandSlug, e.getMessage(), e);
            return ImageUploadResult.failure("Failed to upload brand logo: " + e.getMessage());
        }
    }

    @Override
    public ImageUploadResult uploadProductImage(ProcessedImageResult processedImage, String productId, boolean isThumbnail) {
        try {
            String objectKey = generateProductImageKey(productId, isThumbnail, processedImage.getFileExtension());

            Map<String, String> metadata = Map.of(
                    "entity-type", "product",
                    "entity-id", productId,
                    "image-type", isThumbnail ? "thumbnail" : "catalog",
                    "upload-timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "original-format", processedImage.getMetadata().getOriginalFormat(),
                    "original-size", String.valueOf(processedImage.getMetadata().getOriginalSize()),
                    "processed-width", String.valueOf(processedImage.getMetadata().getWidth()),
                    "processed-height", String.valueOf(processedImage.getMetadata().getHeight())
            );

            return uploadImageToS3(objectKey, processedImage, metadata);

        } catch (Exception e) {
            log.error("Failed to upload product image for product {}: {}", productId, e.getMessage(), e);
            return ImageUploadResult.failure("Failed to upload product image: " + e.getMessage());
        }
    }

    @Override
    public List<ImageUploadResult> uploadProductImages(List<ProcessedImageResult> processedImages, String productId) {
        List<ImageUploadResult> results = new ArrayList<>();

        for (int i = 0; i < processedImages.size(); i++) {
            ProcessedImageResult processedImage = processedImages.get(i);
            boolean isThumbnail = (i == 0);
            ImageUploadResult result = uploadProductImage(processedImage, productId, isThumbnail);
            results.add(result);
        }

        return results;
    }

    @Override
    public PresignedUrlResponse generateReadPresignedUrl(String objectKey, Integer expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String s3Url = String.format("s3://%s/%s", bucketName, objectKey);

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            String imageType = headResponse.metadata().getOrDefault("image-type", "unknown");

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedRequest.url().toString())
                    .objectKey(objectKey)
                    .s3Url(s3Url)
                    .documentType(imageType)
                    .expiresIn(presignedRequest.expiration().getEpochSecond())
                    .build();
        } catch (NoSuchKeyException e) {
            log.error("Image not found: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + objectKey);
        } catch (Exception e) {
            log.error("Failed to generate read URL for image: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate read URL: " + e.getMessage());
        }
    }

    @Override
    public PresignedUrlResponse generateDownloadPresignedUrl(String objectKey, Integer expirationMinutes) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            String filename = extractFilenameFromKey(objectKey);
            String imageType = headResponse.metadata().getOrDefault("image-type", "unknown");

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .responseContentDisposition("attachment; filename=\"" + filename + "\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String s3Url = String.format("s3://%s/%s", bucketName, objectKey);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedRequest.url().toString())
                    .objectKey(objectKey)
                    .s3Url(s3Url)
                    .documentType(imageType)
                    .expiresIn(presignedRequest.expiration().getEpochSecond())
                    .build();
        } catch (NoSuchKeyException e) {
            log.error("Image not found: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + objectKey);
        } catch (Exception e) {
            log.error("Failed to generate download URL for image: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate download URL: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted image: {}", objectKey);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete image: {}", objectKey, e);
            return false;
        }
    }

    @Override
    public List<String> deleteImages(List<String> objectKeys) {
        List<String> failedDeletes = new ArrayList<>();

        for (String objectKey : objectKeys) {
            if (!deleteImage(objectKey)) {
                failedDeletes.add(objectKey);
            }
        }

        return failedDeletes;
    }

    @Override
    public void moveFromTempToPermanent(String tempObjectKey) {

    }

    @Override
    public boolean imageExists(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if image exists: {}", objectKey, e);
            return false;
        }
    }

    private void configureBucketPolicies() {
        try {
            PutBucketVersioningRequest versioningRequest = PutBucketVersioningRequest.builder()
                    .bucket(bucketName)
                    .versioningConfiguration(VersioningConfiguration.builder()
                            .status(BucketVersioningStatus.ENABLED)
                            .build())
                    .build();

            s3Client.putBucketVersioning(versioningRequest);

            BucketLifecycleConfiguration lifecycleConfig = BucketLifecycleConfiguration.builder()
                    .rules(
                            LifecycleRule.builder()
                                    .id("DeleteIncompleteMultipartUploads")
                                    .filter(LifecycleRuleFilter.builder().prefix("").build())
                                    .abortIncompleteMultipartUpload(AbortIncompleteMultipartUpload.builder()
                                            .daysAfterInitiation(1)
                                            .build())
                                    .status(ExpirationStatus.ENABLED)
                                    .build(),
                            LifecycleRule.builder()
                                    .id("TransitionToIA")
                                    .filter(LifecycleRuleFilter.builder().prefix("").build())
                                    .transitions(Transition.builder()
                                            .days(30)
                                            .storageClass(TransitionStorageClass.STANDARD_IA)
                                            .build())
                                    .status(ExpirationStatus.ENABLED)
                                    .build()
                    )
                    .build();

            PutBucketLifecycleConfigurationRequest lifecycleRequest = PutBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucketName)
                    .lifecycleConfiguration(lifecycleConfig)
                    .build();

            s3Client.putBucketLifecycleConfiguration(lifecycleRequest);
            log.info("Bucket policies configured successfully for bucket {}", bucketName);

        } catch (Exception e) {
            log.warn("Failed to configure bucket policies: {}", e.getMessage());
        }
    }

    private void createFolderStructure() {
        List<String> folders = List.of(
                BRAND_FOLDER + "/",
                PRODUCT_FOLDER + "/",
                PRODUCT_FOLDER + "/" + THUMBNAIL_SUBFOLDER + "/",
                PRODUCT_FOLDER + "/" + CATALOG_SUBFOLDER + "/"
        );

        for (String folder : folders) {
            createFolder(folder);
        }
    }

    private void createFolder(String folderKey) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.empty());
            log.debug("Created folder: {}", folderKey);
        } catch (Exception e) {
            log.warn("Failed to create folder {}: {}", folderKey, e.getMessage());
        }
    }

    private String extractFilenameFromKey(String objectKey) {
        return objectKey.substring(objectKey.lastIndexOf('/') + 1);
    }

    private ImageUploadResult uploadImageToS3(String objectKey, ProcessedImageResult processedImage, Map<String, String> metadata) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(processedImage.getContentType())
                    .contentLength(processedImage.getFileSizeBytes())
                    .metadata(metadata)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();

            RequestBody requestBody = RequestBody.fromBytes(processedImage.getImageData());
            s3Client.putObject(putObjectRequest, requestBody);

            String s3Url = String.format("s3://%s/%s", bucketName, objectKey);
            String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);

            log.info("Successfully uploaded image to S3: {}", objectKey);

            return ImageUploadResult.success(
                    objectKey,
                    s3Url,
                    publicUrl,
                    processedImage.getFileSizeBytes(),
                    processedImage.getContentType()
            );

        } catch (Exception e) {
            log.error("Failed to upload image to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

    private String generateBrandLogoKey(String brandSlug, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s/%s/logo-%s-%s%s", BRAND_FOLDER, brandSlug, timestamp, uniqueId, fileExtension);
    }

    private String generateProductImageKey(String productId, boolean isThumbnail, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String subfolder = isThumbnail ? THUMBNAIL_SUBFOLDER : CATALOG_SUBFOLDER;
        return String.format("%s/%s/%s/%s-%s%s", PRODUCT_FOLDER, productId, subfolder, timestamp, uniqueId, fileExtension);
    }
}
