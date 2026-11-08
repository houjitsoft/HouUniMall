package com.houjiang.userms.controller;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.GdbMerchantUploadAlbumDTO;
import com.houjiang.userms.dto.ManageImageGroupDeleteDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName UserUploadAlbumController
 * @Description 用户相册
 * @Author Amos
 * @Date 2023/6/28 16:44
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/user/upload/album")
public class UserUploadAlbumController extends BaseController{

    /**
     * 查询图片分组
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/image/group/query")
    public ResponseResult queryImageGroup(@RequestBody UserTokenBeanDTO param) {
        ResponseResult result =userUploadAlbumService.queryImageGroup(param);
        return result;
    }


    /**
     * 创建分组
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/image/group/create")
    public ResponseResult createGroup(@RequestBody GdbMerchantUploadAlbumDTO param) {
        ResponseResult result =userUploadAlbumService.createGroup(param);
        return result;

    }

    /**
     * 删除分组
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/image/group/delete")
    public ResponseResult deleteImageGroup(@RequestBody ManageImageGroupDeleteDTO param) {
        ResponseResult result =userUploadAlbumService.deleteImageGroup(param);
        return result;
    }
}
