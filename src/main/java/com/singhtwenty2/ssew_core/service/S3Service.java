package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.DocumentDTO.DocumentDeleteRequest;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.DocumentDTO.DocumentDeleteResponse;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlRequest;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;

@Service
public interface S3Service {

    void initializeS3Bucket();

    PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request);

    DocumentDeleteResponse deleteDocuments(DocumentDeleteRequest request);

    PresignedUrlResponse generateReadPresignedUrl(String objectKey, Integer expirationMinutes);

    PresignedUrlResponse generateDownloadPresignedUrl(String objectKey, Integer expirationMinutes);
}
