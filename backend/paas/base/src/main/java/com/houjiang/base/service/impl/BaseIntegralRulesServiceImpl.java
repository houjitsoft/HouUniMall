package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.IntegralRulesDTO;
import com.houjiang.base.entity.BaseIntegralRulesEntity;
import com.houjiang.base.mapper.BaseIntegralRulesMapper;
import com.houjiang.base.service.BaseIntegralRulesService;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

/**
 * @author Amos
 */
@Service
public class BaseIntegralRulesServiceImpl extends ServiceImpl<BaseIntegralRulesMapper, BaseIntegralRulesEntity>
    implements BaseIntegralRulesService{

    /**
     * 查询积分规则信息。
     * 通过调用baseMapper的selectOne方法，查询第一条积分规则数据。
     * 如果查询过程中发生异常，打印异常堆栈跟踪。
     *
     * @return ResponseResult 包含查询结果的响应对象。
     */
    @Override
    public ResponseResult queryIntegralRules() {
        BaseIntegralRulesEntity integralRules = new BaseIntegralRulesEntity();
        try {
            integralRules = this.baseMapper.selectOne(new QueryWrapper<BaseIntegralRulesEntity>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success(integralRules);
    }

    /**
     * 添加或更新积分规则。
     * 首先，通过解析token获取用户信息。
     * 如果用户信息存在，根据传入的param参数的id值判断是进行添加操作还是更新操作。
     * 如果id为null，调用baseMapper的integralRulesAdd方法添加积分规则。
     * 如果id不为null，使用LambdaUpdateWrapper更新相应的积分规则字段。
     * 操作完成后，返回成功的响应结果。
     * 如果用户信息不存在，返回令牌非法的错误响应结果。
     *
     * @param param IntegralRulesDTO 包含积分规则信息的参数对象。
     * @return ResponseResult 表示操作结果的响应对象。
     */
    @Override
    public ResponseResult integralRulesAddOrUpdate(IntegralRulesDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            if (null == param.getId()) {
                this.baseMapper.integralRulesAdd(param);
            } else {
                this.baseMapper.update(null, new LambdaUpdateWrapper<BaseIntegralRulesEntity>()
                        .set(BaseIntegralRulesEntity::getRuleLogin, param.getRuleLogin())
                        .set(BaseIntegralRulesEntity::getRuleRegister, param.getRuleRegister())
                        .set(BaseIntegralRulesEntity::getRuleConsume, param.getRuleConsume())
                        .set(BaseIntegralRulesEntity::getId, param.getId()));
            }
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
}




