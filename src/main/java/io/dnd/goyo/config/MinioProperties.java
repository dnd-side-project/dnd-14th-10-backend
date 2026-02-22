package io.dnd.goyo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String externalEndpoint,
        String accessKey,
        String secretKey,
        String bucket,
        int presignedUrlExpiryMinutes,
        String publicUrlBase
) {
}
