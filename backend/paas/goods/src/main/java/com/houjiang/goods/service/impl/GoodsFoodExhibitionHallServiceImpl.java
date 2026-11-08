package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.FoodsExhibitionHallListDTO;
import com.houjiang.goods.dto.GoodsEntityDTO;
import com.houjiang.goods.entity.GoodsFoodExhibitionHallEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.GoodsBaseMapper;
import com.houjiang.goods.mapper.GoodsFoodExhibitionHallMapper;
import com.houjiang.goods.service.GoodsFoodExhibitionHallService;
import com.houjiang.goods.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Amos
 */
@Service
public class GoodsFoodExhibitionHallServiceImpl extends ServiceImpl<GoodsFoodExhibitionHallMapper, GoodsFoodExhibitionHallEntity>
    implements GoodsFoodExhibitionHallService{

    @Autowired
    private GoodsBaseMapper goodsBaseMapper;
    @Autowired
    private BaseServiceClient baseServiceClient;
    /**
     * 根据展厅ID查询展厅详情。
     *
     * @param param 查询参数，包含展厅ID和用户令牌。
     * @return 展厅的详细信息，包括关联的商品列表。
     */
    @Override
    public ResponseResult queryFoodsHallDetail(FoodsExhibitionHallListDTO param) {
        // 通过用户令牌获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());

        // 验证用户信息是否有效。
        if (null != userInfo) {
            // 根据展厅ID查询展厅信息。
            GoodsFoodExhibitionHallEntity result = this.baseMapper.selectById(param.getExhibitionHallId());
            // 获取展厅关联的商品ID字符串。
            String goodsIds = result.getGoodsId();
            // 初始化商品列表。
            List<GoodsEntityDTO> goodsList = new ArrayList<>();

            // 判断商品ID字符串是否为空，如果不为空则进行处理。
            if (null != goodsIds && !"".equals(goodsIds)) {
                // 如果商品ID字符串包含逗号，说明有多个商品ID。
                if (goodsIds.contains(",")) {
                    // 将商品ID字符串分割成数组。
                    String[] goodsIdss = goodsIds.split(",");
                    // 遍历商品ID数组，查询每个商品的详细信息。
                    for (String s : goodsIdss) {
                        // 将字符串类型的商品ID转换为整型。
                        Integer goodsId = Integer.parseInt(s);
                        // 根据商品ID查询商品名称。
                        String goodsName = goodsBaseMapper.selectById(goodsId).getGoodsName();
                        // 创建商品DTO并设置商品ID和名称。
                        GoodsEntityDTO ge = new GoodsEntityDTO();
                        ge.setGoodsId(goodsId);
                        ge.setGoodsName(goodsName);
                        // 将商品DTO添加到商品列表中。
                        goodsList.add(ge);
                    }

                } else {
                    // 如果商品ID字符串不包含逗号，说明只有一个商品ID。
                    Integer goodsId = Integer.parseInt(goodsIds);
                    // 根据商品ID查询商品名称。
                    String goodsName = goodsBaseMapper.selectById(goodsId).getGoodsName();
                    // 创建商品DTO并设置商品ID和名称。
                    GoodsEntityDTO ge = new GoodsEntityDTO();
                    ge.setGoodsId(goodsId);
                    ge.setGoodsName(goodsName);
                    // 将商品DTO添加到商品列表中。
                    goodsList.add(ge);
                }
            }
            // 将查询到的商品列表设置到展厅信息中。
            result.setGoodsList(goodsList);
            // 返回展厅的详细信息。
            return ResponseResult.success(result);
        }
        // 如果用户信息无效，返回错误响应。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 更新食品展厅详情信息。
     *
     * @param param 包含展厅名称、商品ID、展厅图片和展厅标志的信息对象。
     * @return 如果操作成功，返回带有成功代码和消息的成功结果；如果操作失败，根据缺失的参数返回相应的错误结果；如果token非法，返回相应的错误结果。
     */
    @Override
    public ResponseResult updateFoodsHallDetail(FoodsExhibitionHallListDTO param) {
        // 检查展厅名称是否为空或为空字符串
        if (param.getExhibitionHallName() == null || "".equals(param.getExhibitionHallName())) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        // 检查商品ID是否为空或为空字符串
        else if (param.getGoodsId() == null || "".equals(param.getGoodsId())) {
            return ResponseResult.fail(BizCodeEnume.NO_CHOICE_GOODS);
        }
        // 检查展厅图片是否为空或为空字符串
        else if (param.getExhibitionHallImage() == null || "".equals(param.getExhibitionHallImage())) {
            return ResponseResult.fail(BizCodeEnume.IMAGE_IS_NULL);
        }
        // 检查展厅标志是否为空或为空字符串
        else if (param.getExhibitionHallLogo() == null || "".equals(param.getExhibitionHallLogo())) {
            return ResponseResult.fail(BizCodeEnume.IMAGE_IS_NULL);
        }

        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息不为空，则保存展厅详情信息并返回成功结果
        if (null != userInfo) {
            this.baseMapper.insertFoodsHallDetail(param);
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // 如果用户信息为空，则返回token非法的错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 删除食品展厅详情。
     *
     * @param param 包含展厅ID和用户令牌的参数对象。
     * @return 如果删除成功，返回成功的响应结果；如果删除失败，返回失败的响应结果，可能是因为令牌非法。
     *
     * 本方法首先尝试根据传入的令牌信息获取用户信息。如果用户信息有效，
     * 则删除对应展厅ID的记录；如果用户信息无效，即令牌非法，方法将返回失败结果。
     */
    @Override
    public ResponseResult deleteFoodsHallDetail(FoodsExhibitionHallListDTO param) {
        // 根据令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());

        // 检查用户信息是否有效
        if (null != userInfo) {
            // 删除指定展厅ID的信息
            this.baseMapper.deleteById(param.getExhibitionHallId());
            // 返回删除成功的响应结果
            return ResponseResult.success();
        }

        // 如果用户信息无效，返回令牌非法的失败响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据展览馆名称查询食品展览馆列表。
     *
     * @param param 包含用户令牌和展览馆名称的参数对象。
     * @return 如果令牌有效，返回查询到的展览馆列表；否则返回令牌非法的错误信息。
     */
    @Override
    public ResponseResult queryFoodsHallList(FoodsExhibitionHallListDTO param) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息存在
        if (null != userInfo) {
            // 根据展览馆名称查询展览馆列表
            List<GoodsFoodExhibitionHallEntity> result = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsFoodExhibitionHallEntity>()
                    .like(StringUtils.isNotEmpty(param.getExhibitionHallName()), GoodsFoodExhibitionHallEntity::getExhibitionHallName, param.getExhibitionHallName()));

            // 返回查询结果成功的响应
            return ResponseResult.success(result);
        }
        // 如果令牌无效，返回令牌非法的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
    /**
     * 查询美食展厅信息。
     * 该方法用于获取首页美食展厅的相关信息，包括滑动图片、特色馆和所有展厅。
     *
     * @return ResponseResult 包含展厅详情的响应结果。
     */
    @Override
    public ResponseResult queryFoodExhibitionHall() {
        WebFoodsHallIndexResultVO result = new WebFoodsHallIndexResultVO();

        // 查询首页滑动图片信息
        // 查询首页
        List<WabSystemConfigHallVO> slides = baseServiceClient.querySystemConfigHall("foodSliderPC");

        List<WebIndexSpecialtysHallVO> specialtysHallList =  this.baseMapper.selectByAll();
        for (WebIndexSpecialtysHallVO webIndex : specialtysHallList) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(webIndex.getGoodsId())) {
                // 将商品ID字符串转换为整型列表
                List<String> goodsIdsStr = Arrays.asList(webIndex.getGoodsId().split(","));
                List<Integer> goodsIds = new ArrayList<>();
                for (String goodsIdStr : goodsIdsStr) {
                    goodsIds.add(Integer.parseInt(goodsIdStr));
                }
                // 根据商品ID列表查询商品信息
                List<WapHallGoodsQueryResultVO> commonList = goodsBaseMapper.selectFoodExhibitionHallByGoodsIds(goodsIds);

                webIndex.setGoodsList(commonList);
            }
        }
        // 组装查询结果
        result.setAllHall(specialtysHallList);
        result.setSlides(slides);
        result.setRecommendHall(specialtysHallList);

        return ResponseResult.success(result);
    }

    /**
     * 查询美食展厅详细信息。
     * 根据展厅ID查询展厅的详细信息，并获取该展厅下的商品列表。
     *
     * @param exhibitionHallId 展厅ID
     * @return ResponseResult 包含展厅详细信息的响应结果。
     */
    @Override
    public ResponseResult foodExhibitionHallDetail(Integer exhibitionHallId) {
        WapGoodsFoodExhibitionHallVO hall = this.baseMapper.selectFoodExhibitionHallById(exhibitionHallId);
        if (hall == null) {
            return ResponseResult.success();
        }
        // 将商品ID字符串转换为整型列表
        List<String> goodsIdsStr = Arrays.asList(hall.getGoodsId().split(","));
        List<Integer> goodsIds = new ArrayList<>();
        for (String goodsIdStr : goodsIdsStr) {
            goodsIds.add(Integer.parseInt(goodsIdStr));
        }
        // 根据商品ID列表查询商品信息
        List<WapHallGoodsQueryResultVO> commonList = goodsBaseMapper.selectFoodExhibitionHallByGoodsIds(goodsIds);
        hall.setCommonList(commonList);

        return ResponseResult.success(hall);
    }
}




