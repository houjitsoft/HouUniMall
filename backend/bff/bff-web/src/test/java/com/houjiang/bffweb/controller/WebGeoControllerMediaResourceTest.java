package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerMediaResourceTest {

    @Test
    void forwardsMediaFavoriteStateToGeoService() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("favorite", true, "projectId", 3L);

        controller.mediaResourceFavorite(15L, body);

        verify(feign).mediaResourceFavorite(15L, body);
    }
}
