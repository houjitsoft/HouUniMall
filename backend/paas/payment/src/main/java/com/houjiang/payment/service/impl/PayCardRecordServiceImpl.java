package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.PayCardRecordDTO;
import com.houjiang.payment.entity.PayCardEntity;
import com.houjiang.payment.entity.PayCardRecordEntity;
import com.houjiang.payment.mapper.PayCardMapper;
import com.houjiang.payment.mapper.PayCardRecordMapper;
import com.houjiang.payment.service.PayCardRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PayCardRecordServiceImpl
 * @Description 生成充值卡信息记录表
 * @Author Amos
 * @Date 2023-05-31 15:35:50
 * @Version 1.0
 * @author Amos
 */
@Service
public class PayCardRecordServiceImpl extends ServiceImpl<PayCardRecordMapper, PayCardRecordEntity>
        implements PayCardRecordService {
    @Autowired
    private PayCardMapper payCardMapper;

    @Override
    public ResponseResult query(PayCardRecordDTO payCardRecordDto) {
        // 尝试根据令牌获取用户信息，如果第一次获取失败，可能是因为令牌解析工具的问题，尝试重新获取一次
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(payCardRecordDto.getToken());
        if (userInfo == null) {
            userInfo = ParseTokenUtils.getPlatformUser(payCardRecordDto.getToken());
        }
        // 如果用户信息不为空，则尝试根据用户信息和支付卡记录DTO查询支付卡记录
        if (null != userInfo) {
            try {
                // 将支付卡记录DTO转换为Map，以便于后续的查询操作
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(payCardRecordDto);
                // 执行分页查询，查询符合条件的支付卡记录
                //分页查询数据
                IPage<PayCardRecordEntity> page = this.baseMapper.query(new Query<PayCardRecordEntity>().getPage(data), payCardRecordDto);

                // 返回查询结果，成功获取数据
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 如果在转换过程中发生异常，打印异常信息
                e.printStackTrace();
            }

        }
        // 如果用户信息为空或者查询过程中发生异常，则返回令牌非法的错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult add(PayCardRecordDTO param) {
        // 解析令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (userInfo != null) {
            // 获取当前时间，用于设置支付卡和记录的创建时间
            // 创建时间
            Date creatTime = DateUtils.getNowDate();
            // 初始化支付卡实体列表
            List<PayCardEntity> pay = new ArrayList<PayCardEntity>();

            // 循环生成指定数量的支付卡
            for (int y = 1; y <= param.getCardNumber(); y++) {
                // 生成支付卡序号，保证支付卡号的唯一性
                String str = new DecimalFormat("0000").format(y);
                // 生成支付卡密码
                // 随机密码
                String password = StringRandomUtil.getStringRandom(8);
                // 生成支付卡号
                // 随机卡号
                String cardNum = StringRandomUtil.getStringRandom(4) + str + StringRandomUtil.getStringNum();

                // 创建支付卡实体，并设置相关属性
                PayCardEntity num = new PayCardEntity();
                num.setCardDesc(param.getCardDesc());
                num.setCardNum(cardNum);
                num.setCreatTime(creatTime);
                num.setEtime(param.getEtime());
                num.setPassword(password);
                num.setStatut(CommonType.other.NOT_USED.getCode());
                num.setStime(param.getStime());
                num.setTotalPrice(param.getTotalPrice());
                num.setUseName(param.getUseName());
                pay.add(num);
            }

            // 尝试添加支付卡信息到数据库，如果失败则回滚事务并返回操作失败响应
            try {
                payCardMapper.addPayCardtew(pay);
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED.getCode(), BizCodeEnume.OPERATION_FAILED.getMsg());
            }

            // 创建支付卡记录实体，并设置相关属性
            PayCardRecordEntity payCardRecord = new PayCardRecordEntity();
            payCardRecord.setCardCreator(param.getUseName());
            payCardRecord.setTotalPrice(param.getTotalPrice());
            payCardRecord.setCardDesc(param.getCardDesc());
            payCardRecord.setCreatTime(creatTime);
            payCardRecord.setCardNumber(param.getCardNumber());
            payCardRecord.setCardStart(pay.get(0).getId());
            payCardRecord.setCardEnd(pay.get(0).getId() + param.getCardNumber() - 1);

            // 尝试添加支付卡记录到数据库，如果失败则回滚事务并返回操作失败响应
            try {
                this.baseMapper.insert(payCardRecord);
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED.getCode(), BizCodeEnume.OPERATION_FAILED.getMsg());
            }

            // 操作成功，返回成功响应
            return ResponseResult.success();
        }

        // 令牌非法，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

}
