package com.houjiang.merchant.service.impl.userDistribution;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.distribution.DistributionOrder;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.DistributionEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.distribution.DistributionOrderMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.userDistribution.IUserDistributionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class UserDistributionServiceImpl implements IUserDistributionService {

    @Resource
    UserMapper userMapper;
    @Resource
    DistributionOrderMapper distributionOrderMapper;

    @Override
    public JSONObject count(Integer shopId) {

        JSONObject result = new JSONObject();

        Long userCount = this.userMapper.selectCount(new QueryWrapper<User>().isNull("delete_time"));

        Long userDistributionCount = this.userMapper.selectCount(new QueryWrapper<User>().eq("is_distribution", YesNoEnum.YES.getCode()).isNull("delete_time"));

        BigDecimal allMoney = this.distributionOrderMapper.sum("money", new QueryWrapper<DistributionOrder>().eq("status", DistributionEnum.ORDER_STATUS_SUCCESS.getCode()).eq("delete_time", 0).eq("shop_id", shopId));

        BigDecimal todayMoney = this.distributionOrderMapper.sum("money", new QueryWrapper<DistributionOrder>().eq("status", DistributionEnum.ORDER_STATUS_SUCCESS.getCode()).eq("delete_time", 0).ge("create_time", TimeUtils.today().get(0)).le("create_time", TimeUtils.today().get(1)).eq("shop_id", shopId));

        BigDecimal waitMoney = this.distributionOrderMapper.sum("money", new QueryWrapper<DistributionOrder>().eq("status", DistributionEnum.ORDER_STATUS_WAIT.getCode()).eq("delete_time", 0).eq("shop_id", shopId));

        BigDecimal todayWaitMoney = this.distributionOrderMapper.sum("money", new QueryWrapper<DistributionOrder>().eq("status", DistributionEnum.ORDER_STATUS_WAIT.getCode()).eq("delete_time", 0).ge("create_time", TimeUtils.today().get(0)).le("create_time", TimeUtils.today().get(1)).eq("shop_id", shopId));

        List<Map<String, Object>> incomeRank = this.userMapper.getShopIncomeRank(shopId);
        JSONArray incomeRankArr = new JSONArray();
        if (incomeRank.size() > 0) {
            for (Map<String, Object> item : incomeRank) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("user_distribution_id", item.get("user_distribution_id"));
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", item.get("user_distribution_id")).select("id,sn,avatar,nickname,distribution_total_money"));
                user.setAvatar(UrlUtils.toShopAbsoluteUrl(StringUtils.isNotEmpty(user.getAvatar()) ? user.getAvatar() : ""));
                itemObj.put("user_distribution", user);
                incomeRankArr.add(itemObj);
            }
        }

        List<Map<String, Object>> nextRank = this.userMapper.getShopNextRank(shopId);
        JSONArray nextRankArr = new JSONArray();
        if (nextRank.size() > 0) {
            for (Map<String, Object> item : nextRank) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("user_distribution_id", item.get("user_distribution_id"));
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", item.get("user_distribution_id")).select("id,sn,avatar,nickname,next_nums"));
                user.setAvatar(UrlUtils.toShopAbsoluteUrl(StringUtils.isNotEmpty(user.getAvatar()) ? user.getAvatar() : ""));
                itemObj.put("user_distribution", user);
                nextRankArr.add(itemObj);
            }
        }

        BigDecimal userDistributionCountB =  userDistributionCount != null ? new BigDecimal(userDistributionCount.toString()) : new BigDecimal("0");

        BigDecimal userCountB = userCount != null ? new BigDecimal(userCount.toString()) : new BigDecimal("0");

        result.put("user_nums", userCount);
        result.put("user_distribution_nums", userDistributionCount);

        if (userCount == null || userCount.intValue() == 0) {
            result.put("user_distribution_ratio", "0");
        } else {
            result.put("user_distribution_ratio", userDistributionCountB.divide(userCountB, 3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        result.put("all_money", allMoney);
        result.put("today_money", todayMoney);
        result.put("wait_money", waitMoney);
        result.put("today_wait_money", todayWaitMoney);
        result.put("income_rank", incomeRankArr);
        result.put("next_rank", nextRankArr);

        return result;
    }
}
