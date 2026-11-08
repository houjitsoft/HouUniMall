package com.houjiang.job.job;


import com.alibaba.fastjson.JSON;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;
import com.houjiang.job.feign.BaseServiceClient;
import com.houjiang.job.feign.UserServiceClient;
import com.houjiang.job.mapper.ActivityNoneListMapper;
import com.houjiang.job.mapper.OrderConsignerMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UpdateVirtualOrderTimeJob
 * @Description 修改虚拟订单的过期时间
 * @Author Amos
 * @Date 2023/5/17 14:33
 * @Version 1.0
 * @author Amos
 */
public class UpdateVirtualOrderTimeJob implements BaseJob {

    @Autowired
    private OrderConsignerMapper orderConsignerMapper;

    @Autowired
    private ActivityNoneListMapper activityNoneListMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ResponseResult result = baseServiceClient.queryGoodsVkSet();
        GoodsVkSetVO goodsVkSet = JSON.parseObject(JSON.toJSONString(result.getData()), GoodsVkSetVO.class);
        //修改虚拟商品过期订单
        List<String> orders = orderConsignerMapper.queryVirtualOrderTime();
        if (null != orders && orders.size() > 0) {
            for (String order : orders) {
                orderConsignerMapper.upVirtualOrder(order);
                //判断是否是分销订单
                Integer num = orderConsignerMapper.selectByPrimaryKey(order);
                if (null != num) {
                    orderConsignerMapper.updateOrderAfterSaleTime(order, DateUtils.getAfterDate(goodsVkSet.getAfterSalesDays()));
                }
                //增加积分
                ResponseResult resultResp = baseServiceClient.queryIntegralRules();
                Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();
                Map<String, Object> map = orderConsignerMapper.selectUserMoneyByOrderId(order);
                BigDecimal money = (BigDecimal) map.get("orderPaymentAmount");
                //修改会员积分和会员等级
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                BigDecimal jifen = money.multiply(new BigDecimal(integralRules.get("ruleConsume")));
                long l = jifen.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                ArrayList<String> list = new ArrayList<>();
                //当前逻辑存在问题，需要将积分加到员工账号上
//                    if (ui.getIsEmployee() == 1) {
//                        //当是员工账号是积分归属企业主账号
//                        userId = userServiceClient.getPrimaryAccountId(userId);
//                    }
                list.add(map.get("buyerUserId").toString());
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral((int) l);
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(CommonType.commonStatus.YES.getCode());
                userServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                // 初始化一个Map用于存储积分日志的相关参数
                Map<String, Object> pointParam = new HashMap<>();
                // 设置积分日志的类型为获取积分
                pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode());
                // 设置积分日志的分类为购物
                pointParam.put("ClassId", CommonType.pointType.PURCHASE.getCode());
                // 设置积分日志的时间为当前时间
                pointParam.put("PointsLogTime", DateUtils.getTime());
                // 设置积分日志的描述为购物
                pointParam.put("PointsLogDesc", CommonType.pointType.PURCHASE.getDesc());
                // 设置积分日志的标记为注册
                pointParam.put("PointsLogFlag", "reg");
                // 设置积分日志的用户ID为买家用户ID
                pointParam.put("UserId", map.get("buyerUserId").toString());
                // 设置积分日志的订单ID为当前订单
                pointParam.put("orderId", order);
                // 设置积分日志的积分数量为计算得到的积分值
                pointParam.put("PointsLogPoints", (int) l);
                // 设置积分日志的操作描述为购物
                pointParam.put("PointsLogOperate", CommonType.pointType.PURCHASE.getDesc());
                // 设置积分日志的积分规则比例为消费规则的比例
                pointParam.put("proportion", integralRules.get("ruleConsume"));
                // 调用userServiceClient的insertPointLogByMap方法，插入积分日志
                userServiceClient.insertPointLogByMap(pointParam);

            }
        }
        //下架过期虚拟商品
        List<String> commonIds = activityNoneListMapper.queryGuoQiVirtualGoods();
        if (null != commonIds && commonIds.size() > 0) {
            for (String commonId : commonIds) {
                activityNoneListMapper.updateGoodsStart(commonId);
            }
            List<Integer> list = activityNoneListMapper.queryAllGoodsIdByCommonId(commonIds);
            for (Integer goodsId : list) {
                activityNoneListMapper.updateGoodsState(goodsId);
            }

            Map<String, Object> map = new HashMap<>();
            // 1 是添加  2是删除    屏蔽
            map.put("type", 2);
            //通过mq 更新es的商品数据
            map.put("commonIdS", commonIds);
            MqSendUtils.mqSendGoods(map);
        }
    }
}
