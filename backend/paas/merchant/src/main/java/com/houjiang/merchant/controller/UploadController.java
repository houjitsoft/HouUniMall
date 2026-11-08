package com.houjiang.merchant.controller;

import com.houjiang.merchant.common.enums.FileEnum;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.aop.aspect.RequestType;
import com.houjiang.merchant.service.IFileService;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.enums.AlbumEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.plugin.storage.StorageDriver;
import com.houjiang.merchant.common.plugin.storage.UploadFilesVo;
import com.houjiang.merchant.common.util.StringUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@Tag(name = "上传文件管理")
@RequestMapping("/merchant/upload")
public class UploadController {

    @Resource
    IFileService fileService;

    @Log(title = "上传图片", requestType = RequestType.File)
    @PostMapping("/image")
    @Operation(summary="上传图片")
    public AjaxResult image(@RequestParam("file") MultipartFile[] files, @RequestParam("cid") String requestCid) {

        String cid = StringUtils.isNotEmpty(requestCid) ? requestCid : "0";

        if (files == null || files.length == 0) {
            throw new OperateException("请选择上传图片!");
        }
        for (int i = 0; i < files.length; i++) {
            StorageDriver storageDriver = new StorageDriver();
            UploadFilesVo vo = storageDriver.upload(files[i], "image", AlbumEnum.IMAGE.getCode());
            Map<String, String> album = new LinkedHashMap<>();
            album.put("aid", String.valueOf(MerchantThreadLocal.getAdminId()));
            album.put("cid", cid);
            album.put("type", String.valueOf(AlbumEnum.IMAGE.getCode()));
            album.put("url",  vo.getUrl());
            album.put("name", vo.getName());
            album.put("source", String.valueOf(FileEnum.SOURCE_SHOP.getCode()));
            album.put("sourceId", String.valueOf(MerchantThreadLocal.getShopId()));
            Integer id = fileService.fileAdd(album);
            vo.setId(id);
        }

        return AjaxResult.success();
    }

    @Log(title = "上传图片", requestType = RequestType.File)
    @PostMapping("/file")
    @Operation(summary="上传文件")
    public AjaxResult file(@RequestParam("file") MultipartFile[] files, @RequestParam("cid") String requestCid) {

        String cid = StringUtils.isNotEmpty(requestCid) ? requestCid : "0";

        if (files == null || files.length == 0) {
            throw new OperateException("请选择上传文件!");
        }


        for (int i = 0; i < files.length; i++) {
            StorageDriver storageDriver = new StorageDriver();
            UploadFilesVo vo = storageDriver.upload(files[i], "image", AlbumEnum.Doc.getCode());
            Map<String, String> album = new LinkedHashMap<>();
            album.put("aid", String.valueOf(MerchantThreadLocal.getAdminId()));
            album.put("cid", cid);
            album.put("type", String.valueOf(AlbumEnum.Doc.getCode()));
//        album.put("size", vo.getSize().toString());
//        album.put("ext", vo.getExt());
            album.put("url", "uploads/" + vo.getUrl());
            album.put("name", vo.getName());
            Integer id = fileService.fileAdd(album);
            vo.setId(id);
        }

        return AjaxResult.success();
    }

    @Log(title = "上传视频", requestType = RequestType.File)
    @PostMapping("/video")
    @Operation(summary="上传视频")
    public AjaxResult video(@RequestParam("file") MultipartFile[] files, @RequestParam("cid") String requestCid) {

        String cid = StringUtils.isNotEmpty(requestCid) ? requestCid : "0";


        if (files == null || files.length == 0) {
            throw new OperateException("请选择上传视频!");
        }
        for (int i = 0; i < files.length; i++) {

            StorageDriver storageDriver = new StorageDriver();
            UploadFilesVo vo = storageDriver.upload(files[i], "video", AlbumEnum.Video.getCode());

            Map<String, String> album = new LinkedHashMap<>();
            album.put("cid", cid);
            album.put("aid", String.valueOf(MerchantThreadLocal.getAdminId()));
            album.put("type", String.valueOf(AlbumEnum.Video.getCode()));
//        album.put("ext", vo.getExt());
//        album.put("size", vo.getSize().toString());
            album.put("url",  "uploads/" + vo.getUrl());
            album.put("name", vo.getName());
            Integer id = fileService.fileAdd(album);

            vo.setId(id);
        }
        return AjaxResult.success();
    }

}
