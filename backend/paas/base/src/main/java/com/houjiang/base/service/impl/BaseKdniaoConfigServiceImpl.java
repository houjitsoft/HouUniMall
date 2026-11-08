package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BasekdiniaoConfigDTO;
import com.houjiang.base.entity.BaseKdniaoConfigEntity;
import com.houjiang.base.mapper.BaseKdniaoConfigMapper;
import com.houjiang.base.service.BaseKdniaoConfigService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseKdniaoConfigServiceImpl extends ServiceImpl<BaseKdniaoConfigMapper, BaseKdniaoConfigEntity>
    implements BaseKdniaoConfigService{

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 查询快递鸟配置信息
     * 通过调用这个方法来获取当前配置的快递鸟配置信息。
     * @return ResponseResult 包含查询结果的响应对象
     */
    @Override
    public ResponseResult queryConfigFeign() {
        // 根据状态查询一条快递鸟配置信息
        BaseKdniaoConfigEntity kdiNiaoConfig = this.baseMapper.selectOne(null);
        // 将查询到的配置信息存储到Redis中
        redisCache.setCacheObject(HouConfig.KDINIAO_CONFIG, JSONObject.toJSONString(kdiNiaoConfig));
        // 返回查询结果
        return ResponseResult.success(kdiNiaoConfig);
    }

    /**
     * 更新快递鸟配置信息
     * 该方法用于更新快递鸟的配置信息，需要用户提供有效的token以验证权限。
     * @param basekdiniaoConfigReq 包含更新信息的请求对象
     * @return ResponseResult 表示操作结果的响应对象
     */
    @Override
    public ResponseResult updateKdiniaoConfig(BasekdiniaoConfigDTO basekdiniaoConfigReq) {
        // 验证token是否有效
        if (null != basekdiniaoConfigReq && null != basekdiniaoConfigReq.getToken()) {
            String token = basekdiniaoConfigReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            // 如果token有效，更新配置信息
            if (null != userInfo) {
                this.baseMapper.updateKdiniaoConfig(basekdiniaoConfigReq);
                // 查询并更新Redis中的配置信息
                BaseKdniaoConfigEntity kdiNiaoConfig = this.baseMapper.selectOne(null);
                redisCache.setCacheObject(HouConfig.KDINIAO_CONFIG, JSONObject.toJSONString(kdiNiaoConfig));
                // 返回成功结果
                return ResponseResult.success();
            }
            // 如果token无效，返回错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 如果请求参数无效，返回错误信息
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 查询快递鸟配置信息
     * 该方法用于查询快递鸟的配置信息，需要用户提供有效的token以验证权限。
     * @param param 包含查询参数的Map对象，其中必须包含token
     * @return ResponseResult 包含查询结果的响应对象
     */
    @Override
    public ResponseResult queryConfig(Map<String, Object> param) {
        // 从请求参数中获取token
        Object tokenObj = param.get("token");
        if (tokenObj == null) {
            return ResponseResult.error("token参数不能为空");
        }
        String token = tokenObj.toString();
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
        // 验证token是否有效
        if (null != userInfo) {
            // 从Redis中获取配置信息
            String kdiniaoConfigStr = (String) redisCache.getCacheObject(HouConfig.KDINIAO_CONFIG);
            BaseKdniaoConfigEntity kdiNiaoConfig = JSONObject.parseObject(kdiniaoConfigStr, BaseKdniaoConfigEntity.class);
            // 每次查询时都重新从数据库获取最新的配置信息，并更新Redis中的值
            if (true) {
                kdiNiaoConfig = this.baseMapper.selectOne(null);
                redisCache.setCacheObject(HouConfig.KDINIAO_CONFIG, JSONObject.toJSONString(kdiNiaoConfig));
            }
            // 返回查询结果
            return ResponseResult.success(kdiNiaoConfig);
        }
        // 如果token无效，返回错误信息
        return ResponseResult.fail(CommonStatusEnum.TOKEN_ERROR.getCode(), CommonStatusEnum.TOKEN_ERROR.getValue());
    }

    /**
     * 根据商店ID查询物流配置信息。
     *
     * @param paraMap 包含商店ID的参数映射。
     * @return 如果查询成功，返回包含物流配置的响应结果；如果查询失败或参数为空，返回失败的响应结果。
     */
    @Override
    public ResponseResult queryShopKdNiao(Map<String, Object> paraMap) {
        // 检查shopId参数是否为空
        if (paraMap.get("shopId") == null || paraMap.get("shopId").equals("")) {
           return ResponseResult.fail(2,"请求参数不能为空");
        }
        QueryWrapper<BaseKdniaoConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id",paraMap.get("shopId"));
        BaseKdniaoConfigEntity eqbk = this.baseMapper.selectOne(queryWrapper);
        // 如果查询结果为空，返回失败的响应结果
        if (eqbk == null) {
            return ResponseResult.fail(2,"查询为空！");
        }
        return ResponseResult.success(eqbk);
    }


    /**
     * 根据商店ID删除物流配置参数。
     *
     * @param paraMap 包含商店ID的参数映射。
     * @return 如果删除成功，返回成功的响应结果并包含删除的行数；如果删除失败或参数为空，返回失败的响应结果。
     */
    @Override
    public ResponseResult deleteShopkdniaoParam(Map<String, Object> paraMap) {
        // 检查shopId参数是否为空
        if (paraMap.get("shopId") == null || paraMap.get("shopId").equals("")) {
            return ResponseResult.fail(2,"请求参数不能为空");
        }
        QueryWrapper<BaseKdniaoConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id",paraMap.get("shopId"));
        int delete = this.baseMapper.delete(queryWrapper);
        return ResponseResult.success(delete);
    }
}




