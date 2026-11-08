package com.houjiang.job.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.job.dto.ActivityNoneRecSlideDTO;
import com.houjiang.job.feign.OrderServiceClient;
import com.houjiang.job.mapper.UpdateActivityStatusMapper;
import com.houjiang.job.service.UpdateActivityStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/16 15:40
 */
@Service
public class UpdateActivityStatusServiceImpl implements UpdateActivityStatusService {

    @Autowired
    private UpdateActivityStatusMapper updateActivityStatusMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Override
    public Integer updatePromotionGoods() {
        return updateActivityStatusMapper.updatePromotionGoods();
    }

    @Override
    public Integer updatePromotionDiscountBase() {
        return updateActivityStatusMapper.updatePromotionDiscountBase();
    }

    @Override
    public Integer updatePromotionDiscountGoods() {
        return updateActivityStatusMapper.updatePromotionDiscountGoods();
    }

    @Override
    public void updatePromotionDiscountBase2() {
        updateActivityStatusMapper.updatePromotionDiscountBase2();
    }

    @Override
    public void updatePromotionDiscountGoods2() {
        updateActivityStatusMapper.updatePromotionDiscountGoods2();
    }

    @Override
    public void updatePromotionDiscountBase3() {
        updateActivityStatusMapper.updatePromotionDiscountBase3();
    }

    @Override
    public void updatePromotionDiscountGoods3() {
        updateActivityStatusMapper.updatePromotionDiscountGoods3();
    }


    /**
     * 更新拼团标记状态
     * 该方法用于处理拼团活动的标记状态更新，主要针对过期的拼团标记进行处理，将其状态更新为成团失败。
     * 同时，也会尝试更新相关的订单状态，以确保系统中拼团活动和订单状态的一致性。
     *
     * @return 更新状态的计数。表示有多少个拼团标记的状态被成功更新。
     */
    @Override
    public Integer updatePintuanMarkStatus() {
        // 查询已过期的拼团标记ID列表
        List<String> expireMarkIdList = updateActivityStatusMapper.selectExpireMark();
        // 如果过期标记ID列表不为空
        if (!CollectionUtils.isEmpty(expireMarkIdList)) {
            // 创建更新参数对象，并设置过期标记ID列表和新的状态（成团失败）
            ExpireMarkIdUpdateDTO udpateParam = new ExpireMarkIdUpdateDTO();
            udpateParam.setExpireMarkIdList(expireMarkIdList);
            // 0,等待成团。1为已成团，2为成团失败，3为已完成退款
            udpateParam.setStatus(2);
            // 执行标记状态更新
            updateActivityStatusMapper.updateMarkStatus(udpateParam);

            // 订单状态更新
            List<String> orderIdList = updateActivityStatusMapper.selectByMarkId(expireMarkIdList);
            // 设置订单ID列表到更新参数对象中
            udpateParam.setOrderIdList(orderIdList);

            try {
                // 调用订单服务，逐笔走新 Mall 收银台退款（order 侧按订单状态幂等）
                boolean refunded = orderServiceClient.checkGroupBookingStatus(udpateParam);
                if (refunded) {
                    // 退款已发起：已付款名额置已退款(2)，团态推进到已完成退款(3)
                    if (!CollectionUtils.isEmpty(orderIdList)) {
                        updateActivityStatusMapper.markTempRefunded(orderIdList);
                    }
                    udpateParam.setStatus(3);
                    updateActivityStatusMapper.updateMarkStatus(udpateParam);
                }
            } catch (Exception e) {
                // 退款异常保留成团失败(2)态，下轮 selectExpireMark 不再重选（仅 status=0），
                // 依赖 order 侧订单状态幂等避免重复退款
            }
        }

        // 查询需要更新的详细ID数量
        Integer count = updateActivityStatusMapper.slectDetailId();
        // 初始化更新状态的计数
        Integer updatePintuanMarkStatus = 0;
        // 如果需要更新的详细ID数量大于0
        if (null != count && count.intValue() > 0) {
            // 更新拼团标记状态，并返回更新的数量
            updatePintuanMarkStatus = updateActivityStatusMapper.updatePintuanMarkStatus();
        }

        // 返回更新状态的计数
        return updatePintuanMarkStatus;
    }


    @Override
    public Integer updatePintuanGoods() {
        return updateActivityStatusMapper.updatePintuanGoods();
    }

    @Override
    public Integer updatePintuanStatus() {
        return updateActivityStatusMapper.updatePintuanStatus();
    }

    /**
     * 更新拼团配置信息。
     * 该方法通过查询当前的拼团配置，筛选出状态不为已结束的拼团项，然后更新配置信息或删除已结束的拼团项。
     * <p>
     * 具体步骤如下：
     * 1. 查询所有的拼团配置。
     * 2. 遍历每个配置项，解析出拼团项ID列表。
     * 3. 对于每个拼团项ID，查询其状态。
     * 4. 如果状态不为已结束，则保留该拼团项ID；否则，从列表中移除。
     * 5. 如果保留的拼团项ID列表不为空，则更新配置信息；如果为空，则删除该配置项。
     */
    @Override
    public void updatePintuanConfig() {
        // 查询所有的拼团配置
        List<Map<String, String>> list = updateActivityStatusMapper.selectPinTuanConfig();
        for (Map<String, String> map : list) {
            // 获取配置数据字符串
            String configData = map.get("configData");
            // 解析配置数据字符串为整型ID列表
            List<Integer> commonIdList = JSON.parseArray(configData, Integer.class);
            // 创建一个新的ID列表，用于存储需要保留的ID
            List<Integer> commonIds = new ArrayList<>(commonIdList);
            for (Integer commonId : commonIdList) {
                // 查询每个拼团项的状态
                Integer commonIsXian = updateActivityStatusMapper.selectCommonIsXian(commonId);
                // 如果状态不为已结束（4），则从列表中移除该ID
                if (commonIsXian != 4) {
                    commonIds.remove(commonId);
                }
            }
            // 如果保留的ID列表不为空，则更新配置信息；否则，删除该配置项
            if (!CollectionUtils.isEmpty(commonIds)) {
                map.put("configData", JSON.toJSONString(commonIds));
                updateActivityStatusMapper.updatePinTuanConfig(map);
            } else {
                updateActivityStatusMapper.deletePinTuanConfig(map);
            }
        }
    }


    /**
     * 更新满减赠品的状态。
     * 调用updateManjianzengzheGoods方法，通过mapper层更新满减赠品的活动状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateManjianzengzheGoods() {
        return updateActivityStatusMapper.updateManjianzengzheGoods();
    }

    /**
     * 更新满减赠品的活动状态。
     * 调用updateManjianzengzheStatus方法，通过mapper层更新满减赠品活动的状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateManjianzengzheStatus() {
        return updateActivityStatusMapper.updateManjianzengzheStatus();
    }

    /**
     * 更新技能商品的状态。
     * 调用updateSkillGoodsStatus方法，通过mapper层更新技能商品的活动状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateSkillGoodsStatus() {
        return updateActivityStatusMapper.updateSkillGoodsStatus();
    }

    /**
     * 更新技能商品。
     * 调用updateSkillGoods方法，通过mapper层更新技能商品的信息。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateSkillGoods() {
        return updateActivityStatusMapper.updateSkillGoods();
    }

    /**
     * 更新普通商品的状态。
     * 调用updateGoodsStatus方法，通过mapper层更新普通商品的活动状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateGoodsStatus() {
        return updateActivityStatusMapper.updateGoodsStatus();
    }

    /**
     * 更新技能商品的信息。
     * 调用updateSkillGoods2方法，通过mapper层更新技能商品的详细信息。
     */
    @Override
    public void updateSkillGoods2() {
        updateActivityStatusMapper.updateSkillGoods2();
    }

    /**
     * 更新技能商品的活动状态。
     * 调用updateSkillGoodsStatus2方法，通过mapper层更新技能商品活动的状态。
     */
    @Override
    public void updateSkillGoodsStatus2() {
        updateActivityStatusMapper.updateSkillGoodsStatus2();
    }

    /**
     * 更新普通商品的状态。
     * 调用updateGoodsStatus2方法，通过mapper层更新普通商品的活动状态。
     */
    @Override
    public void updateGoodsStatus2() {
        updateActivityStatusMapper.updateGoodsStatus2();
    }

    /**
     * 更新拼团活动的状态。
     * 调用updatePintuanStatus2方法，通过mapper层更新拼团活动的状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updatePintuanStatus2() {
        return updateActivityStatusMapper.updatePintuanStatus2();
    }

    /**
     * 更新满减赠品的活动状态。
     * 调用updateManjianzengzheStatus2方法，通过mapper层更新满减赠品活动的状态。
     * @return 更新操作影响的行数。
     */
    @Override
    public Integer updateManjianzengzheStatus2() {
        return updateActivityStatusMapper.updateManjianzengzheStatus2();
    }

    /**
     * 根据秒杀库存更新商品的可见状态。
     * 查询秒杀库存为零的商品，并根据库存情况更新其可见状态。
     * 如果秒杀库存大于零，则设置商品状态为可见；否则，设置为不可见。
     */
    @Override
    public void updateSkillGoods3() {
        // 查询秒杀库存为零的商品列表
        List<ActivityNoneRecSlideDTO> activityGoodsList = updateActivityStatusMapper.queryCommonIdBySeckillStockZero();
        // 遍历商品列表
        for (ActivityNoneRecSlideDTO slideDTO : activityGoodsList) {
            // 根据秒杀库存更新商品的可见状态
            if(slideDTO.getSeckillNum() > 0){
                updateActivityStatusMapper.updatCommonIsXian(slideDTO.getCommonId(),2);
            }else {
                updateActivityStatusMapper.updatCommonIsXian(slideDTO.getCommonId(),0);
            }
        }
    }
}
