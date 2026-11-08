package com.houjiang.merchant.service.impl.internal;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.CommonShopStateDTO;
import com.houjiang.commons.dto.goods.ManageShopAddressDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.order.ManageShopAddressDetailDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.shop.ArticleShopDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryEsDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.CommonParameterVO;
import com.houjiang.commons.vo.order.GoodsInformationVO;
import com.houjiang.commons.vo.order.StoreInformationVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.order.WapShopShippingQueryResultVO;
import com.houjiang.commons.vo.order.WapShopShippingResultVO;
import com.houjiang.commons.vo.shop.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.merchant.common.entity.mall.MallExpressTpl;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.entity.mall.MallShopAccountPeriod;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopBank;
import com.houjiang.merchant.common.mapper.mall.MallExpressTplMapper;
import com.houjiang.merchant.common.mapper.mall.MallGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallGoodsSkuMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopAccountLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopAccountPeriodMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopBankMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.service.internal.MerchantInternalService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 内部店铺 API：基于 hou_merchant_mall_shop 承接原 business Feign。
 * 门店/运费计价等复杂能力在 P1/P2 继续补齐，本实现保证调用方可联通。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantInternalServiceImpl implements MerchantInternalService {

    private final MallShopMapper mallShopMapper;
    private final MallShopBankMapper mallShopBankMapper;
    private final MallExpressTplMapper mallExpressTplMapper;
    private final MallGoodsMapper mallGoodsMapper;
    private final MallGoodsSkuMapper mallGoodsSkuMapper;
    private final MallShopAccountPeriodMapper mallShopAccountPeriodMapper;
    private final MallShopAccountLogMapper mallShopAccountLogMapper;
    private final MallShopAdminMapper mallShopAdminMapper;
    private final MallOrderMapper mallOrderMapper;

    /** 账户流水：变动对象 0余额 */
    private static final int CHANGE_OBJECT_BALANCE = 0;

    /** 账户流水：变动类型 10 质保金充值（与 payment assetDetailMap 的 detailType 对齐） */
    private static final int CHANGE_TYPE_RETENTION_RECHARGE = 10;

    /** 账户流水：账户状态 0无变化 1增益 2减益 */
    private static final int ACCOUNT_STATUS_GAIN = 1;

    /** 账户流水 params 列的长度上限，超出截断，避免整条流水写不进去 */
    private static final int PARAMS_MAX_LEN = 250;

    private boolean notDeleted(MallShop shop) {
        return shop != null && (shop.getDeleteTime() == null || shop.getDeleteTime() == 0L);
    }

    /** merchant 1正常 → 旧体系 3开店成功；0冻结 → 0关闭 */
    private int toLegacyStatus(Integer merchantStatus) {
        if (merchantStatus != null && merchantStatus == 1) {
            return CommonType.storeStatus.SUCCESSFULLY.getCode();
        }
        return CommonType.storeStatus.CLOSE.getCode();
    }

    private Integer toMerchantStatusFilter(String legacyStatus) {
        if (!StringUtils.hasText(legacyStatus)) {
            return null;
        }
        if (String.valueOf(CommonType.storeStatus.SUCCESSFULLY.getCode()).equals(legacyStatus)) {
            return 1;
        }
        if (String.valueOf(CommonType.storeStatus.CLOSE.getCode()).equals(legacyStatus)) {
            return 0;
        }
        try {
            return Integer.parseInt(legacyStatus);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private MallShop findShop(Integer shopId) {
        if (shopId == null) {
            return null;
        }
        // 表结构未对齐实体全字段，禁用 selectById
        MallShop shop = mallShopMapper.selectLiteById(shopId);
        return notDeleted(shop) ? shop : null;
    }

    private GdsShopDetailResultVO toDetail(MallShop shop) {
        if (shop == null) {
            return null;
        }
        GdsShopDetailResultVO vo = new GdsShopDetailResultVO();
        vo.setShopId(shop.getId());
        vo.setShopName(shop.getName());
        vo.setUserId(shop.getOwnerUserId());
        vo.setShopLogo(shop.getLogo());
        vo.setWapShopLogo(shop.getLogo());
        vo.setShopCount(shop.getSaleOnlineGoods());
        vo.setShopCollect(shop.getVisitNum());
        vo.setOverallScore(shop.getScore() == null ? null : shop.getScore().doubleValue());
        vo.setShopTel(shop.getMobile());
        vo.setDistrictId(shop.getDistrictId());
        vo.setShopSelfSupport(shop.getType() != null && shop.getType() == 0 ? "true" : "false");
        vo.setShopType(shop.getLegacyShopType() != null ? shop.getLegacyShopType() : 1);
        vo.setShopGradeId(shop.getShopLevelId());
        vo.setShopInvoices(shop.getInvoiceSwitchStatus());
        vo.setInvoiceActive(shop.getInvoiceSwitchStatus());
        return vo;
    }

    @Override
    public ResponseResult queryShopStatus(Map<String, String> shopMap) {
        // hou_merchant_mall_shop 无 owner_user_id / legacy_shop_type，仅按 shopId 查
        if (shopMap == null || !StringUtils.hasText(shopMap.get("shopId"))) {
            return ResponseResult.success((Object) null);
        }
        MallShop shop = findShop(Integer.parseInt(shopMap.get("shopId")));
        if (shop == null) {
            return ResponseResult.success((Object) null);
        }
        Integer ms = toMerchantStatusFilter(shopMap.get("shopStatus"));
        if (ms != null && !Objects.equals(shop.getShopStatus(), ms)) {
            return ResponseResult.success((Object) null);
        }
        CommonShopStateDTO dto = new CommonShopStateDTO();
        dto.setShopId(shop.getId());
        dto.setShopStatus(toLegacyStatus(shop.getShopStatus()));
        return ResponseResult.success(dto);
    }

    @Override
    public String selectShopNameByShopId(String shopId) {
        if (!StringUtils.hasText(shopId)) {
            return null;
        }
        try {
            MallShop shop = findShop(Integer.parseInt(shopId));
            return shop == null ? null : shop.getName();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ResponseResult queryInvoiceSwitch(Map<String, Object> param) {
        return ResponseResult.success(queryShopInvoiceSwitch(param));
    }

    @Override
    public ResponseResult delShopInfo(Map<String, String> map) {
        if (map == null || !StringUtils.hasText(map.get("shopId"))) {
            return ResponseResult.fail("shopId 不能为空");
        }
        Integer shopId = Integer.parseInt(map.get("shopId"));
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return ResponseResult.success();
        }
        shop.setShopStatus(0);
        shop.setDeleteTime(System.currentTimeMillis() / 1000);
        shop.setUpdateTime(shop.getDeleteTime());
        mallShopMapper.updateById(shop);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryGateShopIdByUserId(Map<String, String> gateMap) {
        // 表无 owner_user_id，门店绑定待 P2；暂返回 null 避免坏 SQL
        return ResponseResult.success((Object) null);
    }

    @Override
    public GdsShopDetailResultVO queryShopinfoByShopId(Integer shopId) {
        return toDetail(findShop(shopId));
    }

    @Override
    public List<String> queryMarketingActivitiesList(Integer shopId) {
        return Collections.emptyList();
    }

    @Override
    public ResponseResult insertVkShopBase(UserInfoVO userInfo) {
        log.info("insertVkShopBase deferred to P3, userId={}", userInfo == null ? null : userInfo.getUserId());
        return ResponseResult.success();
    }

    @Override
    public ResponseResult updateShopCollect(Map<String, String> paraMap) {
        return ResponseResult.success();
    }

    @Override
    public ResponseResult addShopUser(ShopUserDTO shopUserDTO) {
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryAddImageSpaceLimit(UserTokenBeanDTO userTokenBeanDTO) {
        Map<String, Object> data = new HashMap<>(2);
        data.put("remain", Integer.MAX_VALUE);
        data.put("limit", Integer.MAX_VALUE);
        return ResponseResult.success(data);
    }

    @Override
    public String getUserIDByShopId(Integer shopId) {
        MallShop shop = findShop(shopId);
        return shop == null ? null : shop.getOwnerUserId();
    }

    @Override
    public ResponseResult selectTemplateById(Integer templateId) {
        if (templateId == null) {
            return ResponseResult.success((Object) null);
        }
        MallExpressTpl tpl = mallExpressTplMapper.selectById(templateId);
        return ResponseResult.success(tpl);
    }

    @Override
    public ResponseResult selectAreaById(Integer areaId) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public void updateVkShopSaleNum(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            log.warn("更新分销小店销量被跳过：订单号为空");
            return;
        }
        MallOrder order = mallOrderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                .select(MallOrder::getShopId, MallOrder::getPayMoney)
                .eq(MallOrder::getSn, orderId)
                .last("LIMIT 1"));
        if (order == null || order.getShopId() == null) {
            log.warn("更新分销小店销量失败：merchant 未找到该订单，无法定位店铺 orderId={}", orderId);
            return;
        }
        BigDecimal payMoney = order.getPayMoney() == null ? BigDecimal.ZERO : order.getPayMoney();
        int payMoneyInt = payMoney.setScale(0, RoundingMode.DOWN).intValue();
        int rows = mallShopMapper.update(new MallShop(), new LambdaUpdateWrapper<MallShop>()
                .eq(MallShop::getId, order.getShopId())
                .setSql("sale_orders = IFNULL(sale_orders, 0) + 1")
                .setSql("sale_money = IFNULL(sale_money, 0) + " + payMoneyInt));
        if (rows <= 0) {
            log.warn("更新分销小店销量未命中任何店铺 orderId={}, shopId={}", orderId, order.getShopId());
        }
    }

    @Override
    public ResponseResult recharge(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            return ResponseResult.fail("质保金充值参数为空");
        }
        String orderSn = firstText(map, "orderId", "order_id", "orderSn", "order_sn", "sn");
        Integer shopId = firstInt(map, "shopId", "shop_id");
        BigDecimal amount = firstDecimal(map, "payPrice", "pay_price", "amount", "money");
        if (!StringUtils.hasText(orderSn) || shopId == null || amount == null) {
            log.warn("质保金充值被拒绝：参数不完整 orderSn={}, shopId={}, amount={}", orderSn, shopId, amount);
            return ResponseResult.fail("质保金充值参数不完整：orderId/shopId/payPrice 必填");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("质保金充值被拒绝：金额非正 orderSn={}, amount={}", orderSn, amount);
            return ResponseResult.fail("质保金充值金额必须大于 0");
        }

        // 幂等：充值单号由 payment 侧生成，同一单号重复到达只记一次账
        Long duplicated = mallShopAccountLogMapper.selectCount(new LambdaQueryWrapper<MallShopAccountLog>()
                .eq(MallShopAccountLog::getFromSn, orderSn)
                .eq(MallShopAccountLog::getChangeType, CHANGE_TYPE_RETENTION_RECHARGE));
        if (duplicated != null && duplicated > 0) {
            log.info("质保金充值重复请求已忽略 orderSn={}", orderSn);
            return ResponseResult.success(1, "重复充值请求已忽略");
        }

        // 先增余额再记流水：余额没加上就必须让 payment 回滚，绝不能返回假成功
        int rows = mallShopMapper.update(new MallShop(), new LambdaUpdateWrapper<MallShop>()
                .eq(MallShop::getId, shopId)
                .setSql("retention_money_balance = IFNULL(retention_money_balance, 0) + " + amount.toPlainString()));
        if (rows <= 0) {
            log.warn("质保金充值失败：店铺不存在或未命中 shopId={}, orderSn={}", shopId, orderSn);
            return ResponseResult.fail("质保金充值失败：店铺不存在");
        }

        MallShopAccountLog accountLog = new MallShopAccountLog();
        long now = System.currentTimeMillis();
        accountLog.setCreateTime(now);
        accountLog.setUpdateTime(now);
        accountLog.setDeleteTime(0L);
        accountLog.setShopId(shopId);
        accountLog.setSn(orderSn);
        accountLog.setChangeObject(CHANGE_OBJECT_BALANCE);
        accountLog.setChangeType(CHANGE_TYPE_RETENTION_RECHARGE);
        accountLog.setAccountStatus(ACCOUNT_STATUS_GAIN);
        accountLog.setFromId(shopId);
        accountLog.setFromSn(orderSn);
        accountLog.setChangeAmount(amount);
        accountLog.setLeftAmount(currentRetentionBalance(shopId));
        accountLog.setRemark("质保金充值");
        accountLog.setParams(truncate(JSON.toJSONString(map), PARAMS_MAX_LEN));
        try {
            mallShopAccountLogMapper.insert(accountLog);
        } catch (DuplicateKeyException e) {
            log.info("质保金充值并发重复请求已忽略 orderSn={}", orderSn);
            return ResponseResult.success(1, "重复充值请求已忽略");
        }
        log.info("质保金充值入账成功 shopId={}, orderSn={}, amount={}", shopId, orderSn, amount);
        return ResponseResult.success();
    }

    /** 店铺质保金余额当前值，读不到就返回 null（不臆造余额） */
    private BigDecimal currentRetentionBalance(Integer shopId) {
        MallShop shop = mallShopMapper.selectOne(new LambdaQueryWrapper<MallShop>()
                .select(MallShop::getRetentionMoneyBalance)
                .eq(MallShop::getId, shopId)
                .last("LIMIT 1"));
        return shop == null ? null : shop.getRetentionMoneyBalance();
    }

    private String firstText(Map<Object, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private Integer firstInt(Map<Object, Object> map, String... keys) {
        String text = firstText(map, keys);
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal firstDecimal(Map<Object, Object> map, String... keys) {
        String text = firstText(map, keys);
        if (text == null) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }

    @Override
    public ShopUserMessageResultVO queryUserId(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return null;
        }
        // hou_merchant_mall_shop 没有 owner_user_id 列，店铺归属用户只能从店铺管理员表取
        MallShopAdmin admin = mallShopAdminMapper.selectOne(new LambdaQueryWrapper<MallShopAdmin>()
                .select(MallShopAdmin::getUserId)
                .eq(MallShopAdmin::getShopId, shopId)
                .orderByDesc(MallShopAdmin::getIsSuper)
                .last("LIMIT 1"));
        ShopUserMessageResultVO vo = new ShopUserMessageResultVO();
        vo.setUserId(admin == null || admin.getUserId() == null ? null : String.valueOf(admin.getUserId()));
        vo.setShopName(shop.getName());
        vo.setShopTel(shop.getMobile());
        return vo;
    }

    @Override
    public ShopBankMessageVO getShopBankMessage(String shopId) {
        if (!StringUtils.hasText(shopId)) {
            return null;
        }
        Integer id = Integer.parseInt(shopId);
        LambdaQueryWrapper<MallShopBank> qw = new LambdaQueryWrapper<>();
        qw.eq(MallShopBank::getShopId, id)
                .and(w -> w.isNull(MallShopBank::getDeleteTime).or().eq(MallShopBank::getDeleteTime, 0L))
                .orderByDesc(MallShopBank::getIsDefault)
                .last("LIMIT 1");
        MallShopBank bank = mallShopBankMapper.selectOne(qw);
        if (bank == null) {
            return null;
        }
        ShopBankMessageVO vo = new ShopBankMessageVO();
        vo.setBankAccountName(bank.getUsername());
        vo.setBankAccountNumber(bank.getCardNumber());
        vo.setBankName(bank.getName());
        vo.setBankAddress(bank.getBranch());
        return vo;
    }

    @Override
    public AccountPeriodVO getAccountPeriodByIds(Integer shopId, String userId) {
        if (shopId == null || !StringUtils.hasText(userId)) {
            log.warn("查询账期参数不完整 shopId={}, userId={}", shopId, userId);
            return null;
        }
        MallShopAccountPeriod entity = mallShopAccountPeriodMapper.selectOne(
                new LambdaQueryWrapper<MallShopAccountPeriod>()
                        .eq(MallShopAccountPeriod::getShopId, shopId)
                        .eq(MallShopAccountPeriod::getUserId, userId)
                        .last("LIMIT 1"));
        if (entity == null) {
            return null;
        }
        AccountPeriodVO vo = new AccountPeriodVO();
        vo.setAccountId(entity.getAccountId());
        vo.setShopId(entity.getShopId());
        vo.setUserId(entity.getUserId());
        vo.setIsAccount(entity.getIsAccount());
        vo.setAccountIsLimit(entity.getAccountIsLimit());
        vo.setAccountPeriodPrice(entity.getAccountPeriodPrice());
        vo.setAccountPeriodTime(entity.getAccountPeriodTime());
        vo.setAccountInsertTime(entity.getAccountInsertTime());
        vo.setAmountUsed(entity.getAmountUsed());
        vo.setAvailableAmount(entity.getAvailableAmount());
        vo.setMonthAmountUsed(entity.getMonthAmountUsed());
        return vo;
    }

    @Override
    public void updateAccountPeriod(AccountPeriodParamDTO periodParam) {
        if (periodParam == null || periodParam.getShopId() == null || !StringUtils.hasText(periodParam.getUserId())) {
            // 内部接口静默吞掉会让 payment 以为账期已更新，这里直接快速失败
            throw new IllegalArgumentException("更新账期参数不完整：shopId 与 userId 必填");
        }
        MallShopAccountPeriod entity = mallShopAccountPeriodMapper.selectOne(
                new LambdaQueryWrapper<MallShopAccountPeriod>()
                        .eq(MallShopAccountPeriod::getShopId, periodParam.getShopId())
                        .eq(MallShopAccountPeriod::getUserId, periodParam.getUserId())
                        .last("LIMIT 1"));
        Date now = new Date();
        if (entity == null) {
            entity = new MallShopAccountPeriod();
            entity.setShopId(periodParam.getShopId());
            entity.setUserId(periodParam.getUserId());
            entity.setIsAccount(defaultZero(periodParam.getIsAccount()));
            entity.setAccountIsLimit(defaultZero(periodParam.getAccountIsLimit()));
            entity.setAccountPeriodPrice(defaultZero(periodParam.getAccountPeriodPrice()));
            entity.setAccountPeriodTime(defaultZero(periodParam.getAccountPeriodTime()));
            entity.setAmountUsed(defaultZero(periodParam.getAmountUsed()));
            entity.setAvailableAmount(defaultZero(periodParam.getAvailableAmount()));
            entity.setMonthAmountUsed(defaultZero(periodParam.getMonthAmountUsed()));
            entity.setAccountInsertTime(now);
            entity.setAccountUpdateTime(now);
            try {
                mallShopAccountPeriodMapper.insert(entity);
                return;
            } catch (DuplicateKeyException e) {
                // 并发下同一店铺+用户已被插入，转为更新
                entity = mallShopAccountPeriodMapper.selectOne(
                        new LambdaQueryWrapper<MallShopAccountPeriod>()
                                .eq(MallShopAccountPeriod::getShopId, periodParam.getShopId())
                                .eq(MallShopAccountPeriod::getUserId, periodParam.getUserId())
                                .last("LIMIT 1"));
                if (entity == null) {
                    throw e;
                }
            }
        }
        applyPeriodChanges(entity, periodParam);
        entity.setAccountUpdateTime(now);
        mallShopAccountPeriodMapper.updateById(entity);
    }

    /** 只覆盖调用方显式传入的字段，避免把已有值清成 null */
    private void applyPeriodChanges(MallShopAccountPeriod entity, AccountPeriodParamDTO periodParam) {
        if (periodParam.getIsAccount() != null) {
            entity.setIsAccount(periodParam.getIsAccount());
        }
        if (periodParam.getAccountIsLimit() != null) {
            entity.setAccountIsLimit(periodParam.getAccountIsLimit());
        }
        if (periodParam.getAccountPeriodPrice() != null) {
            entity.setAccountPeriodPrice(periodParam.getAccountPeriodPrice());
        }
        if (periodParam.getAccountPeriodTime() != null) {
            entity.setAccountPeriodTime(periodParam.getAccountPeriodTime());
        }
        if (periodParam.getAmountUsed() != null) {
            entity.setAmountUsed(periodParam.getAmountUsed());
        }
        if (periodParam.getAvailableAmount() != null) {
            entity.setAvailableAmount(periodParam.getAvailableAmount());
        }
        if (periodParam.getMonthAmountUsed() != null) {
            entity.setMonthAmountUsed(periodParam.getMonthAmountUsed());
        }
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Override
    public ResponseResult getShopList(Integer status) {
        LambdaQueryWrapper<MallShop> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w.isNull(MallShop::getDeleteTime).or().eq(MallShop::getDeleteTime, 0L));
        if (status != null) {
            // orderfood 用营业状态
            qw.eq(MallShop::getBusinessStatus, status);
        }
        return ResponseResult.success(mallShopMapper.selectList(qw));
    }

    @Override
    public ResponseResult getShopById(Integer shopId) {
        return ResponseResult.success(findShop(shopId));
    }

    @Override
    public ResponseResult queryShipping(String message) {
        Exception last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return doQueryShipping(message);
            } catch (Exception e) {
                last = e;
                log.warn("queryShipping attempt {} failed: {}", attempt, rootMessage(e), e);
                if (attempt < 2 && isTransientDbError(e)) {
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                break;
            }
        }
        return ResponseResult.fail(11009, "运费查询失败: " + rootMessage(last));
    }

    private ResponseResult doQueryShipping(String message) {
        CommonParameterVO cp = JSON.parseObject(message, CommonParameterVO.class);
        if (cp == null || CollectionUtils.isEmpty(cp.getStoreInformationList())) {
            return ResponseResult.fail(11009, "订单商品信息不能为空");
        }
        boolean virtualOrder = Objects.equals(cp.getVirtualGoods(), 0);
        List<WapShopShippingQueryResultVO> shopList = new ArrayList<>();
        for (StoreInformationVO store : cp.getStoreInformationList()) {
            if (store == null || store.getShopId() == null || CollectionUtils.isEmpty(store.getGoodsBaseList())) {
                return ResponseResult.fail(11009, "店铺或商品信息缺失");
            }
            MallShop shop = findShop(store.getShopId());
            if (shop == null) {
                return ResponseResult.fail(11009, "店铺不存在: " + store.getShopId());
            }

            List<WapShippingQueryResultVO> goodsRows = new ArrayList<>();
            BigDecimal shopShippingFee = BigDecimal.ZERO;
            for (GoodsInformationVO goodsInfo : store.getGoodsBaseList()) {
                if (goodsInfo == null || goodsInfo.getGoodsId() == null) {
                    return ResponseResult.fail(11009, "商品ID不能为空");
                }
                WapShippingQueryResultVO row = buildShippingGoodsRow(goodsInfo, shop.getId(), virtualOrder);
                if (row == null) {
                    return ResponseResult.fail(11009, "商品不存在或已删除: " + goodsInfo.getGoodsId());
                }
                goodsRows.add(row);
                if (row.getGoodsShippingFee() != null) {
                    shopShippingFee = shopShippingFee.add(row.getGoodsShippingFee());
                }
            }

            WapShopShippingResultVO packageVo = new WapShopShippingResultVO();
            packageVo.setGrossFreight(shopShippingFee);
            packageVo.setGoodsBaseList(goodsRows);

            WapShopShippingQueryResultVO shopVo = new WapShopShippingQueryResultVO();
            shopVo.setShopId(shop.getId());
            shopVo.setShopName(shop.getName());
            shopVo.setShippingFee(virtualOrder ? BigDecimal.ZERO : shopShippingFee);
            shopVo.setBuyerMessage(store.getBuyerMessage());
            shopVo.setShopSelfSupport(Objects.equals(shop.getType(), 0));
            shopVo.setPlatForm(cp.getPlatForm());
            shopVo.setGoodsBaseList(List.of(packageVo));
            shopVo.setShippingFeeList(List.of(shopVo.getShippingFee()));
            shopList.add(shopVo);
        }
        return ResponseResult.success(shopList);
    }

    private static boolean isTransientDbError(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String m = t.getMessage();
            String n = t.getClass().getName();
            if (n.contains("CannotGetJdbcConnection") || n.contains("SQLTransientConnection")
                    || n.contains("ConnectionIsClosed") || (m != null && m.contains("Connection is not available"))) {
                return true;
            }
        }
        return false;
    }

    private static String rootMessage(Throwable e) {
        if (e == null) {
            return "unknown";
        }
        Throwable cur = e;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getMessage();
        }
        if (msg == null || msg.isBlank()) {
            msg = cur.getClass().getSimpleName();
        }
        return msg.length() > 240 ? msg.substring(0, 240) : msg;
    }

    /**
     * 将下单商品映射为计价所需运费商品行（优先按 SKU id，其次商品 id）。
     */
    private WapShippingQueryResultVO buildShippingGoodsRow(GoodsInformationVO goodsInfo, Integer shopId, boolean virtualOrder) {
        Integer reqGoodsId = goodsInfo.getGoodsId();
        Integer goodsNum = goodsInfo.getGoodsNum() == null || goodsInfo.getGoodsNum() <= 0 ? 1 : goodsInfo.getGoodsNum();

        // C 端商品在 hou_mall_*；MP 默认表前缀会落到空的 hou_merchant_mall_*，故显式走 front 查询
        MallGoodsSku sku = mallGoodsSkuMapper.selectFrontById(reqGoodsId);
        MallGoods goods = null;
        if (sku != null && (shopId == null || Objects.equals(sku.getShopId(), shopId))) {
            goods = mallGoodsMapper.selectFrontById(sku.getGoodsId());
        }
        if (goods == null) {
            goods = mallGoodsMapper.selectFrontById(reqGoodsId);
            sku = null;
            if (goods != null && shopId != null && !Objects.equals(goods.getShopId(), shopId)) {
                return null;
            }
            if (goods != null) {
                List<MallGoodsSku> skus = mallGoodsSkuMapper.selectFrontByGoodsId(goods.getId());
                if (!CollectionUtils.isEmpty(skus)) {
                    sku = skus.get(0);
                }
            }
        }
        if (goods == null) {
            return null;
        }

        BigDecimal price = sku != null && sku.getPrice() != null ? sku.getPrice()
                : (goods.getMinPrice() != null ? goods.getMinPrice() : BigDecimal.ZERO);
        BigDecimal marketPrice = sku != null && sku.getMarketPrice() != null ? sku.getMarketPrice()
                : (goods.getMaxLinePrice() != null ? goods.getMaxLinePrice() : price);
        Integer stock = sku != null && sku.getStock() != null ? sku.getStock()
                : (goods.getTotalStock() != null ? goods.getTotalStock() : 0);
        String image = (sku != null && StringUtils.hasText(sku.getImage())) ? sku.getImage() : goods.getImage();
        String spec = sku != null ? sku.getSkuValueArr() : null;

        BigDecimal unitShipping = BigDecimal.ZERO;
        if (!virtualOrder) {
            Integer expressType = goods.getExpressType();
            if (Objects.equals(expressType, 2) && goods.getExpressMoney() != null) {
                unitShipping = goods.getExpressMoney();
            }
            // expressType 0 包邮；1 模板暂按 0（后续可接 MallExpressTpl）
        }
        BigDecimal lineShipping = unitShipping.multiply(BigDecimal.valueOf(goodsNum));

        WapShippingQueryResultVO row = new WapShippingQueryResultVO();
        row.setGoodsId(sku != null ? sku.getId() : goods.getId());
        row.setCommonId(goods.getId());
        row.setGoodsName(goods.getName());
        row.setGoodsSpec(spec);
        row.setGoodsImage(image);
        row.setGoodsPrice(price);
        row.setGoodsMarketPrice(marketPrice);
        row.setGoodsNum(goodsNum);
        row.setGoodsStock(stock);
        row.setGoodsIsShelves(Objects.equals(goods.getStatus(), 1) ? 1 : 2);
        row.setCommonState(Objects.equals(goods.getStatus(), 1) ? 1 : 0);
        row.setCommonVerify(Objects.equals(goods.getAuditStatus(), 1) ? 1 : 0);
        row.setActivityStatus(0);
        row.setDealerGoodsFlag(0);
        row.setDealerGoodsPrice(BigDecimal.ZERO);
        row.setFxGoods(0);
        row.setGoodsType(1);
        row.setVirtualGoods(virtualOrder ? 0 : 1);
        row.setGoodsCubage(sku != null && sku.getWeight() != null ? sku.getWeight() : BigDecimal.ZERO);
        row.setGoodsShippingFee(lineShipping);
        row.setTotalGoodsShippingFee(lineShipping);
        row.setTransportTemplate(goods.getExpressTplId());
        return row;
    }

    @Override
    public ResponseResult querySupplierShipping(String message) {
        // 供应商计价暂复用商城运费组装逻辑
        return queryShipping(message);
    }

    @Override
    public Boolean queryShopSelfSupport(Integer shopId) {
        MallShop shop = findShop(shopId);
        return shop != null && Objects.equals(shop.getType(), 0);
    }

    @Override
    public List<Integer> queryShopGoodsCatIds(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null || shop.getShopCategoryId() == null) {
            return Collections.emptyList();
        }
        return List.of(shop.getShopCategoryId());
    }

    @Override
    public Integer queryGoodsCatByGoodsId(Integer goodsId) {
        return null;
    }

    @Override
    public ResponseResult getGoodsDetails(Integer goodsId) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public ResponseResult queryShopInvoiceSwitchStatus(Map<String, Object> paramTmp) {
        InvoiceSwitchEntityVO data = queryShopInvoiceSwitch(paramTmp);
        int result = 0;
        if (data != null) {
            Integer sw = data.getInvoiceSwitchStatus();
            Integer audit = data.getInvoiceAuditStatus();
            if (Objects.equals(sw, 1) && (Objects.equals(audit, 1) || Objects.equals(audit, 0))) {
                result = 1;
            }
        }
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult queryShopGateId(Map<String, Object> paramMap) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public void updateShopGateStock(String saleSum) {
        log.debug("updateShopGateStock deferred");
    }

    @Override
    public ResponseResult shopScore(Map<Object, Object> map) {
        Object shopIdObj = map == null ? null : map.get("shopId");
        if (shopIdObj == null) {
            return ResponseResult.success((Object) null);
        }
        Integer shopId = Integer.parseInt(String.valueOf(shopIdObj));
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return ResponseResult.success((Object) null);
        }
        Map<String, Object> score = new HashMap<>(4);
        score.put("shopId", shop.getId());
        score.put("score", shop.getScore());
        score.put("star", shop.getStar());
        return ResponseResult.success(score);
    }

    @Override
    public ResponseResult queryShopAddressDetail(ManageShopAddressDetailDTO param) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public ManageShopAddressDTO queryShippingAddressByShopId(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return null;
        }
        ManageShopAddressDTO dto = new ManageShopAddressDTO();
        // 字段以 DTO 实际定义为准，缺失则仅保证非空对象
        return dto;
    }

    @Override
    public ResponseResult queryGateDetail(Map<String, Object> param) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public ResponseResult saveShopEvaluationInfo(Map<String, Object> param) {
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryShopStampByShopId(Map<String, Object> shopMap) {
        if (shopMap == null || shopMap.get("shopId") == null) {
            return ResponseResult.success((Object) null);
        }
        Integer shopId = Integer.parseInt(String.valueOf(shopMap.get("shopId")));
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return ResponseResult.success((Object) null);
        }
        Map<String, Object> data = new HashMap<>(2);
        data.put("shopStamp", shop.getShopStamp());
        data.put("shopStampMsg", shop.getShopStampMsg());
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult queryShippingAddress(Integer shippingAddressId) {
        return ResponseResult.success((Object) null);
    }

    @Override
    public Map<String, String> queryGateStoreName(Integer orderGateShopId) {
        MallShop shop = findShop(orderGateShopId);
        Map<String, String> map = new HashMap<>(2);
        if (shop != null) {
            map.put("storeName", shop.getName());
            map.put("address", shop.getAddressInfo());
        }
        return map;
    }

    @Override
    public List<Integer> queryShopIdByShopName(String shopName) {
        if (!StringUtils.hasText(shopName)) {
            return Collections.emptyList();
        }
        return mallShopMapper.getShopIdsByName(shopName);
    }

    @Override
    public WebShopListQueryEsDTO selectByShopId2(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return null;
        }
        WebShopListQueryEsDTO dto = new WebShopListQueryEsDTO();
        dto.setShopId(shop.getId());
        dto.setShopName(shop.getName());
        return dto;
    }

    @Override
    public InvoiceSwitchEntityVO queryShopInvoiceSwitch(Map<String, Object> paramTmp) {
        InvoiceSwitchEntityVO vo = new InvoiceSwitchEntityVO();
        vo.setInvoiceSwitchStatus(0);
        vo.setInvoiceAuditStatus(3);
        if (paramTmp == null || paramTmp.get("shopId") == null) {
            return vo;
        }
        Integer shopId = Integer.parseInt(String.valueOf(paramTmp.get("shopId")));
        MallShop shop = findShop(shopId);
        if (shop != null) {
            vo.setInvoiceSwitchStatus(shop.getInvoiceSwitchStatus() == null ? 0 : shop.getInvoiceSwitchStatus());
            vo.setInvoiceAuditStatus(shop.getInvoiceAuditStatus() == null ? 3 : shop.getInvoiceAuditStatus());
        }
        return vo;
    }

    @Override
    public Integer queryUserIsGate(Map<String, String> map) {
        return null;
    }

    @Override
    public void setShopViewVolume(String userId, Integer shopId, Integer type, Integer goodsId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return;
        }
        int visit = shop.getVisitNum() == null ? 0 : shop.getVisitNum();
        shop.setVisitNum(visit + 1);
        shop.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopMapper.updateById(shop);
    }

    @Override
    public ResponseResult shopIndex(Integer shopId) {
        return ResponseResult.success(toDetail(findShop(shopId)));
    }

    @Override
    public List<TransportTemplateVO> selectTransportByShopId(Integer shopId) {
        if (shopId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<MallExpressTpl> qw = new LambdaQueryWrapper<>();
        qw.eq(MallExpressTpl::getShopId, shopId)
                .and(w -> w.isNull(MallExpressTpl::getDeleteTime).or().eq(MallExpressTpl::getDeleteTime, 0L));
        List<MallExpressTpl> list = mallExpressTplMapper.selectList(qw);
        List<TransportTemplateVO> result = new ArrayList<>();
        for (MallExpressTpl tpl : list) {
            TransportTemplateVO vo = new TransportTemplateVO();
            vo.setId(tpl.getId());
            vo.setName(tpl.getName());
            vo.setStatus(1);
            result.add(vo);
        }
        return result;
    }

    @Override
    public void deleteBindGoodsCat(Integer catId) {
        log.debug("deleteBindGoodsCat deferred, catId={}", catId);
    }

    @Override
    public void deleteClassBindTmp(Integer catId) {
        log.debug("deleteClassBindTmp deferred, catId={}", catId);
    }

    @Override
    public List<ArticleShopDTO> selectAllShop() {
        LambdaQueryWrapper<MallShop> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w.isNull(MallShop::getDeleteTime).or().eq(MallShop::getDeleteTime, 0L));
        return mallShopMapper.selectList(qw).stream().map(shop -> {
            ArticleShopDTO dto = new ArticleShopDTO();
            dto.setShopId(shop.getId());
            dto.setShopName(shop.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ShopListQueryDTO selectByH5ShopId(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return null;
        }
        ShopListQueryDTO dto = new ShopListQueryDTO();
        dto.setShopId(shop.getId());
        dto.setShopName(shop.getName());
        return dto;
    }

    @Override
    public ManageShopBaseVO selectGdsShopBaseByShopId(Integer shopId) {
        MallShop shop = findShop(shopId);
        if (shop == null) {
            return null;
        }
        ManageShopBaseVO vo = new ManageShopBaseVO();
        vo.setShopName(shop.getName());
        vo.setDistrictId(shop.getDistrictId());
        vo.setShopSelfSupport(shop.getType() != null && shop.getType() == 0 ? "2" : "1");
        vo.setShopStatus(toLegacyStatus(shop.getShopStatus()));
        if (shop.getShopCategoryId() != null) {
            vo.setCatIdList(List.of(shop.getShopCategoryId()));
        }
        return vo;
    }

    @Override
    public List<Integer> queryJoinCat(Integer shopId) {
        return queryShopGoodsCatIds(shopId);
    }
}
