package com.houjiang.merchant.service.impl.finance;

import com.houjiang.merchant.service.finance.IFinanceService;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.vo.finance.FinanceIndexVo;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@Service
public class FinanceServiceImpl implements IFinanceService {
    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    IMallShopService iMallShopService;
    @Resource
    IMallRefundService iMallRefundService;
    @Resource
    IMallSettlementService iMallSettlementService;
    @Resource
    IMallShopWithdrawService iMallShopWithdrawService;
    @Override
    public FinanceIndexVo index(Integer shopId) {
        MallShopDetailVo shop = iMallShopService.detail(shopId);
        FinanceIndexVo vo = new FinanceIndexVo();
        vo.setSaleMoney(shop.getSaleMoney());
        vo.setSaleOrders(iMallOrderService.getPayIsCount(shopId));
        vo.setRefundMoney(iMallRefundService.getRefunMoneyByShopId(shopId));
        vo.setRefundWaitMoney(iMallRefundService.getRefundWaitMoneyByShopId(shopId));
        vo.setSettlementOrders(iMallSettlementService.getSettlementOrders(shopId));
        vo.setSettlementOrderMoney(iMallSettlementService.getSettlementOrderMoney(shopId));
        vo.setSettlementWaitNum(iMallOrderService.getWaitSettlementAllOrderNum(shopId));
        vo.setSettlementWaitMoney(iMallOrderService.getWaitSettlementAllOrderMoeny(shopId));
        vo.setSettlementMoney(iMallSettlementService.getSettlementMoney(shopId));
        vo.setSettlementServiceMoney(iMallSettlementService.getSettlementServiceMoney(shopId));
        vo.setCanWithdrawMoney(shop.getMoney());
        vo.setWithdrawMoney(iMallShopWithdrawService.getWithdrawMoney(shopId));
        vo.setWithdrawServiceMoney(iMallShopWithdrawService.getWithdrawServiceMoney(shopId));
        vo.setWithdrawSuccessMoney(iMallShopWithdrawService.getWithdrawSuccessMoney(shopId));
        return vo;
    }
}
