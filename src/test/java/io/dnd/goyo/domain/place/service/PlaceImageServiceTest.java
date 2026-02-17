package io.dnd.goyo.domain.place.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.dto.request.PlaceImageRequest;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class PlaceImageServiceTest {

    @InjectMocks
    private PlaceImageService placeImageService;

    @Mock
    private FileStorage fileStorage;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Nested
    @DisplayName("이미지 교체 시")
    class ReplaceImages {

        private final Long placeId = 1L;
        private Place place;

        @BeforeEach
        void setUp() {
            place = mock(Place.class);
        }

        @Test
        void 이미지_목록이_null이면_교체하지_않는다() {
            // when
            placeImageService.replaceImages(place, placeId, null);

            // then
            verify(place, never()).replaceImages(any());
        }

        @Test
        void 이미지_리스트가_비어있으면_교체하지_않는다() {
            // when
            placeImageService.replaceImages(place, placeId, List.of());

            // then
            verify(place, never()).replaceImages(any());
        }

        @Test
        void 삭제할_이미지가_없으면_저장소_삭제를_호출하지_않는다() {
            // given
            PlaceImage existingImage = mock(PlaceImage.class);
            given(existingImage.getImageKey()).willReturn("image1.jpg");
            given(place.getImages()).willReturn(List.of(existingImage));

            List<PlaceImageRequest> newImages = List.of(new PlaceImageRequest("image1.jpg", 0, true));

            // when
            placeImageService.replaceImages(place, placeId, newImages);
            triggerAfterCommit();

            // then
            verify(place).replaceImages(anyList());
            verify(fileStorage, never()).deleteObjects(any());
        }

        @Test
        void 삭제할_이미지가_있으면_커밋_후_저장소에서_삭제한다() {
            // given
            PlaceImage oldImage = mock(PlaceImage.class);
            given(oldImage.getImageKey()).willReturn("old-image.jpg");
            given(place.getImages()).willReturn(List.of(oldImage));

            List<PlaceImageRequest> newImages = List.of(new PlaceImageRequest("new-image.jpg", 0, true));

            // when
            placeImageService.replaceImages(place, placeId, newImages);
            triggerAfterCommit();

            // then
            verify(place).replaceImages(anyList());
            verify(fileStorage).deleteObjects(List.of("old-image.jpg"));
        }

        private void triggerAfterCommit() {
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
        }
    }
}
