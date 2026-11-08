package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.distribution.*;
import com.houjiang.mall.mapper.distribution.DistributionOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销 C 端服务（买家/分销员视角）
 *
 * <p>与后台管理接口（{@code /mall/distribution/**}）分离：这里全部以「当前登录用户」为维度，
 * 用户身份由 {@link com.houjiang.mall.service.pay.MallCurrentUserResolver} 从 token 解析，
 * 不信任任何入参里的 userId，避免越权查看他人佣金。
 *
 * @author Amos
 */
@Slf4j
@Service
public class DistributionClientService {

    private final DistributorService distributorService;
    private final CommissionRecordService commissionRecordService;
    private final DistributionOrderService distributionOrderService;
    private final DistributionWithdrawService distributionWithdrawService;
    private final DistributionSettingService distributionSettingService;
    private final DistributorLevelService distributorLevelService;
    private final DistributionOrderMapper distributionOrderMapper;

    public DistributionClientService(DistributorService distributorService,
                                     CommissionRecordService commissionRecordService,
                                     DistributionOrderService distributionOrderService,
                                     DistributionWithdrawService distributionWithdrawService,
                                     DistributionSettingService distributionSettingService,
                                     DistributorLevelService distributorLevelService,
                                     DistributionOrderMapper distributionOrderMapper) {
        this.distributorService = distributorService;
        this.commissionRecordService = commissionRecordService;
        this.distributionOrderService = distributionOrderService;
        this.distributionWithdrawService = distributionWithdrawService;
        this.distributionSettingService = distributionSettingService;
        this.distributorLevelService = distributorLevelService;
        this.distributionOrderMapper = distributionOrderMapper;
    }

    /** 分销员状态：0 待审核 1 正常 2 已拒绝/禁用 */
    public static final int DISTRIBUTOR_STATUS_NORMAL = 1;

    /**
     * 首页概览：账户金额 + 业绩数据。字段命名沿用 C 端旧分销中心，前端可直接替换数据源。
     */
    public Map<String, Object> summary(Long userId) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = findDistributor(userId);
        if (distributor == null) {
            res.put("isDistributor", false);
            // 非分销员也要知道「分销功能开没开」：C 端个人中心据此决定是否展示「申请分销员」入口
            DistributionSetting platformSetting = distributionSettingService.getSetting(null);
            res.put("enabled", platformSetting == null ? 0 : platformSetting.getEnabled());
            res.put("applyMode", platformSetting == null ? 0 : platformSetting.getApplyMode());
            res.put("canWithdrawCommission", BigDecimal.ZERO.setScale(2));
            res.put("todaySettlement", BigDecimal.ZERO.setScale(2));
            res.put("totalSettlement", BigDecimal.ZERO.setScale(2));
            res.put("totalWithdraw", BigDecimal.ZERO.setScale(2));
            res.put("totalEstimatedIncome", BigDecimal.ZERO.setScale(2));
            res.put("orderCount", 0);
            res.put("subCount", 0);
            return res;
        }
        BigDecimal available = nz(distributor.getAvailableCommission());
        BigDecimal pending = nz(distributor.getPendingCommission());
        BigDecimal withdrawn = nz(distributor.getWithdrawnCommission());

        res.put("isDistributor", true);
        res.put("distributorId", distributor.getId());
        res.put("distributorNo", distributor.getDistributorNo());
        res.put("status", distributor.getStatus());
        res.put("realName", distributor.getRealName());
        res.put("phone", distributor.getPhone());
        res.put("levelId", distributor.getLevelId());
        res.put("levelName", resolveLevelName(distributor.getLevelId()));
        // 可提现金额
        res.put("canWithdrawCommission", available.setScale(2, RoundingMode.HALF_UP));
        // 预估收益：待结算佣金
        res.put("totalEstimatedIncome", pending.setScale(2, RoundingMode.HALF_UP));
        // 累计提现
        res.put("totalWithdraw", withdrawn.setScale(2, RoundingMode.HALF_UP));
        // 累计结算 = 可提现 + 已提现（与旧口径一致）
        res.put("totalSettlement", available.add(withdrawn).setScale(2, RoundingMode.HALF_UP));
        // 今日结算：当日完成结算的佣金合计
        res.put("todaySettlement", sumSettledToday(distributor.getId()));
        res.put("orderCount", distributor.getOrderCount() == null ? 0 : distributor.getOrderCount());
        res.put("subCount", distributor.getSubCount() == null ? 0 : distributor.getSubCount());

        // 我的邀请人（上级分销员）：C 端「我的推荐人」弹窗用
        if (distributor.getParentId() != null && distributor.getParentId() > 0) {
            Distributor parent = distributorService.getById(distributor.getParentId());
            if (parent != null) {
                Map<String, Object> parentInfo = new HashMap<>();
                parentInfo.put("distributorId", parent.getId());
                parentInfo.put("distributorNo", parent.getDistributorNo());
                parentInfo.put("realName", parent.getRealName());
                parentInfo.put("phone", maskPhone(parent.getPhone()));
                res.put("parent", parentInfo);
            }
        }

        DistributionSetting setting = distributionSettingService.getSetting(distributor.getShopId());
        res.put("enabled", setting.getEnabled());
        res.put("maxLevel", setting.getMaxLevel());
        res.put("minWithdrawAmount", nz(setting.getMinWithdrawAmount()).setScale(2, RoundingMode.HALF_UP));
        res.put("settleDays", setting.getSettleDays());
        return res;
    }

    /** 我的团队：直接下级（一级）+ 提示二级总数 */
    public Map<String, Object> team(Long userId, int page, int size) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = requireDistributor(userId);
        if (distributor == null) {
            res.put("total", 0);
            res.put("list", new ArrayList<>());
            res.put("secondLevelCount", 0);
            return res;
        }
        Page<Distributor> p = new Page<>(page, size);
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Distributor::getParentId, distributor.getId());
        wrapper.eq(Distributor::getDeleted, 0);
        wrapper.orderByDesc(Distributor::getCreateTime);
        IPage<Distributor> list = distributorService.page(p, wrapper);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Distributor sub : list.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("distributorId", sub.getId());
            row.put("userId", sub.getUserId());
            row.put("realName", sub.getRealName());
            row.put("phone", maskPhone(sub.getPhone()));
            row.put("distributorNo", sub.getDistributorNo());
            row.put("levelName", resolveLevelName(sub.getLevelId()));
            row.put("status", sub.getStatus());
            row.put("orderCount", sub.getOrderCount());
            row.put("totalCommission", nz(sub.getTotalCommission()).setScale(2, RoundingMode.HALF_UP));
            row.put("createTime", sub.getCreateTime());
            rows.add(row);
        }
        // 二级：下级们的下级
        int secondLevel = 0;
        if (!list.getRecords().isEmpty()) {
            List<Long> subIds = list.getRecords().stream().map(Distributor::getId).toList();
            LambdaQueryWrapper<Distributor> secondWrapper = new LambdaQueryWrapper<>();
            secondWrapper.in(Distributor::getParentId, subIds);
            secondWrapper.eq(Distributor::getDeleted, 0);
            secondLevel = (int) distributorService.count(secondWrapper);
        }
        res.put("total", list.getTotal());
        res.put("pages", list.getPages());
        res.put("list", rows);
        res.put("secondLevelCount", secondLevel);
        return res;
    }

    /**
     * 佣金明细
     *
     * @param status null=全部 0=待结算 1=已结算 2=已失效
     * @param level  null=全部 1=一级佣金 2=二级佣金
     * @param today  true=只要今天完成结算的记录
     */
    public Map<String, Object> commissions(Long userId, Integer status, Integer level, boolean today,
                                           int page, int size) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = findDistributor(userId);
        if (distributor == null) {
            res.put("total", 0);
            res.put("list", new ArrayList<>());
            return res;
        }
        Page<CommissionRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getDistributorId, distributor.getId());
        wrapper.eq(status != null, CommissionRecord::getStatus, status);
        wrapper.eq(level != null && level > 0, CommissionRecord::getLevel, level);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        if (today) {
            wrapper.ge(CommissionRecord::getSettleTime, LocalDate.now().atStartOfDay());
        }
        wrapper.orderByDesc(CommissionRecord::getCreateTime);
        IPage<CommissionRecord> list = commissionRecordService.page(p, wrapper);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CommissionRecord record : list.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", record.getId());
            row.put("orderId", record.getOrderId());
            row.put("orderNo", record.getOrderNo());
            row.put("goodsName", record.getGoodsName());
            row.put("orderAmount", nz(record.getOrderAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("commissionAmount", nz(record.getCommissionAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("commissionRate", nz(record.getCommissionRate()));
            row.put("level", record.getLevel());
            row.put("status", record.getStatus());
            row.put("settleTime", record.getSettleTime());
            row.put("createTime", record.getCreateTime());
            rows.add(row);
        }
        res.put("total", list.getTotal());
        res.put("pages", list.getPages());
        res.put("list", rows);
        return res;
    }

    /** 推广订单 */
    public Map<String, Object> orders(Long userId, int page, int size) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = findDistributor(userId);
        if (distributor == null) {
            res.put("total", 0);
            res.put("list", new ArrayList<>());
            return res;
        }
        Page<DistributionOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<DistributionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionOrder::getDistributorId, distributor.getId());
        wrapper.eq(DistributionOrder::getDeleted, 0);
        wrapper.orderByDesc(DistributionOrder::getCreateTime);
        IPage<DistributionOrder> list = distributionOrderService.page(p, wrapper);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (DistributionOrder order : list.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getId());
            row.put("orderId", order.getOrderId());
            row.put("orderNo", order.getOrderNo());
            row.put("orderAmount", nz(order.getOrderAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("commissionAmount", nz(order.getCommissionAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("orderStatus", order.getOrderStatus());
            row.put("createTime", order.getCreateTime());
            // 佣金实际以 commission_record 为准（可能一/二级各一条）
            LambdaQueryWrapper<CommissionRecord> rw = new LambdaQueryWrapper<>();
            rw.eq(CommissionRecord::getOrderId, order.getOrderId());
            rw.eq(CommissionRecord::getDistributorId, distributor.getId());
            rw.eq(CommissionRecord::getDeleted, 0);
            BigDecimal commission = BigDecimal.ZERO;
            for (CommissionRecord r : commissionRecordService.list(rw)) {
                commission = commission.add(nz(r.getCommissionAmount()));
            }
            row.put("realCommission", commission.setScale(2, RoundingMode.HALF_UP));
            rows.add(row);
        }
        res.put("total", list.getTotal());
        res.put("pages", list.getPages());
        res.put("list", rows);
        return res;
    }

    /**
     * 我推广的客户：按买家去重，带订单数 / 累计金额 / 最近下单时间。
     *
     * <p>只统计未失效的订单（退款关闭的不计入），与「我的团队（下级分销员）」是两个不同口径。
     */
    public Map<String, Object> customers(Long userId, int page, int size) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = findDistributor(userId);
        if (distributor == null) {
            res.put("total", 0);
            res.put("totalCount", 0);
            res.put("list", new ArrayList<>());
            return res;
        }
        long offset = (long) Math.max(page - 1, 0) * size;
        List<Map<String, Object>> rows = distributionOrderMapper
                .selectCustomerStats(distributor.getId(), offset, size);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("buyerId", row.get("buyerId"));
            item.put("orderCount", row.get("orderCount"));
            Object amount = row.get("totalAmount");
            item.put("totalAmount", amount == null ? BigDecimal.ZERO
                    : new BigDecimal(amount.toString()).setScale(2, RoundingMode.HALF_UP));
            Object commission = row.get("totalCommission");
            item.put("totalCommission", commission == null ? BigDecimal.ZERO
                    : new BigDecimal(commission.toString()).setScale(2, RoundingMode.HALF_UP));
            item.put("lastOrderTime", row.get("lastOrderTime"));
            list.add(item);
        }
        long total = distributionOrderMapper.countCustomers(distributor.getId());
        res.put("total", total);
        res.put("totalCount", total);
        res.put("pages", size <= 0 ? 0 : (total + size - 1) / size);
        res.put("list", list);
        return res;
    }

    /** 提现记录 */
    public Map<String, Object> withdrawList(Long userId, int page, int size) {
        Map<String, Object> res = new HashMap<>();
        Distributor distributor = findDistributor(userId);
        if (distributor == null) {
            res.put("total", 0);
            res.put("list", new ArrayList<>());
            return res;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("distributorId", distributor.getId());
        IPage<DistributionWithdraw> list = distributionWithdrawService.pageList(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (DistributionWithdraw w : list.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", w.getId());
            row.put("withdrawNo", w.getWithdrawNo());
            row.put("amount", nz(w.getAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("fee", nz(w.getFee()).setScale(2, RoundingMode.HALF_UP));
            row.put("actualAmount", nz(w.getActualAmount()).setScale(2, RoundingMode.HALF_UP));
            row.put("payType", w.getPayType());
            row.put("status", w.getStatus());
            row.put("auditRemark", w.getAuditRemark());
            row.put("createTime", w.getCreateTime());
            rows.add(row);
        }
        res.put("total", list.getTotal());
        res.put("pages", list.getPages());
        res.put("list", rows);
        return res;
    }

    /** 申请提现 */
    public DistributionWithdraw withdrawApply(Long userId, Map<String, Object> params) {
        Distributor distributor = requireDistributor(userId);
        if (distributor == null) {
            throw new IllegalStateException("还不是分销员");
        }
        DistributionWithdraw withdraw = new DistributionWithdraw();
        withdraw.setDistributorId(distributor.getId());
        withdraw.setShopId(distributor.getShopId());
        withdraw.setUserId(userId);
        withdraw.setAmount(new BigDecimal(String.valueOf(params.getOrDefault("amount", "0"))));
        withdraw.setPayType(params.get("payType") == null ? 1 : Integer.parseInt(params.get("payType").toString()));
        withdraw.setAccountName(str(params.get("accountName")));
        withdraw.setAccountNo(str(params.get("accountNo")));
        withdraw.setBankName(str(params.get("bankName")));
        withdraw.setRemark(str(params.get("remark")));
        return distributionWithdrawService.apply(withdraw);
    }

    /**
     * 申请成为分销员
     *
     * @param userId    当前用户
     * @param params    realName / phone / inviteCode（上级分销员编号，可空）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> apply(Long userId, Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        Distributor exist = findDistributor(userId);
        if (exist != null) {
            res.put("ok", false);
            res.put("reason", exist.getStatus() != null && exist.getStatus() == DISTRIBUTOR_STATUS_NORMAL
                    ? "已是分销员" : "已提交申请，请等待审核");
            res.put("status", exist.getStatus());
            return res;
        }
        DistributionSetting setting = distributionSettingService.getSetting(
                params.get("shopId") == null ? null : Long.parseLong(params.get("shopId").toString()));
        if (setting.getEnabled() == null || setting.getEnabled() != 1) {
            res.put("ok", false);
            res.put("reason", "分销功能未开启");
            return res;
        }

        Distributor distributor = new Distributor();
        distributor.setUserId(userId);
        distributor.setShopId(setting.getShopId() == null ? 0L : setting.getShopId());
        // real_name / phone 在库里是 NOT NULL 且无默认值，C 端可能不填，这里兜底空串
        distributor.setRealName(blankToEmpty(str(params.get("realName"))));
        distributor.setPhone(blankToEmpty(str(params.get("phone"))));
        // 未指定等级时挂平台默认等级，避免佣金比例解析不到
        DistributorLevel defaultLevel = findDefaultLevel(distributor.getShopId());
        if (defaultLevel != null) {
            distributor.setLevelId(defaultLevel.getId());
        }
        // 上级：按邀请码（分销员编号）绑定
        String inviteCode = str(params.get("inviteCode"));
        if (inviteCode != null && !inviteCode.isBlank()) {
            LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Distributor::getDistributorNo, inviteCode.trim());
            wrapper.eq(Distributor::getDeleted, 0);
            wrapper.last("limit 1");
            Distributor parent = distributorService.getOne(wrapper);
            if (parent != null && parent.getStatus() != null && parent.getStatus() == DISTRIBUTOR_STATUS_NORMAL) {
                distributor.setParentId(parent.getId());
            }
        }
        // 无门槛（applyMode = 1 或未配置）直接通过，否则待审核
        Integer applyMode = setting.getApplyMode();
        boolean needAudit = applyMode != null && applyMode == 2;
        distributorService.apply(distributor);
        if (!needAudit) {
            distributor.setStatus(DISTRIBUTOR_STATUS_NORMAL);
            distributor.setAuditTime(LocalDateTime.now());
            distributorService.updateById(distributor);
            if (distributor.getParentId() != null) {
                distributorService.lambdaUpdate()
                        .eq(Distributor::getId, distributor.getParentId())
                        .setSql("sub_count = sub_count + 1")
                        .update();
            }
        }
        res.put("ok", true);
        res.put("needAudit", needAudit);
        res.put("distributorId", distributor.getId());
        res.put("distributorNo", distributor.getDistributorNo());
        return res;
    }

    public Distributor findDistributor(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Distributor::getUserId, userId);
        wrapper.eq(Distributor::getDeleted, 0);
        wrapper.last("limit 1");
        return distributorService.getOne(wrapper);
    }

    private Distributor requireDistributor(Long userId) {
        Distributor distributor = findDistributor(userId);
        if (distributor == null || distributor.getStatus() == null
                || distributor.getStatus() != DISTRIBUTOR_STATUS_NORMAL) {
            return null;
        }
        return distributor;
    }

    private BigDecimal sumSettledToday(Long distributorId) {
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getDistributorId, distributorId);
        wrapper.eq(CommissionRecord::getStatus, DistributionSettleService.RECORD_STATUS_SETTLED);
        wrapper.eq(CommissionRecord::getDeleted, 0);
        wrapper.ge(CommissionRecord::getSettleTime, LocalDate.now().atStartOfDay());
        BigDecimal sum = BigDecimal.ZERO;
        for (CommissionRecord record : commissionRecordService.list(wrapper)) {
            sum = sum.add(nz(record.getCommissionAmount()));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private DistributorLevel findDefaultLevel(Long shopId) {
        try {
            return distributorLevelService.getDefaultLevel(shopId);
        } catch (Exception e) {
            log.warn("查询分销默认等级失败: shopId={}, err={}", shopId, e.getMessage());
            return null;
        }
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveLevelName(Long levelId) {
        if (levelId == null) {
            return null;
        }
        DistributorLevel level = distributorLevelService.getById(levelId);
        return level == null ? null : level.getName();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }
}
