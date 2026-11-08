package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CoordinateVO {

    /**
     * 城市经纬度
     */
    private String LatAndLng;

    /**
     * 城市
     */
    private String Location;
}
