package com.houjiang.job.service;

/**
 * @author Amos
 * @date 2023/5/16 15:40
 */
public interface UpdateActivityStatusService {

    /**
     *  商品状态更新
     * 
     */
    Integer updatePromotionGoods();



    /**
     *  限时折扣过期状态更新
     * 
     */
    Integer updatePromotionDiscountBase();


    /**
     *限时折扣商品过期状态更新
/**
     * 
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
     * 更新拼团信息
     *
     */
    Integer updatePintuanMarkStatus();

    /**
     * 商品状态更新
     * 
     */
    Integer updatePintuanGoods();

    /**
     * 拼团过期状态更新
     * 
     */
    Integer updatePintuanStatus();

    /**
     * 拼团配置更新
     *
     */
    void updatePintuanConfig();

    /**
     * 满减赠折商品状态更新
     * 
     */
    Integer updateManjianzengzheGoods();

    /**
     * 满减赠折过期状态更新
     * 
     */
    Integer updateManjianzengzheStatus();


    /**
     * 商品状态更新
     * @return
     */
    Integer updateSkillGoodsStatus();


    /**
     * 秒杀商品过期状态更新
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
     * 拼团待开启状态更新
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
     * 更新技能商品信息方法3
     *
     * 该方法旨在更新技能商店中的特定商品信息。考虑到技能商品的多样性及其信息的复杂性，
     * 此方法作为更新技能商品信息的第三个版本，可能对前两个版本的更新策略进行了优化或改进。
     * 具体的更新逻辑和策略未在代码中体现，因此在使用该方法时，需要参考相关的文档或注释了解其行为。
     */
    void updateSkillGoods3();
}
