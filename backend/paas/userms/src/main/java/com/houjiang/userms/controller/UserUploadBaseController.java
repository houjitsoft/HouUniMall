package com.houjiang.userms.controller;

import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.ManageBatchDeleteImageDTO;
import com.houjiang.userms.dto.ManageImageNameUpdateParamDTO;
import com.houjiang.userms.dto.ManageImageSpaceQueryDTO;
import com.houjiang.userms.dto.ManageImageUploadParamDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName UserUploadBaseController
 * @Description 用户附件表-图片、视频
 * @Author Amos
 * @Date 2023/6/29 9:09
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/user/upload/base")
public class UserUploadBaseController extends BaseController{

    /**
     * 图片空间查询
     *
     * @param param
     * @return
     */
    @PostMapping("/image/space/query")
    public ResponseResult queryImageSpace(@RequestBody ManageImageSpaceQueryDTO param) throws IllegalAccessException {
        ResponseResult result =userUploadBaseService.queryImageSpace(param);
        return result;
    }


    /**
     * 删除图片
     * @param param
     * @return
     */
    @PostMapping("/image/delete")
    public ResponseResult deleteImage(@RequestBody ManageBatchDeleteImageDTO param) {
        ResponseResult result =userUploadBaseService.deleteImage(param);
        return result;
    }

    /**
     * 更改图片名称
     * @param param
     * @return
     */
    @PostMapping("/image/name/update")
    public ResponseResult updateImageName(@RequestBody ManageImageNameUpdateParamDTO param) {
        ResponseResult result =userUploadBaseService.updateImageName(param);;
        return result;
    }


    /**
     * 上传图片
     * @param param
     * @return
     */
    @PostMapping("/image/upload")
    public ResponseResult unloadImage(@RequestBody ManageImageUploadParamDTO param) {
        ResponseResult result =userUploadBaseService.uploadImage(param);
        return result;
    }

    /**
     * 查询店铺图片空间数量
     * @param shopId
     * @return
     */
    @GetMapping("/fegin/selectShopImageSpaceCount")
    public Integer selectShopImageSpaceCount(@RequestParam("shopId") Integer shopId,@RequestParam("type") String type){
        Integer result=userUploadBaseService.selectShopImageSpaceCount(shopId,type);
        return  result;
    }

    /**
     * 插入视频
     * @param param
     */
    @PostMapping("/feign/insertVideo")
    public void insertVideo(@RequestBody ManageShopVideoDTO param){
        userUploadBaseService.insertVideo(param);
    }

    /**
     * 更新视频
     * @param param
     */
    @PostMapping("/feign/updateVideo")
    public void updateVideo(@RequestBody ManageShopVideoDTO param){
        userUploadBaseService.updateVideo(param);
    }

    /**
     * 查询店铺空间图片
     */
    @PostMapping("/feign/selectImageSpaceList")
    public List<ManageShopVideoDTO> selectImageSpaceList(@RequestBody ManageShopVideoDTO param){
        List<ManageShopVideoDTO> result = userUploadBaseService.selectImageSpaceList(param);
        return result;
    }
}
