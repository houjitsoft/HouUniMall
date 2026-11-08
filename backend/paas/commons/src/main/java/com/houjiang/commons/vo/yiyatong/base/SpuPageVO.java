package com.houjiang.commons.vo.yiyatong.base;

import com.houjiang.commons.vo.yiyatong.PageBaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName SpuPageVO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 16:17
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SpuPageVO extends PageBaseVO {

    /**
     * SPU集合
     */
    private List<String> spuIdList;
}
