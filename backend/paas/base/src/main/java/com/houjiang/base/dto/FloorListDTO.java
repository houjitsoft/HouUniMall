package com.houjiang.base.dto;


import lombok.Data;

import java.util.List;

/**
 * 楼层
 * @author Amos
 */
@Data
public class FloorListDTO {

    /**
     * 楼层更新参数集合
     */
    private List<FloorDTO> floorUpdateList;

}
