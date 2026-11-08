package com.houjiang.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DesensitizationUtil;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.OperationEntityVO;
import com.houjiang.commons.vo.base.PlatformoperationVO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import com.houjiang.commons.vo.shop.RetentionShopInfoVO;
import com.houjiang.commons.vo.shop.ShopEvaluationVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.entity.OrderComplaintsEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderComplaintsMapper;
import com.houjiang.order.mapper.OrderGoodsMapper;
import com.houjiang.order.service.OrderComplaintsService;
import com.houjiang.order.vo.SoldOrderByCommonalityDetailsVO;
import com.houjiang.order.vo.SoldOrderByGoodsDetailsVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName OrderComplaintsServiceImpl
 * @Description  用户投诉表
 * @Author Amos
 * @Date 2023-08-23 09:38:15
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderComplaintsServiceImpl extends ServiceImpl<OrderComplaintsMapper, OrderComplaintsEntity>
        implements OrderComplaintsService {

    @Autowired
    private OrderBaseMapper orderBaseMapper;
    @Autowired
    private OrderGoodsMapper orderGoodsMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Override
    public ResponseResult addcomplaints(Map<String, Object> map) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        String complaintsId = String.valueOf(map.get("complaintsId"));
        String userId = userInfo.getUserId();
        //获取订单信息
        String orderId = String.valueOf(map.get("orderId"));
        // 查询当前订单是否 已被投诉
        if(complaintsId == null || complaintsId == ""){
            Integer num = this.baseMapper.queryOrderRevokeComplaints(orderId,userId);
            if ( num != 0) {
                return ResponseResult.fail(BizCodeEnume.REVOKE_COMPLAINTS_SUCCESS.getCode(), BizCodeEnume.REVOKE_COMPLAINTS_SUCCESS.getMsg());
            }
        }


        map.put("userId",userId);

        DateFormat dFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (complaintsId == null || complaintsId == "") {
            //生成投诉单号
            StringBuffer id = new StringBuffer("TS");
            DateFormat dFormat3 = new SimpleDateFormat("yyyyMMdd");

            String format = dFormat3.format(new Date());

            Random random = new Random();
            String verificationCode = String.valueOf(random.nextInt(9) + 1);
            for (int i = 0; i < 5; i++) {
                verificationCode += random.nextInt(10);
            }
            id.append(format).append(verificationCode);
            map.put("complaintsId", id.toString());
        }
        map.put("complaintsStatus", 0);
        //投诉凭证
        List vouchers = (List) map.get("complaintsVouchers");
        String s = JSON.toJSONString(vouchers);
        map.put("complaintsVoucher",s);

        //投诉商品
        List goodsId = (List) map.get("goodsIds");
        String goodsIds = JSON.toJSONString(goodsId);
        map.put("complaintsGoodsid",goodsIds);


        SoldOrderByCommonalityDetailsVO sobcd = orderBaseMapper.soldOrderByCommonalityDetails(userId, orderId);

        if (map.get("complaintsType").equals(1)) {
            //投诉超时发货直接判定
            PlatformoperationVO queryplatform = queryplatform();
            if (queryplatform == null) {
                return ResponseResult.fail(0, "投诉失败");
            }
            //获取超时发货退款比例
            OperationEntityVO sendGoods = queryplatform.getSendGoods();

            //下单时间
            Date createTime = sobcd.getOrderCreateTime();
            //订单发货时间
            Date shippingTime = sobcd.getOrderShippingTime();
            //查询是否有超时商品
            List list = determinedeliverytime(userId, orderId, createTime, shippingTime);

            if (list != null && list.size() > 0) {
                //投诉成立  有超时发货订单
                map.put("complaintsStatus", 1);

                //计算退费金额
                BigDecimal multiply = calculateamount(sobcd.getOrderPaymentAmount(), sendGoods);
                map.put("compensationAmount", multiply);

                //获取商家质保金余额
                Integer selectshop = this.baseMapper.selectshop(sobcd.getShopId());
                if(null == selectshop){
                    selectshop = 0;
                }
                BigDecimal bigDecimal = new BigDecimal(selectshop);
                if (bigDecimal.compareTo(new BigDecimal(0)) <= 0 || bigDecimal.compareTo(multiply) < 0) {
                    //质保金余额不足 暂不赔付
                    map.put("compensationStatus", 0);
                } else {
                    //进行赔付
                    Integer integer = determinedeliverytime2(userId, multiply,orderId);
                    if (integer == 1) {
                        //退款成功
                        map.put("compensationStatus", 1);
                    }

                    //修改商家质保金余额
                    //查询质保金信息
                    RetentionShopInfoVO selectretention = this.baseMapper.selectretention(sobcd.getShopId());
                    //质保金余额-
                    BigDecimal payPrice = selectretention.getRetentionMoneyBalance().subtract(multiply);

                    //质保金可提现
                    BigDecimal payPrice1 = payPrice.subtract(selectretention.getRetentionMoneyLimit());
                    if (payPrice1.compareTo(new BigDecimal(0)) == -1) {
                        payPrice1 = new BigDecimal(0);
                    }

                    //待补缴
                    BigDecimal payPrice2 = new BigDecimal(0);
                    if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) < 0) {
                        //待补缴金额
                        payPrice2 = selectretention.getRetentionMoneyPayment().add(multiply);
                        if (payPrice2.compareTo(new BigDecimal(0)) == -1) {
                            payPrice2 = new BigDecimal(0);
                        }
                    }

                    //累计赔付金额
                    BigDecimal payPrice3 = selectretention.getAccumulatedCompensation().add(multiply);
                    HashMap<Object, Object> paymap = new HashMap<>();
                    paymap.put("payPrice", payPrice);
                    paymap.put("payPrice1", payPrice1);
                    paymap.put("payPrice2", payPrice2);
                    paymap.put("payPrice3", payPrice3);
                    paymap.put("shopId", sobcd.getShopId());
                    if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) > 0){
                        paymap.put("retentionStatus",1);
                    }else if (payPrice.compareTo(new BigDecimal(0)) >= 0 && payPrice.compareTo(new BigDecimal(0)) == -1){
                        paymap.put("retentionStatus",2);

                        //发送站内信
                        String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
                        this.baseMapper.insertSysMessage1("",userId,"",5,
                                "补缴质保金",content, String.valueOf(sobcd.getShopId()));
                    }else if (payPrice.compareTo(new BigDecimal(0)) == -1){

                        //发送站内信
                        String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
                        this.baseMapper.insertSysMessage1("",userId,"",5,
                                "补缴质保金",content, String.valueOf(sobcd.getShopId()));
                        paymap.put("retentionStatus",3);
                        paymap.put("paymentTime", new Date());
                    }
                    //更新shopbase表
                    this.baseMapper.updateshopbase(paymap);
                }
            } else {
                //投诉不成立
                map.put("complaintsStatus", 2);
                map.put("compensationAmount", 0);
                map.put("compensationStatus", 1);
            }
        } else if (map.get("complaintsType").equals(2)) {
            //虚假发货
            PlatformoperationVO queryplatform = queryplatform();
            if (queryplatform == null) {
                return ResponseResult.fail(0, "投诉失败");
            }
            //获取修改发货配置
            OperationEntityVO falseShipment = queryplatform.getFalseShipment();

            //计算退费金额
            BigDecimal multiply = calculateamount(sobcd.getOrderPaymentAmount(), falseShipment);
            map.put("compensationAmount", multiply);

        } else if (map.get("complaintsType").equals(3)) {
            //违规出售
            PlatformoperationVO queryplatform = queryplatform();
            if (queryplatform == null) {
                return ResponseResult.fail(0, "投诉失败");
            }
            //获取违规出售罚款
            Integer multiply = queryplatform.getComplaintGoods();

            map.put("compensationAmount", multiply);

        } else if (map.get("complaintsType").equals(4)) {
            //骚扰辱骂
            PlatformoperationVO queryplatform = queryplatform();
            if (queryplatform == null) {
                return ResponseResult.fail(0, "投诉失败");
            }
            //获取骚扰辱骂罚款金额
            Integer multiply = queryplatform.getComplaintShop();

            map.put("compensationAmount", multiply);

        }
        map.put("shopId",sobcd.getShopId());
        map.put("fundDestination",1);
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,7);
        map.put("failureTime",dFormat4.format(instance.getTime()));
        Integer integer = 0;
        Integer id = 0;
        map.put("id",0);
        if(complaintsId != null && complaintsId != ""){
            //更新投诉
            map.put("createTime",new Date());
            integer = this.baseMapper.updatecomplaints(map);
            id = (Integer) map.get("id");
        }else{
            map.put("createTime",new Date());

            integer = this.baseMapper.addcomplaints(map);
            id = (Integer) map.get("id");
        }

        if (integer > 0){
            return ResponseResult.success("投诉成功",id);
        }


        return  ResponseResult.fail(0, "投诉失败");
    }
    /** 查询平台管理规范 */
    public PlatformoperationVO queryplatform(){
        PlatformoperationVO platformoperation = new PlatformoperationVO();
        //查询平台管理规范
        String configValue = baseServiceClient.selectConfigValueFeign("platform_operation_specifications");
        if (configValue != null ){
            platformoperation = JSONObject.parseObject(configValue, PlatformoperationVO.class);
            return platformoperation;
        }
        return null;
    }
    /** 判断订单商品是否超时发货 */
    public List determinedeliverytime(String userId,String orderId,Date createTime,Date shippingTime){
        ArrayList<Map> list = new ArrayList<>();
        ArrayList commonid = new ArrayList<>();
        //当前时间
        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        //订单创建时间
        instance.setTime(createTime);

        //获取商品信息
        List<SoldOrderByGoodsDetailsVO> sobgdList = orderBaseMapper.soldOrderByGoodsDetails(userId, orderId);
        if (sobgdList == null ){
            return list;
        }
        for (SoldOrderByGoodsDetailsVO soldOrderByGoodsDetails : sobgdList) {
            commonid.add(soldOrderByGoodsDetails.getCommonId());
        }
        if (commonid != null && commonid.size() > 0){
            List<Map> querydeliverytime = this.baseMapper.querydeliverytime(commonid);
            for (Map map : querydeliverytime) {
                Integer deliveryTime = (Integer) map.get("deliveryTime");

                instance.add(Calendar.HOUR,deliveryTime);
                //最后发货时间
                Date time = instance.getTime();
                if (shippingTime != null){
                    //已发货
                    int i = shippingTime.compareTo(time);
                    if (i > 0 ){
                        //超时发货订单
                        list.add(map);
                    }
                }else{
                    //未发货
                    int i = date.compareTo(time);
                    if (i > 0){
                        //超时发货订单
                        list.add(map);
                    }
                }
            }
        }
        return list;
    }
    /** 计算退费金额 */
    public BigDecimal calculateamount(BigDecimal paymentAmount, OperationEntityVO sendGoods){
        //计算赔付金额
        //退款百分比
        BigDecimal amount = BigDecimal.valueOf(sendGoods.getAmount());
        //最高可退
        BigDecimal highest = BigDecimal.valueOf(sendGoods.getHighest());
        //最低退
        BigDecimal minimum = BigDecimal.valueOf(sendGoods.getMinimum());
        //应退金额
        BigDecimal multiply = paymentAmount.multiply(amount.divide(new BigDecimal(100)));
        //大于最高可退
        if (multiply.compareTo(highest) > 0){
            multiply = highest;
        }
        //小于最低可退
        if (multiply.compareTo(minimum) < 0){
            multiply = minimum;
        }
        return multiply;
    }
    /** 有超时发货商品进行赔付 */
    @Transactional
    public Integer determinedeliverytime2(String userId,BigDecimal money,String orderId){
        // 初始化资产详情和管理资金的映射表
        Map<String,Object> assetDetailMap = new HashMap<>();
        Map<String,Object> ManageMoney = new HashMap<>();

        try {
            // 设置资产详情信息，用于记录用户的投诉退款详情
            // 退回用户余额
            assetDetailMap.put("detailType", 12);
            assetDetailMap.put("assetType", 1);
            assetDetailMap.put("assetAmount", money);
            assetDetailMap.put("assetDirection", "投诉退款");
            assetDetailMap.put("payType", 1);
            assetDetailMap.put("userId", userId);
            assetDetailMap.put("orderId", orderId);

            // 设置管理资金的信息，用于更新用户的卡片会员详情
            ManageMoney.put("money", money);
            ManageMoney.put("userId", userId);
            // 更新用户的卡片会员详情
            Integer integer = this.baseMapper.updateCardMemberDetailPort(ManageMoney);

            // 插入资产详情记录
            Integer integer1 = this.baseMapper.insertDetail(assetDetailMap);

            // 初始化消费记录映射表
            Map<String, Object> comRecord = new HashMap<String, Object>();
            // 格式化当前日期
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String recordDate = dateformat.format(new Date());
            // 从日期字符串中提取年、月、日
            String[] reDate = recordDate.split("-");
            String year = reDate[0];
            String month = reDate[1];
            String day = reDate[2];

            // 设置消费记录的相关信息，用于记录投诉退款的详细情况
            String recordTitle = "投诉退款";
            String record_desc = "投诉退款";
            int tradeTypeId = 3;
            int userType = 1;
            int recordWithdraw = 0;
            double osCommisAmount = 0;
            double osCommisReturnAmount = 0;
            comRecord.put("recordDate", recordDate);
            comRecord.put("recordMoney", money);
            comRecord.put("orderId", money);
            comRecord.put("year", year);
            comRecord.put("month", month);
            comRecord.put("day", day);
            comRecord.put("plusOrMinus", 1);
            comRecord.put("recordTitle", recordTitle);
            comRecord.put("record_desc", record_desc);
            comRecord.put("tradeTypeId", tradeTypeId);
            comRecord.put("userType", userType);
            comRecord.put("recordWithdraw", recordWithdraw);
            comRecord.put("osCommisAmount", osCommisAmount);
            comRecord.put("osCommisReturnAmount", osCommisReturnAmount);
            comRecord.put("money", money);
            comRecord.put("userId", userId);
            comRecord.put("isCurrentUser", 1);
            // 插入消费记录
            Integer integer2 = this.baseMapper.insertConsumeRecordport(comRecord);

            // 如果所有操作都成功，则返回1，表示操作完成
            if (integer > 0 && integer1 > 0 && integer2 > 0){
                return 1;
            }
        }catch (Exception e){

        }
        // 如果有任何异常或失败，则返回2，表示操作未完成
        return 2;

    }

    /**
     * 根据订单ID查询订单发货情况。
     *
     * @param map 包含令牌和订单ID的映射表。
     * @return 返回查询结果，如果令牌无效或订单信息有误，则返回失败响应；否则根据订单状态返回是否超时发货的响应。
     */
    @Override
    public ResponseResult query(Map<String, Object> map) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 检查令牌是否有效
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 获取用户ID
        String userId = userInfo.getUserId();

        // 将订单ID转换为字符串
        String orderId = String.valueOf(map.get("orderId"));
        // 根据用户ID和订单ID查询订单发货详情
        SoldOrderByCommonalityDetailsVO sobcd = orderBaseMapper.soldOrderByCommonalityDetails(userId, orderId);
        // 检查订单信息是否存在
        if (sobcd == null){
            return ResponseResult.fail(0, "订单信息有误",sobcd.getOrderStatus());
        }
        // 根据订单状态判断是否已发货或已取消
        if (sobcd.getOrderStatus() == 1 || sobcd.getOrderStatus() == 7){
            return ResponseResult.success(1,"未超时发货",sobcd.getOrderStatus());
        }
        // 获取订单创建时间和发货时间
        // 下单时间
        Date createTime = sobcd.getOrderCreateTime();
        // 订单发货时间
        Date shippingTime = sobcd.getOrderShippingTime();
        // 判断是否有商品发货超时
        // 查询是否有超时商品
        List list = determinedeliverytime(userId, orderId, createTime, shippingTime);
        // 如果有超时商品，则返回超时发货响应
        if (list != null && list.size() > 0){
            return ResponseResult.success(1,"有超时发货",sobcd.getOrderStatus());
        }
        // 如果没有超时商品，则返回未超时发货响应
        return ResponseResult.success(1,"未超时发货",sobcd.getOrderStatus());
    }

    /**
     * 判断订单是否在售后期内。
     *
     * @param map 包含请求信息的Map，其中必须包含"token"和"orderId"。
     * @return 如果订单在售后期内，则返回成功结果并说明处于售后期；如果订单不在售后期内或者订单信息有误，则返回失败结果并给出具体原因；如果token无效，则返回失败结果并说明token无效。
     */
    @Override
    public ResponseResult isAfterSales(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 检查token是否有效
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 获取用户ID
        String userId = userInfo.getUserId();

        // 从请求参数中获取订单ID，并转换为字符串类型
        String orderId = String.valueOf(map.get("orderId"));
        // 根据用户ID和订单ID查询订单的售后详情
        SoldOrderByCommonalityDetailsVO sobcd = orderBaseMapper.soldOrderByCommonalityDetails(userId, orderId);
        // 检查订单信息是否存在
        if (sobcd == null){
            return ResponseResult.fail(0, "订单信息有误");
        }
        // 初始化日期格式化对象，用于解析订单的最后退货时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取订单的最后退货时间
        String returnTime = sobcd.getOrderLastReturnTime();

        Date parse = null;
        // 如果最后退货时间不为空，则尝试解析为日期对象
        if (returnTime != null) {
            try {
                parse = format.parse(returnTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 如果解析成功，并且最后退货时间早于或等于当前时间，则订单处于售后期
            if (parse != null && parse.compareTo(new Date()) <= 0) {
                return ResponseResult.success(1,"该订单不在售后期",2);
            }
        }
        // 如果订单不在售后期，则返回说明订单在售后期的结果
        return ResponseResult.success(1,"在售后期",1);
    }

    @Override
    public ResponseResult isComplaints(Map<String, Object> map) {
        // 通过token获取用户信息，token存储在map中
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示token无效，返回错误响应
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 获取用户的ID，并将其添加到map中
        String userId = userInfo.getUserId();
        map.put("userId",userId);
        // 查询投诉信息，传入包含用户ID的map
        Map<String,Object> result = this.baseMapper.isComplaints(map);
        // 返回查询结果，表示操作成功
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult queryComplaints(Map<String, Object> map) {
        // 通过token获取用户信息，token存储在map中的"token"键对应的值中
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，表示token无效，返回相应的错误响应
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 从用户信息中提取用户ID，并将其添加到map中，以便后续查询投诉信息时使用
        String userId = userInfo.getUserId();
        map.put("userId",userId);
        // 打印map内容，用于调试和日志记录
        System.out.println(map);
        // 根据map中的条件查询投诉信息，返回分页结果
        IPage<OrderComplaintsEntity> page = this.baseMapper.queryComplaints(new Query<OrderComplaintsEntity>().getPage(map),map);
        // 返回查询结果的成功响应
        return ResponseResult.success(PageUtils.getPage(page));

    }

    /**
     * 查询投诉详情
     * @param map 包含token等信息的Map对象
     * @return 返回投诉详情的响应结果
     */
    @Override
    public ResponseResult complaintsDetail(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 若用户信息为空，则返回token无效的响应结果
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 获取用户ID，并将其加入到map中
        String userId = userInfo.getUserId();
        map.put("userId",userId);

        // 根据map查询投诉详情
        OrderComplaintsEntity complaintsdetail = this.baseMapper.complaintsdetail(map);
        // 若投诉详情不为空，则处理投诉详情中的数据
        if (complaintsdetail != null){
            // 将投诉凭证字符串转换为List<String>
            // 凭证
            String complaintsVoucher = complaintsdetail.getComplaintsVoucher();
            List<String> list = JSON.parseArray(complaintsVoucher, String.class);
            complaintsdetail.setComplaintsVouchers(list);

            // 将投诉商品ID字符串转换为List<Integer>
            // 商品id
            String complaintsGoodsid = complaintsdetail.getComplaintsGoodsid();
            List<Integer> list1 = JSON.parseArray(complaintsGoodsid, Integer.class);
            complaintsdetail.setComplaintsGoodsids(list1);

            // 若投诉商品ID列表不为空且不为空列表，则查询相关订单商品详情，并设置到投诉详情中
            if (list1 != null && list1.size() > 0){
                HashMap<String, Object> map1 = new HashMap<>();
                String orderId = complaintsdetail.getOrderId();
                // 若订单ID以"T"开头，查询并设置关联的投诉订单详情
                if (orderId.startsWith("T")){
                    OrderComplaintsEntity orderComplaintsEntity = this.baseMapper.complaintsdetail(map);
                    complaintsdetail.setCorrelationComplaint(orderComplaintsEntity);
                }
                // 设置订单ID和商品ID列表到map中，用于查询订单商品详情
                map1.put("orderId",orderId);
                map1.put("list",list1);
                List<GoodsDetailsListVO> orderGoods = orderGoodsMapper.queryComplaintsByGoodsList(map1);
                complaintsdetail.setGoods(orderGoods);
            }

            // 将卖家凭证字符串转换为List<String>
            // 卖家凭证
            String sellerVoucher = complaintsdetail.getSellerVoucher();
            List<String> list2 = JSON.parseArray(sellerVoucher, String.class);
            complaintsdetail.setSellerVouchers(list2);
        }
        // 返回处理后的投诉详情的响应结果
        return ResponseResult.success(complaintsdetail);
    }

    /**
     * 取消投诉的方法
     *
     * @param map 包含令牌和投诉ID的映射表
     * @return 根据操作结果返回不同的响应结果
     *
     * 本方法首先通过令牌解析获取用户信息，如果令牌无效，则返回相应的错误结果。
     * 接着，使用用户ID和投诉ID尝试取消投诉。如果取消成功，则返回成功结果；
     * 否则，返回失败结果。
     */
    @Override
    public ResponseResult revokeComplaints(Map<String, Object> map) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 检查用户信息是否有效
        if (userInfo == null ){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 获取用户ID
        String userId = userInfo.getUserId();
        // 尝试取消投诉，返回影响的行数
        Integer integer = this.baseMapper.revokeComplaints(userId, String.valueOf(map.get("complaintsId")));
        // 根据取消结果返回相应的响应
        if (integer > 0){
            return ResponseResult.success(1,"撤销成功",1);
        }
        return ResponseResult.fail(2,"撤销失败",2);
    }

    @Override
    public ResponseResult queryComplaintsByPlatform(Map<String, Object> map) {
        // 检查平台令牌是否存在，如果不存在，则权限不足，返回错误信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 初始化一个HashMap用于后续操作
        HashMap<Object, Object> hashMap = new HashMap<>();
        // 如果查询参数中的startTime为空，则将其设置为null，以便后续处理
        if (StringUtils.isEmpty(map.get("startTime"))){
            map.put("startTime",null);
        }
        // 如果查询参数中的endTime为空，则将其设置为null，以便后续处理
        if (StringUtils.isEmpty(map.get("endTime"))){
            map.put("endTime",null);
        }

        // 根据查询条件查询投诉记录，并分页处理
        IPage<OrderComplaintsEntity> page=this.baseMapper.queryComplaintsByPlatform(new Query<OrderComplaintsEntity>().getPage(map),
                map);
        // 获取脱敏开关的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        // 如果脱敏开关打开，并且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
            for (OrderComplaintsEntity vo : page.getRecords()) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, OrderComplaintsEntity.class), vo);
            }
        }
        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));

    }

    @Override
    public ResponseResult queryComplaintsDetailByPlatform(Map<String, Object> map) {
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));

        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        HashMap<Object, Object> hashMap = new HashMap<>();
        OrderComplaintsEntity querycomplaintsdetail = this.baseMapper.querycomplaintsdetail(map);
        if (querycomplaintsdetail != null) {
            String complaintsVoucher = querycomplaintsdetail.getComplaintsVoucher();
            List<String> strings = JSON.parseArray(complaintsVoucher, String.class);
            querycomplaintsdetail.setComplaintsVouchers(strings);

            String sellerVoucher = querycomplaintsdetail.getSellerVoucher();
            List<String> strings1 = JSON.parseArray(sellerVoucher, String.class);
            querycomplaintsdetail.setSellerVouchers(strings1);

            String goodsid = querycomplaintsdetail.getComplaintsGoodsid();
            List<Integer> list1 = JSON.parseArray(goodsid, Integer.class);
            querycomplaintsdetail.setComplaintsGoodsids(list1);
            if (list1 != null && list1.size() > 0){
                HashMap<String, Object> map1 = new HashMap<>();
                String orderId = querycomplaintsdetail.getOrderId();
                if (orderId.startsWith("T")){
                    OrderComplaintsEntity orderComplaintsEntity = this.baseMapper.querycomplaintsdetail1(orderId);
                    querycomplaintsdetail.setCorrelationComplaint(orderComplaintsEntity);
                }
                map1.put("orderId",orderId);
                map1.put("list",list1);
                List<GoodsDetailsListVO> orderGoods = this.baseMapper.queryGoodsList(map1);
                if (orderGoods != null && orderGoods.size() > 0){
                    for (GoodsDetailsListVO orderGood : orderGoods) {
                        Integer normalStatus = orderGood.getOrderStatus();
                        Integer orderRefundStatus = orderGood.getOrderRefundStatus();
                        Integer orderReturnStatus = orderGood.getOrderReturnStatus();
                        Integer orderBarterStatus = orderGood.getOrderBarterStatus();
                        Integer status;
                        if (normalStatus == 3 ) {
                            // 未发货状态
                            if (orderRefundStatus == 1 || orderRefundStatus == 3) {
                                // 未发货退款中
                                status = 22;
                            } else if (orderRefundStatus == 2) {
                                // 商家拒绝或审核通过，继续发货，但不可再次申请退款
                                status = 26;
                            } else if (orderRefundStatus == 0 ) {
                                status = normalStatus;
                            } else{
                                status = 3;
                            }
                        } else if (normalStatus == 6) {
                            // 已完成状态
                            if (orderRefundStatus == 1 || orderRefundStatus == 3) {
                                // 收货退款中
                                status = 22;
                            } else if (orderRefundStatus == 2) {
                                //收货退款完成
                                status = 26;
                            } else if (orderRefundStatus == 0 && orderReturnStatus == 0 && orderBarterStatus == 0) {
                                //无退货退款
                                status = 6;
                            } else if (orderReturnStatus == 1 || orderReturnStatus == 3) {
                                //} else if (orderReturnStatus == 1 ) {
                                // 收货退货中
                                status = 24;
                            } else if (orderReturnStatus == 2) {
                                //退货完成
                                status = 25;
                            } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTERING.getCode()) ||
                                    orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_FAILED.getCode())){
                                // 换货中
                                status = 29;
                            } else if(orderBarterStatus.equals(CommonType.orderBarterStatus.BARTER_COMPLETED.getCode())){
                                // 换货完成
                                status = 30;
                            }else{
                                status = 6;
                            }
                        } else {
                            status = normalStatus;
                        }
                        orderGood.setOrderStatus(status);
                    }
                }
                querycomplaintsdetail.setGoods(orderGoods);
            }
        }
        //查询店铺评分
        Integer shopId = querycomplaintsdetail.getShopId();
        ShopEvaluationVO see = this.baseMapper.ShopScore(shopId);
        if(see != null) {
            double add = 0;
            double servicecreditScore = see.getAvgServicecredit();
            double desccreditScore = see.getAvgDesccredit();
            double deliverycreditScore = see.getAvgDeliverycredit();
            if (String.valueOf(servicecreditScore) == null || see.getAvgServicecredit() == 0) {
                see.setEvaluationServicecredit(5);
                add = add + 5;
            } else {
                see.setEvaluationServicecredit(see.getAvgServicecredit());
                add = add + see.getAvgServicecredit();
            }
            if (String.valueOf(desccreditScore) == null || see.getAvgDesccredit() == 0) {
                see.setEvaluationDesccredit(5);
                add = add + 5;
            } else {
                see.setEvaluationDesccredit(see.getAvgDesccredit());
                add = add + see.getAvgDesccredit();
            }
            if (String.valueOf(deliverycreditScore) == null || see.getAvgDeliverycredit() == 0) {
                see.setEvaluationDeliverycredit(5);
                add = add + 5;
            } else {
                see.setEvaluationDeliverycredit(see.getAvgDeliverycredit());
                add = add + see.getAvgDeliverycredit();
            }
            BigDecimal two = new BigDecimal(add);
            double three = two.divide(new BigDecimal(3)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            hashMap.put("score",three);
        }else{
            hashMap.put("score",5);
        }
        //查询历史投诉单
        Integer integer = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<OrderComplaintsEntity>()
                .eq(OrderComplaintsEntity::getShopId, querycomplaintsdetail.getShopId())));
        if (integer == null){
            hashMap.put("historicalComplaints",0);
        }
        hashMap.put("historicalComplaints",integer);
        //查询历史申诉单
        Integer integer1 = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<OrderComplaintsEntity>()
                .eq(OrderComplaintsEntity::getShopId, querycomplaintsdetail.getShopId())
                .eq(OrderComplaintsEntity::getIsAppeal, CommonType.commonStatus.YES.getCode())));
        if (integer1 == null){
            hashMap.put("historicalAppeal",0);
        }
        hashMap.put("historicalAppeal",integer1);

        hashMap.put("list",querycomplaintsdetail);
        return ResponseResult.success(hashMap);
    }

    /**
     * 处理投诉更新
     * @param map 包含投诉相关信息的Map对象，其中包含token、status、amount等关键信息
     * @return 根据处理结果返回相应的响应结果
     */
    @Override
    @GlobalTransactional
    public ResponseResult updateComplaint(Map<String, Object> map) {
        // 检查token是否存在，如果不存在则返回权限不足的错误信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 从map中获取投诉状态和赔偿金额等信息
        Integer status = (Integer) map.get("status");
        String amount1 = String.valueOf(map.get("amount"));
        String content = "";
        // 根据map中的信息查询投诉详情
        OrderComplaintsEntity querycomplaintsdetail = this.baseMapper.querycomplaintsdetail(map);
        if (querycomplaintsdetail == null){
            return ResponseResult.fail(0, "投诉单信息错误");
        }

        // 初始化赔偿金额
        BigDecimal amount = new BigDecimal(querycomplaintsdetail.getCompensationAmount());
        // 如果map中提供了赔偿金额，则使用该金额
        // 优先平台指定金额
        if (amount1 != null && !amount1.isEmpty()) {
            amount = new BigDecimal(amount1);
        }

        // 根据投诉类型设置扣款通知内容
        Integer type = querycomplaintsdetail.getComplaintsType();
        switch (type) {
            case 1:
                content = "扣款通知：您的店铺违反了【卖家未按时发货】规则将扣款" + amount + "元";
                break;
            case 2:
                content = "扣款通知：您的店铺违反了【卖家虚假发货、缺货】规则将扣款" + amount + "元";
                break;
            case 3:
                content = "扣款通知：您的店铺违反了【卖家违规出售商品、发布虚假商品信息】规则将扣款" + amount + "元";
                break;
            case 4:
                content = "扣款通知：您的店铺违反了【卖家辱骂、骚扰、拉黑等】规则将扣款" + amount + "元";
                break;
            case 5:
                content = "扣款通知：您的店铺违反了【售后退款】规则将扣款" + amount + "元";
                break;
        }

        // 根据投诉状态进行不同的处理
        if (status == 1) {
            // 投诉成立的处理逻辑
            RetentionShopInfoVO selectretention = this.baseMapper.selectretention(querycomplaintsdetail.getShopId());
            // 质保金余额
            BigDecimal balance = selectretention.getRetentionMoneyBalance();
            // 判断质保金余额是否足够支付赔偿金额
            if (amount.compareTo(balance) > 0) {
                // 质保金余额不足，设置赔偿状态为未赔付
                map.put("compensationStatus", 0);
            } else {
                // 质保金余额足够，设置赔偿状态为已赔付，并处理赔偿金额的转移
                map.put("compensationStatus", 1);
                determinedeliverytime1(querycomplaintsdetail.getUserId(), amount, querycomplaintsdetail.getOrderId());
            }
            // 更新店铺质保金余额
            updateshopmoney(querycomplaintsdetail.getShopId(), amount);
            // 设置投诉状态和赔偿金额等信息，并更新投诉处理状态
            map.put("complaintsStatus",1);
            map.put("compensationAmount",amount);
            map.put("shopId",querycomplaintsdetail.getShopId());
            map.put("complaintsId",querycomplaintsdetail.getComplaintsId());
            this.baseMapper.handlingcomplaints(map);

            // 发送短信通知
            this.baseMapper.insertSysMessage1(querycomplaintsdetail.getComplaintsId(),selectretention.getUserId(),"",2,
                    "申诉处理结果",content, String.valueOf(selectretention.getShopId()));
            return ResponseResult.success("处理成功");
        } else if (status == 2) {
            // 投诉不成立的处理逻辑
            map.put("complaintsStatus",2);
            map.put("compensationAmount",amount);
            map.put("shopId",querycomplaintsdetail.getShopId());
            map.put("complaintsId",querycomplaintsdetail.getComplaintsId());
            this.baseMapper.handlingcomplaints(map);
            return ResponseResult.success("处理成功");
        }
        // 如果投诉状态不符合预期，则返回处理失败的错误信息
        return ResponseResult.fail(0, "处理失败");
    }

    /**
     * 查询月罚款金额
     * 该方法通过调用baseMapper的querymonthFine方法来获取月罚款金额
     *
     * @param map 包含查询条件的映射
     * @return 罚款金额的字符串表示
     */
    @Override
    public String querymonthFine(Map<String, Object> map) {
        String result = this.baseMapper.querymonthFine(map);
        return result;
    }

    /**
     * 添加投诉记录
     * 该方法通过调用baseMapper的addComplaints方法来添加投诉记录
     *
     * @param map 包含投诉信息的映射
     * @return 插入结果的整数表示
     */
    @Override
    public Integer addComplaints(Map<String, Object> map) {
        Integer result = this.baseMapper.addComplaints(map);
        return result;
    }

    /**
     * 更新投诉支付状态
     * 该方法通过调用baseMapper的updateispay方法来更新投诉的支付状态
     *
     * @param id 要更新的投诉记录的ID
     */
    @Override
    public void updateispay(Integer id) {
        this.baseMapper.updateispay(id);
    }

    /**
     * 处理投诉
     * 该方法通过调用baseMapper的handlingComplaints方法来处理投诉
     *
     * @param map 包含处理投诉所需信息的映射
     * @return 处理结果的整数表示
     */
    @Override
    public Integer handlingComplaints(Map<String, Object> map) {
        Integer result = this.baseMapper.handlingComplaints(map);
        return result;
    }

    /**
     * 查询未支付的投诉记录
     * 该方法通过调用baseMapper的unpaidcomplaints方法来获取未支付的投诉记录
     *
     * @param shopId 商店ID，用于查询特定商店的未支付投诉记录
     * @return 未支付投诉记录的列表
     */
    @Override
    public List<OrderComplaintsVO> unpaidcomplaints(String shopId) {
        List<OrderComplaintsVO> result = this.baseMapper.unpaidcomplaints(shopId);
        return result;
    }

    /**
     * 查询保留金额总数
     * 该方法通过调用baseMapper的selectretentiontotal方法来获取保留金额总数
     *
     * @param shopId 商店ID，用于查询特定商店的保留金额总数
     * @param complaintsStatus 投诉状态，用于过滤查询结果
     * @return 保留金额总数的整数表示
     */
    @Override
    public Integer selectretentiontotal(String shopId, Integer complaintsStatus) {
        Integer result = this.baseMapper.selectretentiontotal(shopId, complaintsStatus);
        return result;
    }

    /**
     * 根据投诉ID查询投诉详情
     * 该方法通过调用baseMapper的queryComplaintsId方法来获取投诉详情
     *
     * @param complaintsId 投诉ID，用于查询特定投诉的详情
     * @return 投诉详情的对象表示
     */
    @Override
    public OrderComplaintsVO queryComplaintsId(String complaintsId) {
        OrderComplaintsVO result = this.baseMapper.queryComplaintsId(complaintsId);
        return result;
    }

    /**
     * 根据投诉ID查询投诉详情
     *
     * @param complaintsId 投诉ID
     * @return 返回OrderComplaintsVO对象，包含投诉详细信息
     */
    @Override
    public OrderComplaintsVO queryComplaintsId1(Integer complaintsId) {
        OrderComplaintsVO result = this.baseMapper.queryComplaintsId1(complaintsId);
        return result;
    }

    /**
     * 根据投诉ID选择订单ID
     *
     * @param complaintsId 投诉ID字符串形式
     * @return 返回一个Map对象，包含订单ID等相关信息
     */
    @Override
    public Map<String, Object> selectOrderId(String complaintsId) {
        Map<String, Object> result = this.baseMapper.selectOrderId(complaintsId);
        return result;
    }

    /**
     * 查询特定店铺的所有罚款记录
     *
     * @param shopId 店铺ID字符串形式
     * @return 返回一个字符串，概括了所有罚款信息
     */
    @Override
    public String queryAllFine(String shopId) {
        String result = this.baseMapper.queryAllFine(shopId);
        return result;
    }


    /**
     * 处理投诉退款，更新用户资产和记录消费信息。
     * @param userId 用户ID。
     * @param money 退款金额。
     * @param orderId 订单ID。
     * @return 返回处理结果，1表示处理成功，2表示处理失败。
     */
    @Transactional
    public Integer determinedeliverytime1(String userId,BigDecimal money,String orderId){
        // 初始化资产详情和管理资金的映射对象
        Map<String,Object> assetDetailMap = new HashMap<>();
        Map<String,Object> ManageMoney = new HashMap<>();

        // 退回用户余额
        assetDetailMap.put("detailType", 12);
        assetDetailMap.put("assetType", 1);
        assetDetailMap.put("assetAmount", money);
        assetDetailMap.put("assetDirection", "投诉退款");
        assetDetailMap.put("payType", 1);
        assetDetailMap.put("userId", userId);
        assetDetailMap.put("orderId", orderId);

        // 设置管理资金信息，用于更新用户余额
        ManageMoney.put("money", money);
        ManageMoney.put("userId", userId);
        // 更新用户卡会员详情
        Integer integer = this.baseMapper.updateCardMemberDetailPort(ManageMoney);

        // 插入资产详情记录
        Integer integer1 = this.baseMapper.insertDetail(assetDetailMap);

        // 初始化消费记录映射对象
        Map<String, Object> comRecord = new HashMap<String, Object>();
        // 格式化当前日期
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        String recordDate = dateformat.format(new Date());
        // 提取日期的年、月、日
        String[] reDate = recordDate.split("-");
        String year = reDate[0];
        String month = reDate[1];
        String day = reDate[2];
        // 设置消费记录的相关信息
        String recordTitle = "投诉退款";
        String record_desc = "投诉退款";
        int tradeTypeId = 3;
        int userType = 1;
        int recordWithdraw = 0;
        double osCommisAmount = 0;
        double osCommisReturnAmount = 0;
        comRecord.put("recordDate", recordDate);
        comRecord.put("recordMoney", money);
        comRecord.put("orderId", money);
        comRecord.put("year", year);
        comRecord.put("month", month);
        comRecord.put("day", day);
        comRecord.put("plusOrMinus", 1);
        comRecord.put("recordTitle", recordTitle);
        comRecord.put("record_desc", record_desc);
        comRecord.put("tradeTypeId", tradeTypeId);
        comRecord.put("userType", userType);
        comRecord.put("recordWithdraw", recordWithdraw);
        comRecord.put("osCommisAmount", osCommisAmount);
        comRecord.put("osCommisReturnAmount", osCommisReturnAmount);
        comRecord.put("money", money);
        comRecord.put("userId", userId);
        // 标记当前订单是否属于当前用户
        // 判断当前订单是否属于当前用户 1 是 2 否
        comRecord.put("isCurrentUser", 1);
        // 插入消费记录
        Integer integer2 = this.baseMapper.insertConsumeRecordport(comRecord);

        // 检查所有操作是否成功，如果都成功则返回1，否则返回2
        if (integer > 0 && integer1 > 0 && integer2 > 0){
            return 1;
        }
        return 2;
    }



    /** 罚扣质保金 */
    @Transactional
    public Integer updateshopmoney(Integer shopId,BigDecimal amount){
        RetentionShopInfoVO selectretention = this.baseMapper.selectretention2(shopId);
        //质保金余额-
        BigDecimal payPrice = selectretention.getRetentionMoneyBalance().subtract(amount);
        //质保金可提现
        BigDecimal payPrice1 = payPrice.subtract(selectretention.getRetentionMoneyLimit());
        if (payPrice1.compareTo(new BigDecimal(0)) == -1) {
            payPrice1 = new BigDecimal(0);
        }

        //待补缴
        BigDecimal payPrice2 = new BigDecimal(0);
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) < 0) {
            //待补缴金额  基础额度 - 余额
            payPrice2 = selectretention.getRetentionMoneyLimit().subtract(payPrice);
            if (payPrice2.compareTo(new BigDecimal(0)) == -1) {
                payPrice2 = new BigDecimal(0);
            }
        }

        //累计赔付金额
        BigDecimal payPrice3 = selectretention.getAccumulatedCompensation().add(amount);
        HashMap<Object, Object> paymap = new HashMap<>();
        paymap.put("payPrice", payPrice);
        paymap.put("payPrice1", payPrice1);
        paymap.put("payPrice2", payPrice2);
        paymap.put("payPrice3", payPrice3);
        paymap.put("shopId", shopId);
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) >= 0) {
            paymap.put("retentionStatus", 1);
        } else if (payPrice.compareTo(new BigDecimal(0)) >= 0 && payPrice2.compareTo(new BigDecimal(0)) > 0) {
            //发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            this.baseMapper.insertSysMessage1("",selectretention.getUserId(),"",5,
                    "补缴质保金",content, String.valueOf(selectretention.getShopId()));

            paymap.put("retentionStatus", 2);
        } else if (payPrice.compareTo(new BigDecimal(0)) == -1) {
            //发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            this.baseMapper.insertSysMessage1("",selectretention.getUserId(),"",5,
                    "补缴质保金",content, String.valueOf(selectretention.getShopId()));

            paymap.put("retentionStatus", 3);
            paymap.put("paymentTime", new Date());
        }
        //更新shopbase表
        if(selectretention.getRetentionStatus() == 3){
            paymap.put("paymentTime", null);
        }
        return  this.baseMapper.updateshopbase(paymap);
    }

}
