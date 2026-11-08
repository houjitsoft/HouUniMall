package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseModularizationdEntity;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName BaseModularizationdService
 * @Description  组件化 页面
 * @Author Amos
 * @Date 2023-07-04 17:58:51
 * @Version 1.0
 * @author Amos
 */
public interface BaseModularizationdService extends IService<BaseModularizationdEntity> {

    /*
/**
     * 添加或修改 组件化页面
     */
    ResponseResult ModularizationdAddOrUPdate(ModularizationdDTO param);

    /*
/**
     * 查询组件化页面 列表
     */
    ResponseResult queryModularizationdAll(ModularizationdDTO param);

    /*
/**
     * 查询组件化页面 列表
     */
    ResponseResult queryModularizationdById(ModularizationdDTO param);

    /*
/**
     * 删除   组件化页面
     */
    ResponseResult modularizationdDelete(ModularizationdDTO param);

    /**
     * 将页面设置是否使用
     */
    ResponseResult modularizationdUpdateStart(ModularizationdDTO param);

    /**
     * 根据id 查询系统默认页面
     */
    ResponseResult ModularizationdDefaultByPageId(ModularizationdDTO param);

    /**
     * 二级页面使用
     */
    ResponseResult UpdateSecondLevelStart(ModularizationdDTO param);

    /**
     * 获取首页页面
     *
     * @return
     */
    ResponseResult modularizationdUpdateIndex();

    /**
     * 获取导航列表
     * @param param
     * @return
     */
    ResponseResult querybottomNavigation(Map<String, Object> param);

    /**
     * 修改底部导航栏配置
     * @param param
     * @return
     */
    ResponseResult bottomNavigation(ModularizationdDTO param);


    /**
     * 添加底部导航栏配置
     * @param param
     * @return
     */
    Integer add(ModularizationdDTO param);

    /**
     * 查询移动端页面模板库（后台模板选择页用）。
     *
     * @return 模板列表，含 code / name / summary / themeColor / style / columns
     */
    ResponseResult queryPageTemplates();

    /**
     * 读取某页面当前使用的模板 code。
     *
     * @param param 含 pageId
     * @return 当前 templateCode，未设置时为空串
     */
    ResponseResult queryPageTemplateCode(ModularizationdDTO param);

    /**
     * 为某页面选择模板（按页面生效，不影响其他页面）。
     *
     * @param param 含 pageId 与 templateCode
     * @return 是否成功；templateCode 不在库内时返回失败
     */
    ResponseResult applyPageTemplate(ModularizationdDTO param);
}
