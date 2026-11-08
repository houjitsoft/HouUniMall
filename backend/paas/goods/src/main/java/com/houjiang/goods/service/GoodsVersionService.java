package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.GoodsVersionDTO;
import com.houjiang.goods.dto.ManageCommonIdsDTO;
import com.houjiang.goods.entity.GoodsVersionEntity;
import com.houjiang.goods.vo.ManageGdsCommonQueryVO;
import org.apache.poi.ss.formula.functions.T;

/**
 * 商品版本(GoodsVersion)表服务接口
 *
 * @author Amos
 * @since 2025-04-29 13:25:21
 */
public interface GoodsVersionService extends IService<GoodsVersionEntity> {

    /**
     * 添加商品版本信息
     * <p>
     * 本方法用于添加商品的版本信息，通过查询商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    void addGoodsVersion(ManageCommonIdsDTO param);

    /**
     * 同步商品版本信息
     * <p>
     * 本方法用于同步商品的版本信息，通过查询特定条件下的商品公共ID列表，
     * 然后针对每个商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    void syncGoodsVersion(ManageCommonIdsDTO dto);

    /**
     * 查询商品版本详情
     *
     * @param dto 商品版本查询DTO，包含分页信息和商品通用ID
     * @return 返回包含商品版本信息的分页响应结果
     */
    ResponseResult<PageUtils<ManageGdsCommonQueryVO>> queryGoodsVersionDetails(GoodsVersionDTO dto);

    /**
     * 查询商品版本列表
     *
     * @param dto 商品版本查询条件DTO，包含分页信息和查询条件
     * @return 包含商品版本实体的分页工具类
     */
    ResponseResult<PageUtils<GoodsVersionEntity>> queryGoodsVersionList(GoodsVersionDTO dto);

    /**
     * 回滚商品版本
     *
     * @param dto 商品版本回滚条件DTO，包含版本号和商品通用ID
     * @return 返回响应结果
     */
    ResponseResult<T> rollBackGoodsVersion(GoodsVersionDTO dto);

}
