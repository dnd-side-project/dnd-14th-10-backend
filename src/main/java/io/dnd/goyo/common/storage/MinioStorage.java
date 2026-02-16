package io.dnd.goyo.common.storage;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorage implements FileStorage {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.presigned-url-expiry-minutes}")
    private int expiryMinutes;

    @Value("${minio.public-url-base}")
    private String publicUrlBase;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("MinIO 버킷 생성 완료: {}", bucket);
                } catch (ErrorResponseException e) {
                    String code = e.errorResponse().code();
                    if ("BucketAlreadyOwnedByYou".equals(code) || "BucketAlreadyExists".equals(code)) {
                        log.info("MinIO 버킷 이미 존재함 (Race Condition 무시): {}", bucket);
                    } else {
                        throw e;
                    }
                }
            } else {
                log.info("MinIO 버킷 이미 존재함: {}", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO 초기화 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "MinIO 초기화에 실패했습니다.");
        }
    }

    @Override
    public String generatePublicUrl(String objectKey) {
        return publicUrlBase + "/" + objectKey;
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드 URL 생성에 실패했습니다.");
        }
    }
}
