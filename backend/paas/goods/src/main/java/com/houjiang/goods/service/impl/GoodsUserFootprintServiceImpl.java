package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PointsRatioUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.CartGuessLikeGoodsQueryDTO;
import com.houjiang.goods.dto.WapUserFootPrintDTO;
import com.houjiang.goods.entity.GoodsUserFootprintEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.GoodsCommonMapper;
import com.houjiang.goods.mapper.GoodsUserFootprintMapper;
import com.houjiang.goods.service.GoodsUserFootprintService;
import com.houjiang.goods.vo.ImUserFootVO;
import com.houjiang.goods.vo.WapCommonBaseUserFootVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsUserFootprintServiceImpl
 * @Description 商品访问足迹表
 * @Author Amos
 * @Date 2023-06-08 10:01:59
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsUserFootprintServiceImpl extends ServiceImpl<GoodsUserFootprintMapper, GoodsUserFootprintEntity>
        implements GoodsUserFootprintService {

    @Autowired
    private GoodsCommonMapper getGoodsCommonMapper;
    @Autowired
    private GoodsCommonMapper goodsCommonMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 根据购物车和喜欢的商品查询用户可能喜欢的商品。
     * <p>
     * 该方法通过用户的购物车数据和喜欢的商品数据，结合数据库查询，推测并返回用户可能喜欢的商品列表。
     * 主要用于个性化推荐场景，提升用户购物体验。
     *
     * @param param 包含用户token、限制返回的商品数量等信息的查询参数。
     * @return 返回可能喜欢的商品列表，以及操作结果的成功与否信息。
     */
    @Override
    public ResponseResult queryCartGuessLikeGoods(CartGuessLikeGoodsQueryDTO param) {
        // 初始化商品结果列表
        List<GdsListQueryResultVO> results = Lists.newArrayList();
        // 获取用户设置的每页商品数量
        Integer limitSize = param.getLimitSize();
        // 通过token获取当前用户的信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 根据用户ID查询其购物车中商品的分类ID列表
        // 查询用户足迹商品 的所属分类
        List<Integer> catIds = this.baseMapper.selectCartGuessLikeGoods(limitSize, userInfo.getUserId());
        // 将查询到的分类ID列表设置到查询参数中，用于后续更精确的查询
        param.setCatIds(catIds);
        // 根据更新后的查询参数，查询用户可能喜欢的商品列表
        results = goodsCommonMapper.selectUserFoot(param);

        // 返回查询结果的成功响应
        return ResponseResult.success(results);
    }

    /**
     * 根据用户足迹参数查询用户足迹信息。
     *
     * @param param 用户足迹查询参数，包含用户token等信息。
     * @return 查询结果，包含分页信息和用户足迹详情。
     */
    @Override
    public ResponseResult queryUserFoot(WapUserFootPrintDTO param) {
        IPage<WapCommonBaseUserFootVO> page = null;

        try {
            // 通过token获取用户信息
            // 获取用户
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

            // 将查询参数转换为Map，方便后续处理
            Map data = ParseMapUtils.beanToMap(param);
            // 设置用户ID，用于查询用户的足迹信息
            param.setUserId(userInfo.getUserId());

            // 根据用户ID查询用户的足迹商品ID列表
            // 查询commodIds
            List<Integer> commonIds = this.baseMapper.selectUserFoot(param);
            if (!CollectionUtils.isEmpty(commonIds)) {
                // 如果查询到足迹商品ID列表，则设置到查询参数中，用于进一步查询足迹详情
                param.setCommonIds(commonIds);
                // 根据足迹商品ID列表查询足迹详情，并进行分页处理
                page = getGoodsCommonMapper.selectByCommonIds(new Query<WapCommonBaseUserFootVO>().getPage(data), param);
            } else {
                // 如果没有查询到足迹商品ID列表，则直接创建一个空的分页对象
                page = new Query<WapCommonBaseUserFootVO>().getPage(data);
            }

            // 计算价格
            ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
            if (pointsCashRatio.getCode() == 1) {
                PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                List<WapCommonBaseUserFootVO> list = page.getRecords();
                list.forEach(item -> {
                    if (Objects.nonNull(item.getPointsRate())) {
                        PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(item.getCommonPrice(), item.getPointsRate());
                        item.setCashPrice(pointsRatio.getCashPrice());
                    }
                });
            }

            // 返回查询结果，包含分页信息和足迹详情
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 如果查询过程中出现异常，则返回失败结果，并包含异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 查询用户足迹信息
     * 该方法用于查询用户在移动端的足迹信息，通过用户的足迹数据进行商品推荐或数据展示
     *
     * @param param 用户足迹信息的封装对象，包含了查询所需的用户ID和其他查询条件
     * @return 返回查询结果的响应，包括是否成功和足迹数据列表
     * <p>
     * 方法首先将参数对象转换为Map类型，以便于后续的查询操作如果转换过程中抛出IllegalAccessException，
     * 则通过抛出RuntimeException来处理异常接着，使用getGoodsCommonMapper方法获取到商品公共数据访问对象，
     * 并调用其imQueryFootPrint方法进行足迹数据的查询最后，将查询结果通过PageUtils工具类进行处理，
     * 以适应前端所需的分页数据格式，并返回成功的结果
     */
    @Override
    public ResponseResult imQueryFootPrint(WapUserFootPrintDTO param) {
        Map data = null;
        try {
            // 将用户足迹参数对象转换为Map类型，以便于后续查询操作
            data = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            // 如果转换过程中出现非法访问异常，通过抛出运行时异常来处理
            throw new RuntimeException(e);
        }
        // 使用获取的商品公共数据访问对象进行用户足迹数据的查询
        IPage<ImUserFootVO> page = getGoodsCommonMapper.imQueryFootPrint(new Query<WapCommonBaseUserFootVO>().getPage(data), param);
        ;
        // 将查询结果进行分页处理，并返回成功的结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 删除用户足迹。
     *
     * @param map 包含用户token和要删除的足迹ID列表的映射。
     * @return 删除操作的结果。
     */
    @Override
    public ResponseResult deleteFootPrint(Map<String, Object> map) {
        // 尝试根据token获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());

        // 检查用户信息是否有效，若无效则返回错误响应
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将用户ID添加到map中，用于后续的足迹删除操作
        map.put("userId", userInfoVO.getUserId());

        // 获取待删除的足迹ID列表
        List list = (List) map.get("commonIds");

        // 使用LambdaQueryWrapper构建查询条件，根据用户ID和足迹ID列表删除相应的足迹记录
        this.baseMapper.delete(new LambdaQueryWrapper<GoodsUserFootprintEntity>()
                .eq(GoodsUserFootprintEntity::getUserId, userInfoVO.getUserId())
                .in(map.containsKey("commonIds"), GoodsUserFootprintEntity::getCommonId, list));

        // 删除成功后返回成功响应
        return ResponseResult.success();
    }

    /**
     * 根据用户 token 查询随机推荐商品。
     * <p>
     * 本方法首先尝试从用户 token 中获取用户信息，然后根据用户的足迹记录（商品分类访问记录）
     * 选取最近访问的10个商品分类。根据这些分类，随机查询推荐商品返回给用户。
     * 如果传入的参数中包含 shopId，则只返回该商店内的推荐商品；否则，返回所有商店的推荐商品。
     *
     * @param map 包含用户 token 和可选的商店 ID 的参数映射。
     * @return 返回包含推荐商品信息的响应结果。
     */
    @Override
    public ResponseResult queryUserRandomGoods(Map<String, Object> map) {
        ResponseResult resp = new ResponseResult();
        // 通过 token 获取用户信息
        // 获取用户id
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        List<Integer> catIds = new ArrayList<>();
        // 如果用户信息存在，则根据用户足迹查询最近访问的10个商品分类
        if (null != userInfo) {
            //根据用户id查询足迹 得出用户访问的最近10个商品分类
            List<GoodsUserFootprintEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsUserFootprintEntity>()
                    .eq(GoodsUserFootprintEntity::getUserId, userInfo.getUserId())
                    .groupBy(GoodsUserFootprintEntity::getCatId)
                    .orderByDesc(GoodsUserFootprintEntity::getFootprintTime)
                    .last("LIMIT 10"));
            // 提取商品分类ID
            catIds = list.stream().map(GoodsUserFootprintEntity::getCatId).collect(Collectors.toList());
        }
        // 根据分类ID和可选的商店ID查询随机推荐商品
        List<Map<String, Object>> resultMap;

        // 计算价格
        ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();

        if (map.get("shopId") != null && map.get("shopId") != "") {
            resultMap = this.baseMapper.queryCatRandomGoods(catIds, (Integer) map.get("shopId"));
            for (Map<String, Object> goods : resultMap) {
                Object pointsRate = goods.get("pointsRate");

                if (null != pointsRate && pointsCashRatio.getCode() == 1) {
                    Double rate = (Double) pointsRate;
                    BigDecimal goodsPrice = (BigDecimal) goods.get("commonPrice");
                    PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                    PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(goodsPrice, rate);
                    goods.put("cashPrice", pointsRatio.getCashPrice());
                }
            }
        } else {
            resultMap = this.baseMapper.queryCatRandomGoods1(catIds);
            for (Map<String, Object> goods : resultMap) {
                Object pointsRate = goods.get("pointsRate");
                if (null != pointsRate && pointsCashRatio.getCode() == 1) {
                    Double rate = (Double) pointsRate;
                    BigDecimal goodsPrice = (BigDecimal) goods.get("commonPrice");
                    PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                    PointsRatioUtil.PointsCashRatioRes pointsRatio = PointsRatioUtil.getPointsRatio(goodsPrice, rate);
                    goods.put("cashPrice", pointsRatio.getCashPrice());
                }
            }
        }
        // 设置响应结果的状态码和数据
        resp.setCode(1);
        resp.setData(resultMap);
        return resp;
    }
}
