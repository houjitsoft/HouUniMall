package com.houjiang.merchant.common.mapper.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.RechargeOrder;
import com.houjiang.merchant.common.entity.user.UserWithdraw;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户提现Mapper
 * @author Amos
 */
@Mapper
public interface UserWithdrawMapper extends IBaseMapper<UserWithdraw> {

    /**
     * 生成唯一单号
     *
     * @author Amos
     * @param field 字段名
     * @return String
     */
    default String randMakeOrderSn(String field, String prefix) {
        /**
         * TODO 补充方法说明
         */
        String date = TimeUtils.timestampToDate(System.currentTimeMillis()/1000, "yyyyMMddHHmmss");
        String sn;
        /**
         * TODO 补充方法说明
         */
        while (true) {
            /**
             * TODO 补充方法说明
             */
            sn = prefix + date + ToolUtils.randomInt(4);
            /**
             * TODO 补充方法说明
             * @param QueryWrapper<UserWithdraw>( TODO
             */
            UserWithdraw snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<UserWithdraw>()
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
