package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.ModularizationFlieDTO;
import com.houjiang.base.entity.BaseModularizationFlieEntity;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName BaseModularizationFlieService
 * @Description  组件化  图片 视频空间
 * @Author Amos
 * @Date 2023-07-04 13:17:22
 * @Version 1.0
 * @author Amos
 */
public interface BaseModularizationFlieService extends IService<BaseModularizationFlieEntity> {

    /**
     * 根据类型，和名称模糊查询 图片库
     * @param param
     */
    ResponseResult queryYfModularizationFlie(ModularizationFlieDTO param);

    /**
     * 视频url 视频封面 写入数据库
     *
     * @param ossUploadUpdate
     */
    ResponseResult insertVideoUrl(OssUploadUpdateParamDTO ossUploadUpdate);

    /**
     * 上传图片
     *
     * @param modularizationFlieDTO
     */
    ResponseResult uploadImage(ModularizationFlieDTO modularizationFlieDTO);

    /**
     * 删除
     * @param param
     * @return
     */
    ResponseResult deleteYfModularizationFlie(Map<String,Object> param);

    /**
     * 店铺装修查询图片
     * @param param
     * @return
     */
    ResponseResult queryYfModularizationFlieShop(ModularizationFlieDTO param);

    /**
     * 商家上传图片
     *
     * @param param
     * @return
     */
    ResponseResult uploadShopImg(ModularizationFlieDTO param);

    /**
     * 商家上传视频
     * @param param
     * @return
     */
    ResponseResult uploadShopVideo(OssUploadUpdateParamDTO ossUploadUpdate);

    /**
     * 登记已上传文件到素材库
     */
    ResponseResult registerStorageFile(Map<String, Object> param);

}
