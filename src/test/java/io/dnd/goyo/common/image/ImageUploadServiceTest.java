package io.dnd.goyo.common.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @Test
    void 정상적인_이미지_파일명으로_Presigned_URL을_생성() {
        // given
        String filename = "dnd-14-place.jpg";
        String expectedUrl = "http://minio/bucket/place/uuid.jpg";
        given(fileStorage.generatePresignedUrl(anyString())).willReturn(expectedUrl);

        // when
        String result = imageUploadService.createPresignedUrl(ImageType.PLACE, filename);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void 지원하지_않는_확장자는_예외_발생() {
        // given
        String filename = "funny.gif";

        // when & then
        assertThatThrownBy(() -> imageUploadService.createPresignedUrl(ImageType.PLACE, filename))
                .isInstanceOf(BusinessException.class)
                .hasMessage("지원하지 않는 파일 형식입니다.");
    }

    @Test
    void 확장자가_없는_파일은_예외_발생() {
        // given
        String filename = "readme";

        // when & then
        assertThatThrownBy(() -> imageUploadService.createPresignedUrl(ImageType.PLACE, filename))
                .isInstanceOf(BusinessException.class)
                .hasMessage("지원하지 않는 파일 형식입니다.");
    }
}
