package com.houjiang.merchant.common.mapper.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.log.UserAccountLog;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * 账户变动Mapper
 */
@Mapper
public interface UserAccountLogMapper extends IBaseMapper<UserAccountLog> {

    /**
     * 增加
     *
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param changeAmount 变动金额
     * @param sourceId 来源ID
     * @param sourceSn 来源订单
     * @param remark 备注信息
     * @param extra 预留字段
     */
    default void add(Integer userId, Integer changeType, BigDecimal changeAmount,
                     Integer sourceId, String sourceSn, String remark, String extra) {

        /**
         * TODO 补充方法说明
         * @param i++ TODO
         */
        for (int i=0; i<=0; i++) {
            /**
             * TODO 补充方法说明
             */
            UserMapper userMapper = SpringUtils.getBean(UserMapper.class);
            /**
             * TODO 补充方法说明
             */
            User user = userMapper.selectById(userId);
            /**
             * TODO 补充方法说明
             */
            if (StringUtils.isNull(user)) {
                break;
            }

            /**
             * TODO 补充方法说明
             */
            Integer changeObject = LogMoneyEnum.getChangeObject(changeType);

            /**
             * TODO 补充方法说明
             */
            BigDecimal leftAmount = user.getUserMoney().add(changeAmount);
            /**
             * TODO 补充方法说明
             */
            UserAccountLog logMoney = new UserAccountLog();
            /**
             * TODO 补充方法说明
             */
            logMoney.setSn(this.randMakeOrderSn());
            /**
             * TODO 补充方法说明
             */
            logMoney.setUserId(userId);
            /**
             * TODO 补充方法说明
             */
            logMoney.setFromSn(sourceSn);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeObject(changeObject);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeType(changeType);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeAmount(changeAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setLeftAmount(leftAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setAccountStatus(1);
            /**
             * TODO 补充方法说明
             */
            logMoney.setRemark(remark);
            /**
             * TODO 补充方法说明
             */
            logMoney.setParams("");
            /**
             * TODO 补充方法说明
             */
            logMoney.setCreateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            logMoney.setUpdateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            this.insert(logMoney);
        }
    }

    /**
     * 增加
     *
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param changeAmount 变动金额
     * @param sourceId 来源ID
     * @param sourceSn 来源订单
     * @param remark 备注信息
     * @param extra 预留字段
     */
    default void addDistribution(Integer userId, Integer changeType, BigDecimal changeAmount,
                     Integer sourceId, String sourceSn, String remark, String extra) {

        /**
         * TODO 补充方法说明
         * @param i++ TODO
         */
        for (int i=0; i<=0; i++) {
            /**
             * TODO 补充方法说明
             */
            UserMapper userMapper = SpringUtils.getBean(UserMapper.class);
            /**
             * TODO 补充方法说明
             */
            User user = userMapper.selectById(userId);
            /**
             * TODO 补充方法说明
             */
            if (StringUtils.isNull(user)) {
                break;
            }

            Integer changeObject = 1;

            /**
             * TODO 补充方法说明
             */
            BigDecimal leftAmount = user.getDistributionMoney().add(changeAmount);
            /**
             * TODO 补充方法说明
             */
            UserAccountLog logMoney = new UserAccountLog();
            /**
             * TODO 补充方法说明
             */
            logMoney.setSn(this.randMakeOrderSn());
            /**
             * TODO 补充方法说明
             */
            logMoney.setUserId(userId);
            /**
             * TODO 补充方法说明
             */
            logMoney.setFromSn(sourceSn);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeObject(changeObject);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeType(changeType);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeAmount(changeAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setLeftAmount(leftAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setAccountStatus(1);
            /**
             * TODO 补充方法说明
             */
            logMoney.setRemark(remark);
            /**
             * TODO 补充方法说明
             */
            logMoney.setParams("");
            /**
             * TODO 补充方法说明
             */
            logMoney.setCreateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            logMoney.setUpdateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            this.insert(logMoney);
        }
    }

    /**
     * 减少
     *
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param changeAmount 变动金额
     * @param sourceId 来源ID
     * @param sourceSn 来源订单
     * @param remark 备注信息
     * @param extra 预留字段
     */
    default void dec(Integer userId, Integer changeType, BigDecimal changeAmount,
                     Integer sourceId, String sourceSn, String remark, String extra) {

        /**
         * TODO 补充方法说明
         * @param i++ TODO
         */
        for (int i=0; i<=0; i++) {
            /**
             * TODO 补充方法说明
             */
            UserMapper userMapper = SpringUtils.getBean(UserMapper.class);
            /**
             * TODO 补充方法说明
             */
            User user = userMapper.selectById(userId);
            /**
             * TODO 补充方法说明
             */
            if (StringUtils.isNull(user)) {
                break;
            }

            /**
             * TODO 补充方法说明
             */
            BigDecimal leftAmount = user.getUserMoney().subtract(changeAmount);

            /**
             * TODO 补充方法说明
             */
            Integer changeObject = LogMoneyEnum.getChangeObject(changeType);
            /**
             * TODO 补充方法说明
             */
            UserAccountLog logMoney = new UserAccountLog();
            /**
             * TODO 补充方法说明
             */
            logMoney.setSn(this.randMakeOrderSn());
            /**
             * TODO 补充方法说明
             */
            logMoney.setUserId(userId);
            /**
             * TODO 补充方法说明
             */
            logMoney.setFromSn(sourceSn);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeObject(changeObject);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeType(changeType);
            /**
             * TODO 补充方法说明
             */
            logMoney.setChangeAmount(changeAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setLeftAmount(leftAmount);
            /**
             * TODO 补充方法说明
             */
            logMoney.setAccountStatus(2);
            /**
             * TODO 补充方法说明
             */
            logMoney.setRemark(remark);
            /**
             * TODO 补充方法说明
             */
            logMoney.setParams("");
            /**
             * TODO 补充方法说明
             */
            logMoney.setCreateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            logMoney.setUpdateTime(System.currentTimeMillis() / 1000);
            /**
             * TODO 补充方法说明
             */
            this.insert(logMoney);
        }
    }

    /**
     * 生成唯一流水号
     *
     * @author Amos
     * @return String
     */
    default String randMakeOrderSn() {
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
            sn = date + ToolUtils.randomInt(4);
            /**
             * TODO 补充方法说明
             * @param QueryWrapper<UserAccountLog>( TODO
             */
            UserAccountLog snModel = this.selectOne(
                    /**
                     * TODO 补充方法说明
                     * @return TODO
                     */
                    new QueryWrapper<UserAccountLog>()
                            .select("id")
                            .eq("sn", sn)
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
