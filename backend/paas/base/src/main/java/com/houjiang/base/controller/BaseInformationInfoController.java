package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseInformationInfoDTO;
import com.houjiang.base.service.BaseInformationInfoService;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 资讯文章表(BaseInformationInfo)表控制层
 *
 * @author Amos
 * @since 2024-01-18 15:31:10
 */
@RestController
@RequestMapping("baseInformationInfo")
public class BaseInformationInfoController {
    /**
     * 服务对象
     */
    @Resource
    private BaseInformationInfoService baseInformationInfoService;

    /**
     * 平台查看资讯信息
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/queryByPlatform")
    public ResponseResult queryByPlatform(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.queryByPlatform(param);
    }
    /**
     * 平台根据咨询id查询资讯信息
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/queryByPlatformByInformationId")
    public ResponseResult queryByPlatformByInformationId(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.queryByPlatformByInformationId(param);
    }
    /**
     * 平台添加资讯信息
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"informationTitle","informationDesc"})
    @AdminTokenLimit
    @PostMapping("/add")
    public ResponseResult add(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.add(param);
    }
    /**
     * 平台在列表修改游览量
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/updatePageView")
    public ResponseResult updatePageView(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.updatePageView(param);
    }
    /**
     * 平台删除资讯信息
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.delete(param);
    }
    /**
     * 用户端 查看20条资讯信息
     *
     * @return
     */
    @PostMapping("/userQueryInformationInfo")
    public ResponseResult userQueryInformationInfo() {
        return baseInformationInfoService.userQueryInformationInfo();
    }
    /**
     * 用户端 根据分类id查询 资讯信息
     *
     * @return
     */
    @PostMapping("/userQueryInformationInfoById")
    public ResponseResult userQueryInformationInfoById(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.userQueryInformationInfoById(param);
    }
    /**
     * 用户端 查看 资讯信息详情
     *
     * @return
     */
    @PostMapping("/queryInformationInfo")
    public ResponseResult queryInformationInfo(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.queryInformationInfo(param);
    }

    /**
     * 用户端  资讯信息详情 底部 上一篇 下一篇 数据集
     *
     * @return
     */
    @PostMapping("/queryInformationInfoNextChapter")
    public ResponseResult queryInformationInfoNextChapter(@RequestBody BaseInformationInfoDTO param) {
        return baseInformationInfoService.queryInformationInfoNextChapter(param);
    }
}

