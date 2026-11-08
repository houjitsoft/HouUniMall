package com.houjiang.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.order.OrderBaseListVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.WaitSettlementVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.FinancialSettlementDTO;
import com.houjiang.order.dto.WaitOrderDTO;
import com.houjiang.order.entity.OrderSettlementEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.feign.PayMentServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderReturnMapper;
import com.houjiang.order.mapper.OrderSettlementMapper;
import com.houjiang.order.service.OrderSettlementService;
import com.houjiang.order.vo.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class OrderSettlementServiceImpl extends ServiceImpl<OrderSettlementMapper, OrderSettlementEntity>
    implements OrderSettlementService{

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Resource
    private PayMentServiceClient payMentServiceClient;

    @Resource
    private BaseServiceClient baseServiceClient;

    @Resource
    private OrderReturnMapper orderReturnMapper;

    @Override
    public ResponseResult exportExcel(Map<String, Object> paraMap, HttpServletResponse response) throws IOException, ParseException {
        OrderSettlementEntity orderSettlement = this.baseMapper.selectById(paraMap.get("orderSettlementId").toString());
        if (orderSettlement.getOsDate() == null) {
            paraMap.put("osStartDate", orderSettlement.getOsStartDate());
            paraMap.put("osEndDate", orderSettlement.getOsEndDate());
            paraMap.remove("orderSettlementId");
        }
        paraMap.put("shopId", orderSettlement.getShopId());
        String osDatetime = DateUtils.dateConvert(orderSettlement.getOsDatetime().toString()).split(" ")[0];
        paraMap.put("osDatetime", osDatetime);
        Page<OrderBaseVO> page = new Page<>(0,-1);
        IPage<OrderBaseVO> result;
        if("3".equals(orderSettlement.getOsOrderType().toString())){
            result=orderBaseMapper.listSupplyBaseInfo(page,paraMap);
        }else if ("2".equals(orderSettlement.getOsOrderType().toString())) {
            result=orderBaseMapper.listDealerBaseInfo(page,paraMap);
        }else{
            result=orderBaseMapper.listBaseInfo(page,paraMap);
        }
        String[] headers = {"订单编号", "订单金额(不含运费)", "红包金额", "运费", "佣金", "分享佣金", "推广佣金", "会员折扣",
                "经销折扣", "供货价", "下单日期", "成交日期", "买家","订单商品","商品总金额"};
        List<Map<String, String>> list = Lists.newArrayList();
        //在表中存放查询到的数据放入对应的列
        for (OrderBaseVO orderBaseInfo : result.getRecords()) {
            String OrderCreateTime = DateUtils.dateConvert(orderBaseInfo.getOrderCreateTime().toString());
            String OrderFinishedTime = DateUtils.dateConvert(orderBaseInfo.getOrderFinishedTime().toString());
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("订单编号", orderBaseInfo.getOrderId());
            mPut.put("订单金额 (不含运费)（元）", orderBaseInfo.getOrderPaymentAmount()+"");
            mPut.put("红包金额", orderBaseInfo.getOrderRptPrice()+"");
            mPut.put("运费", orderBaseInfo.getOrderShippingFee()+"");
            mPut.put("佣金", orderBaseInfo.getOrderCommissionFee()+"");
            mPut.put("分享佣金", orderBaseInfo.getFxCommission()+"");
            mPut.put("推广佣金", orderBaseInfo.getTgCommission()+"");
            mPut.put("会员折扣", orderBaseInfo.getBaseMemberDiscount()+"");
            mPut.put("经销折扣", null != orderBaseInfo.getOrderDiscountFee()?orderBaseInfo.getOrderDiscountFee()+"":"0");
            mPut.put("供货价", orderBaseInfo.getGoodsPriceSum()+"");
            mPut.put("下单日期", OrderCreateTime);
            mPut.put("成交日期", OrderFinishedTime);
            mPut.put("买家", orderBaseInfo.getBuyerUserName());
            mPut.put("订单商品", orderBaseInfo.getGoodsName());
            mPut.put("商品总金额", orderBaseInfo.getOrderGoodsAmount()+"");
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("订单详情数据", list);
        String url = ExcelsUtil.createExcel(headers, m, new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult getMerchantAmountExp(FinancialSettlementDTO param) {
        if (param.getStartTime() == "" && param.getEndTime() == "") {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finishTime = dateformat.format(new Date());
            param.setEndTime(finishTime);
            param.setStartTime("2017-01-01");
        }
        List<FinancialSettlementDTO> financialList = this.baseMapper.getMerchantAmountExp(param);

        String[] title = {"店铺名", "订单总额", "退单总额", "红包总额", "退还红包总额", "罚款总额", "提现总额", "未提现总额","未结算金额"};

        List<Map<String, String>> list = Lists.newArrayList();

        for (FinancialSettlementDTO res : financialList) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            // 订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提',
            map.put("店铺名", res.getShopName());
            map.put("订单总额", res.getOsAmount() + "");
            map.put("退单总额", res.getOsOrderReturnAmount() + "");
            map.put("红包总额", res.getOsRedpacketAmount() + "");
            map.put("退还红包总额", res.getOsRedpacketReturnAmount() + "");
            map.put("罚款总额", res.getOsFineAmount() + "");
            map.put("提现总额", res.getWithdrawalAmount() + "");
            map.put("未提现总额", res.getUndrawnAmount() + "");
            map.put("未结算金额", res.getNotSettlementMoney() + "");
            list.add(map);
        }
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, map,
                new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 根据业务需求处理业务列表的查询。
     * 该方法主要用于根据用户信息和请求参数，查询相应的业务列表，并进行分页处理。
     *
     * @param paraMap 请求参数的映射表，包含token等信息。
     * @return 返回处理结果的响应对象。
     */
    @Override
    public ResponseResult businessList(Map<String, String> paraMap) {
        // 从参数映射中获取token字符串
        String tokenString = paraMap.get("token");
        // 使用token解析出用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(tokenString);
        // 将用户的店铺ID添加到参数映射中，用于后续查询
        paraMap.put("shopId", userInfo.getShopId());

        // 根据用户的店铺类型，确定订单的结算类型
        if (userInfo.getShopType() == 1 || userInfo.getShopType() == 3) {
            // 对于自销和经销类型的店铺，结算类型默认为1，如有指定则使用指定值
            paraMap.put("osOrderType", StringUtils.isBlank(paraMap.get("osOrderType")) ? "1" : paraMap.get("osOrderType"));
        } else {
            // 对于供应商类型的店铺，结算类型固定为3
            //供应商
            paraMap.put("osOrderType", "3");
        }

        // 根据请求参数中的页码和每页记录数创建分页对象
        Page<OrderSettlementVO> page = new Page<>(Integer.parseInt(paraMap.get("page")), Integer.parseInt(paraMap.get("limit")));
        // 调用基础Mapper接口查询业务列表
        IPage<OrderSettlementVO> result = this.baseMapper.businessList(page, paraMap);
        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public ResponseResult exportexcelSettlementBusiness(Map<String, String> paraMap) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        String tokenString = paraMap.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(tokenString);
        paraMap.put("shopId", userInfo.getShopId());
        if (userInfo.getShopType().intValue() == 1) {
            // 商家需要根据结算类型查询  自销和经销结算
            paraMap.put("osOrderType", StringUtils.isBlank(paraMap.get("osOrderType")) ? "1" : paraMap.get("osOrderType"));
        } else {
            paraMap.put("osOrderType", null);
        }
        List<OrderSettlementVO> listSettlement = this.baseMapper.exportexcelSettlementBusiness(paraMap);
        // 新增数据行，并且设置单元格数据

        int rowNum = 1;

        String[] title = {"序号", "店铺名称", "结算单号", "开始时间", "结束时间", "本期应收", "订单金额", "红包金额", "退还红包金额", "佣金金额", "退还佣金金额",
                "退单金额", "结算状态"};

        List<Map<String, String>> list = Lists.newArrayList();
        // 在表中存放查询到的数据放入对应的列
        for (OrderSettlementVO orderSettlement : listSettlement) {
            String osState = orderSettlement.getOsState();
            String state = null;
            if ("1".equals(osState)) {
                state = "已出账";
            } else if ("2".equals(osState)) {
                state = "商家已确认";
            } else if ("3".equals(osState)) {
                state = "平台已审核";
            } else if ("4".equals(osState)) {
                state = "结算完成";
            } else {
                state = "已提现";
            }
            orderSettlement.setOsState(state);
            HashMap<String, String> mPut = Maps.newHashMap();
            mPut.put("序号", rowNum+"");
            mPut.put("店铺名称", orderSettlement.getShopName());
            mPut.put("结算单号", orderSettlement.getOsId());
            mPut.put("开始时间", orderSettlement.getOsStartDate()+"");
            mPut.put("结束时间", orderSettlement.getOsEndDate()+"");
            mPut.put("本期应收", orderSettlement.getOsAmount()+"");
            mPut.put("订单金额", orderSettlement.getOsOrderAmount());
            mPut.put("红包金额", orderSettlement.getOsRedpacketAmount());
            mPut.put("退还红包金额", orderSettlement.getOsRedpacketReturnAmount());
            mPut.put("佣金金额", orderSettlement.getOsCommisAmount());
            mPut.put("退还佣金金额", orderSettlement.getOsCommisReturnAmount());
            mPut.put("退单金额", orderSettlement.getOsOrderReturnAmount());
            mPut.put("结算状态", orderSettlement.getOsState());
            rowNum++;
            list.add(mPut);
        }
        Map<String, List<Map<String, String>>> m = Maps.newHashMap();
        m.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, m, new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult settlementDetail(Map<String, String> paraMap) {
        OrderSettlementVO orderSettlement = this.baseMapper.settlementDetail(paraMap);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String osDatetime = dateFormat.format(orderSettlement.getOsDatetime());
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("osId", orderSettlement.getOsId());
        returnMap.put("shopName", orderSettlement.getShopName());
        returnMap.put("date", orderSettlement.getOsStartDate().toString() + " 至  " + orderSettlement.getOsEndDate().toString());
        returnMap.put("osDatetime", osDatetime);
        String osAmountDetail = null;
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));

        if(orderSettlement.getOsOrderType().equals("4")){
            osAmountDetail= orderSettlement.getOsAmount() + " = " +orderSettlement.getOsCommisAmount()
                    + "(佣金金额) - " + orderSettlement.getOsCommisReturnAmount() + "(退还佣金)";
        } else if (userInfo.getShopType() == 1) {
            // 商家中心
            // 商家经销结算单
            if (orderSettlement.getOsOrderType().equals("2")) {
                //订单金额（不含运费）-供货价-抽佣+会员折扣+退还抽佣-退单金额（不含运费）+供货价-退还会员折扣
                osAmountDetail = orderSettlement.getOsAmount() + " = "
                        + orderSettlement.getOsOrderAmount() + "(商品金额 不含运费) - "
                        + orderSettlement.getOsSupplyPrice() + "(供货价) - "
                        + orderSettlement.getOsCommisAmount() + "(佣金金额) + "
                        + orderSettlement.getOsBaseMemberDiscount() + "(会员折扣) + "
                        + orderSettlement.getOsCommisReturnAmount() + "(退还佣金) - "
                        + orderSettlement.getOsOrderReturnAmount() + "(退单金额) + "
                        + orderSettlement.getOsReturnSupplyPrice() + "(退还供货价) - "
                        + orderSettlement.getOsReturnMemberDiscount() + "(退还会员折扣)";
            } else {
                BigDecimal osAmount = new BigDecimal(orderSettlement.getOsOrderAmount());
                osAmountDetail = orderSettlement.getOsAmount() + " = " + osAmount + "(商品金额 不含运费) + "
                        + orderSettlement.getOsRedpacketAmount() + "(红包金额) - "
                        + orderSettlement.getSelfSellingCommission() + "(自销佣金金额) - "
                        + orderSettlement.getSelfSellingReturnCash() + "(自销退单金额) + "
                        + orderSettlement.getSelfSellingShippingFee() + "(自销商品运费) - "
                        + orderSettlement.getSellOnAmount() + "(订单协议价) - "
                        + orderSettlement.getSellOnCommission() + "(经销佣金金额) - "
                        + orderSettlement.getSellOnReturnCash() + "(经销退单金额) + "
                        + orderSettlement.getSellOnReturnCommission() + "(经销退还佣金) + "
                        + orderSettlement.getSellOnReturnCash() + "(退还订单协议价) + "
                        + orderSettlement.getOsBaseMemberDiscount() + "(会员折扣) - "
                        + orderSettlement.getOsRedpacketReturnAmount() + "(退还红包金额) + "
                        + orderSettlement.getOsReturnTgCommission() + "(退还推广佣金) + "
                        + orderSettlement.getOsReturnFxCommission() + "(退还分享佣金) + "
                        + orderSettlement.getOsReturnMemberDiscount() + "(退还会员折扣) - "
                        + orderSettlement.getOsTgCommission() + "(推广佣金) - "
                        + orderSettlement.getOsFxCommission() + "(分享佣金) ";
            }
        } else {
            // 供应商结算单
            osAmountDetail = orderSettlement.getOsAmount() + " = " + orderSettlement.getOsOrderAmount() + "(供货价 不含运费) -"
                    + orderSettlement.getOsCommisAmount() + "(佣金金额) -"
                    + orderSettlement.getOsOrderReturnAmount() + "(退还供货价) +  "
                    + orderSettlement.getOsCommisReturnAmount() + "(退还佣金) + "
                    + orderSettlement.getOsShippingAmount() + "(运费)" ;
        }
        returnMap.put("osAmountDetail", osAmountDetail);
        returnMap.put("osState", orderSettlement.getOsState());
        returnMap.put("osAmount", orderSettlement.getOsAmount());
        return ResponseResult.success(returnMap);
    }

    /**
     * 根据条件查询订单的基本信息。
     *
     * @param paraMap 查询参数的映射表，包含各种条件。
     * @return 返回包含查询结果的响应对象。
     */
    @Override
    public ResponseResult listBaseInfo(Map<String, Object> paraMap) {
        // 通过参数映射表查询订单结算信息
        OrderSettlementVO orderSettlement = this.baseMapper.getSettlementDate(paraMap);

        // 检查订单结算日期是否为空
        // 如果时间在 2019-01-28 之前，则需要这个时间
        if (StringUtils.isBlank(orderSettlement.getOsDate())) {
            try {
                // 如果日期为空，则尝试将开始和结束日期转换为指定格式的字符串，并更新参数映射表
                paraMap.put("osStartDate", DateUtils.dateConvert(orderSettlement.getOsStartDate().toString()));
                paraMap.put("osEndDate", DateUtils.dateConvert(orderSettlement.getOsEndDate().toString()));
            } catch (ParseException e) {
                // 日志打印解析异常
                e.printStackTrace();
            }

            // 移除订单结算ID参数，因为它不再需要
            paraMap.remove("orderSettlementId");
        }

        // 更新参数映射表，添加商店ID和订单结算日期
        paraMap.put("shopId", orderSettlement.getShopId());
        paraMap.put("osDatetime", orderSettlement.getOsDate());

        IPage<OrderBaseInfoVO> listOrderBase;

        // 根据订单结算类型，选择不同的查询方法
        if (orderSettlement.getOsOrderType().equals("3")) {
            // 代发货订单供应商结算数据
            // 查询代发货订单的供应商结算基本信息
            listOrderBase = this.baseMapper.listShopSupplyBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);
        } else if (orderSettlement.getOsOrderType().equals("4")) {
            // 查询普通订单的基本信息
            listOrderBase = this.baseMapper.listBaseInfo2(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);
        }else if (orderSettlement.getOsOrderType().equals("2")) {
            // 查询经销商订单的基本信息
            listOrderBase = this.baseMapper.listShopDealerBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);
        } else {
            // 查询普通订单的基本信息
            listOrderBase = this.baseMapper.listShopBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);

            // 对于查询结果中的每个订单，计算实际支付金额（订单支付金额减去订单供应金额）
            for (OrderBaseInfoVO orderBaseInfo : listOrderBase.getRecords()) {
                BigDecimal b1 = new BigDecimal(orderBaseInfo.getOrderPaymentAmount().toString());
                BigDecimal b2 = new BigDecimal(orderBaseInfo.getOrderSupplyAmount().toString());
                orderBaseInfo.setOrderPaymentAmount(b1.subtract(b2).doubleValue());
            }
        }

        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(listOrderBase));
    }

    @Override
    public ResponseResult listReutrnOrder(Map<String, Object> paraMap) {
        // 根据参数映射表获取订单结算信息
        OrderSettlementVO orderSettlement = this.baseMapper.getSettlementDate(paraMap);

        // 检查订单结算日期是否为空，如果为空，则尝试根据开始和结束日期重新计算结算日期
        if (StringUtils.isBlank(orderSettlement.getOsDate())) {
            try {
                // 将开始和结束日期字符串转换为指定格式的日期字符串
                paraMap.put("osStartDate", DateUtils.dateConvert(orderSettlement.getOsStartDate().toString()));
                paraMap.put("osEndDate", DateUtils.dateConvert(orderSettlement.getOsEndDate().toString()));
            } catch (ParseException e) {
                // 日志打印解析异常，但不中断执行
                e.printStackTrace();
            }
            // 移除订单结算ID，因为重新计算了日期
            paraMap.remove("orderSettlementId");
            // 添加标记，指示需要处理退货店铺
            paraMap.put("returnShopHandle", "returnShopHandle");
        }

        // 更新参数映射表，添加店铺ID和结算日期
        paraMap.put("shopId", orderSettlement.getShopId());
        paraMap.put("osDatetime", orderSettlement.getOsDate());

        // 根据订单结算类型，调用不同的查询方法获取退货详情列表
        IPage<ReturnOrderDetailListVO> listOrderBase;
        if (orderSettlement.getOsOrderType().equals("3")) {
            // 供应商退货订单查询
            listOrderBase = this.baseMapper.listSupplyReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else if (orderSettlement.getOsOrderType().equals("4")) {
            // 平台退货订单查询
            listOrderBase = this.baseMapper.listReutrnOrder2(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else if (orderSettlement.getOsOrderType().equals("2")) {
            // 经销商退货订单查询
            listOrderBase = this.baseMapper.listDealerReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else {
            // 店铺退货订单查询
            listOrderBase = this.baseMapper.listShopReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        }

        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(listOrderBase));

    }

    @Override
    public ResponseResult exportexcelSettlementBaseInfo(Map<String, Object> paraMap) {
        OrderSettlementVO orderSettlement = this.baseMapper.getSettlementDate(paraMap);
        // 如果时间在 2019-01-28 之前，则需要这个时间
        if (StringUtils.isBlank(orderSettlement.getOsDate())) {
            try {
                paraMap.put("osStartDate", DateUtils.dateConvert(orderSettlement.getOsStartDate().toString()));
                paraMap.put("osEndDate", DateUtils.dateConvert(orderSettlement.getOsEndDate().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            paraMap.remove("orderSettlementId");
        }
        paraMap.put("shopId", orderSettlement.getShopId());
        paraMap.put("osDatetime", orderSettlement.getOsDate());
        List<OrderBaseInfoVO> listBaseInfo;
        if (orderSettlement.getOsOrderType().equals("3")) {
            listBaseInfo = this.baseMapper.listSupplyBaseInfoExport(paraMap);
        } else {
            listBaseInfo = this.baseMapper.listBaseInfoExport(paraMap);
        }
        // 设置要导出的文件的名字
        String fileName = paraMap.get("shopName") + "订单" + ".xls";
        String[] title = {"订单编号", "订单金额", "供货价", "红包金额", "会员折扣", "运费", "佣金","分享佣金","推广佣金", "下单日期", "成交日期", "买家"};
        List<Map<String, String>> list = Lists.newArrayList();
        // 在表中存放查询到的数据放入对应的列
        for (OrderBaseInfoVO orderBaseInfo : listBaseInfo) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            map.put("订单编号", orderBaseInfo.getOrderId());
            map.put("订单金额", orderBaseInfo.getOrderPaymentAmount() + "");
            map.put("供货价", null == orderBaseInfo.getOrderSupplyAmount() ? "0.00" : orderBaseInfo.getOrderSupplyAmount() + "");
            map.put("红包金额", orderBaseInfo.getOrderRptPrice() + "");
            map.put("会员折扣", orderBaseInfo.getBaseMemberDiscount() + "");
            map.put("运费", orderBaseInfo.getOrderShippingFee() + "");
            map.put("佣金", orderBaseInfo.getOrderCommissionFee() + "");
            map.put("分享佣金", null == orderBaseInfo.getFxCommission() ? "0.00" : orderBaseInfo.getFxCommission() + "");
            map.put("推广佣金", null == orderBaseInfo.getTgCommission() ? "0.00" : orderBaseInfo.getTgCommission() + "");
            map.put("下单日期", orderBaseInfo.getOrderCreateTime());
            map.put("成交日期", orderBaseInfo.getOrderFinishedTime());
            map.put("买家", orderBaseInfo.getBuyerUserName());
            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put(fileName, list);
        String url = ExcelsUtil.createExcel(title, map,
                new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult exportexcelSettlementReturnOrder(Map<String, Object> paraMap) {

        OrderSettlementVO orderSettlement = this.baseMapper.getSettlementDate(paraMap);
        List<ReturnOrderDetailListVO> listReutrnOrderExport;
        if (orderSettlement.getOsOrderType().equals("3")) {
            listReutrnOrderExport = this.baseMapper.listSupplyReutrnOrderExport(paraMap);
        } else {
            listReutrnOrderExport = this.baseMapper.listReutrnOrderExport(paraMap);
        }
        // 设置要导出的文件的名字
        String fileName = paraMap.get("shopName") + "退单" + ".xls";

        String[] headers = {"订单编号", "退款金额", "供货价", "退还佣金", "退款理由", "退款时间", "买家","订单商品","商品金额"};
        List<Map<String, String>> list = Lists.newArrayList();
        // 在表中存放查询到的数据放入对应的列
        for (ReturnOrderDetailListVO returnOrderDetail : listReutrnOrderExport) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            map.put("订单编号", returnOrderDetail.getOrderId());
            map.put("退款金额", returnOrderDetail.getReturnCash() + "");
            map.put("供货价", null == returnOrderDetail.getOrderSupplyAmount() ? "0.00" : returnOrderDetail.getOrderSupplyAmount() + "");
            map.put("退还佣金", returnOrderDetail.getReturnCommisionFee() + "");
            map.put("退款理由", returnOrderDetail.getReturnReason() + "");
            map.put("退款时间", returnOrderDetail.getReturnAddTime() + "");
            map.put("买家", returnOrderDetail.getBuyerUserAccount());
            map.put("订单商品", returnOrderDetail.getGoodsName());
            map.put("商品金额", returnOrderDetail.getOrderGoodsPaymentAmount()+"");
            list.add(map);
        }

        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put(fileName, list);
        String url = ExcelsUtil.createExcel(headers, map,
                new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult waitSettlement(UserTokenBeanDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        String shopId = userInfo.getShopId();
        // 平台应付金额 =订单金额 + 红包金额 - 佣金金额 - 退单金额 - 退还红包金额 + 退还佣金-罚款金额
        WaitSettlementVO result = new WaitSettlementVO();
        StringBuilder osAmountStr = new StringBuilder();
        // 获取上次结算时间
        Date beformDate = this.baseMapper.getBeformDate(shopId,1);
        Date dBefore = null;
        if(null == beformDate){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dBefore = sdf.parse("2019-04-10 00:00:00");
            } catch (ParseException ignored) {
            }
        }else{
            dBefore = beformDate;
        }
        // 准备查询条件，主要过滤出当前商家ID和指定时间范围内的数据
        Map<String, Object> mapAccount = new HashMap<>();
        mapAccount.put("shopId", shopId);
        mapAccount.put("dropShipping", 2);
        mapAccount.put("dBefore", dBefore);
        // 查询商家结算数据
        ShopSettleAccountInfoDTO shopSettleAccountData = getShopSettleAccountData(mapAccount);
        BigDecimal totalAmount = new BigDecimal(0.00);
        // 有订单有退单的情况
        totalAmount = totalAmount
                .add(shopSettleAccountData.getOrderPaymentAmount()).subtract(shopSettleAccountData.getOrderShippingFee())
                .add(shopSettleAccountData.getOrderRptPrice())
                .add(shopSettleAccountData.getBaseMemberDiscount())
                .subtract(shopSettleAccountData.getReturnRptCash())
                .subtract(shopSettleAccountData.getReturnMemberDiscount())
                .add(shopSettleAccountData.getReturnTgCommission())
                .add(shopSettleAccountData.getReturnFxCommission())
                .subtract(shopSettleAccountData.getTgCommission()).subtract(shopSettleAccountData.getFxCommission())
                .subtract(shopSettleAccountData.getSelfSellingCommission())
                .subtract(shopSettleAccountData.getSelfSellingReturnCash())
                .add(shopSettleAccountData.getSelfSellingReturnCommission())
                .add(shopSettleAccountData.getSelfSellingShippingFee())
                .subtract(shopSettleAccountData.getSellOnAmount())
                .subtract(shopSettleAccountData.getSellOnCommission())
                .subtract(shopSettleAccountData.getSellOnReturnCash())
                .add(shopSettleAccountData.getSellOnReturnCommission())
                .add(shopSettleAccountData.getReturnSellOnAmount());
        //四舍五入
        totalAmount= totalAmount.setScale(2, RoundingMode.HALF_UP);
        osAmountStr.append(totalAmount).append(" = ")
                .append(shopSettleAccountData.getOrderPaymentAmount().subtract(shopSettleAccountData.getOrderShippingFee())
                        .setScale(2, RoundingMode.HALF_UP)).append("(商品金额 不含运费) + ")
                .append(shopSettleAccountData.getOrderRptPrice()).append("(红包金额) - ")
                .append(shopSettleAccountData.getSelfSellingCommission()).append("(自销佣金金额) - ")
                .append(shopSettleAccountData.getSelfSellingReturnCash()).append("(自销退单金额) + ")
                .append(shopSettleAccountData.getSelfSellingReturnCommission()).append("(自销退还佣金) + ")
                .append(shopSettleAccountData.getSelfSellingShippingFee()).append("(自销商品运费) - ")
                .append(shopSettleAccountData.getSellOnAmount()).append("(订单协议价) - ")
                .append(shopSettleAccountData.getSellOnCommission()).append("(经销佣金金额)- ")
                .append(shopSettleAccountData.getSellOnReturnCash()).append("(经销退单金额) + ")
                .append(shopSettleAccountData.getSellOnReturnCommission()).append("(经销退还佣金) + ")
                .append(shopSettleAccountData.getReturnSellOnAmount()).append("(退还订单协议价) + ")
                .append(shopSettleAccountData.getBaseMemberDiscount()).append("(会员折扣) - ")
                .append( shopSettleAccountData.getReturnRptCash()).append("(退还红包金额) + ")
                .append(shopSettleAccountData.getReturnTgCommission()).append("(退还推广佣金) + ")
                .append(shopSettleAccountData.getReturnFxCommission()).append("(退还分享佣金) - ")
                .append(shopSettleAccountData.getReturnMemberDiscount()).append("(退还会员折扣) - ")
                .append(shopSettleAccountData.getTgCommission()).append("(推广佣金) - ")
                .append(shopSettleAccountData.getFxCommission()).append("(分享佣金)");

        result.setOsAmountStr(osAmountStr.toString());
        result.setBeformDate(beformDate);
        return ResponseResult.success(result);
    }
    /**
     * 获取商家结算数据 用于商家 和 定时任务
     * @return
     */
    @Override
    public ShopSettleAccountInfoDTO getShopSettleAccountData(Map<String, Object> mapAccount){
        ShopSettleAccountInfoDTO data = new ShopSettleAccountInfoDTO();
        // 查询商家订单结算数据
        ShopSettleAccountInfoDTO orderSettleAccountOrder = this.baseMapper.getSettleAllAccountOrder(mapAccount);
        if(null != orderSettleAccountOrder){
            data.setOrderShippingFee(orderSettleAccountOrder.getOrderShippingFee());
            data.setOrderPaymentAmount(orderSettleAccountOrder.getOrderPaymentAmount());
            data.setOrderRptPrice(orderSettleAccountOrder.getOrderRptPrice());
            data.setBaseMemberDiscount(orderSettleAccountOrder.getBaseMemberDiscount());
        }
        // 查询商家订单结算佣金数据
        ShopSettleAccountInfoDTO settleOrderCommission = this.baseMapper.getSettleOrderCommission(mapAccount);
        if(null != settleOrderCommission){
            data.setTgCommission(settleOrderCommission.getTgCommission());
            data.setFxCommission(settleOrderCommission.getFxCommission());
            data.setSelfSellingCommission(settleOrderCommission.getSelfSellingCommission());
            data.setSelfSellingShippingFee(settleOrderCommission.getSelfSellingShippingFee());
        }
        // 查询商家查询商家要结算的订单
        List<String> orderIdList = this.baseMapper.getSettleAllOrderList(mapAccount);
        data.setOrderIdList(orderIdList);

        // 查询商家退单结算数据
        // 获取单个店铺所有未结算的退单金额
        ShopSettleAccountInfoDTO returnSettleAccountOrder = this.baseMapper.getSettleAllAccountReturn(mapAccount);
        if(null == returnSettleAccountOrder){
            returnSettleAccountOrder = new ShopSettleAccountInfoDTO();
        }
        data.setReturnRptCash(returnSettleAccountOrder.getReturnRptCash());
        data.setReturnMemberDiscount(returnSettleAccountOrder.getReturnMemberDiscount());
        data.setReturnTgCommission(returnSettleAccountOrder.getReturnTgCommission());
        data.setReturnFxCommission(returnSettleAccountOrder.getReturnFxCommission());
        data.setSelfSellingReturnCash(returnSettleAccountOrder.getSelfSellingReturnCash());
        data.setSelfSellingReturnCommission(returnSettleAccountOrder.getSelfSellingReturnCommission());
        //自销退单金额
        BigDecimal selfSellingReturnCash = returnSettleAccountOrder.getSelfSellingReturnCash();
        //自销退还佣金
        BigDecimal selfSellingReturnCommission = returnSettleAccountOrder.getSelfSellingReturnCommission();
        ShopSettleAccountInfoDTO returnSupplierAndShopOrder = this.baseMapper.getSettleSupplierAndShopOrder(mapAccount);
        if(returnSupplierAndShopOrder != null && returnSupplierAndShopOrder.getSelfSellingReturnCash() != null){
            if(selfSellingReturnCash.compareTo(BigDecimal.ZERO) > 0 && selfSellingReturnCash.compareTo(returnSupplierAndShopOrder.getSelfSellingReturnCash()) > 0){
                selfSellingReturnCash = selfSellingReturnCash.subtract(returnSupplierAndShopOrder.getSelfSellingReturnCash());
                selfSellingReturnCommission = selfSellingReturnCommission.subtract(returnSupplierAndShopOrder.getSelfSellingReturnCommission());
                data.setSelfSellingReturnCash(selfSellingReturnCash);
                data.setSelfSellingReturnCommission(selfSellingReturnCommission);
            }
        }
        // 主单 经销商品 佣金
        ShopSettleAccountInfoDTO sellOnAccountOrder = orderBaseMapper.getSellOnAccountShop(mapAccount);
        if(null == sellOnAccountOrder){
            sellOnAccountOrder = new ShopSettleAccountInfoDTO();
        }
        //经销商品佣金
        data.setSellOnCommission(sellOnAccountOrder.getSellOnCommission());
        // 主单 经销商品金额 订单协议价
        BigDecimal sellOnAmount = orderBaseMapper.getSellOnAccountTotal(mapAccount);
        data.setSellOnAmount(sellOnAmount == null ? new BigDecimal(0.00) : sellOnAmount);

        //查询经销退单金额和经销退单佣金
        ShopSettleAccountInfoDTO sellOnAccountOrderReturn = this.baseMapper.getReturnSellOnSupplierAndShopOrder(mapAccount);
        if(null == sellOnAccountOrderReturn){
            sellOnAccountOrderReturn = new ShopSettleAccountInfoDTO();
        }
        //经销退单金额
        BigDecimal sellOnReturnCash = sellOnAccountOrderReturn.getSellOnReturnCash();
        //经销退单佣金
        BigDecimal sellOnReturnCommission = sellOnAccountOrderReturn.getSellOnReturnCommission();
        data.setSellOnReturnCash(sellOnReturnCash);
        data.setSellOnReturnCommission(sellOnReturnCommission);
        BigDecimal returnSellOnAmount = this.baseMapper.getReturnSellOnAmount(mapAccount);
        returnSellOnAmount = returnSellOnAmount == null ? new BigDecimal(0.00) : returnSellOnAmount;
        data.setReturnSellOnAmount(returnSellOnAmount);

        // 查询商家查询商家要结算的退单
        List<String> returnOrderIdList = this.baseMapper.getSettleAllReturnOrderIdList(mapAccount);
        data.setReturnOrderIdList(returnOrderIdList);
        return data;
    }
    /**
     * 根据条件查询待处理订单列表。
     *
     * @param param 查询条件，包含分页信息和订单相关条件。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult waitOrderList(WaitOrderDTO param) {
        // 通过token获取用户信息，用于后续获取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 获取用户所属店铺ID
        String shopId = userInfo.getShopId();
        // 将店铺ID设置到查询参数中，用于精准查询用户所属店铺的订单
        param.setShopId(shopId);

        // 查询该店铺上次结算的时间，用于确定待处理订单的时间范围
        // 获取上次结算时间
        Date beformDate = this.baseMapper.getBeformDate(shopId,1);
        // 如果没有上次结算时间，则默认从2019年4月10日开始查询
        Date dBefore = null;
        if(null == beformDate){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dBefore = df.parse("2019-04-10 00:00:00");
            } catch (ParseException e) {
                // 解析异常处理，此处可忽略，因为默认日期已给出
            }
        }else{
            dBefore = beformDate;
        }

        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<OrderBaseListVO> pageObj = new Page<>(page, limit);
        // 根据分页信息、查询条件和上次结算时间查询待处理订单列表
        IPage<OrderBaseListVO> orderList = this.baseMapper.selectWaitOrderList(pageObj,param,dBefore);
        if (!orderList.getRecords().isEmpty()) {
            for (OrderBaseListVO vo : orderList.getRecords()) {
                ShopSettleAccountInfoDTO selfSellingAccountOrder = orderBaseMapper.getSelfSellingCommission(vo.getOrderId());
                //自销商品佣金
                BigDecimal selfSellingCommission = selfSellingAccountOrder.getSelfSellingCommission();
                //自销商品运费
                BigDecimal selfSellingShippingFee = selfSellingAccountOrder.getSelfSellingShippingFee();
                if (Objects.equals(vo.getDropShipping(), CommonType.dropShipping.DROPSHIPPING_YES.getCode())) {
                    // 主单 经销商品 佣金
                    ShopSettleAccountInfoDTO sellOnAccountOrder = orderBaseMapper.getSellOnAccountOrder(vo.getOrderId());
                    // 主单 经销商品金额
                    BigDecimal sellOnAmount = orderBaseMapper.getSellOnAccount(vo.getOrderId());
                    //经销商品佣金
                    BigDecimal sellOnCommission = sellOnAccountOrder.getSellOnCommission();
                    vo.setSellOnAmount(sellOnAmount);
                    vo.setSellOnCommission(sellOnCommission);
                }
                vo.setSelfSellingCommission(selfSellingCommission);
                vo.setSelfSellingShippingFee(selfSellingShippingFee);
            }
        }
        // 返回查询结果的响应对象
        return ResponseResult.success(orderList);
    }


    /**
     * 根据传入的参数查询待返回清单。
     * @param param 包含订单信息和分页信息的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult waitReturnList(WaitOrderDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 获取用户所属店铺ID
        String shopId = userInfo.getShopId();
        // 将店铺ID设置到查询参数中
        param.setShopId(shopId);

        // 查询该店铺上次结算的时间
        // 获取上次结算时间
        Date beformDate = this.baseMapper.getBeformDate(shopId,1);
        Date dBefore = null;
        // 如果没有上次结算时间，则默认为2019-04-10 00:00:00
        if(null == beformDate){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dBefore = df.parse("2019-04-10 00:00:00");
            } catch (ParseException e) {
                // 解析异常处理逻辑
            }
        }else{
            dBefore = beformDate;
        }

        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<ReturnBaseListVO> pageObj = new Page<>(page, limit);
        // 根据分页对象和查询参数，以及上次结算时间查询待返回清单
        IPage<ReturnBaseListVO> returnList = this.baseMapper.selectWaitReturnList(pageObj,param,dBefore);
        if (!returnList.getRecords().isEmpty()) {
            for (ReturnBaseListVO vo : returnList.getRecords()) {
                ShopSettleAccountInfoDTO selfSellingAccountOrderReturn = orderReturnMapper.getSelfSellingReturnCash(vo.getOrderId());
                //自销退单金额
                BigDecimal selfSellingReturnCash = selfSellingAccountOrderReturn.getSelfSellingReturnCash();
                //自销退还佣金
                BigDecimal selfSellingReturnCommission = selfSellingAccountOrderReturn.getSelfSellingReturnCommission();
                ShopSettleAccountInfoDTO sellOnAccountOrderReturn = orderReturnMapper.getSellOnReturnCash(vo.getOrderId());
                //经销退单金额
                BigDecimal sellOnReturnCash = sellOnAccountOrderReturn.getSellOnReturnCash();
                //经销退单佣金
                BigDecimal sellOnReturnCommission = sellOnAccountOrderReturn.getSellOnReturnCommission();
                vo.setSelfSellingReturnCash(selfSellingReturnCash);
                vo.setSelfSellingReturnCommission(selfSellingReturnCommission);
                vo.setSellOnReturnCash(sellOnReturnCash);
                vo.setSellOnReturnCommission(sellOnReturnCommission);
            }
        }
        // 返回查询结果
        return ResponseResult.success(returnList);
    }

    /**
     * 根据传入的参数查询待经销商返回的列表。
     *
     * @param param 包含订单信息和token的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     * @throws IllegalAccessException 如果访问权限不足，抛出此异常。
     */
    @Override
    public ResponseResult waitDealerReturnList(WaitOrderDTO param) throws IllegalAccessException {
        // 通过token获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 获取用户所属店铺ID。
        String shopId = userInfo.getShopId();
        // 将店铺ID设置到查询参数中。
        param.setShopId(shopId);

        // 查询上次结算的时间。
        Date beformDate = this.baseMapper.getBeformDate(shopId,2);
        // 如果没有上次结算时间，则默认为2019年4月10日0点。
        Date dBefore = null;
        if(null == beformDate){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dBefore = df.parse("2019-04-10 00:00:00");
            } catch (ParseException e) {
                // 解析异常时不做处理。
            }
        }else{
            dBefore = beformDate;
        }

        // 将查询参数转换为Map对象。
        Map data= ParseMapUtils.beanToMap(param);
        // 根据转换后的参数、店铺ID和上次结算时间查询待经销商返回的列表。
        IPage<ReturnBaseListVO> page=this.baseMapper.selectWaitDealerReturnList(new Query<ReturnBaseListVO>().getPage(data),
                shopId, param,dBefore);

        // 返回查询结果。
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据传入的财务结算DTO参数，查询商家的结算金额。
     * 如果未指定开始和结束时间，则默认查询从2017年1月1日到当前时间的结算信息。
     * 此外，如果系统配置中开启了“门店收银”功能，则会额外计算并加上web端支付宝和微信的支付和退款金额。
     *
     * @param param 财务结算DTO对象，包含查询所需的参数。
     * @return 返回查询结果的响应对象，其中包含分页处理后的结算信息。
     * @throws IllegalAccessException 如果访问参数字段失败，抛出此异常。
     */
    @Override
    public ResponseResult getMerchantAmount(FinancialSettlementDTO param) throws IllegalAccessException {
        // 检查是否未指定开始和结束时间，如果是，则设置默认值
        if (Objects.equals(param.getStartTime(), "") && Objects.equals(param.getEndTime(), "")) {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finishTime = dateformat.format(new Date());
            param.setEndTime(finishTime);
            param.setStartTime("2017-01-01");
        }

        // 将参数对象转换为Map，以便进行查询
        Map data= ParseMapUtils.beanToMap(param);
        // 执行查询，获取分页后的结算信息
        IPage<FinancialSettlementDTO> page=this.baseMapper.getMerchantAmount(new Query<FinancialSettlementDTO>().getPage(data),
                param);
        List<FinancialSettlementDTO> records = page.getRecords();

        // 检查系统配置，看是否开启了“门店收银”功能
        ResponseResult res = baseServiceClient.detectingApplicationStatus();
        List<Map> list = JSON.parseArray(JSON.toJSONString(res.getData()), Map.class);
        boolean flag = false;
        for (Map map : list) {
            if("门店收银".equals(map.get("configKey"))){
                flag = (Boolean) map.get("configStatus");
                break;
            }
        }

        // 如果开启了“门店收银”功能，额外计算并加上web端支付宝和微信的支付和退款金额
        if (flag) {
            for (FinancialSettlementDTO record : records) {
                // 查询web端支付宝支付金额
                BigDecimal amount =  this.baseMapper.queryWebPostAmount(record.getShopId());
                // 查询web端支付宝退款金额
                BigDecimal returnAmount = this.baseMapper.queryWebPostReturnAmount(record.getShopId());
                // 如果金额为空，则默认为0
                if(amount == null){
                    amount = new BigDecimal(0);
                }
                if(returnAmount == null){
                    returnAmount = new BigDecimal(0);
                }
                // 更新结算信息，加上web端支付宝和微信的支付和退款金额
                record.setOsAmount(record.getOsAmount().add(amount));
                record.setOsOrderReturnAmount(record.getOsOrderReturnAmount().add(returnAmount));
            }
        }

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据传入的Map参数更新订单结算信息。
     * <p>
     * 本方法通过调用BaseMapper的updateOrderSettlementByMap方法，使用传入的Map对象来更新订单结算信息。
     * 如果更新成功（即影响的行数大于0），则返回成功的响应结果；否则，返回失败的响应结果。
     * </p>
     *
     * @param map 包含更新订单结算所需信息的Map对象。
     * @return 如果更新成功，返回成功的响应结果；如果更新失败，返回失败的响应结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrderSettlementByMap(Map<String, Object> map) {
        // 调用BaseMapper的updateOrderSettlementByMap方法来更新订单结算信息
        Integer count = this.baseMapper.updateOrderSettlementByMap(map);

        // 如果更新的行数大于0，说明更新成功
        if (count > 0) {
            // 返回表示更新成功的响应结果
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }

        // 如果更新的行数不大于0，说明更新失败
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    @Override
    public ResponseResult platFormReturnorderinfoExcel(Map<String, Object> paraMap) {
        OrderSettlementEntity orderSettlementEntity = this.baseMapper.selectById(paraMap.get("orderSettlementId").toString());
        //如果时间在 2019-01-28 之前，则需要这个时间
        if (orderSettlementEntity.getOsDate() == null) {
            paraMap.put("osStartDate", orderSettlementEntity.getOsStartDate());
            paraMap.put("osEndDate", orderSettlementEntity.getOsEndDate());
            paraMap.remove("orderSettlementId");
            paraMap.put("returnShopHandle", "returnShopHandle");
        }
        paraMap.put("shopId", orderSettlementEntity.getShopId());

        IPage<ReturnOrderDetailListVO> listReutrnOrderExport;
        if ("3".equals(orderSettlementEntity.getOsOrderType().toString())) {
            listReutrnOrderExport = this.baseMapper.listSupplyReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else if ("2".equals(orderSettlementEntity.getOsOrderType().toString())) {
            listReutrnOrderExport = this.baseMapper.listDealerReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else {
            listReutrnOrderExport = this.baseMapper.listReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        }
        String[] headers = {"订单编号", "退单编号", "退单金额（不含运费）", "退还佣金", "退还红包金额", "退还会员折扣", "退还经销折扣", "退还供货价",
                "退货日期", "退货完成日期", "退货理由", "买家","订单商品","商品总金额"};

        List<ReturnOrderDetailListVO> records = listReutrnOrderExport.getRecords();
        List<Map<String, String>> list = Lists.newArrayList();
        for (ReturnOrderDetailListVO res : records) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();

            map.put("订单编号", res.getOrderId());
            map.put("退单编号", res.getReturnCode());
            map.put("退单金额（不含运费）", res.getReturnCash().toString());
            map.put("退还佣金", res.getReturnRptCash().toString());
            map.put("退还红包金额", res.getReturnMemberDiscount().toString());
            map.put("退还会员折扣", res.getReturnSupperDiscountFee().toString());
            map.put("退还经销折扣", res.getOrderReturnPrice().toString());
            map.put("退还供货价", res.getOrderReturnPrice().toString());
            map.put("退货日期", res.getReturnAddTime());
            map.put("退货完成日期", DateUtils.formateForSdfCn(res.getReturnFinishTime()));
            map.put("退货理由", res.getReturnReason());
            map.put("买家", res.getBuyerUserAccount());
            map.put("订单商品", res.getGoodsName());
            map.put("商品总金额", res.getOrderGoodsAmount()+"");
            list.add(map);
        }
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult platFormReturnorderinfo(Map<String, Object> paraMap) {
        // 根据订单结算ID查询订单结算信息
        OrderSettlementEntity orderSettlementEntity = this.baseMapper.selectById(paraMap.get("orderSettlementId").toString());

        // 如果订单结算日期为空，则将订单的开始和结束日期添加到参数Map中，并移除订单结算ID，同时添加处理标记
        // 如果时间在 2019-01-28 之前，则需要这个时间
        if (orderSettlementEntity.getOsDate() == null) {
            paraMap.put("osStartDate", orderSettlementEntity.getOsStartDate());
            paraMap.put("osEndDate", orderSettlementEntity.getOsEndDate());
            paraMap.remove("orderSettlementId");
            paraMap.put("returnShopHandle", "returnShopHandle");
        }

        // 将订单结算的店铺ID添加到参数Map中
        paraMap.put("shopId", orderSettlementEntity.getShopId());

        // 根据订单结算的类型，调用不同的查询方法
        IPage<ReturnOrderDetailListVO> page;
        if ("3".equals(orderSettlementEntity.getOsOrderType().toString())) {
            // 供应商退货订单查询
            page = this.baseMapper.listSupplyReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else if ("2".equals(orderSettlementEntity.getOsOrderType().toString())) {
            // 经销商退货订单查询
            page = this.baseMapper.listDealerReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        } else {
            // 普通退货订单查询
            page = this.baseMapper.listReutrnOrder(new Query<ReturnOrderDetailListVO>().getPage(paraMap), paraMap);
        }

        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(page));

    }

    @Override
    public ResponseResult listBaseorderinfo(Map<String, Object> paraMap) {
        OrderSettlementVO orderSettlementEntity = this.baseMapper.getSettlementDate(paraMap);
        if (orderSettlementEntity.getOsDate() == null) {
            paraMap.remove("orderSettlementId");
            try {
                // 如果日期为空，则尝试将开始和结束日期转换为指定格式的字符串，并更新参数映射表
                paraMap.put("osStartDate", DateUtils.dateConvert(orderSettlementEntity.getOsStartDate().toString()));
                paraMap.put("osEndDate", DateUtils.dateConvert(orderSettlementEntity.getOsEndDate().toString()));
            } catch (ParseException e) {
                // 日志打印解析异常
                e.printStackTrace();
            }
        }

        // 更新参数映射表，添加商店ID和订单结算日期
        paraMap.put("shopId", orderSettlementEntity.getShopId());
        paraMap.put("osDatetime", orderSettlementEntity.getOsDate());

        IPage<OrderBaseInfoVO> listOrderBase;
        // 根据订单结算类型，选择不同的查询方法
        if (orderSettlementEntity.getOsOrderType().equals("3")) {
            // 代发货订单供应商结算数据
            // 查询代发货订单的供应商结算基本信息
            listOrderBase = this.baseMapper.listShopSupplyBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);
        } else if (orderSettlementEntity.getOsOrderType().equals("2")) {
            // 查询经销商订单的基本信息
            listOrderBase = this.baseMapper.listShopDealerBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);
        } else {
            // 查询普通订单的基本信息
            listOrderBase = this.baseMapper.listShopBaseInfo(new Query<OrderBaseInfoVO>().getPage(paraMap), paraMap);

            // 对于查询结果中的每个订单，计算实际支付金额（订单支付金额减去订单供应金额）
            for (OrderBaseInfoVO orderBaseInfo : listOrderBase.getRecords()) {
                BigDecimal b1 = new BigDecimal(orderBaseInfo.getOrderPaymentAmount().toString());
                BigDecimal b2 = new BigDecimal(orderBaseInfo.getOrderSupplyAmount().toString());
                orderBaseInfo.setOrderPaymentAmount(b1.subtract(b2).doubleValue());
            }
        }

        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(listOrderBase));

    }

    @Override
    public ResponseResult platFormSettlementdetail(Map<String, Object> paraMap) {
        OrderSettlementEntity orderSettlement = this.baseMapper.selectById(paraMap.get("osId").toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String osDatetime = dateFormat.format(orderSettlement.getOsDatetime());
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("osId", orderSettlement.getOsId());
        returnMap.put("shopName", orderSettlement.getShopName());
        returnMap.put("date", orderSettlement.getOsStartDate());
        returnMap.put("osDatetime", osDatetime);

        String osAmountDetail;
        if(orderSettlement.getOsOrderType().toString().equals("2")){
            //订单金额（不含运费）-供货价-抽佣+会员折扣+退还抽佣-退单金额（不含运费）+供货价-退还会员折扣
            osAmountDetail = orderSettlement.getOsAmount() + " = "
                    + orderSettlement.getOsOrderAmount() + "(商品金额 不含运费) - "
                    + orderSettlement.getOsSupplyPrice() + "(供货价) - "
                    + orderSettlement.getOsCommisAmount() + "(佣金金额) + "
                    + orderSettlement.getOsBaseMemberDiscount() + "(会员折扣) + "
                    + orderSettlement.getOsCommisReturnAmount() + "(退还佣金) - "
                    + orderSettlement.getOsOrderReturnAmount() + "(退单金额) + "
                    + orderSettlement.getOsReturnSupplyPrice() + "(退还供货价) - "
                    + orderSettlement.getOsReturnMemberDiscount() + "(退还会员折扣)";
        }else if(orderSettlement.getOsOrderType().toString().equals("3")){
            // 供应商结算单
            osAmountDetail = orderSettlement.getOsAmount() + " = " + orderSettlement.getOsOrderAmount() + "(供货价 不含运费) -"
                    + orderSettlement.getOsSupperDiscountFee() + "(经销折扣) - "
                    + orderSettlement.getOsCommisAmount() + "(佣金金额) -"
                    + orderSettlement.getOsOrderReturnAmount() + "(退还供货价) +  "
                    + orderSettlement.getOsCommisReturnAmount() + "(退还佣金) + "
                    + orderSettlement.getOsReturnSupperDiscountFee() + "(退还经销折扣) + "
                    + orderSettlement.getOsShippingAmount() + "(运费)" ;
        }else{
            BigDecimal osAmount = orderSettlement.getOsOrderAmount();
            osAmountDetail = orderSettlement.getOsAmount() + " = " + osAmount + "(商品金额 不含运费)" + " + "
                    + orderSettlement.getOsRedpacketAmount() + "(红包金额) - "
                    + orderSettlement.getOsCommisAmount() + "(佣金金额) + "
                    + orderSettlement.getOsBaseMemberDiscount() + "(会员折扣) - "
                    + orderSettlement.getOsOrderReturnAmount() + "(退单金额) - "
                    + orderSettlement.getOsRedpacketReturnAmount() + "(退还红包金额) + "
                    + orderSettlement.getOsCommisReturnAmount() + "(退还佣金) + "
                    + orderSettlement.getOsReturnTgCommission() + "(退还推广佣金) + "
                    + orderSettlement.getOsReturnFxCommission() + "(退还分享佣金) + "
                    + orderSettlement.getOsShippingAmount() + "(运费) - "
                    + orderSettlement.getOsReturnMemberDiscount() + "(退还会员折扣) - "
                    + (null == orderSettlement.getOsTgCommission()?0.00:orderSettlement.getOsTgCommission()) + "(推广佣金) - "
                    + (null == orderSettlement.getOsFxCommission()?0.00:orderSettlement.getOsFxCommission()) + "(分享佣金) ";
        }
        returnMap.put("osAmount", osAmountDetail);
        returnMap.put("osState", orderSettlement.getOsState());

        return ResponseResult.success(returnMap);
    }

    /**
     * 根据传入的参数查询平台订单结算列表。
     *
     * @param map 包含查询参数和令牌信息的Map对象。
     * @return 返回查询结果的ResponseResult对象。
     *         如果令牌信息有效，返回包含结算列表的响应结果；
     *         如果令牌信息无效，返回失败的响应结果。
     */
    @Override
    public ResponseResult queryPlatFormListSettlement(Map<String, Object> map) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 根据查询条件分页查询平台订单结算信息
            // 分页查询数据
            IPage<PlatFormOrderSettlementVO> page = this.baseMapper.queryPlatFormListSettlement(new Query<PlatFormOrderSettlementVO>().getPage(map), map);

            // 返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 如果令牌信息无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult queryPlatFormListSettlementExcel(Map<String, Object> map) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            List<PlatFormOrderSettlementVO> page = this.baseMapper.queryPlatFormListSettlementExcel(map);
            String[] headers = {"账单编号", "订单金额(含运费)", "运费", "收取佣金", "退单金额", "退还佣金",
                    "店铺费用", "本期应结", "出账日期", "账单状态", "商家名称", "开始日期", "结束日期"};

            List<Map<String, String>> list = Lists.newArrayList();
            for (PlatFormOrderSettlementVO platFormOrderSettlement : page) {
                HashMap<String, String> mapExcel = Maps.newHashMap();
                mapExcel.put("账单编号", platFormOrderSettlement.getOsId());
                mapExcel.put("订单金额(含运费)", platFormOrderSettlement.getOsOrderAmount());
                mapExcel.put("运费", platFormOrderSettlement.getOsShippingAmount());
                mapExcel.put("收取佣金", platFormOrderSettlement.getOsCommisAmount());
                mapExcel.put("退单金额", platFormOrderSettlement.getOsOrderReturnAmount());
                mapExcel.put("退还佣金", platFormOrderSettlement.getOsCommisReturnAmount());
                mapExcel.put("店铺费用", platFormOrderSettlement.getOsShopCostAmount());
                mapExcel.put("本期应结", platFormOrderSettlement.getOsAmount());
                mapExcel.put("出账日期", platFormOrderSettlement.getOsDate().toString());
                mapExcel.put("账单状态", platFormOrderSettlement.getOsShopCostAmount());
                mapExcel.put("商家名称", platFormOrderSettlement.getShopName());
                mapExcel.put("开始日期", platFormOrderSettlement.getOsStartDate());
                mapExcel.put("结束日期", platFormOrderSettlement.getOsEndDate());
                list.add(mapExcel);
            }
            // excel文件名
            Map<String, List<Map<String, String>>> data = Maps.newHashMap();
            data.put("合并数据", list);
            String url = ExcelsUtil.createExcel(headers, data, new int[]{0});
            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }


    /**
     * 确认结算。
     * 通过传入的token和结算信息，验证用户身份并确认结算操作。
     * 如果验证成功，将结算信息插入到支付记录中，并更新结算订单的状态。
     *
     * @param map 包含token、settlementId和settlementMoney等信息的映射。
     * @return 根据操作结果返回相应的响应结果。
     */
    @Override
    public ResponseResult confirmSettlement(Map<Object, Object> map) {
        // 从映射中提取token字符串
        String token = map.get("token").toString();
        // 通过token获取用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        // 如果获取用户信息失败，则直接返回失败结果
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 将用户信息转换为UserInfoVO对象
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 从用户信息中提取店铺ID
        String shopId = ui.getShopId();
        // 从映射中提取结算ID和结算金额
        // 结算编号
        String settlementId = (String) map.get("settlementId");
        // 结算金额
        String settlementMoney = map.get("settlementMoney") + "";

        // 准备结算参数映射
        Map<String, String> paraMap = new HashMap<>(8);
        paraMap.put("osId", settlementId);
        // 根据结算ID查询订单结算实体
        OrderSettlementEntity orderSettlementEntity = this.baseMapper.selectById(settlementId);

        // 准备插入支付记录的映射
        Map<String, Object> settlementMap = new HashMap<>();
        settlementMap.put("token", token);
        settlementMap.put("settlementId", settlementId);
        settlementMap.put("settlementMoney", settlementMoney);
        settlementMap.put("settlementType", orderSettlementEntity.getOsOrderType());
        // 尝试插入支付记录，如果成功，则更新结算订单状态
        if (payMentServiceClient.insertSettlementRecord(settlementMap)) {
            this.baseMapper.updateSettlementOrder(settlementId, Integer.valueOf(shopId));
            // 返回成功响应
            return ResponseResult.success();
        }
        // 如果插入支付记录失败，则返回余额不足的错误响应
        return ResponseResult.fail(BizCodeEnume.BALANCE_NOT_ENOUGH_ERROR);
    }

    /**
     * 更新订单结算状态
     *
     * @param list 包含订单ID的列表，用于指定需要更新结算状态的订单
     * @return 返回更新操作的结果，true表示成功，false表示失败
     */
    @Override
    public boolean updateOrderSettlementStatus(List<String> list) {
        boolean  result = this.baseMapper.updateOrderSettlementStatus(list);
        return result;
    }

    /**
     * 更新结算状态
     *
     * @param list 包含需要更新结算状态的实体ID列表
     * @return 返回更新操作的结果，true表示成功，false表示失败
     */
    @Override
    public boolean updateSettlementStatus(List<String> list) {
        boolean  result = this.baseMapper.updateSettlementStatus(list);
        return result;
    }

    /**
     * 检查结算金额
     *
     * @param shopId 商家ID，用于标识需要检查结算金额的商家
     * @return 返回检查操作的结果，true表示结算金额正确，false表示结算金额不正确或存在问题
     */
    @Override
    public boolean checkSettlementAmount(String shopId) {
        boolean  result = this.baseMapper.checkSettlementAmount(shopId);
        return result;
    }

    /**
     * 获取供应商结算数据
     * @param mapAccount
     * @return
     */
    @Override
    public SupplierShopSettleAccountInfoDTO getSupplierSettleAccountData(Map<String, Object> mapAccount) {
        SupplierShopSettleAccountInfoDTO data = new SupplierShopSettleAccountInfoDTO();
        // 查询供应商订单数据
        SupplierShopSettleAccountInfoDTO settleOrdeData = this.baseMapper.getSettleAccountOrderFeign(mapAccount);
        if(null == settleOrdeData){
            settleOrdeData = new SupplierShopSettleAccountInfoDTO();
        }
        data.setOrderPaymentAmount(settleOrdeData.getOrderPaymentAmount());
        data.setOrderCommissionFee(settleOrdeData.getOrderCommissionFee());
        data.setOrderShippingFee(settleOrdeData.getOrderShippingFee());
        // 查询供应商订单id列表
        List<String> orderList = this.baseMapper.getSettleAccountOrderIdListFeign(mapAccount);
        data.setOrderIdList(orderList);

        // 查询供应商退单数据
        SupplierShopSettleAccountInfoDTO settleReturnOrdeData =this.baseMapper.getSettleAccountReturnOrderFeign(mapAccount);
        if(null == settleReturnOrdeData){
            settleReturnOrdeData = new SupplierShopSettleAccountInfoDTO();
        }
        data.setReturnCash(settleReturnOrdeData.getReturnCash());
        data.setOrderReturnCommissionFee(settleReturnOrdeData.getOrderReturnCommissionFee());
        data.setReturnShippingFee(settleReturnOrdeData.getReturnShippingFee());
        // 查询供应商退单id列表
        List<String> returOrderList = this.baseMapper.getSettleAccountReturnOrderIdListFeign(mapAccount);
        data.setReturnOrderIdList(returOrderList);

        return data;
    }
}




