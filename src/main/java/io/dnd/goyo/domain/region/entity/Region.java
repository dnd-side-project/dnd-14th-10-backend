package io.dnd.goyo.domain.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @Column(name = "region_code")
    private Integer id;

    @Column(nullable = false)
    private String stateName;

    @Column(nullable = false)
    private String districtName;

    @Column(nullable = false)
    private String fullName;
}
