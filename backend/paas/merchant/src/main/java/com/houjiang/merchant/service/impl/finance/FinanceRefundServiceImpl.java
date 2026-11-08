package com.houjiang.merchant.service.impl.finance;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.service.finance.IFinanceRefundService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceRefundSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceRefundListVo;
import com.houjiang.merchant.vo.finance.FinanceRefundLogVo;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.RefundLog;
import com.houjiang.merchant.common.entity.RefundRecord;
import com.houjiang.merchant.common.enums.PaymentEnum;
import com.houjiang.merchant.common.enums.RefundEnum;
import com.houjiang.merchant.common.mapper.RefundLogMapper;
import com.houjiang.merchant.common.mapper.RefundRecordMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款记录服务实现类
 */
@Service
public class FinanceRefundServiceImpl implements IFinanceRefundService {

    @Resource
    RefundRecordMapper refundRecordMapper;

    @Resource
    RefundLogMapper refundLogMapper;

    /**
     * 退款记录列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<FinanceRechargeListVo>
     */
    @Override
    public PageResult<FinanceRefundListVo> list(PageValidate pageValidate, FinanceRefundSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<RefundRecord> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.selectAll(RefundRecord.class)
                .select("U.id as user_id,U.nickname,U.avatar, U.account")
                .leftJoin("?_user U ON U.id=t.user_id".replace("?_", GlobalConfig.tablePrefix))
                .orderByDesc("id");

        refundRecordMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "like:sn@t.sn:str",
                "like:order_sn@t.order_sn:str",
                "=:refund_type@t.refund_type:int",
                "=:refund_status@t.refund_status:int",
                "datetime:start_time-end_time@t.create_time:str",
        });


        if (StringUtils.isNotEmpty(searchValidate.getUser_info())) {
            String keyword = searchValidate.getUser_info();
            mpjQueryWrapper.nested(wq->wq
                    .like("U.nickname", keyword).or()
                    .like("U.account", keyword).or()
                    .like("U.sn", keyword).or()
                    .like("U.mobile", keyword));
        }

        IPage<FinanceRefundListVo> iPage = refundRecordMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                FinanceRefundListVo.class,
                mpjQueryWrapper);

        for (FinanceRefundListVo vo : iPage.getRecords()) {
            vo.setRefundTypeText(RefundEnum.getRefundTypeMsg(vo.getRefundType()));
            vo.setRefundStatusText(RefundEnum.getRefundStatusMsg(vo.getRefundStatus()));
            vo.setRefundWayText(PaymentEnum.getPayWayMsg(vo.getRefundWay()));
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(vo.getAvatar()));
        }

        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("total", refundRecordMapper.selectCount(null));
        extend.put("ing", refundRecordMapper.selectCount(new QueryWrapper<RefundRecord>().eq("refund_status", 0)));
        extend.put("success", refundRecordMapper.selectCount(new QueryWrapper<RefundRecord>().eq("refund_status", 1)));
        extend.put("error", refundRecordMapper.selectCount(new QueryWrapper<RefundRecord>().eq("refund_status", 2)));

        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("totalRefundAmount", refundRecordMapper.sum("order_amount", new QueryWrapper<>()));
        stat.put("ingRefundAmount", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 0)));
        stat.put("successRefundAmount", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 1)));
        stat.put("errorRefundAmount", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 2)));
        extend.put("stat", stat);

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords(), extend);
    }

    /**
     * 退款日志
     *
     * @author Amos
     * @param recordId 记录ID
     * @return List<FinanceRefundLogVo>
     */
    @Override
    public List<FinanceRefundLogVo> log(Integer recordId) {
        MPJQueryWrapper<RefundLog> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.selectAll(RefundLog.class)
                .select("sa.name as handler")
                .eq("t.record_id", recordId)
                .innerJoin("?_admin sa ON sa.id=t.handle_id".replace("?_", GlobalConfig.tablePrefix))
                .orderByDesc("t.id");

        List<FinanceRefundLogVo> list = refundLogMapper.selectJoinList(FinanceRefundLogVo.class, mpjQueryWrapper);

        for (FinanceRefundLogVo vo : list) {
            vo.setRefundStatusText(RefundEnum.getRefundStatusMsg(vo.getRefundStatus()));
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
        }

        return list;
    }

    @Override
    public Map<String, Object> stat() {
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("total", refundRecordMapper.sum("order_amount", new QueryWrapper<>()));
        stat.put("ing", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 0)));
        stat.put("success", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 1)));
        stat.put("error", refundRecordMapper.sum("order_amount", new QueryWrapper<RefundRecord>().eq("refund_status", 2)));
        return stat;
    }
}
