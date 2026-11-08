package com.houjiang.payment.service.impl;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.handle.PayStrategyHandleContext;
import com.houjiang.payment.handle.PayStrategyHandleFactory;
import com.houjiang.payment.service.PayEntranceService;
import com.houjiang.payment.service.PayStrategyHandleService;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/27 17:36
 */
@Service
public class PayEntranceServiceImpl implements PayEntranceService {

    final Logger log = LoggerFactory.getLogger(PayEntranceServiceImpl.class);

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult payMentUnifyInlet(Map<Object, Object> payInfo, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        //初始化支付类型
        PayStrategyHandleFactory.init();

        //策略类,指定走具体哪一个策略
        PayStrategyHandleContext context = new PayStrategyHandleContext();

        //根据type判断当前支付类型
        PayStrategyHandleService handle = PayStrategyHandleFactory.getPayStrategyHandle(payInfo);

        //设置统一调用接口
        context.setPayStrategyHandleService(handle);


        //保存全局事务id
        ResponseResult result =null;
        payInfo.put("xid", RootContext.getXID());
        try {
            //根据支付类型执行指定的支付接口
            result = context.getMethod(payInfo, request, response);




        }catch (Exception e){
            e.printStackTrace();
            //异常回滚数据
            String token = (String) payInfo.get("token");
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            //获取缓存订单支付key
            if (RedisUtil.exists(HouConfig.DETERMINEWHETHERTOPAY + "-" + payInfo.get("orderIdList")+ "-" + userInfo.getUserId() + "-" + payInfo.get("type").toString())) {
                //删除支付失败时,缓存中的key
                RedisUtil.remove(HouConfig.DETERMINEWHETHERTOPAY + "-" + payInfo.get("orderIdList")+ "-" + userInfo.getUserId() + "-" + payInfo.get("type").toString());
            }
            // 手动回滚事务
            log.info("---"+ RootContext.getXID());
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            log.info("balancePaymentErrorResults:" + e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }finally {

        }

        //返回执行结果
        return result;
    }
}
