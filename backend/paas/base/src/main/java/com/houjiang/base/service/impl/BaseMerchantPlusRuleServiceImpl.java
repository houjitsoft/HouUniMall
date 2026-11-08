package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseMerchantPlusRuleEntity;
import com.houjiang.base.mapper.BaseMerchantPlusRuleMapper;
import com.houjiang.base.service.BaseMerchantPlusRuleService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.secverifyapiutil.BaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * plus会员规则表(BaseMerchantPlusRule)
 *
 * @since 2023-05-15 15:00:26
 * @author Amos
 */
@Service
public class BaseMerchantPlusRuleServiceImpl extends ServiceImpl<BaseMerchantPlusRuleMapper, BaseMerchantPlusRuleEntity>
        implements BaseMerchantPlusRuleService {

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    @Autowired
    private BaseMerchantPlusRuleMapper baseMerchantPlusRuleMapper;

    /**
     * 查询商家+规则
     * @return 返回查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult queryMerchantPlusRule() {
        try {
            Integer integer = baseMerchantPlusRuleMapper.queryMerchantPlusRule();
            // 无记录时按关闭处理，避免前端 PLUS 入口异常
            return ResponseResult.success(integer == null ? 0 : integer);
        } catch (Exception e) {
            // 缺表或库异常时软关闭，避免包装成 11009「数据保存失败」打断商城页
            return ResponseResult.success(0);
        }
    }

    /**
     * 查询Plus用户信息
     * @return 返回查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult selectPlusUser() {
        String s = this.baseMapper.selectplusrule1();
        return ResponseResult.success(s);
    }

    /**
     * 获取业务规则信息
     * @return 返回业务规则的ResponseResult对象
     */
    @Override
    public ResponseResult getBusinessRule() {
        String s = this.baseMapper.getBusinessRule();
        return ResponseResult.success(s);
    }

    /**
     * 根据传入的参数查询规则信息
     * @param map 包含查询参数的Map对象
     * @return 返回查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult ruleselect(Map<String, Object> map) {
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token").toString());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        BaseMerchantPlusRuleEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMerchantPlusRuleEntity>());
        return ResponseResult.success(entity);
    }

    /**
     * 更新商家+规则信息
     * @param map 包含更新参数的Map对象
     * @return 返回操作结果的ResponseResult对象
     */
    @Override
    public ResponseResult updateplusrule(Map<String, Object> map) {
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token").toString());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 对传入的参数进行处理，将特定值转换为空字符串或解码Base64
        if (map.get("plusPropaganda").equals("0")) {
            map.put("plusPropaganda","");
        }
        if (map.get("plusSigns").equals("0")) {
            map.put("plusSigns","");
        }
        if (map.get("plusMemberagreement") != null && !map.get("plusMemberagreement").equals("")) {
            String data = String.valueOf(map.get("plusMemberagreement"));
            map.put("plusMemberagreement", BaseUtils.getFromBase64(data));
        }
        if (map.get("plusMerchantnotice") != null && !map.get("plusMerchantnotice").equals("")) {
            String data = String.valueOf(map.get("plusMerchantnotice"));
            map.put("plusMerchantnotice", BaseUtils.getFromBase64(data));
        }

        Integer integer = 0;
        // 根据条件判断是更新还是插入规则信息
        if (map.get("plusRuleid") != null && !"".equals(map.get("plusRuleid"))) {
            integer = this.baseMapper.updateplusrule(map);
        } else {
            integer = this.baseMapper.insertplusrule(map);
        }

        // 根据操作结果返回相应的ResponseResult对象
        if (integer > 0) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     *  查询plus规则
     * @return
     */
    @Override
    public Integer selectplus() {
        Integer result = this.baseMapper.selectplus();
        return result;
    }
}

