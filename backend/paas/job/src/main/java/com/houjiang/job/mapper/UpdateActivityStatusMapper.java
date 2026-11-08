package com.houjiang.job.mapper;

import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.job.dto.ActivityNoneRecSlideDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/16 15:42
 */
@Mapper
public interface UpdateActivityStatusMapper {

    /**
     *  商品状态更新
     * @return
     */
    Integer updatePromotionGoods();

    /**
     *  限时折扣过期状态更新
     * @return
     */
    Integer updatePromotionDiscountBase();

    /**
     *限时折扣商品过期状态更新
/**
     * @return
     */
    Integer updatePromotionDiscountGoods();


    /**
     * 更新限时折扣
     */
    void updatePromotionDiscountBase2();

    /**
     * 更新限时折扣商品
     */
    void updatePromotionDiscountGoods2();

    /**
     * 更新限时折扣
     */
    void updatePromotionDiscountBase3();

    /**
     * 更新限时折扣商品
     */
    void updatePromotionDiscountGoods3();


    /**
     * 查询过期的团标识
     * @return
     */
    List<String> selectExpireMark();

    /**
     * 更新拼团信息
     * @param param
     */
    void updateMarkStatus(@Param("param") ExpireMarkIdUpdateDTO param);

    /**
     * 订单状态更新
     * @param expireMarkIdList
     * @return
     */
    List<String> selectByMarkId(@Param("expireMarkIdList")List<String> expireMarkIdList);

    /**
     * 拼团失败退款后，将已付款名额（temp.pay_start=1）置已退款(2)。
     * @param orderIdList 参与退款的订单号列表
     */
    void markTempRefunded(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询过期的拼团数量
     * @return
     */
    Integer slectDetailId();

    /**
     * 拼团过期状态更新
     * @return
     */
    Integer updatePintuanMarkStatus();


    /**
     * 商品状态更新
     * @return
     */
    Integer updatePintuanGoods();

    /**
     * 拼团过期状态更新
     * @return
     */
    Integer updatePintuanStatus();

    /**
     * 满减赠折商品状态更新
     * @return
     */
    Integer updateManjianzengzheGoods();

    /**
     * 满减赠折过期状态更新
     * @return
     */
    Integer updateManjianzengzheStatus();


    /**
     * 商品状态更新
     * @return
     */
    Integer updateSkillGoodsStatus();

    /**
     * 秒杀商品状态更新
     * @return
     */
    Integer updateSkillGoods();

    /**
     * 秒杀过期状态更新
     * @return
     */
    Integer updateGoodsStatus();


    /**
     * 更新技能商品信息的第二版本方法。
     * <p>
     * 该方法旨在优化前一版本的技能商品更新逻辑，可能涉及数据库操作或与外部系统的交互。
     * 具体实现细节未在此处展示，注释主要为了说明此方法的存在目的和使用场景。
     * </p>
     */
    void updateSkillGoods2();

    /**
     * 更新技能商品状态的第二版本方法。
     * <p>
     * 与前一版本的更新技能商品状态方法相比，此版本可能提供了更高效的实现方式，
     * 或者增加了对特定场景的兼容处理。注释的目的是提醒使用者此方法的改进点和适用情境。
     * </p>
     */
    void updateSkillGoodsStatus2();

    /**
     * 更新商品状态的第二版本方法。
     * <p>
     * 此方法是针对商品状态更新逻辑的第二个版本，旨在提升性能、增加鲁棒性或解决前一版本存在的问题。
     * 方法的具体实现和应用场景通过注释进行说明，以便于使用者理解和正确调用。
     * </p>
     */
    void updateGoodsStatus2();

    /**
     *拼团待开启状态更新
/**
     * @return
     */
    Integer updatePintuanStatus2();

    /**
     *满减赠折待开始状态更新
/**
     * @return
     */
    Integer updateManjianzengzheStatus2();

    /**
     * 查询秒杀库存为0的商品的通用ID列表。
     * <p>
     * 该方法用于获取那些秒杀库存已经清零的商品的通用ID列表。这可能用于后续的特定业务逻辑，
     * 比如清理过期的秒杀商品信息或者发送库存告急的通知。
     *
     * @return List<ActivityNoneRecSlideDTO> - 秒杀库存为0的商品的通用ID列表。
     */
    List<ActivityNoneRecSlideDTO> queryCommonIdBySeckillStockZero();

    /**
     * 更新商品的可见性状态。
     * <p>
     * 通过指定的commonId和commonIsXian参数，更新对应商品的可见性状态。这可能用于控制商品在前端的显示状态，
     * 比如下架商品或者将商品设置为隐藏。
     *
     * @param commonId 商品的通用ID，用于唯一标识一个商品。
     * @param commonIsXian 商品的可见性状态，通常为0表示隐藏，1表示可见。
     */
    void updatCommonIsXian(@Param("commonId") Integer commonId,@Param("commonIsXian") Integer commonIsXian);

    /**
     * 查询拼团配置信息。
     * <p>
     * 该方法用于获取拼团活动的配置信息。这些配置可能包括拼团活动的规则、优惠信息等。
     *
     * @return List<Map<String, String>> - 拼团配置信息的列表，每个元素是一个包含配置键值对的Map。
     */
    List<Map<String, String>> selectPinTuanConfig();

    /**
     * 根据商品通用ID查询其可见性状态。
     * <p>
     * 通过指定的商品通用ID，查询该商品的可见性状态。这可以用于前端展示商品信息时控制显示状态，
     * 或者在业务逻辑中根据商品状态做出相应的处理。
     *
     * @param commonId 商品的通用ID，用于唯一标识一个商品。
     * @return Integer - 商品的可见性状态，通常为0表示隐藏，1表示可见。
     */
    Integer selectCommonIsXian(Integer commonId);

    /**
     * 更新拼团配置信息。
     * <p>
     * 通过传入一个包含配置键值对的Map，更新拼团活动的配置信息。这可以用于修改拼团活动的规则、
     * 优惠信息等。
     *
     * @param map 包含拼团配置键值对的Map，键表示配置项的名称，值表示配置项的值。
     */
    void updatePinTuanConfig(Map<String, String> map);

    /**
     * 删除拼团配置信息。
     * <p>
     * 通过传入一个包含配置键值对的Map，删除对应的拼团配置信息。这可以用于清理不再需要的拼团活动配置，
     * 或者更新配置时的旧配置清理。
     *
     * @param map 包含拼团配置键值对的Map，键表示配置项的名称，值表示配置项的值。
     */
    void deletePinTuanConfig(Map<String, String> map);
}
