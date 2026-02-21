package io.dnd.goyo.domain.place.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.dto.PlaceWithDistance;
import io.dnd.goyo.domain.place.dto.request.NearbyFilterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import io.dnd.goyo.domain.place.entity.QPlace;
import io.dnd.goyo.domain.place.entity.QPlaceDetail;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import io.dnd.goyo.domain.place.enums.RandomThemeType;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    private static final Set<String> ALLOWED_SCORE_COLUMNS = Arrays.stream(RandomThemeType.values())
            .map(RandomThemeType::getScoreColumn)
            .collect(Collectors.toUnmodifiableSet());

    private static final double MOOD_NOISY_MAX = 37.5;
    private static final double MOOD_CHATTING_MAX = 62.5;
    private static final double MOOD_CALM_MAX = 87.5;

    private static final double SPACE_SMALL_MAX = 33.33;
    private static final double SPACE_MEDIUM_MAX = 66.67;

    private static final long REGION_CODE_MULTIPLIER = 100000L;
    private static final long REGION_CODE_RANGE = 99999L;

    @Override
    public List<PlaceWithDistance> findByFilterWithDistance(PlaceFilterRequest request, double longitude, double latitude, int size) {
        QPlace place = QPlace.place;
        QPlaceDetail placeDetail = QPlaceDetail.placeDetail;

        NumberExpression<Double> moodAvg = placeDetail.totalQuietScore.doubleValue().divide(placeDetail.reviewCount.add(1));
        NumberExpression<Double> spaceSizeAvg = placeDetail.totalSpaceSizeScore.doubleValue().divide(placeDetail.reviewCount.add(1));

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(place.status.eq(PlaceStatus.ACTIVE));

        if (request.category() != null) {
            builder.and(place.category.eq(request.category()));
        }

        if (request.spaceSize() != null) {
            builder.and(buildSpaceSizeCondition(spaceSizeAvg, request.spaceSize()));
        }

        if (request.moods() != null && !request.moods().isEmpty()) {
            builder.and(buildMoodsCondition(moodAvg, request.moods()));
        }

        if (request.regionCodes() != null && !request.regionCodes().isEmpty()) {
            builder.and(buildRegionCodesCondition(place, request.regionCodes()));
        }

        List<Long> filteredIds = queryFactory
                .select(place.id)
                .from(place)
                .join(place.placeDetail, placeDetail)
                .where(builder)
                .fetch();

        if (filteredIds.isEmpty()) {
            return List.of();
        }

        return findPlacesWithDistanceAndCursor(filteredIds, longitude, latitude, request.lastDistance(), size);
    }

    @Override
    public List<PlaceWithDistance> findNearbyWithFilters(NearbyFilterRequest request, double longitude, double latitude, int size) {
        List<Long> nearbyPlaceIds = findNearbyPlaceIds(longitude, latitude, request.resolvedRadius());

        if (nearbyPlaceIds.isEmpty()) {
            return List.of();
        }

        QPlace place = QPlace.place;
        QPlaceDetail placeDetail = QPlaceDetail.placeDetail;

        NumberExpression<Double> moodAvg = placeDetail.totalQuietScore.doubleValue().divide(placeDetail.reviewCount.add(1));
        NumberExpression<Double> spaceSizeAvg = placeDetail.totalSpaceSizeScore.doubleValue().divide(placeDetail.reviewCount.add(1));

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(place.id.in(nearbyPlaceIds));
        builder.and(place.status.eq(PlaceStatus.ACTIVE));

        if (request.category() != null) {
            builder.and(place.category.eq(request.category()));
        }

        if (request.spaceSize() != null) {
            builder.and(buildSpaceSizeCondition(spaceSizeAvg, request.spaceSize()));
        }

        if (request.moods() != null && !request.moods().isEmpty()) {
            builder.and(buildMoodsCondition(moodAvg, request.moods()));
        }

        List<Long> filteredIds = queryFactory
                .select(place.id)
                .from(place)
                .join(place.placeDetail, placeDetail)
                .where(builder)
                .fetch();

        if (filteredIds.isEmpty()) {
            return List.of();
        }

        return findPlacesWithDistanceAndCursor(filteredIds, longitude, latitude, request.lastDistance(), size);
    }

    private List<Long> findNearbyPlaceIds(double longitude, double latitude, double radiusMeters) {
        String sql = """
                SELECT p.id FROM places p
                WHERE p.status = 'ACTIVE'
                AND ST_DWithin(
                    CAST(p.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                    :radiusMeters
                )
                LIMIT 1000
                """;

        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("radiusMeters", radiusMeters)
                .getResultList();

        return result.stream()
                .map(id -> ((Number) id).longValue())
                .toList();
    }

    private List<PlaceWithDistance> findPlacesWithDistanceAndCursor(
            List<Long> placeIds,
            double longitude,
            double latitude,
            Double lastDistance,
            int size
    ) {
        String sql = buildDistanceQuerySql(lastDistance);

        var query = entityManager.createNativeQuery(sql)
                .setParameter("placeIds", placeIds.toArray(new Long[0]))
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("limit", size + 1);

        if (lastDistance != null) {
            query.setParameter("lastDistance", lastDistance);
        }

        List<?> result = query.getResultList();

        return result.stream()
                .map(row -> {
                    Object[] columns = (Object[]) row;
                    Long placeId = ((Number) columns[0]).longValue();
                    Double distance = ((Number) columns[1]).doubleValue();
                    return new PlaceWithDistance(placeId, distance);
                })
                .toList();
    }

    @NotNull
    private static String buildDistanceQuerySql(Double lastDistance) {
        String distanceCondition = lastDistance != null
                ? "AND ST_Distance(CAST(p.location AS geography), CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)) > :lastDistance"
                : "";

        return String.format("""
                SELECT p.id,
                       ST_Distance(
                           CAST(p.location AS geography),
                           CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                       ) as distance
                FROM places p
                WHERE p.id = ANY(:placeIds)
                %s
                ORDER BY distance ASC
                LIMIT :limit
                """, distanceCondition);
    }

    private BooleanExpression buildSpaceSizeCondition(NumberExpression<Double> spaceSizeAvg, SpaceSize spaceSize) {
        return switch (spaceSize) {
            case SMALL  -> spaceSizeAvg.lt(SPACE_SMALL_MAX);
            case MEDIUM -> spaceSizeAvg.goe(SPACE_SMALL_MAX).and(spaceSizeAvg.lt(SPACE_MEDIUM_MAX));
            case LARGE  -> spaceSizeAvg.goe(SPACE_MEDIUM_MAX);
        };
    }

    private Predicate buildMoodsCondition(NumberExpression<Double> moodAvg, List<Mood> moods) {
        BooleanBuilder moodBuilder = new BooleanBuilder();
        for (Mood mood : moods) {
            moodBuilder.or(buildMoodCondition(moodAvg, mood));
        }
        return moodBuilder;
    }

    private BooleanExpression buildMoodCondition(NumberExpression<Double> moodAvg, Mood mood) {
        return switch (mood) {
            case NOISY    -> moodAvg.lt(MOOD_NOISY_MAX);
            case CHATTING -> moodAvg.goe(MOOD_NOISY_MAX).and(moodAvg.lt(MOOD_CHATTING_MAX));
            case CALM     -> moodAvg.goe(MOOD_CHATTING_MAX).and(moodAvg.lt(MOOD_CALM_MAX));
            case SILENT   -> moodAvg.goe(MOOD_CALM_MAX);
        };
    }

    private Predicate buildRegionCodesCondition(QPlace place, List<Long> regionCodes) {
        BooleanBuilder regionBuilder = new BooleanBuilder();
        for (Long code : regionCodes) {
            long rangeStart = code * REGION_CODE_MULTIPLIER;
            long rangeEnd = rangeStart + REGION_CODE_RANGE;
            regionBuilder.or(place.regionCode.value.between(rangeStart, rangeEnd));
        }
        return regionBuilder;
    }

    @Override
    public List<Long> findByThemeScore(
            double longitude,
            double latitude,
            double radiusMeters,
            String category,
            String scoreColumn,
            double minScore,
            double maxScore,
            int limit
    ) {
        if (scoreColumn == null || !ALLOWED_SCORE_COLUMNS.contains(scoreColumn)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String sql = String.format("""
                SELECT p.id FROM places p
                JOIN place_details pd ON pd.place_id = p.id
                WHERE ST_DWithin(
                    CAST(p.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                    :radiusMeters
                )
                AND p.category = :category
                AND p.status = 'ACTIVE'
                AND (pd.%s::float / (pd.review_count + 1)) >= :minScore
                AND (pd.%s::float / (pd.review_count + 1)) <= :maxScore
                ORDER BY (pd.wish_count + pd.review_count) DESC,
                    ST_Distance(
                        CAST(p.location AS geography),
                        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                    ) ASC
                LIMIT :limit
                """, scoreColumn, scoreColumn);

        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("radiusMeters", radiusMeters)
                .setParameter("category", category)
                .setParameter("minScore", minScore)
                .setParameter("maxScore", maxScore)
                .setParameter("limit", limit)
                .getResultList();

        return result.stream()
                .map(id -> ((Number) id).longValue())
                .toList();
    }

}
