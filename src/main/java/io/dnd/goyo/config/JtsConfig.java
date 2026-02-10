package io.dnd.goyo.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JtsConfig {

    // GPS 표준 좌표계(WGS84)의 SRID
    private static final int SRID_WGS84 = 4326;

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory(new PrecisionModel(), SRID_WGS84);
    }
}
