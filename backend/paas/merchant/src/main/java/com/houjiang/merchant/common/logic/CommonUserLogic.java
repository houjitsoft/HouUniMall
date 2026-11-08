package com.houjiang.merchant.common.logic;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.houjiang.merchant.common.entity.log.UserAccountLog;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.mapper.log.UserAccountLogMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;

import java.math.BigDecimal;
import java.util.Map;

public class CommonUserLogic {

    public static JSONObject getUserMoneys(Integer userId) {
        JSONObject userMoneys = new JSONObject();
        UserMapper model = SpringUtils.getBean(UserMapper.class);
        User user = model.selectById(userId);
        if (StringUtils.isNotNull(user)) {
            userMoneys.put("user_money", user.getUserMoney());
            userMoneys.put("distribution_money", user.getDistributionMoney());
            userMoneys.put("all", user.getUserMoney().add(user.getDistributionMoney()));
        }
        return userMoneys;
    }

    /**
     * 扣减分销佣金
     * @param userId 用户ID
     * @param money 扣减金额
     * @return boolean 是否扣减成功
     * @author Amos
     * @datetime 2024-12-19
     */
    public static boolean decDistributionMoney(Integer userId, BigDecimal money) {
        UserMapper userMapper = SpringUtils.getBean(UserMapper.class);

        // 确保金额保留2位小数
        money = money.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 先查询用户当前分销佣金是否充足
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("id", userId)
                .isNull("delete_time")
                .last("limit 1"));

        if (user == null || user.getDistributionMoney() == null) {
            return false;
        }

        // 检查余额是否充足
        if (user.getDistributionMoney().compareTo(money) < 0) {
            return false;
        }

        // 执行扣减操作
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                .isNull("delete_time")
                .ge("distribution_money", money)
                .setSql("distribution_money = distribution_money - " + money);

        int result = userMapper.update(null, updateWrapper);
        return result > 0;
    }

    /**
     * 创建账户变动日志
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param money 变动金额
     * @param fromSn 来源单号
     * @param remark 备注
     * @param params 扩展参数
     * @return boolean 是否创建成功
     * @author Amos
     * @datetime 2024-12-19
     * @description 记录用户账户变动日志，支持余额和佣金变动
     */
    public static boolean createAccountLog(Integer userId, Integer changeType, BigDecimal money,
                                           String fromSn, String remark, String params) {
        UserMapper userMapper = SpringUtils.getBean(UserMapper.class);
        UserAccountLogMapper accountLogMapper = SpringUtils.getBean(UserAccountLogMapper.class);

        // 查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("id", userId)
                .isNull("delete_time")
                .last("limit 1"));

        if (user == null) {
            return false;
        }

        // 确保金额保留2位小数
        money = money.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 获取变动对象类型
        Integer changeObject = LogMoneyEnum.getChangeObject(changeType);

        // 根据变动对象获取剩余金额
        BigDecimal leftAmount;
        switch (changeObject) {
            // 用户余额
            case 0:
                leftAmount = user.getUserMoney();
                break;
            // 分销佣金
            case 1:
                leftAmount = user.getDistributionMoney();
                break;
            default:
                leftAmount = BigDecimal.ZERO;
                break;
        }

        // 确保剩余金额保留2位小数
        leftAmount = leftAmount.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 获取账户状态（增加或减少）
        Integer accountStatus = getAccountStatus(changeType);

        // 创建账户日志记录
        UserAccountLog accountLog = new UserAccountLog();
        accountLog.setSn(generateAccountLogSn(accountLogMapper));
        accountLog.setUserId(userId);
        accountLog.setChangeObject(changeObject);
        accountLog.setChangeType(changeType);
        accountLog.setAccountStatus(accountStatus);
        accountLog.setLeftAmount(leftAmount);
        accountLog.setChangeAmount(money);
        accountLog.setFromSn(fromSn != null ? fromSn : "");
        accountLog.setRemark(remark != null ? remark : "");
        accountLog.setParams(params != null ? params : "");
        accountLog.setCreateTime(System.currentTimeMillis() / 1000);
        accountLog.setUpdateTime(System.currentTimeMillis() / 1000);

        // 保存日志记录
        int result = accountLogMapper.insert(accountLog);
        return result > 0;
    }

    /**
     * 根据变动类型获取账户状态
     * @param changeType 变动类型
     * @return Integer 账户状态 1=增加 2=减少
     * @author Amos
     * @datetime 2024-12-19
     */
    private static Integer getAccountStatus(Integer changeType) {
        // 根据变动类型判断是增加还是减少
        if (changeType.equals(LogMoneyEnum.DISTRIBUTION_MONEY_WITHDRAW.getCode()) ||
                changeType.equals(LogMoneyEnum.USER_MONEY_ORDER_PAY.getCode()) ||
                changeType.equals(LogMoneyEnum.USER_MONEY_SYS_DEC.getCode()) ||
                changeType.equals(LogMoneyEnum.DISTRIBUTION_MONEY_SYS_DEC.getCode())) {
            // 减少
            return LogMoneyEnum.ACCOUNT_STATUS_DOWN.getCode();
        } else {
            // 增加
            return LogMoneyEnum.ACCOUNT_STATUS_UP.getCode();
        }
    }

    /**
     * 生成账户日志流水号
     * @param accountLogMapper 账户日志Mapper
     * @return String 流水号
     * @author Amos
     * @datetime 2024-12-19
     */
    private static String generateAccountLogSn(UserAccountLogMapper accountLogMapper) {
        String date = TimeUtils.timestampToDate(System.currentTimeMillis() / 1000, "yyyyMMddHHmmss");
        String sn;
        while (true) {
            sn = date + ToolUtils.randomInt(6);
            UserAccountLog snModel = accountLogMapper.selectOne(
                    new QueryWrapper<UserAccountLog>()
                            .select("id")
                            .eq("sn", sn)
                            .last("limit 1"));
            if (snModel == null) {
                break;
            }
        }
        return sn;
    }

    /**
     * 增加分销佣金
     * @param userId 用户ID
     * @param money 增加金额
     * @return boolean 是否增加成功
     * @author Amos
     * @datetime 2024-12-19
     * @description 增加用户分销佣金和总收入佣金
     */
    public static boolean incDistributionMoney(Integer userId, BigDecimal money) {
        UserMapper userMapper = SpringUtils.getBean(UserMapper.class);

        // 确保金额保留2位小数
        money = money.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 先查询用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("id", userId)
                .isNull("delete_time")
                .last("limit 1"));

        if (user == null) {
            return false;
        }

        // 执行增加操作
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                .isNull("delete_time")
                .setSql("distribution_money = distribution_money + " + money)
                .setSql("distribution_total_money = distribution_total_money + " + money)
                .setSql("update_time = " + (System.currentTimeMillis() / 1000));

        int result = userMapper.update(null, updateWrapper);
        return result > 0;
    }

    public static Boolean incMoney(Integer id, BigDecimal realMoney) {
        UserMapper userMapper = SpringUtils.getBean(UserMapper.class);

        // 确保金额保留2位小数
        realMoney = realMoney.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 使用UpdateWrapper进行原子性更新操作
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .isNull("delete_time")
                .setSql("user_money = user_money + " + realMoney)
                .setSql("update_time = " + (System.currentTimeMillis() / 1000));

        int result = userMapper.update(null, updateWrapper);
        return result > 0;
    }
}
