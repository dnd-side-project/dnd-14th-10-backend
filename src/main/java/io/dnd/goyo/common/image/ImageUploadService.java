package io.dnd.goyo.common.image;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final FileStorage fileStorage;
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");

    public String createPresignedUrl(ImageType imageType, String originalFilename) {
        String extension = extractExtension(originalFilename);
        validateExtension(extension);
        String objectName = imageType.getPath() + "/" + UUID.randomUUID() + extension;
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
