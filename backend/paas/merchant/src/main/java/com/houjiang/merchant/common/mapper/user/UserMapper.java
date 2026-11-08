package com.houjiang.merchant.common.mapper.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends IBaseMapper<User> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT id, sn, avatar, nickname, distribution_total_money as all_money FROM hou_merchant_user ORDER BY (distribution_total_money * 10) DESC LIMIT 10")
    List<Map<String, Object>> getIncomeRank();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT id, sn, avatar, nickname, next_nums FROM hou_merchant_user ORDER BY next_nums DESC LIMIT 10")
    List<Map<String, Object>> getNextRank();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT `user_distribution_id` FROM `hou_merchant_distribution_order` `do` WHERE (  `do`.`shop_id` = #{shopId} ) AND `do`.`delete_time` = '0' GROUP BY `do`.`user_distribution_id` ORDER BY sum(money) desc LIMIT 10")
    List<Map<String, Object>> getShopIncomeRank(@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT `do`.`user_distribution_id` FROM `hou_merchant_distribution_order` `do` INNER JOIN `hou_merchant_user` `ud` ON `ud`.`id`=`do`.`user_distribution_id` WHERE (  `do`.`shop_id` = #{shopId} ) AND `do`.`delete_time` = '0' GROUP BY `do`.`user_distribution_id` ORDER BY ud.next_nums desc LIMIT 10")
    List<Map<String, Object>> getShopNextRank(@Param("shopId") Integer shopId);

    /**
     * 生成唯一单号
     *
     * @author Amos
     * @param field 字段名
     * @return String
     */
    default Integer randMakeSn(String field) {
        Integer sn;
        /**
         * TODO 补充方法说明
         */
        while (true) {
            /**
             * TODO 补充方法说明
             */
            sn = Integer.parseInt(ToolUtils.randomInt(8));
            /**
             * TODO 补充方法说明
             * @param QueryWrapper<User>( TODO
             */
            User snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<User>()
                            .select("id")
                            .eq(field, sn)
                            .last("limit 1"));
            /**
             * TODO 补充方法说明
             * @param null TODO
             */
            if (snModel == null) {
                break;
            }
        }
        return sn;
    }
}
