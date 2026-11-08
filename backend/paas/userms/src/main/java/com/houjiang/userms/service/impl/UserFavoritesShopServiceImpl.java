package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.shop.ShopIndexVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserFavoritesShopEntity;
import com.houjiang.userms.feign.MerchantServiceClient;
import com.houjiang.userms.feign.GoodsServiceClient;
import com.houjiang.userms.mapper.UserFavoritesShopMapper;
import com.houjiang.userms.service.UserFavoritesShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏店铺服务实现类
 * @ClassName UserFavoritesShopServiceImpl
 * @Description 收藏的店铺服务实现
 * @Author Amos
 * @Date 2023-05-26 16:35:37
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserFavoritesShopServiceImpl extends ServiceImpl<UserFavoritesShopMapper, UserFavoritesShopEntity>
        implements UserFavoritesShopService {
    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    /**
     * 检查店铺是否被收藏
     * @param paraMap 包含token和shopId的参数映射
     * @return 返回收藏状态的结果
     */
    @Override
    public ResponseResult check(Map<String, String> paraMap) {
        // 根据token获取用户信息，检查token是否有效
        if(null != paraMap.get("token") && StringUtils.isNotBlank(paraMap.get("token"))){
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
            // 将用户ID添加到参数映射中
            paraMap.put("userId", userInfo.getUserId());
            // 查询店铺是否被收藏
            String torf = this.baseMapper.checkFavoriteShopOrNot(paraMap);
            if (null != torf) {
                // 已收藏的情况
                return ResponseResult.success("已收藏", 1);
            }
        }
        // 未收藏的情况
        return ResponseResult.success("未收藏", 0);
    }

    /**
     * 添加或删除收藏的店铺
     * @param paraMap 包含token、shopId等参数的映射
     * @return 返回操作结果的状态
     */
    @Override
    public ResponseResult add(Map<String, String> paraMap) {
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 将用户ID添加到参数映射中
        paraMap.put("userId", userInfo.getUserId());

        // 检查店铺是否已被收藏
        String favoritesShopId = this.baseMapper.checkFavoriteShopOrNot(paraMap);
        if (null != favoritesShopId) {
            // 如果已收藏，则删除收藏
            Integer integer = this.baseMapper.deleteFavoriteShopOrNot(paraMap);
            paraMap.put("type", "2");
            // 调用业务服务更新收藏状态
            ResponseResult resultResp = merchantServiceClient.updateShopCollect(paraMap);
            if (1 == integer && resultResp.getCode() == 1) {
                // 删除收藏成功
                return ResponseResult.success("删除收藏的店铺");
            }
            // 删除收藏失败
            return ResponseResult.fail(0, "删除收藏的店铺失败");
        }
        // 如果未收藏，则添加收藏
        Integer torf = this.baseMapper.addFavoriteShop(paraMap);
        paraMap.put("type", "1");
        // 调用业务服务更新收藏状态
        ResponseResult resultResp = merchantServiceClient.updateShopCollect(paraMap);
        if (1 == torf && resultResp.getCode() == 1) {
            // 添加店铺会员信息
            ShopUserDTO shopUserDTO = new ShopUserDTO();
            shopUserDTO.setUserId(userInfo.getUserId());
            shopUserDTO.setShopId(paraMap.get("shopId"));
            merchantServiceClient.addShopUser(shopUserDTO);
            // 添加收藏成功
            return ResponseResult.success("增加收藏的店铺");
        }
        // 添加收藏失败
        return ResponseResult.fail(0, "增加收藏的店铺失败");
    }

    /**
     * 根据传入的参数获取用户收藏的店铺信息，并分页查询。
     * @param paraMap 包含token、page、limit等参数的映射。
     * @return 返回查询结果，如果查询异常或分页参数缺失，则返回失败响应。
     */
    @Override
    public ResponseResult get(Map<String, Object> paraMap) {
        try {
            // 从参数中获取token，用于识别用户
            String token = (String) paraMap.get("token");
            // 通过token从Redis中获取用户ID
            String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
            // 将用户ID添加到参数映射中
            paraMap.put("userId", userId);

            // 检查page和limit参数是否存在
            if (null == paraMap.get("page") && null == paraMap.get("limit")) {
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }

            // 将参数对象转换为Map，以适应MyBatis的查询需求
            // 将对象转换为map
            Map data = ParseMapUtils.beanToMap(paraMap);

            // 执行分页查询，根据用户ID获取收藏的店铺信息
            // 分页查询数据
            IPage<UserFavoritesShopEntity> page = this.baseMapper.selectPage(new Query<UserFavoritesShopEntity>().getPage(data), new LambdaQueryWrapper<UserFavoritesShopEntity>()
                    .eq(UserFavoritesShopEntity::getUserId, userId)
                    .groupBy(UserFavoritesShopEntity::getShopId)
                    .orderByDesc(UserFavoritesShopEntity::getFavoritesShopTime));

            // 如果查询结果为空，则返回空集合提示信息
            // 查询是否有数据
            if (CollectionUtils.isEmpty(page.getRecords())) {
                return ResponseResult.fail(BizCodeEnume.COLLECTION_IS_EMPTY);
            }

            // 处理查询结果，封装为前端需要的格式
            List<ShopIndexVO> results = new ArrayList<ShopIndexVO>();
            for (UserFavoritesShopEntity userFavoriteShop : page.getRecords()) {
                // 准备查询店铺详情的参数
                HashMap<String, String> selectMap = new HashMap<String, String>();
                selectMap.put("orderType", "2");
                selectMap.put("currentPage", "1");
                selectMap.put("pageSize", "5");
                selectMap.put("shopId", userFavoriteShop.getShopId().toString());

                // 调用商品服务获取店铺详情
                ResponseResult resultResp = goodsServiceClient.getDetailByShopId(selectMap);
                if (null != resultResp.getData()) {
                    // 将获取的店铺详情转换为前端需要的VO对象
                    ShopIndexVO shopIndexQueryResult = JSON.parseObject(JSON.toJSONString(resultResp.getData()), ShopIndexVO.class);
                    results.add(shopIndexQueryResult);
                }
            }

            // 构建分页响应结果
            IPage<ShopIndexVO> resultPage=new Page<>();
            resultPage.setTotal(page.getTotal());
            resultPage.setRecords(results);
            resultPage.setPages(page.getPages());
            return ResponseResult.success(BizCodeEnume.SUCCESSFULLY_QUERIED_FAVORITE_STORES.getMsg(), PageUtils.getPage(resultPage));
        } catch (Exception e) {
            // 捕获异常，打印异常信息，并返回失败响应
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据传入的参数删除用户收藏的店铺。
     * @param paraMap 包含token、shopIds等参数的映射。
     * @return 如果删除成功，则返回成功响应；如果参数无效，则返回失败响应。
     */
    @Override
    public ResponseResult delete(Map<String, Object> paraMap) {
        String token = (String) paraMap.get("token");
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
        paraMap.put("userId", userId);

        /** 从参数中获取要删除的店铺ID列表 */
        @SuppressWarnings("unchecked")
        List<String> shopIds = (List<String>) paraMap.get("shopIds");
        if (null == shopIds) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }

        // 分别删除指定的店铺，并更新业务服务中的收藏状态
        for (String shopId : shopIds) {
            this.baseMapper.delete(new LambdaQueryWrapper<UserFavoritesShopEntity>()
                    .eq(UserFavoritesShopEntity::getShopId,shopId)
                    .eq(UserFavoritesShopEntity::getUserId,userId));


            Map<String, String> delMap = new HashMap<String, String>();
            delMap.put("shopId", shopId);
            delMap.put("type", "2");
            merchantServiceClient.updateShopCollect(delMap);
        }
        return ResponseResult.success();
    }

    /**
     * 根据店铺ID删除用户收藏的该店铺。
     * @param shopId 店铺ID。
     * @return 如果删除成功，则返回成功响应；如果删除失败，则返回失败响应。
     */
    @Override
    public ResponseResult delFavoritesByShopId(Integer shopId) {
        // 执行删除操作
        int count = this.baseMapper.delete(new LambdaQueryWrapper<UserFavoritesShopEntity>()
                .eq(UserFavoritesShopEntity::getShopId, shopId));
        if (count > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.DELETE_ERROR.getCode(), BizCodeEnume.DELETE_ERROR.getMsg());
    }

    @Override
    public void deleteUserFavoritesShop(String shopId) {
        this.baseMapper.deleteUserFavoritesShop(shopId);
    }
}
