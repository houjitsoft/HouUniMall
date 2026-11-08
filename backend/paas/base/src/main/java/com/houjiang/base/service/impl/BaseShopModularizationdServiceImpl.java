package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseShopModularizationdEntity;
import com.houjiang.base.feign.BusinessClient;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseShopModularizationdMapper;
import com.houjiang.base.service.BaseShopModularizationdService;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.goods.ModularizationdGoodsVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseShopModularizationdServiceImpl extends ServiceImpl<BaseShopModularizationdMapper, BaseShopModularizationdEntity>
    implements BaseShopModularizationdService{

    @Autowired
    private BaseModularizationdServiceImpl baseModularizationdServiceImpl;

    @Autowired
    private BusinessClient businessClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private RedisCache redisCache;


    // 注解解释略

    /**
     * 通过令牌验证用户权限并进行底部导航的添加或更新操作
     * @param param 包含导航信息的DTO对象
     * @return 操作结果的ResponseResult对象
     */
    @Override
    public ResponseResult bottomNavigation(ModularizationdDTO param) {
        try {
            // 从Redis中根据令牌获取用户信息
            UserInfoVO userInfo = null;
            String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + param.getToken());
            if (StringUtils.isNotBlank(userInfoStr)) {
                userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
                if(null == userInfo || null == userInfo.getShopId()){
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
                }
                // 验证商家ID是否存在
                Integer shopId = Integer.valueOf(userInfo.getShopId());

                // 设置参数中的商家ID和其他必要信息
                param.setShopId(shopId);
                param.setPageType(1);
                param.setPageStart(1);
                param.setPageIsIndex(3);

                // 判断是新增还是更新操作
                if (null == param.getPageId() || 0 == param.getPageId()) {
                    // 新增操作
                    String UUidPageId = UUIDUtil.getUUID1();
                    param.setPageId(Integer.valueOf(UUidPageId.substring(0, 7)));
                    int result = this.baseMapper.add(param);
                    if (0 == result) {
                        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                    }
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                } else {
                    // 更新操作
                    int result = this.baseMapper.updateInfo(param);
                    if (0 == result) {
                        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                    }
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据令牌验证用户权限并查询底部导航信息
     * @param param 包含查询参数的Map对象
     * @return 查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult querybottomNavigation(Map<String, Object> param) {
        UserInfoVO userInfo = null;
        // 从Redis中根据令牌获取用户信息
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + param.get("token").toString());
        if (StringUtils.isNotBlank(userInfoStr)) {
            userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            // 验证商家ID是否存在
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            HashMap<String, Object> map = new HashMap<>();

            // 根据商家ID查询底部导航信息
            BaseShopModularizationdEntity list = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseShopModularizationdEntity>()
                    .eq(BaseShopModularizationdEntity::getShopId, shopId)
                    .eq(BaseShopModularizationdEntity::getPageIsIndex, 3));
            if (list != null) {
                map.put("pagedata", list.getPageData());
                map.put("pageId", list.getPageId());
                return ResponseResult.success(map);
            } else {
                return ResponseResult.fail(BizCodeEnume.SELECT_DATA_NULL);
            }
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 根据shopId查询底部导航信息
     * @param param 包含查询参数的Map对象
     * @return 查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult indexQueryBottomNavigation(Map<String, Object> param) {
        if (null != param.get("shopId")) {
            Integer shopId = Integer.valueOf(param.get("shopId").toString());
            HashMap<String, Object> map = new HashMap<>();

            // 根据商家ID查询底部导航信息
            BaseShopModularizationdEntity list = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseShopModularizationdEntity>()
                    .eq(BaseShopModularizationdEntity::getShopId, shopId)
                    .eq(BaseShopModularizationdEntity::getPageIsIndex, 3));
            if (list != null) {
                map.put("pagedata", list.getPageData());
                map.put("pageId", list.getPageId());
                return ResponseResult.success(map);
            } else {
                return ResponseResult.fail(BizCodeEnume.SELECT_DATA_NULL);
            }
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 更新首页导航信息
     * @param map 包含更新信息的Map对象
     * @return 更新结果的ResponseResult对象
     */
    @Override
    public ResponseResult modularizationdUpdateIndex(Map<String, Object> map) {
        if (null == map && null == map.get("shopId")) {
            return ResponseResult.fail(2, "shopId不能为空");
        }
        try {
            // 根据shopId查询首页导航信息
            ModularizationdDTO modularizationdParam = this.baseMapper.selectModularizationdByStart((Integer) map.get("shopId"));
            // 查询关联的商品信息
            List<ModularizationdGoodsVO> modularizationdGoodsParams = queryModularizationdGoodsInfo(modularizationdParam.getGoodsAllIdJson());
            modularizationdParam.setGoodsList(modularizationdGoodsParams);
            // 查询商家基本信息
            GdsShopDetailResultVO shopBaseEntity = businessClient.queryShopinfoByShopId((Integer) map.get("shopId"));
            modularizationdParam.setShopName(shopBaseEntity.getShopName());
            modularizationdParam.setShopCollect(shopBaseEntity.getShopCollect());
            modularizationdParam.setShopLogo(shopBaseEntity.getShopLogo());
            return ResponseResult.success(modularizationdParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(2, "当前商家没有设置首页");
    }

    /**
     * 增加或更新模块化数据。
     * @param param 包含模块化信息的DTO对象。
     * @return 操作结果的ResponseResult对象。
     */
    @Override
    public ResponseResult ModularizationdAddOrUPdate(ModularizationdDTO param) {
        Integer result = null;
        try {
            // 设置默认页面类型
            if(null == param.getPageType()){
                param.setPageType(0);
            }
            // 从Redis中获取用户信息
            UserInfoVO userInfo =null;
            String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER+param.getToken());
            if(StringUtils.isNotBlank(userInfoStr)) {
                userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

                // 检查用户是否有店铺
                if (userInfo.getShopId() == null) {
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
                }
                Integer shopId = Integer.valueOf(userInfo.getShopId());
                param.setShopId(shopId);

                // 处理新增逻辑
                if (null == param.getPageId()) {
                    // 设置商品ID的JSON字符串
                    if (null != param.getGoodsAllId() && param.getGoodsAllId().size() > 0) {
                        param.setGoodsAllIdJson(JsonUtils.toString(param.getGoodsAllId()));
                    }
                    // 设置页面起始位置
                    if (null != param.getPageIsIndex() && 2 == param.getPageIsIndex()) {
                        param.setPageStart(0);
                    } else {
                        param.setPageStart(0);
                    }
                    // 生成并设置页面ID
                    String UUidPageId=UUIDUtil.getUUID1();
                    param.setPageId(Integer.valueOf(UUidPageId.substring(0,7)));
                    result =  this.baseMapper.add(param);
                    // 返回结果处理
                    if (0 == result) {
                        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                    }
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                } else {
                    // 设置商品ID的JSON字符串
                    if (null != param.getGoodsAllId() && param.getGoodsAllId().size() > 0) {
                        param.setGoodsAllIdJson(JsonUtils.toString(param.getGoodsAllId()));
                    }
                    result = this.baseMapper.updateInfo(param);
                    // 返回结果处理
                    if (0 == result) {
                        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                    }
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                }
            }else{
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(e.getMessage());
        }

    }

    /**
     * 查询所有模块化数据。
     * @param param 包含查询条件的DTO对象。
     * @return 查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryModularizationdAll(ModularizationdDTO param) {
        try {
            // 从Redis中获取用户信息
            UserInfoVO userInfo =null;
            String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER+param.getToken());
            if(StringUtils.isNotBlank(userInfoStr)){
                userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

                // 检查用户是否有店铺
                if(userInfo.getShopId()==null ){
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
                }
                Integer shopId = Integer.valueOf(userInfo.getShopId());
                param.setShopId(shopId);

                Map data= ParseMapUtils.beanToMap(param);
                IPage<BaseShopModularizationdEntity> page=this.baseMapper.queryModularizationdAll(new Query<BaseShopModularizationdEntity>().getPage(data),
                        param);

                // 处理二级页面的URL设置
                if(null != param.getPageIsIndex() && 2 == param.getPageIsIndex()){
                    if(page.getRecords().size()>0){
                        for (BaseShopModularizationdEntity modularizationdParam : page.getRecords()) {
                            if(null != modularizationdParam.getPageStart() && 0 == modularizationdParam.getPageStart()){
                                modularizationdParam.setPageUrl("/store/shopDiy/shopDiy?pageId="+modularizationdParam.getPageId());
                            }
                        }
                    }
                }
                return  ResponseResult.success(PageUtils.getPage(page));
            }else{
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据页面ID查询模块化数据。
     * @param param 包含页面ID的DTO对象。
     * @return 查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryModularizationdShopById(ModularizationdDTO param) {
        if(null == param.getPageId()){
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(),BizCodeEnume.MISSING_PARAMETER.getMsg());
        }
        ModularizationdDTO  modularizationdParam =  this.baseMapper.queryModularizationdById(param);
        List<ModularizationdGoodsVO> modularizationdGoodsParams = queryModularizationdGoodsInfo(modularizationdParam.getGoodsAllIdJson());
        modularizationdParam.setGoodsList(modularizationdGoodsParams);
        return ResponseResult.success(modularizationdParam);
    }

    /**
     * 根据模块化DTO删除模块化数据
     * @param param 删除操作的参数DTO，包含必要信息如token和pageId
     * @return 删除操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult modularizationdDelete(ModularizationdDTO param) {
        // 验证token有效性
        if(null != param && null != param.getToken()){
            UserInfoVO userInfo =null;
            userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 检查用户是否有权限
            if(userInfo.getShopId()==null ){
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
            Integer result = null;
            try {
                // 检查pageId是否为空
                if(null == param.getPageId()){
                    return ResponseResult.fail(0, "pageid 不能为null");
                }
                result =  this.baseMapper.deleteById(param.getPageId());
                // 判断删除结果
                if(0 == result){
                    return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
                }
                return ResponseResult.fail(BizCodeEnume.DELETE_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.error(e.getMessage());
            }

        }
        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
    }

    /**
     * 根据模块化DTO更新模块化数据的启动状态
     * @param param 更新操作的参数DTO，包含必要信息如token、pageId和pageStart
     * @return 更新操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult modularizationdUpdateStart(ModularizationdDTO param) {
        // 验证token有效性
        if(null != param && null != param.getToken()) {
            UserInfoVO userInfo =null;
            userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 检查用户是否有权限
            if(userInfo.getShopId()==null ){
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            Integer result = null;
            try {
                // 检查pageId和pageStart是否为空
                if (null == param.getPageId()) {
                    return ResponseResult.fail(0, "pageid 不能为null");
                }
                if (null == param.getPageStart()) {
                    return ResponseResult.fail(0, "pageStart 不能为null");
                }
                // 先取消其他设置为首页的页面
                this.baseMapper.update(null,new LambdaUpdateWrapper<BaseShopModularizationdEntity>()
                                .eq(BaseShopModularizationdEntity::getPageIsIndex,"1")
                                .eq(BaseShopModularizationdEntity::getShopId,shopId)
                                .set(BaseShopModularizationdEntity::getPageStart,"2"));
                // 更新指定页面的启动状态
                result =  this.baseMapper.update(null,new LambdaUpdateWrapper<BaseShopModularizationdEntity>()
                            .eq(BaseShopModularizationdEntity::getPageId,param.getPageId())
                            .eq(BaseShopModularizationdEntity::getShopId,shopId)
                            .set(BaseShopModularizationdEntity::getPageStart,param.getPageStart()));
                // 判断更新结果
                if (0 == result) {
                    return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                }
                return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseResult.error(e.getMessage());
            }
        }
        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
    }

    /**
     * 查询模块化商品信息
     * @param goodsAllIdJson 商品ID的JSON字符串
     * @return 模块化商品信息的列表
     */
    public List<ModularizationdGoodsVO> queryModularizationdGoodsInfo(String goodsAllIdJson){
        List<ModularizationdGoodsVO> list = new ArrayList<>();
        if(null == goodsAllIdJson){
            return null;
        }
        List<String> goodsAllIdJsons = JsonUtils.string2Obj(goodsAllIdJson, List.class, String.class);
        for (String goodsId : goodsAllIdJsons) {
            // 根据商品ID查询商品信息
            ModularizationdGoodsVO  modularizationdGoodsParam = goodsClient.queryModularizationdGoodsInfo(goodsId);
            Map<String,Object> activityMap = baseModularizationdServiceImpl.dealActivityStatus(Integer.parseInt(goodsId));
            if(null == modularizationdGoodsParam){
                modularizationdGoodsParam = new ModularizationdGoodsVO();
                //设置默认下架状态
                modularizationdGoodsParam.setGoodsStart(2);
                modularizationdGoodsParam.setGoodsId(Integer.valueOf(goodsId));
            }else{
                //设置默认上架状态
                modularizationdGoodsParam.setGoodsStart(1);
            }
            // 根据活动状态设置商品活动信息
            if(null != activityMap.get("activityStatus") && 1 == (Integer) activityMap.get("activityStatus")){
                modularizationdGoodsParam.setActivityStatus(1);
                if(null != activityMap.get("activityPrice")){
                    modularizationdGoodsParam.setCommonPrice((BigDecimal) activityMap.get("activityPrice"));
                    modularizationdGoodsParam.setStartTime(activityMap.get("startTime").toString());
                    modularizationdGoodsParam.setEndTime( activityMap.get("endTime").toString());
                }
            }else {
                modularizationdGoodsParam.setActivityStatus(2);
            }
            list.add(modularizationdGoodsParam);
        }
        return list;
    }
}




