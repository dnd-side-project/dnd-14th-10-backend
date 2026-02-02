package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceImageService {

    private final FileStorage fileStorage;

    private static final String PLACE_IMAGE_DIR = "place/";
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");

    public String createPresignedUrl(String originalFilename) {
        String extension = extractExtension(originalFilename);
        validateExtension(extension);

        String objectName = PLACE_IMAGE_DIR + UUID.randomUUID() + extension;

        return fileStorage.generatePresignedUrl(objectName);
    }

    private String extractExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return originalFilename.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    private void validateExtension(String extension) {
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 파일 형식입니다.");
        }
    }
}
