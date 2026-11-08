package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ManageTransportAreaVO
 * @Author Amos
 * @Date 2023/6/27 14:42
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageTransportAreaVO {
    /**
     * id
     */
    private Integer id;
    /**
     * 名
     */
    private String name;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 区域id
     */
    private String areaIds;
}
