package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName JoinVoucherActivityDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/12 16:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class JoinVoucherActivityDTO extends UserTokenBeanDTO {

    /**
     * 代金券模版编号集合
     */
    private List<Integer> voucherTIds;

    /**
     * 代金券模版编号
     */
    private Integer voucherTId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品id集合
     */
    private List<Integer> commonIdList;

    /**
     * 商品集合
     */
    private List<VoucherGoodsListDTO> goodsList;

    /**
     * 审核 1通过，0未通过，10审核中
     */
    private Integer activityVerify = 1;

    /**
     * 待审核数
     */
    private Integer waitVerifyGoodsNum;

    /**
     * 通过审核的商品数
     */
    private Integer passVerifyGoodsNum;

    /**
     * 拒绝审核审核商品数
     */
    private Integer refuseVerifyGoodsNum;

    /**
     * 提报商品总数
     */
    private Integer allGoodsNum;
}
