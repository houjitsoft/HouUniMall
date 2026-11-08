package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class VoucherTemplateUpdateDTO extends UserTokenBeanDTO {

    /**
     * 代金券模版编号
     */
    private Integer voucherTId;
    /**
     * 代金券模版名称
     */
    private String voucherTTitle;
    /**
     * 代金券模版描述
     */
    private String voucherTDesc;
    /**
     * 店铺分类id
     */
    private Integer shopClassId;

    /**
     * 代金券模版有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTStartDate;

    /**
     * 代金券模版有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTEndDate;

    /**
     * 代金券模版面额
     */
    private BigDecimal voucherTPrice;
    /**
     * 代金券使用时的订单限额
     */
    private BigDecimal voucherTLimit;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     *店铺名
     */
    private String shopName;
    /**
     * 代金券模版的创建者id
     */
    private String voucherTCreatorId;
    /**
     * 代金券模版状态(1-有效,2-失效,3-审核中，4-审核失败)
     */
    private Integer voucherTState;
    /**
     * 模版可发放的代金券总数
     */
    private Integer voucherTTotal;
    /**
     * 模版已发放的代金券数量
     */
    private Integer voucherTGiveout;
    /**
     * 模版已经使用过的代金券
     */
    private Integer voucherTUsed;
    /**
     * 代金券领取方式，1-积分兑换(默认)，2-卡密兑换，3-免费领取
     */
    private Integer voucherTAccessMethod;

    /**
     * 模版的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTAddDate;
    /**
     * 模版的最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTUpdateDate;

    /**
     * 每人限领张数
     */
    private Integer voucherTEachlimit;

    /**
     * 代金券类型：1-单个店铺，2-多店铺参与'
     */
    private Integer voucherTType;

    /**
     * 商品id
     */
    private List<Integer> commonIdList;
    /**
     * 用户id
     */
    private List<VoucherTemplateDTO> userList;

    /**
     * 代金券类型：1 是店铺优惠券 2是商品优惠券 3 商品兑换券
     */
    private Integer isShopVoucher;

    /**
     * 自定义代金券模板图片
     */
    private String voucherTCustomimg;
    /**
     * 商品名称
     */
    private String commonName;

    public void setVoucherTPrice(BigDecimal voucherTPrice) {
        if (null == voucherTPrice) {
            voucherTPrice = new BigDecimal(0);
        }
        this.voucherTPrice = voucherTPrice;
    }

    public void setVoucherTLimit(BigDecimal voucherTLimit) {
        if (null == voucherTLimit) {
            voucherTLimit = new BigDecimal(0);
        }
        this.voucherTLimit = voucherTLimit;
    }
}
