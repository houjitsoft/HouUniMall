package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 分账协议信息DTO
 * @author Amos
 */
@Data
public class DivisionMemInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分账方商户会员编号(必填)
     */
    private String divisionBizUserId;

    /**
     * 分账方式(必填)
     * 1：分账比例
     */
    private String divisionType;

    /**
     * 最高分账比例(必填)
     * 最多4位小数，如最高分账比例为5.55%，填写0.0555
     */
    private String divisionRate;

    /**
     * 分账业务佐证材料fileId(必填)
     * 需先使用文件上传接口获取
     */
    private String divisionProcotolFileId;
}
