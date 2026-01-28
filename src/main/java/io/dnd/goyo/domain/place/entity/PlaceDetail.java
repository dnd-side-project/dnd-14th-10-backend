package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceDetail {

    @Id
    private Long placeDetailId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false)
    private Double rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutletScore outletScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrowdStatus crowdStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceSize spaceSize;

    @Column(nullable = false)
    private Integer wishCount;
}
