package com.houjiang.merchant.service.impl.finance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.service.finance.IFinanceWalletService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.log.UserAccountLog;
import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.mapper.log.UserAccountLogMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户余额记录服务实现类
 * @author Amos
 */
@Service
public class FinanceWalletServiceImpl implements IFinanceWalletService {

    @Resource
    UserAccountLogMapper logMoneyMapper;

    @Override
    public PageResult<FinanceWalletListVo> list(PageValidate pageValidate, FinanceWalletSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<UserAccountLog> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.selectAll(UserAccountLog.class)
                .select("U.id as user_id,U.sn as sn,U.nickname,U.avatar, U.account, U.mobile")
                .leftJoin("?_user U ON U.id=t.user_id".replace("?_", GlobalConfig.tablePrefix))
                .orderByDesc("id");

        logMoneyMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "=:change_type@t.change_type:int",
                "datetime:start_time-end_time@t.create_time:str",
        });

        Integer changeObject = changeObjectForAccountType(searchValidate.getAccount_type());
        if (changeObject != null) {
            mpjQueryWrapper.eq("t.change_object", changeObject);
        }

        if (StringUtils.isNotEmpty(searchValidate.getUser_info())) {
            String keyword = searchValidate.getUser_info();
            mpjQueryWrapper.nested(wq->wq
                    .like("U.nickname", keyword).or()
                    .like("U.account", keyword).or()
                    .like("U.sn", keyword).or()
                    .like("U.mobile", keyword));
        }

        IPage<FinanceWalletListVo> iPage = logMoneyMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                FinanceWalletListVo.class,
                mpjQueryWrapper);

        for (FinanceWalletListVo vo : iPage.getRecords()) {
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setChangeTypeDesc(LogMoneyEnum.getMsgByCode(Integer.parseInt(vo.getChangeType())));
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(vo.getAvatar()));
        }

        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("changeType", LogMoneyEnum.getTypeList());

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords(), extend);
    }

    @Override
    public Map<Integer, String> getUmChangeType() {
        return LogMoneyEnum.getTypeList();
    }

    static Integer changeObjectForAccountType(String accountType) {
        if ("balance".equals(accountType)) {
            return 0;
        }
        if ("commission".equals(accountType)) {
            return 1;
        }
        return null;
    }
}
