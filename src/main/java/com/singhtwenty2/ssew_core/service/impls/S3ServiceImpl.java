package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.DocumentDTO.*;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlRequest;
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

    @Value("${aws.s3.temp-folder}")
    private String tempFolder;

    @Value("${aws.s3.permanent-folder}")
    private String permanentFolder;

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
                        .build();
                s3Client.createBucket(createBucketRequest);
                log.info("Bucket {} created successfully", bucketName);
            }

            BucketLifecycleConfiguration lifecycleConfig = BucketLifecycleConfiguration.builder()
                    .rules(
                            LifecycleRule.builder()
                                    .id("ExpireTempFiles")
                                    .filter(LifecycleRuleFilter.builder()
                                            .prefix(tempFolder + "/")
                                            .build())
                                    .expiration(LifecycleExpiration.builder()
                                            .days(7)
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
            log.info("Lifecycle configuration applied to bucket {}", bucketName);

            createFolder(tempFolder + "/brand-docs/");
            createFolder(tempFolder + "/product-docs/");
            createFolder(permanentFolder + "/brand-docs/");
            createFolder(permanentFolder + "/product-docs/");
        } catch (Exception e) {
            log.error("Failed to initialize S3 bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize S3 bucket", e);
        }
    }

    @Override
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        String fileExtension = getFileExtension(request.getFile_name());
        String uniqueFileName = generateUniqueFileName(fileExtension);

        String objectKey = String.format("%s/%s-docs/%s",
                tempFolder,
                request.getEntity_type().toLowerCase(),
                uniqueFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getContent_type())
                .metadata(Map.of(
                        "document-type", request.getDocument_type(),
                        "entity-type", request.getEntity_type()))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String s3Url = String.format("s3://%s/%s", bucketName, objectKey);
        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                objectKey,
                s3Url,
                request.getDocument_type(),
                presignedRequest.expiration().getEpochSecond()
        );
    }

    @Override
    public DocumentDeleteResponse deleteDocuments(DocumentDeleteRequest request) {
        List<DocumentStatusInfo> deletedDocuments = new ArrayList<>();
        for (String objectKey : request.getObject_keys()) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                s3Client.deleteObject(deleteRequest);

                deletedDocuments.add(new DocumentStatusInfo(
                        objectKey,
                        null,
                        null,
                        "SUCCESS",
                        "Document deleted successfully"
                ));

            } catch (Exception e) {
                log.error("Failed to delete document: {}", e.getMessage(), e);
                deletedDocuments.add(new DocumentStatusInfo(
                        objectKey,
                        null,
                        null,
                        "FAILURE",
                        "Failed to delete document: " + e.getMessage()
                ));
            }
        }

        return new DocumentDeleteResponse(
                request.getEntity_type(),
                request.getEntity_id(),
                deletedDocuments
        );
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
            String documentType = headResponse.metadata().getOrDefault("document-type", "unknown");
            return new PresignedUrlResponse(
                    presignedRequest.url().toString(),
                    objectKey,
                    s3Url,
                    documentType,
                    presignedRequest.expiration().getEpochSecond()
            );
        } catch (NoSuchKeyException e) {
            log.error("Object not found: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Object not found: " + objectKey
            );
        } catch (Exception e) {
            log.error("Failed to generate read URL for object: {}", objectKey, e);
            throw new ServiceException("Failed to generate read URL: " + e.getMessage());
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
            String filename = headResponse.metadata().getOrDefault("original-filename",
                    objectKey.substring(objectKey.lastIndexOf('/') + 1));
            String documentType = headResponse.metadata().getOrDefault("document-type", "unknown");

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
            return new PresignedUrlResponse(
                    presignedRequest.url().toString(),
                    objectKey,
                    s3Url,
                    documentType,
                    presignedRequest.expiration().getEpochSecond()
            );
        } catch (NoSuchKeyException e) {
            log.error("Object not found: {}", objectKey, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with key: " + objectKey);
        } catch (Exception e) {
            log.error("Failed to generate download URL for object: {}", objectKey, e);
            throw new ServiceException("Failed to generate download URL: " + e.getMessage());
        }
    }

    private void createFolder(String folderKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderKey)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.empty());
        log.info("Created folder: {}", folderKey);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex);
    }

    private String generateUniqueFileName(String fileExtension) {
        return UUID.randomUUID().toString() + fileExtension;
    }
}
