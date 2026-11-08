package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
/**
 * @ClassName
 * @Description  平台收款银行卡表
 * @Author Amos
 * @Date 2023-07-03 09:56:25
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_platform_brank")
@Data
public class BasePlatformBrankEntity  implements Serializable {

    private Integer id;
    /**
     * 银行卡卡号
     */
    private String bankCardId;
    /**
     * 开户人姓名
     */
    private String bankCardName;
    /**
     * 开户银行名称
     */
    private String bankName;
    /**
     * 备注
     */
    private String brankNote;
    /**
     * 修改时间
     */
    private String brankUpdateTime;
    /**
     * 银联号
     */
    private String brankUnionpayGood;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
