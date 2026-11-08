package com.houjiang.job.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.kuaidiniao.KdniaoTrackQueryAPI;
import com.houjiang.job.dto.ActivityNoneRecSlideDTO;
import com.houjiang.job.mapper.AppJiPushMapper;
import com.houjiang.job.vo.AppJiPushOrderVO;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppJiPushOrderJob
 * @Description App推送  订单签收推送
 * @Author Amos
 * @Date 2023/5/17 14:01
 * @Version 1.0
 * @author Amos
 */
public class AppJiPushOrderJob implements BaseJob {

    @Autowired
    private AppJiPushMapper appJiPushMapper;


    /** 日志对象，用于记录执行过程中的信息 */
    private static final Logger logger = LoggerFactory.getLogger(AppJiPushOrderJob.class);

    /**
     * 执行订单签收推送任务。
     *
     * @param context 上下文对象，包含任务执行的相关信息。
     * @throws JobExecutionException 如果任务执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 打印任务开始信息
        System.out.println("AppJiPushOrderJob 订单签收推送");

        // 初始化物流查询API
        KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();

        // 查询待推送的订单物流信息
        List<AppJiPushOrderVO> logisticsList = appJiPushMapper.queryOrderLogistics();

        // 初始化用户ID列表，用于存储需要推送通知的用户ID
        List<String> userIdList = new ArrayList<>();

        // 初始化活动信息对象，用于设置推送的额外信息
        ActivityNoneRecSlideDTO activity = new ActivityNoneRecSlideDTO();

        try {
            // 遍历物流信息列表，查询每个订单的物流状态
            for (AppJiPushOrderVO list : logisticsList) {
                // 根据快递公司编码和运单号查询物流状态
                String result = KdniaoTrackQueryAPI.getOrderTracesByJson(list.getExpressPinyin(), list.getOrderShippingCode(), "");

                // 解析物流状态信息
                JSONObject jsonObject = JSON.parseObject(result);

                // 检查物流状态是否为已签收
                if (jsonObject.get("State") != null && !"".equals(jsonObject.get("State"))) {
                    if (jsonObject.get("State").equals("3")) {
                        // 如果订单已签收，将用户ID添加到推送列表
                        userIdList.add(list.getBuyerUserId());
                    }
                }
            }

            // 如果有需要推送通知的用户
            if (userIdList.size() > 0) {
                // 打印推送用户数量
                System.out.println(userIdList.size());

                // 设置活动类型为订单签收通知
                activity.setActionType(40);

                // 将活动信息转换为JSON字符串，作为推送的额外参数
                String extra = JSONObject.toJSONString(activity);

                // 注释掉的代码是之前使用的推送方法，现在已不再使用
//                JiPushUtil.IOSSendToAliasList(userIdList, "", " ", "您的订单已签收，请尽快核实商品", extra);
//                JiPushUtil.AndoridSendToAliasList(userIdList, "", " ", "您的订单已签收，请尽快核实商品", extra);
            }
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        }

        // 打印任务完成信息
        System.out.println("AppJiPushOrderJob 完成订单签收推送");
    }
}
