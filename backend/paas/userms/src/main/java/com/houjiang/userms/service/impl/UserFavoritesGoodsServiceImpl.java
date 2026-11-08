package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.GdsDetailsCommonQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.UserFavoriteGoodsDTO;
import com.houjiang.userms.entity.UserFavoritesGoodsEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.feign.GoodsServiceClient;
import com.houjiang.userms.mapper.UserFavoritesGoodsMapper;
import com.houjiang.userms.service.UserFavoritesGoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @ClassName UserFavoritesGoodsServiceImpl
 * @Description 收藏的商品
 * @Author Amos
 * @Date 2023-05-26 16:24:17
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserFavoritesGoodsServiceImpl extends ServiceImpl<UserFavoritesGoodsMapper, UserFavoritesGoodsEntity>
        implements UserFavoritesGoodsService {
    @Autowired
    private GoodsServiceClient goodsServiceClient;


    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 检查用户是否收藏了商品。
     *
     * @param paraMap 包含用户令牌(token)和其他参数的映射。
     * @return 如果用户已收藏商品，则返回已收藏的状态；否则返回未收藏的状态。
     */
    @Override
    public ResponseResult check(Map<String, String> paraMap) {
        if(StringUtils.isBlank(paraMap.get("token"))){
            return ResponseResult.success(0);
        }
        // 通过令牌获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 将用户ID添加到参数映射中，以便后续查询收藏状态。
        paraMap.put("userId", userInfo.getUserId());
        // 查询用户是否收藏了商品。
        String torf = this.baseMapper.checkGoodsFavoriteOrNot(paraMap);
        // 如果查询结果不为空，则根据查询结果返回用户收藏状态。
        if (null != torf) {
            return ResponseResult.success("已收藏",1);
        }
        // 如果查询结果为空，则表示用户未收藏商品。
        return ResponseResult.success("未收藏",0);
    }
    /**
     * 添加收藏商品
     *
     * @param paraMap 包含token、commonId、goodsId等参数的映射
     * @return 如果收藏成功，返回成功的响应结果；如果收藏失败或已收藏，返回失败的响应结果
     */
    @Override
    public ResponseResult add(Map<String, String> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 将用户ID添加到参数映射中
        paraMap.put("userId", userInfo.getUserId());

        // 检查商品是否已被收藏
        String favoritesGoodsId =  this.baseMapper.checkGoodsFavoriteOrNot(paraMap);
        if (null != favoritesGoodsId) {
            // 如果商品已被收藏，则返回失败结果
            return ResponseResult.fail(0, "重复收藏");
        }

        // 尝试添加收藏商品记录
        Integer torf = this.baseMapper.addFavoriteGoods(paraMap);

        // 准备更新商品收藏数的参数
        // 更新商品的收藏量
        Map<String, String> updateMap = new HashMap<String, String>();
        updateMap.put("commonId", paraMap.get("commonId"));
        updateMap.put("goodsId", paraMap.get("goodsId"));
        // type=1表示增加收藏数
        updateMap.put("type", "1");

        // 调用商品服务更新商品收藏数
        ResponseResult resultResp = goodsServiceClient.updateGoodsCollect(updateMap);

        if (1 == torf && resultResp.getCode() == 1) {
            // 如果添加收藏记录成功，并且商品收藏数更新成功，则返回成功结果
            return ResponseResult.success("增加收藏的商品成功");
        }

        // 如果有任何一步失败，则返回失败结果
        return ResponseResult.fail(0, "增加收藏的商品失败");
    }

    /**
     * 根据传入的参数获取用户收藏的商品信息。
     *
     * @param paraMap 包含各种查询参数的映射，其中必须包含token以识别用户。
     * @return 返回查询结果的ResponseResult对象，包含用户收藏的商品信息。
     */
    @Override
    public ResponseResult get(Map<String, Object> paraMap) {
        try {
            // 从参数映射中提取token
            String token = (String) paraMap.get("token");
            // 通过token从Redis中获取用户ID
            String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
            // 将用户ID添加到参数映射中，以便后续使用
            paraMap.put("userId", userId);
            // 初始化结果映射，用于存储查询结果
            Map<String, Object> resultMap = new HashMap<>();
            // 检查是否指定了页码和每页数量，如果没有，则返回参数异常错误
            if (null == paraMap.get("page") && null == paraMap.get("limit")) {
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }
            // 将参数映射对象转换为Map，以适应查询接口的要求
            // 将对象转换为map
            Map data = ParseMapUtils.beanToMap(paraMap);
            // 执行分页查询，获取用户收藏的商品信息
            // 分页查询数据
            IPage<UserFavoriteGoodsDTO> page = this.baseMapper.getFavoriteGoods2(new Query<UserFavoriteGoodsDTO>().getPage(data), paraMap);
            // 检查查询结果是否为空，如果为空，则返回空集合提示信息
            // 查询是否有数据
            if (CollectionUtils.isEmpty(page.getRecords())) {
                return ResponseResult.success(BizCodeEnume.COLLECTION_IS_EMPTY.getCode(), BizCodeEnume.COLLECTION_IS_EMPTY.getMsg());
            }
            // 初始化用于存储详细信息的列表
            List<UserFavoriteGoodsDTO> list = new ArrayList<>();
            // 遍历查询结果，为每个商品补充详细信息
            for (UserFavoriteGoodsDTO userFavoriteGoods : page.getRecords()) {
                // 根据商品的commonId调用商品服务获取详细信息
                ResponseResult result = goodsServiceClient.getDetailByCommonId(userFavoriteGoods.getCommonId());
                // 如果详细信息不为空，则将其复制到商品对象中
                if (!ObjectUtils.isEmpty(result.getData())) {
                    GdsDetailsCommonQueryResultVO gdsDetailsCommonQueryResult = JSON.parseObject(JSON.toJSONString(result.getData()), GdsDetailsCommonQueryResultVO.class);
                    BeanUtils.copyProperties(gdsDetailsCommonQueryResult, userFavoriteGoods);
                    // 计算嘉币价格
                    ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
                    if (pointsCashRatio.getCode() == 1) {
                        Double rate = userFavoriteGoods.getPointsRate();
                        if (Objects.nonNull(userFavoriteGoods.getPointsRate())) {
                            PointsCashRatioVo ratioVo = pointsCashRatio.getData();
                            PointsRatioUtil.PointsCashRatioRes ratioRes = PointsRatioUtil.getPointsRatio(userFavoriteGoods.getCommonPrice(), rate);
                            userFavoriteGoods.setCashPrice(ratioRes.getCashPrice());
                        }
                    }

                }
            }
            // 返回成功的查询结果，包含补充了详细信息的商品列表
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.SUCCESSFULLY_QUERIED_FAVORITE_PRODUCTS.getMsg(), PageUtils.getPage(page));
        } catch (Exception e) {
            // 打印异常堆栈跟踪，然后返回失败的查询结果，包含异常信息
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }
    /**
     * 删除收藏商品
     *
     * @param paraMap 参数映射表，包含token和commonIds等信息
     * @return 删除操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult delete(Map<String, Object> paraMap) {
        // 从参数映射中获取token
        String token = (String) paraMap.get("token");
        // 根据token解析用户信息，检查token有效性
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfoVO == null){
            // 如果token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 获取用户ID
        String userId = userInfoVO.getUserId();
        // 将用户ID添加到参数映射中
        paraMap.put("userId",userId );

        // 从参数映射中获取收藏商品的ID列表
        List<String> commonIds = (List<String>) paraMap.get("commonIds");
        if (null == commonIds) {
            // 如果ID列表为空，返回失败结果
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }
        // 遍历收藏商品的ID列表
        for (String commonId : commonIds) {
            // 根据commonId和userId查询收藏商品信息
            // 调用mall库进行减收藏量
            UserFavoritesGoodsEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserFavoritesGoodsEntity>()
                    .eq(UserFavoritesGoodsEntity::getCommonId, commonId)
                    .eq(UserFavoritesGoodsEntity::getUserId, userId));
            // 根据commonId和userId删除收藏商品信息
            this.baseMapper.delete(new LambdaQueryWrapper<UserFavoritesGoodsEntity>()
                    .eq(UserFavoritesGoodsEntity::getCommonId, commonId)
                    .eq(UserFavoritesGoodsEntity::getUserId, userId));
            // 获取商品ID
            Integer goodsId = entity.getGoodsId();

            // 构建更新商品收藏量的参数
            // 更新商品的收藏量
            Map<String, String> updateMap = new HashMap<String, String>();
            updateMap.put("commonId", commonId);
            updateMap.put("goodsId", goodsId.toString());
            // 设置更新类型为减少收藏量
            // type=1增加，2减少
            updateMap.put("type", "2");
            // 调用商品服务更新商品收藏量
            ResponseResult resultResp = goodsServiceClient.updateGoodsCollect(updateMap);
        }

        // 如果所有操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 查询用户的收藏和足迹信息
     *
     * @param paraMap 参数映射表，包含token等信息
     * @return 查询结果，包含收藏商品数量、收藏店铺数量、足迹数量等信息
     */
    @Override
    public ResponseResult query(Map<String, String> paraMap) {
        // 初始化默认收藏和足迹数量
        Map<String, Object> map = new HashMap<>();
        map.put("favoriteGoodsCount", 0);
        map.put("favoriteShopCount", 0);
        map.put("footPrintCount", 0);
        // 检查token是否存在并有效
        if (null != paraMap.get("token") && !"".equals(paraMap.get("token"))) {
            // 从参数中获取token
            String token = paraMap.get("token");
            // 从Redis中获取用户ID
            UserInfoVO ui = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
            if(ui == null){
                map.put("favoriteGoodsCount", 0);
                map.put("favoriteShopCount", 0);
                map.put("footPrintCount", 0);
            }else {
                String userId = ui.getUserId();
                // 根据用户ID查询收藏的商品列表
                //查询收藏的商品
                List<UserFavoriteGoodsDTO> favoriteGoodsList = this.baseMapper.getFavoriteGoods(userId);
                // 根据用户ID查询收藏的店铺列表
                //查询收藏的店铺
                List<Map<String, Object>> favoriteShopList = this.baseMapper.getFavoriteShops(userId);
                // 将token添加到结果映射中
                map.put("token", token);
                // 根据用户ID查询足迹数量
                //查询游览足迹
                paraMap.put("userId", userId);
                Integer integer = this.baseMapper.selectUserFootCount(paraMap);

                // 如果收藏商品列表不为空，更新收藏商品数量
                if (null != favoriteGoodsList) {
                    map.put("favoriteGoodsCount", favoriteGoodsList.size());
                }
                // 如果收藏店铺列表不为空，更新收藏店铺数量
                if (null != favoriteGoodsList) {
                    map.put("favoriteShopCount", favoriteShopList.size());
                }
                // 更新足迹数量
                map.put("footPrintCount", integer);
            }
        }
        // 调用基础服务查询广告位信息
        //获取广告位
        ResponseResult result = baseServiceClient.queryMePage(paraMap);
        // 如果广告位信息不为空，添加到结果映射中
        if (null != result.getData()) {
            map.put("mePageAdvertising", result.getData());
        }
        // 返回查询结果
        return ResponseResult.success(map);

    }


}
