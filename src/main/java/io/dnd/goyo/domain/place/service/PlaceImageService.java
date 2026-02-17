package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.dto.request.PlaceImageRequest;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceImageService {

    private final FileStorage fileStorage;

    public void replaceImages(Place place, Long placeId, List<PlaceImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        Set<String> oldKeys = place.getImages().stream()
                .map(PlaceImage::getImageKey)
                .collect(Collectors.toSet());

        Set<String> newKeys = images.stream()
                .map(PlaceImageRequest::imageKey)
                .collect(Collectors.toSet());

        List<String> orphanedKeys = oldKeys.stream()
                .filter(key -> !newKeys.contains(key))
                .toList();

        List<PlaceImage> newImages = images.stream()
                .filter(Objects::nonNull)
                .map(img -> PlaceImage.of(img.imageKey(), Boolean.TRUE.equals(img.isRepresentative()), img.sequence()))
                .toList();

        place.replaceImages(newImages);
        scheduleOrphanedImagesDeletion(orphanedKeys, placeId);
    }

    private void scheduleOrphanedImagesDeletion(List<String> orphanedKeys, Long placeId) {
        if (orphanedKeys.isEmpty()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    fileStorage.deleteObjects(orphanedKeys);
                } catch (Exception e) {
                    log.warn("장소 이미지 MinIO 삭제 실패 (placeId: {}): {}", placeId, e.getMessage());
                }
            }
        });
    }
}
