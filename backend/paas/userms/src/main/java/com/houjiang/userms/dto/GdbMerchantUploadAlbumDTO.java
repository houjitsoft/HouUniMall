package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName GdbMerchantUploadAlbumDTO
 * @Author Amos
 * @Date 2023/6/28 17:16
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantUploadAlbumDTO extends UserTokenBeanDTO {
    /**
     * 商品图片Id
     */
    private Integer albumId;
    /**
     * 商品图片地址
     */
    private String albumName;
    /**
     * 封面
     */
    private String albumCover;
    /**
     * 描述
     */
    private String albumDesc;
    /**
     * 内容数量
     */
    private Integer albumNum;
    /**
     * 默认相册，1是，0否
     */
    private Byte albumIsDefault;
    /**
     * 排序
     */
    private Short albumDisplayorder;
    /**
     * 附件册
     */
    private String albumType;
    /**
     * 所属用户id
     */
    private String userId;
}
