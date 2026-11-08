package com.houjiang.base.controller;


import com.houjiang.base.dto.ModularizationFlieDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseModularizationFlieController
 * @Description  组件化  图片 视频空间
 * @Author Amos
 * @Date 2023-07-04 13:17:22
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseModularizationFlie")
public class BaseModularizationFlieController extends BaseController{

    /**
     * 根据类型，和名称模糊查询 图片库
     * @param param
     * @return
     */
    @PostMapping("/queryFileList")
    public ResponseResult queryYfModularizationFlie(@RequestBody ModularizationFlieDTO param){
        return baseModularizationFlieService.queryYfModularizationFlie(param);
    }

    /**
     * 视频url 视频封面 写入数据库
     *
     * @param ossUploadUpdate
     * @return
     */
    @PostMapping("/uploadVideo")
    public ResponseResult insertVideoUrl(@RequestBody OssUploadUpdateParamDTO ossUploadUpdate) {
        return baseModularizationFlieService.insertVideoUrl(ossUploadUpdate);
    }

    /**
     * 上传图片
     *
     * @param modularizationFlieDTO
     * @return
     */
    @PostMapping("/uploadImage")
    public ResponseResult uploadImage(@RequestBody ModularizationFlieDTO modularizationFlieDTO) {
        return baseModularizationFlieService.uploadImage(modularizationFlieDTO);
    }

    /**
     * 删除
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteYfModularizationFlie(@RequestBody Map<String,Object> param){
        ResponseResult result=baseModularizationFlieService.deleteYfModularizationFlie(param);
        return result;
    }

    /**
     * 店铺装修查询图片
     * @param param
     * @return
     */
    @PostMapping("/queryShopImg")
    public ResponseResult queryYfModularizationFlieShop(@RequestBody ModularizationFlieDTO param){
        return baseModularizationFlieService.queryYfModularizationFlieShop(param);
    }

    /**
     * 商家上传图片
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/uploadShopImg")
    public ResponseResult uploadShopImg(@RequestBody ModularizationFlieDTO param) {
        return  baseModularizationFlieService.uploadShopImg(param);
    }
    /**
     * 商家上传视频
     *
     * @param ossUploadUpdate
     * @return
     */
    @PostMapping(value = "/uploadShopVideo")
    public ResponseResult uploadShopVideo(@RequestBody OssUploadUpdateParamDTO ossUploadUpdate) {
        return  baseModularizationFlieService.uploadShopVideo(ossUploadUpdate);
    }

    /**
     * 登记已上传文件到素材库
     */
    @PostMapping("/register")
    public ResponseResult registerStorageFile(@RequestBody Map<String, Object> param) {
        return baseModularizationFlieService.registerStorageFile(param);
    }
}
