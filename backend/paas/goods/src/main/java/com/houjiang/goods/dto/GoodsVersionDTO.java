package com.houjiang.goods.dto;

import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.querypage.PageHelper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsVersionDTO extends PageHelper {

    /**
     * 商品公共id
     */
    @NotNull(message = "商品公共id不能为空", groups = {Select.commonId.class,Select.versionDetails.class})
    private Integer commonId;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 操作人名称
     */
    private String operateUserName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 版本号
     */
    @NotEmpty(message = "版本号不能为空", groups = {Select.versionDetails.class})
    private String versionNo;

}
