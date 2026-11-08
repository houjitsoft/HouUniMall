package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName BrandVO
 * @Description
 * @Author Amos
 * @Date 2024/11/7 15:34
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BrandVO extends BrandPageVO{

    private List<BrandInfoVO> records;

    private Boolean optimizeCountSql;

    private List orders;

}
