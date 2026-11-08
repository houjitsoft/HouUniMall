package com.houjiang.commons.vo.yiyatong;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName SpuPageVO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 15:09
 * @Version 1.0
 * @author Amos
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class SpuPageBaseVO extends PageBaseVO {

    /**
     * SPU集合
     */
    private List<SpuPageBaseVO> spuIdList;
}
