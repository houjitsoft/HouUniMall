package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.util.List;

/**
 * 商品状态
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlatFormGoodsStateDTO extends UserTokenBeanDTO {

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品是否被锁定，默认为1,不锁定；2锁定
     */
    private Integer commonIsLocked;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品是否屏蔽：1-否；2-是
     */
    private Integer commonIsScreen;

    /**
     * 商品id集合
     */
    private List<Integer> commonIdList;

    /**
     * 商品id和类型集合
     */
    private List<CommonIdandTypesDTO>   commonList;

    /**
     * 商品id集合
     */
    private List<Integer> supplierCommonIdList;

    /**
     * 违规原因
     */
    private String commonStateRemark;

    /**
     * 审核失败原因
     */
    private String commonVerifyRemark;

    /**
     * 操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    private String orderOperatorTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonVerifyTime;


    private Integer commonId;


}
