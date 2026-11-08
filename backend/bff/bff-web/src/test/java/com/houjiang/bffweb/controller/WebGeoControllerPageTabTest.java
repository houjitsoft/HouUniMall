package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerPageTabTest {

    @Test
    void forwardsProjectContextQueryParametersToGeoService() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        controller.pageTab("topicAuthority", 3L);

        verify(feign).pageTab("topicAuthority", 3L, 3L);
    }

    @Test
    void forwardsProjectContextWhenRefreshingPageTab() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("originalParagraph", "source");

        controller.pageTabRefresh("featuredSnippet", 3L, body);

        verify(feign).pageTabRefresh("featuredSnippet", 3L, 3L, body);
    }
}
