package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.GdbMerchantUploadAlbumDTO;
import com.houjiang.userms.dto.ManageImageGroupDeleteDTO;
import com.houjiang.userms.entity.UserUploadAlbumEntity;

/**
 * @ClassName UserUploadAlbumService
 * @Description 用户相册
 * @Author Amos
 * @Date 2023/6/28 16:47
 * @Version 1.0
 * @author Amos
 */
public interface UserUploadAlbumService extends IService<UserUploadAlbumEntity> {

    /**
     * 查询图片分组
     *
     * @param param
     * @return
     */
    ResponseResult queryImageGroup(UserTokenBeanDTO param);

    /**
     * 创建分组
     *
     * @param param
     * @return
     */
    ResponseResult createGroup(GdbMerchantUploadAlbumDTO param);

    /**
     * 删除
     * @param param
     * @return
     */
    ResponseResult deleteImageGroup(ManageImageGroupDeleteDTO param);
}
