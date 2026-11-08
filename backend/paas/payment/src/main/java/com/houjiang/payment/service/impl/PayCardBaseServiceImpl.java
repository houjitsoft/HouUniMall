package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.CardAmountDTO;
import com.houjiang.payment.dto.RechargeCardDTO;
import com.houjiang.payment.dto.ReturnCardBaseInfoDTO;
import com.houjiang.payment.entity.PayCardBaseEntity;
import com.houjiang.payment.entity.PayCardEntity;
import com.houjiang.payment.entity.PayCardInfoEntity;
import com.houjiang.payment.mapper.PayCardBaseMapper;
import com.houjiang.payment.mapper.PayCardInfoMapper;
import com.houjiang.payment.mapper.PayCardMapper;
import com.houjiang.payment.service.PayCardBaseService;
import com.houjiang.payment.vo.*;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class PayCardBaseServiceImpl extends ServiceImpl<PayCardBaseMapper, PayCardBaseEntity>
    implements PayCardBaseService{

    @Autowired
    private PayCardMapper payCardMapper;

    @Autowired
    private PayCardInfoMapper payCardInfoMapper;
    @Override
    public ResponseResult queryDetail(ReturnCardBaseInfoDTO dto) {
        // 通过token获取用户信息，用于后续的操作权限判断
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        Map<String,Object> map=new HashMap();
        // 如果userInfo为null，再次尝试获取用户信息，确保获取用户的准确性
        if (userInfo==null){
            userInfo=ParseTokenUtils.getPlatformUser(dto.getToken());
        }
        // 当用户信息存在时，执行后续业务逻辑
        if (null != userInfo) {
            try {
                // 将dto对象转换为Map，方便后续的数据处理和查询
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(dto);
                // 执行分页查询，获取卡基信息报告
                //分页查询数据
                IPage<PayCardBaseInfoVO> page = this.baseMapper.queryCardbaseInfoReport(new Query<PayCardBaseInfoVO>().getPage(data), dto);

                // 查询未充值金额
                double money = this.baseMapper.queryNoRechargeMoney(dto);

                // 将查询结果和未充值金额放入map中，准备返回给前端
                map.put("amoney",money);
                map.put("result",PageUtils.getPage(page));
                // 返回成功的响应结果，包含查询数据
                return ResponseResult.success(map);
            } catch (IllegalAccessException e) {
                // 当出现异常时，打印堆栈信息，便于问题排查
                e.printStackTrace();
            }

        }
        // 当用户信息不存在或出现异常时，返回token非法的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult queryList(Map<String, Object> map) {
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());

        // 如果用户信息不为空
        if (null != userInfo) {
            // 根据分页参数和查询条件查询支付卡列表
            // 分页查询数据
            IPage<PayCardBaseVO> page = this.baseMapper.queryList(new Query<PayCardBaseVO>().getPage(map), map);

            // 查询激活但未充值的金额
            double money = this.baseMapper.queryActivationNoRechargeMoney();

            // 将查询到的未充值金额和分页结果放入map中
            map.put("amoney", money);
            map.put("result", PageUtils.getPage(page));
            // 返回查询结果成功的响应
            return ResponseResult.success(map);

        }
        // 如果用户信息为空，返回token非法的响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult queryCardConsumeExcel(CardAmountDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            List<CardIncomeListVO> page = this.baseMapper.selectCardConsumeExcel(param);
            String[] headers = {"商户订单id", "用户id", "昵称", "金额", "标题", "描述",
                    "创建时间", "类型", "付款状态", "充值的付款方式", "支付金额", "支付方式", "退款金额"};
            List<Map<String, String>> list = Lists.newArrayList();
            for (CardIncomeListVO cardAmountList : page) {
                HashMap<String, String> map = Maps.newHashMap();
                map.put("商户订单id", cardAmountList.getOrderId());
                map.put("用户id", cardAmountList.getUserId());
                map.put("昵称", cardAmountList.getUserNickname());
                map.put("金额", cardAmountList.getRecordMoney() + "");
                map.put("标题", cardAmountList.getRecordTitle());
                map.put("描述", cardAmountList.getRecordDesc());
                map.put("创建时间", cardAmountList.getRecordTime() == null ? "" :  DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss",cardAmountList.getRecordTime()));
                if (cardAmountList.getTradeTypeId() == null){
                    cardAmountList.setTradeTypeId(-1);
                }
                if (cardAmountList.getRecordStatus() == null){
                    cardAmountList.setRecordStatus(-1);
                }
                    switch (cardAmountList.getTradeTypeId()) {
                        case 1:
                            map.put("类型", "购物");
                            break;
                        case 2:
                            map.put("类型", "转账");
                            break;
                        case 3:
                            map.put("类型", "充值");
                            break;
                        case 4:
                            map.put("类型", "提现");
                            break;
                        case 5:
                            map.put("类型", "退款");
                            break;
                        case 10:
                            map.put("类型", "佣金");
                            break;
                        case 11:
                            map.put("类型", "结算");
                            break;
                        case -1:
                            map.put("类型", "");
                            break;
                    }
                    switch (cardAmountList.getRecordStatus()) {
                        case 1:
                            map.put("付款状态", "待付款");
                            break;
                        case 2:
                            map.put("付款状态", "已付款");
                            break;
                        case 3:
                            map.put("付款状态", "待发货");
                            break;
                        case 4:
                            map.put("付款状态", "已发货");
                            break;
                        case 5:
                            map.put("付款状态", "已签收");
                            break;
                        case 6:
                            map.put("付款状态", "确认收货");
                            break;
                        case 7:
                            map.put("付款状态", "取消");
                            break;
                        case -1:
                            map.put("付款状态", "");
                            break;
                    }
                map.put("充值的付款方式", cardAmountList.getDepositPayChannel() == null ? "" : cardAmountList.getDepositPayChannel().toString());
                map.put("支付金额", cardAmountList.getTradePaymentRechargeCard() + "");
                map.put("支付方式", cardAmountList.getPayCode() + "");
                map.put("退款金额", cardAmountList.getOrderRefundAmount() == null ? "0.00" : cardAmountList.getOrderRefundAmount().toString());
                list.add(map);
            }

            // excel文件名
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("充值消费卡支出", list);
            String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult queryCardConsume(CardAmountDTO param) {
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            try {
                // 将请求参数转换为Map对象，方便后续处理
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 根据请求参数和转换后的Map对象进行分页查询
                //分页查询数据
                IPage<CardIncomeListVO> page = this.baseMapper.selectCardConsume(new Query<CardIncomeListVO>().getPage(data), param);
                // 查询指定条件下的总消费金额
                BigDecimal totalAmount = this.baseMapper.selectCardConsumeAmount(param);

                Integer returnFlag = 0;
                // 遍历查询结果，进行数据处理和计算
                for (CardIncomeListVO e : page.getRecords()) {
                    // 根据支付方式调整总消费金额，或设置交易金额为充值卡金额
                    if (CommonType.payType.CARD_AND_ALIPAY_PAYMENT.getCode().equals(e.getPayCode())
                            || CommonType.payType.CARD_AND_WECHAT_PAYMENT.getCode().equals(e.getPayCode())) {
                        totalAmount = totalAmount.subtract(e.getTradeThirdPartyAmount());
                    } else {
                        e.setTradePaymentRechargeCard(e.getRecordMoney());
                    }
                    // 根据虚拟商品和店铺提取状态，设置订单类型
                    Integer virtualGoods = e.getVirtualGoods();
                    Integer orderIsShopTake = e.getOrderIsShopTake();
                    if( null != orderIsShopTake && 1 == orderIsShopTake){
                        e.setOrderType(3);
                    }else{
                        if(null != virtualGoods && 0 == virtualGoods){
                            e.setOrderType(2);
                            e.setPastDue(e.getPastDue());
                            e.setIsCancel(e.getIsCancel());
                        }else{
                            e.setOrderType(1);
                        }
                    }
                    // 初始化退款和退货状态
                    Integer returnStatus =e.getOrderReturnStatus()==null?0:Integer.valueOf(e.getOrderReturnStatus()) ;
                    Integer refundStatus = e.getOrderRefundStatus()==null?0:Integer.valueOf(e.getOrderRefundStatus());
                    // 根据退款和退货状态，计算订单的退还款项，并设置返回标志
                    if (2 == returnStatus || 2 == refundStatus) {
                        BigDecimal returnAmount=null;
                        if(e.getOrderRefundAmount()==null){
                            returnAmount = BigDecimal.valueOf(0);
                        }else{
                            returnAmount = e.getOrderRefundAmount();
                        }

                        BigDecimal orderAmount = e.getOrderPaymentAmount();
                        if (orderAmount.compareTo(returnAmount) > 0) {
                            returnFlag = 2;
                        } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                            returnFlag = 1;
                        }
                    }
                    e.setReturnFlag(returnFlag);
                    // 根据退款和退货状态，设置是否存在退货标志
                    if(0 != returnStatus && 0 != refundStatus){
                        e.setIsExistReturn("1");
                    }else{
                        e.setIsExistReturn("0");
                    }

                }
                // 构建返回的数据Map，包含总消费金额和分页信息
                Map<String, Object> map = new HashMap<>();
                map.put("totalAmount", totalAmount);
                map.put("currPage", PageUtils.getPage(page).getCurrPage());
                map.put("pageSize", PageUtils.getPage(page).getPageSize());
                map.put("list", PageUtils.getPage(page).getList());
                map.put("totalCount", PageUtils.getPage(page).getTotalCount());
                map.put("totalPage", PageUtils.getPage(page).getTotalPage());

                // 返回处理后的数据
                return ResponseResult.success(map);
            } catch (IllegalAccessException e) {
                // 打印异常信息
                e.printStackTrace();
            }
        }
        // 如果token非法，返回失败结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult queryCardAmountListExcel(CardAmountDTO cardAmountQueryParam) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(cardAmountQueryParam.getToken());
        if (null != userInfo) {
            List<CardAmountListVO> cardAmountLists = this.baseMapper.queryCardAmountListExcel(cardAmountQueryParam);
            String[] headers = {"购物卡卡号", "购物卡密码", "购物卡面额", "领卡人账号", "卡片描述", "有效时间", "失效时间"};
            List<Map<String, String>> list = Lists.newArrayList();
            for (CardAmountListVO cardAmountList : cardAmountLists) {
                HashMap<String, String> map = Maps.newHashMap();
                map.put("购物卡卡号", cardAmountList.getCardCode());
                map.put("购物卡密码", cardAmountList.getCardPassword());
                map.put("购物卡面额", cardAmountList.getCardMoney());
                map.put("领卡人账号", cardAmountList.getUserAccount());
                map.put("卡片描述", cardAmountList.getCardDesc());
                map.put("有效时间", DateUtils.dateTime(cardAmountList.getCardStartTime()));
                map.put("失效时间", DateUtils.dateTime(cardAmountList.getCardEndTime()));
                list.add(map);
            }

            // excel文件名
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("合并数据", list);
            String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据卡余额查询条件，查询卡余额列表。
     *
     * @param cardAmountQueryParam 卡余额查询条件的DTO（数据传输对象）。
     * @return 查询结果的响应对象。
     */
    @Override
    public ResponseResult queryCardAmountList(CardAmountDTO cardAmountQueryParam) {
        // 通过token获取用户信息，用于后续的权限验证或个性化查询。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(cardAmountQueryParam.getToken());
        // 验证用户信息是否有效。
        if (null != userInfo) {
            try {
                // 将查询条件DTO转换为Map，以适应数据库查询的需求。
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(cardAmountQueryParam);
                // 执行分页查询，获取卡余额列表。
                //分页查询数据
                IPage<CardAmountListVO> page = this.baseMapper.queryCardAmountList(new Query<CardAmountListVO>().getPage(data), cardAmountQueryParam);
                // 计算所有卡的总余额。
                BigDecimal totalAmount = this.baseMapper.selectCardAmount(cardAmountQueryParam);
                // 构建查询结果的Map，包含总余额和分页后的数据。
                Map<String, Object> map = new HashMap<>();
                map.put("totalAmount", totalAmount);
                map.put("data", PageUtils.getPage(page));
                // 返回查询成功的响应结果，包含总余额和分页后的数据。
                return ResponseResult.success(map);
            } catch (IllegalAccessException e) {
                // 处理转换过程中的异常，打印堆栈跟踪。
                e.printStackTrace();
            }
        }
        // 如果用户信息无效或查询过程中出现异常，则返回查询失败的响应结果，提示token非法。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult check(RechargeCardDTO checkRechargeCardRequest) throws TransactionException {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(checkRechargeCardRequest.getToken());
        // 如果用户信息存在
        if (null != userInfo) {
            // 创建一个map用于存储更新充值卡的参数
            Map<Object, Object> map = new HashMap<>();
            // 将请求中的充值卡信息放入map中
            map.put("totalPrice", checkRechargeCardRequest.getTotalPrice());
            map.put("startCard", checkRechargeCardRequest.getStartCard());
            map.put("finishCard", checkRechargeCardRequest.getFinishCard());
            map.put("finishTime", checkRechargeCardRequest.getFinishTime());
            map.put("startTime", checkRechargeCardRequest.getStartTime());
            // 根据起始充值卡号查询充值卡信息
            PayCardEntity card = payCardMapper.selectOne(new LambdaQueryWrapper<PayCardEntity>()
                    .eq(PayCardEntity::getId, checkRechargeCardRequest.getStartCard()));
            // 如果充值卡不存在，返回失败结果
            if (card == null) {
                return ResponseResult.fail(BizCodeEnume.CARD_IS_NUll);
            }
            // 如果充值卡的结束时间不为空，表示该卡已更新，返回失败结果
            Date s = card.getEtime();
            if (s != null) {
                return ResponseResult.fail(BizCodeEnume.UPDATE_RECHARGE_CARD_ERROP.getCode(), BizCodeEnume.UPDATE_RECHARGE_CARD_ERROP.getMsg());
            }
            // 更新充值卡端口信息
            Integer cardCount = payCardMapper.updateRechargeCardPort(map);
            // 初始化充值卡信息列表
            List<PayCardInfoVO> cardInfo = null;
            // 创建充值卡基础信息结果对象
            PayCardBaseVO baseResult = new PayCardBaseVO();
            // 设置充值卡的基础信息
            baseResult.setCardDesc(checkRechargeCardRequest.getCardDesc());
            baseResult.setCardEndTime(checkRechargeCardRequest.getFinishTime());
            baseResult.setCardName(checkRechargeCardRequest.getTotalPrice() + "元");
            baseResult.setCardNum(
                    Integer.parseInt(checkRechargeCardRequest.getFinishCard()) - Integer.parseInt(checkRechargeCardRequest.getStartCard()) + 1 + "");
            baseResult.setCardPrize("{\"m\":" + checkRechargeCardRequest.getTotalPrice() + "}");
            baseResult.setCardStartTime(checkRechargeCardRequest.getStartTime());
            // 如果更新充值卡端口信息成功
            if (cardCount > 0) {
                // 根据更新后的充值卡端口信息查询充值卡基础参数
                cardInfo = payCardMapper.queryPayBaseParam(map);
                try {
                    // 插入充值卡基础信息
                    Integer cardBase = this.baseMapper.insetPayCardBase(baseResult);
                    // 如果充值卡信息列表不为空
                    if (cardInfo.size() > 0) {
                        // 遍历充值卡信息列表
                        for (PayCardInfoVO payCardInfoResult : cardInfo) {
                            // 设置充值卡ID
                            payCardInfoResult.setCardId(baseResult.getId() + "");
                            // 根据充值卡代码查询充值卡信息
                            PayCardInfoEntity payCardInfoResult1 = payCardInfoMapper.selectOne(new LambdaQueryWrapper<PayCardInfoEntity>()
                                    .eq(PayCardInfoEntity::getCardCode, payCardInfoResult.getCardCode()));
                            // 如果充值卡信息存在，更新充值卡信息
                            if (payCardInfoResult1 != null) {
                                payCardInfoMapper.updatePayCardInfoBase(payCardInfoResult);
                            } else {
                                // 如果充值卡信息不存在，插入充值卡信息
                                List<PayCardInfoVO> cardInfo2 = new ArrayList<>();
                                cardInfo2.add(payCardInfoResult);
                                payCardInfoMapper.insetPayCardInfoBase(cardInfo2, baseResult.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果发生异常，回滚全局事务，并返回操作失败的结果
                    GlobalTransactionContext.reload(RootContext.getXID()).rollback();
                    return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                }
            } else {
                // 如果更新充值卡端口信息失败，返回失败结果
                return ResponseResult.fail(BizCodeEnume.UPDATE_RECHARGE_CARD_ERROP.getCode(), BizCodeEnume.UPDATE_RECHARGE_CARD_ERROP.getMsg());
            }
            // 返回操作成功的结果
            return ResponseResult.success();
        }
        // 如果用户信息不存在，返回token非法的结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
}




