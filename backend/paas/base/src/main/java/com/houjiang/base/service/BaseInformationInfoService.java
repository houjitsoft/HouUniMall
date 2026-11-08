package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseInformationInfoDTO;
import com.houjiang.base.entity.BaseInformationInfoEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * 资讯文章表(BaseInformationInfo)表服务接口
 *
 * @author Amos
 * @since 2024-01-18 15:31:10
 */
public interface BaseInformationInfoService extends IService<BaseInformationInfoEntity> {

    /**
     * 平台查看资讯信息
     *
     * @param param
     * @return
     */
    ResponseResult queryByPlatform(BaseInformationInfoDTO param);

    /**
     * 平台根据咨询id查询资讯信息
     * @param param
     * @return
     */
    ResponseResult queryByPlatformByInformationId(BaseInformationInfoDTO param);

    /**
     * 平台添加资讯信息
     *
     * @param param
     * @return
     */
    ResponseResult add(BaseInformationInfoDTO param);
    /**
     * 平台在列表修改游览量
     * @param param
     * @return
     */
    ResponseResult updatePageView(BaseInformationInfoDTO param);
    /**
     * 平台删除资讯信息
     * @param param
     * @return
     */
    ResponseResult delete(BaseInformationInfoDTO param);

    /**
     * 用户端 查看20条资讯信息
     * @return
     */
    ResponseResult userQueryInformationInfo();

    /**
     * 用户端 根据分类id查询 资讯信息
     * @param param
     * @return
     */
    ResponseResult userQueryInformationInfoById(BaseInformationInfoDTO param);

    /**
     * 用户端 查看 资讯信息详情
     * @param param
     * @return
     */
    ResponseResult queryInformationInfo(BaseInformationInfoDTO param);

    /**
     * 用户端  资讯信息详情 上一篇 下一篇 数据集
     * @param param
     * @return
     */
    ResponseResult queryInformationInfoNextChapter(BaseInformationInfoDTO param);
}
