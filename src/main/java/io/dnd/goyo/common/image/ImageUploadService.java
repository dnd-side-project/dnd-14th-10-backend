package io.dnd.goyo.common.image;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import io.dnd.goyo.common.storage.FileStorage;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final FileStorage fileStorage;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    public List<PresignedUrlItem> createPresignedUrls(ImageType imageType, List<String> filenames) {
        return filenames.stream()
                .map(filename -> createPresignedUrlItem(imageType, filename))
                .collect(Collectors.toList());
    }

    private PresignedUrlItem createPresignedUrlItem(ImageType imageType, String originalFilename) {
        String extension = extractExtension(originalFilename);
        validateExtension(extension);
        
        String objectKey = imageType.getPath() + "/" + UUID.randomUUID() + extension;
        String url = fileStorage.generatePresignedUrl(objectKey);

        return PresignedUrlItem.of(originalFilename, url, objectKey);
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
