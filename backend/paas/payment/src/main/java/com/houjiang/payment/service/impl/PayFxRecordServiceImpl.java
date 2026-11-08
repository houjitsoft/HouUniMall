package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.PayFxRecordEntity;
import com.houjiang.payment.mapper.PayFxRecordMapper;
import com.houjiang.payment.service.PayFxRecordService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayFxRecordServiceImpl extends ServiceImpl<PayFxRecordMapper, PayFxRecordEntity>
    implements PayFxRecordService{

    /**
     * 根据传入的参数查询用户的支付记录。
     *
     * @param param 包含查询参数的Map，其中必须包含token用于用户身份验证。
     * @return 返回查询结果，如果token无效，则返回失败响应。
     */
    @Override
    public ResponseResult query(Map<String, Object> param) {
        // 通过token获取用户信息，用于后续的用户身份验证。
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息获取失败（token无效），则返回错误响应。
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 将用户ID添加到查询参数中，用于过滤查询结果。
        param.put("userId", ui.getUserId());

        // 根据用户ID和记录状态查询支付记录，并按创建时间降序排序。
        IPage<PayFxRecordEntity> payFxRecordList = this.page(new Query<PayFxRecordEntity>().getPage(param),
                new LambdaQueryWrapper<PayFxRecordEntity>()
                        .eq(PayFxRecordEntity::getUserId, ui.getUserId())
                        .eq(PayFxRecordEntity::getRecordStatus, 2)
                        .eq(PayFxRecordEntity::getTradeTypeId, 2)
                        .orderByDesc(PayFxRecordEntity::getCreateTime));

        // 返回查询结果的分页信息。
        return ResponseResult.success(PageUtils.getPage(payFxRecordList));
    }
}




