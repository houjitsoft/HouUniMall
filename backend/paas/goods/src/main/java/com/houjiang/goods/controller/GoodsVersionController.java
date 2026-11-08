package com.houjiang.goods.controller;


import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.GoodsVersionDTO;
import com.houjiang.goods.dto.ManageCommonIdsDTO;
import com.houjiang.goods.entity.GoodsVersionEntity;
import com.houjiang.goods.service.GoodsVersionService;
import com.houjiang.goods.vo.ManageGdsCommonQueryVO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 商品版本(GoodsVersion)表控制层
 *
 * @author Amos
 * @since 2025-04-29 13:25:21
 */
@RestController
@RequestMapping("goodsVersion")
public class GoodsVersionController {
    /**
     * 服务对象
     */
    @Resource
    private GoodsVersionService goodsVersionService;

    /**
     * 添加商品版本信息
     * <p>
     * 本方法用于添加商品的版本信息，通过查询商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    @PostMapping("addGoodsVersion")
    public ResponseResult<T> addGoodsVersion(@RequestBody ManageCommonIdsDTO param) {
        goodsVersionService.addGoodsVersion(param);
        return ResponseResult.success();
    }

    /**
     * 同步商品版本信息
     * <p>
     * 本方法用于同步商品的版本信息，通过查询特定条件下的商品公共ID列表，
     * 然后针对每个商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    @PostMapping("syncGoodsVersion")
    public ResponseResult<T> syncGoodsVersion(@RequestBody ManageCommonIdsDTO dto) {
        goodsVersionService.syncGoodsVersion(dto);
        return ResponseResult.success();
    }

    /**
     * 查询商品版本详情
     *
     * @param dto 商品版本查询DTO，包含分页信息和商品通用ID
     * @return 返回包含商品版本信息的分页响应结果
     */
    @PostMapping("queryGoodsVersionDetails")
    public ResponseResult<PageUtils<ManageGdsCommonQueryVO>> queryGoodsVersionDetails(@RequestBody @Validated({Select.commonId.class}) GoodsVersionDTO dto) {
        return goodsVersionService.queryGoodsVersionDetails(dto);
    }

    /**
     * 查询商品版本列表
     *
     * @param dto 商品版本查询条件DTO，包含分页信息和查询条件
     * @return 包含商品版本实体的分页工具类
     */
    @PostMapping("queryGoodsVersionList")
    public ResponseResult<PageUtils<GoodsVersionEntity>> queryGoodsVersionList(@RequestBody GoodsVersionDTO dto) {
        return goodsVersionService.queryGoodsVersionList(dto);
    }

    /**
     * 回滚商品版本
     *
     * @param dto 商品版本回滚条件DTO，包含版本号和商品通用ID
     * @return 返回响应结果
     */
    @PostMapping("rollBackGoodsVersion")
    public ResponseResult<T> rollBackGoodsVersion(@RequestBody @Validated({Select.versionDetails.class}) GoodsVersionDTO dto) {
        return goodsVersionService.rollBackGoodsVersion(dto);
    }

}

