package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.ManageBatchDeleteImageDTO;
import com.houjiang.userms.dto.ManageImageNameUpdateParamDTO;
import com.houjiang.userms.dto.ManageImageSpaceQueryDTO;
import com.houjiang.userms.dto.ManageImageUploadParamDTO;
import com.houjiang.userms.entity.UserUploadBaseEntity;

import java.util.List;

/**
 * @ClassName UserUploadBaseService
 * @Description 用户附件表-图片、视频
 * @Author Amos
 * @Date 2023/6/29 9:13
 * @Version 1.0
 * @author Amos
 */
public interface UserUploadBaseService extends IService<UserUploadBaseEntity> {

    /**
     * 图片空间查询
     *
     * @param param
     * @return
     */
    ResponseResult queryImageSpace(ManageImageSpaceQueryDTO param) throws IllegalAccessException;

    /**
     * 删除图片
     * @param param
     * @return
     */
    ResponseResult deleteImage(ManageBatchDeleteImageDTO param);

    /**
     * 更改图片名称
     * @param param
     * @return
     */
    ResponseResult updateImageName(ManageImageNameUpdateParamDTO param);

    /**
     * 上传图片
     * @param param
     * @return
     */
    ResponseResult uploadImage(ManageImageUploadParamDTO param);


    /**
     * 查询店铺图片空间数量
     * @param shopId
     * @return
     */
    Integer selectShopImageSpaceCount(Integer shopId,String type);

    /**
     * 插入视频
     * @param param
     */
    void insertVideo(ManageShopVideoDTO param);

    /**
     * 更新视频
     * @param param
     */
    void updateVideo(ManageShopVideoDTO param);

    /**
     * 查询店铺空间图片
     */
    List<ManageShopVideoDTO> selectImageSpaceList(ManageShopVideoDTO param);
}
