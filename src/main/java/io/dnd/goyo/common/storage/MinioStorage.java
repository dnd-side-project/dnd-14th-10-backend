package io.dnd.goyo.common.storage;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorage implements FileStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.bucket()).build());
            if (!found) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.bucket()).build());
                    log.info("MinIO 버킷 생성 완료: {}", minioProperties.bucket());
                } catch (ErrorResponseException e) {
                    String code = e.errorResponse().code();
                    if ("BucketAlreadyOwnedByYou".equals(code) || "BucketAlreadyExists".equals(code)) {
                        log.info("MinIO 버킷 이미 존재함 (Race Condition 무시): {}", minioProperties.bucket());
                    } else {
                        throw e;
                    }
                }
            } else {
                log.info("MinIO 버킷 이미 존재함: {}", minioProperties.bucket());
            }
        } catch (Exception e) {
            log.error("MinIO 초기화 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "MinIO 초기화에 실패했습니다.");
        }
    }

    @Override
    public String generatePublicUrl(String objectKey) {
        return minioProperties.publicUrlBase() + "/" + objectKey;
    }

    @Override
    public void deleteObjects(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        List<DeleteObject> deleteObjects = objectKeys.stream()
                .map(DeleteObject::new)
                .toList();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(minioProperties.bucket())
                        .objects(deleteObjects)
                        .build()
        );

        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                log.warn("MinIO 객체 삭제 실패 - key: {}, message: {}", error.objectName(), error.message());
            } catch (Exception e) {
                log.warn("MinIO 객체 삭제 결과 확인 중 오류: {}", e.getMessage());
            }
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        try {
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .expiry(minioProperties.presignedUrlExpiryMinutes(), TimeUnit.MINUTES)
                            .build()
            );
            return presignedUrl.replace(minioProperties.endpoint(), minioProperties.externalEndpoint());
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드 URL 생성에 실패했습니다.");
        }
    }
}
