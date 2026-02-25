package io.dnd.goyo.domain.placetag.repository;

import io.dnd.goyo.domain.placetag.dto.PlaceTagMappingDto;
import io.dnd.goyo.domain.placetag.entity.PlaceTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {

    @Query(value = """
            SELECT pt.place_id FROM place_tags pt
            JOIN places p ON p.id = pt.place_id
            WHERE pt.tag_id IN (:tagIds)
              AND p.region_code BETWEEN :regionCode * 100000 AND (:regionCode * 100000) + 99999
              AND p.category = :category
              AND p.status = 'ACTIVE'
              AND p.id NOT IN (:excludeIds)
            GROUP BY pt.place_id
            ORDER BY COUNT(pt.tag_id) DESC
            LIMIT :candidateLimit
            """, nativeQuery = true)
    List<Long> findCandidatePlaceIds(
            @Param("tagIds") List<Long> tagIds,
            @Param("regionCode") long regionCode,
            @Param("category") String category,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("candidateLimit") int candidateLimit
    );

    @Query("SELECT new io.dnd.goyo.domain.placetag.dto.PlaceTagMappingDto(pt.place.id, pt.tag.id) " +
            "FROM PlaceTag pt WHERE pt.place.id IN :placeIds")
    List<PlaceTagMappingDto> findPlaceTagMappings(@Param("placeIds") List<Long> placeIds);

    @Query("SELECT pt.tag.id FROM PlaceTag pt WHERE pt.place.id = :placeId")
    List<Long> findTagIdsByPlaceId(@Param("placeId") Long placeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlaceTag pt WHERE pt.place.id = :placeId")
    void deleteAllByPlaceId(@Param("placeId") Long placeId);
}
