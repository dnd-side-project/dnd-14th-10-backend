package io.dnd.goyo.common.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import io.dnd.goyo.common.storage.FileStorage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @Captor
    private ArgumentCaptor<String> objectKeyCaptor;

    @Test
    void 단건_이미지_Presigned_URL_생성_성공() {
        // given
        String filename = "profile.jpg";
        String expectedUrl = "http://minio/bucket/user/uuid.jpg";
        given(fileStorage.generatePresignedUrl(anyString())).willReturn(expectedUrl);

        // when
        List<PresignedUrlItem> results = imageUploadService.createPresignedUrls(ImageType.USER, List.of(filename));

        // then
        assertThat(results).hasSize(1);
        PresignedUrlItem result = results.getFirst();
        assertThat(result.filename()).isEqualTo(filename);
        assertThat(result.url()).isEqualTo(expectedUrl);
        assertThat(result.objectKey()).startsWith("user/").endsWith(".jpg");

        verify(fileStorage).generatePresignedUrl(objectKeyCaptor.capture());
        assertThat(objectKeyCaptor.getValue()).isEqualTo(result.objectKey());
    }

    @Test
    void 다건_이미지_Presigned_URL_생성_성공() {
        // given
        List<String> filenames = List.of("place1.jpg", "place2.png");
        String expectedUrl = "http://minio/bucket/place/uuid.jpg";
        given(fileStorage.generatePresignedUrl(anyString())).willReturn(expectedUrl);

        // when
        List<PresignedUrlItem> results = imageUploadService.createPresignedUrls(ImageType.PLACE, filenames);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).filename()).isEqualTo("place1.jpg");
        assertThat(results.get(1).filename()).isEqualTo("place2.png");

        verify(fileStorage, times(2)).generatePresignedUrl(objectKeyCaptor.capture());
        List<String> capturedKeys = objectKeyCaptor.getAllValues();
        assertThat(capturedKeys.get(0)).startsWith("place/").endsWith(".jpg");
        assertThat(capturedKeys.get(1)).startsWith("place/").endsWith(".png");
    }

    @Test
    void 지원하지_않는_확장자는_예외_발생() {
        // given
        List<String> filenames = List.of("funny.gif");

        // when & then
        assertThatThrownBy(() -> imageUploadService.createPresignedUrls(ImageType.PLACE, filenames))
                .isInstanceOf(BusinessException.class)
                .hasMessage("지원하지 않는 파일 형식입니다.");
    }

    @Test
    void 확장자가_없는_파일은_예외_발생() {
        // given
        List<String> filenames = List.of("readme");

        // when & then
        assertThatThrownBy(() -> imageUploadService.createPresignedUrls(ImageType.PLACE, filenames))
                .isInstanceOf(BusinessException.class)
                .hasMessage("지원하지 않는 파일 형식입니다.");
    }
}
