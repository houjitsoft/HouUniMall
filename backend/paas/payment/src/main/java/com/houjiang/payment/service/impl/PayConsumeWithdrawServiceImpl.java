package com.houjiang.payment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.order.OrderSettlementVO;
import com.houjiang.commons.vo.shop.ShopBankMessageVO;
import com.houjiang.commons.vo.shop.ShopUserMessageResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.ConsumeWithdrawDTO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.PayAssetDetailsMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeWithdrawMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.PayConsumeWithdrawService;
import com.houjiang.payment.vo.PageInfoVO;
import com.houjiang.payment.vo.WithdrawRecordVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class PayConsumeWithdrawServiceImpl extends ServiceImpl<PayConsumeWithdrawMapper, PayConsumeWithdrawEntity>
        implements PayConsumeWithdrawService {

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Autowired
    private PayUserResourceMapper payUserResourceMapper;

    @Autowired
    private PayAssetDetailsMapper payAssetDetailsMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Resource
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private SupplierServiceClient supplierServiceClient;

    @Override
    public ResponseResult querySettlementOrder(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在，如果不存在则返回token失效的错误信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 解析token获取UserInfoVO对象
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 获取用户ID
            String userId = userInfo.getUserId();
            // 如果userInfo中包含shopId，则通过shopId获取用户ID（商家店铺主账号用户ID）
            // 获取商家店铺主账号用户id
            if (null != userInfo.getShopId()) {
                userId = usermsServiceClient.getUserIDByShopId(userInfo.getShopId());
            }
            // 从请求参数中获取结算类型，如果不存在则为null
            Integer settleType = null == map.get("settleType") ? null : (Integer) map.get("settleType");
            // 根据请求参数创建Page对象，用于分页查询
            Page<OrderSettlementVO> page = new Page<>(Integer.parseInt(map.get("page") + ""), Integer.parseInt(map.get("limit") + ""));
            IPage<OrderSettlementVO> settlementList;
            // 根据settleType是否为null，调用不同的querySettlementOrder方法进行查询
            if (null == settleType) {
                settlementList = this.baseMapper.querySettlementOrder(page, userId,null);
            } else {
                settlementList = this.baseMapper.querySettlementOrder(page, userId, settleType);
            }
            // 根据用户ID和结算类型，查询结算记录的总金额
            BigDecimal sumRecordMoney = this.baseMapper.getSumRecordMoney(userId,settleType);
            // 创建PageInfoVO对象，设置分页查询结果和结算记录总金额
            PageInfoVO infoVO = new PageInfoVO();
            infoVO.setPageList(PageUtils.getPage(settlementList));
            infoVO.setSumRecordMoney(sumRecordMoney);
            // 返回查询结果成功的响应
            return ResponseResult.success(infoVO);
        } catch (Exception e) {
            // 如果出现异常，则返回异常信息和错误码500
            return ResponseResult.fail(500, e.getMessage());
        }

    }

    @Override
    public ResponseResult queryMoney(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在，如果不存在，则返回token失效的错误信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 从Redis中获取用户信息字符串
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            // 将用户信息字符串反序列化为UserInfoDTO对象
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            // 从UserInfoDTO中获取UserInfoVO对象
            UserInfoVO userInfo = dto.getUser();
            // 从UserInfoVO中获取用户ID
            String userId = userInfo.getUserId();
            /** 从请求参数中获取记录ID列表 */
            @SuppressWarnings("unchecked")
            List<Integer> recordIdList = (List<Integer>) map.get("recordId");
            // 如果记录ID列表为空，查询并返回该用户的全部金额
            if (recordIdList.isEmpty()) {
                // 判断为空，处理所有金额
                BigDecimal totalMoney = this.baseMapper.queryAllMoney(userId);
                return ResponseResult.success(totalMoney);
            }
            // 如果记录ID列表不为空，查询并返回该用户在指定记录上的金额
            BigDecimal recordMoney = this.baseMapper.queryRecordMoney(recordIdList, userId);
            return ResponseResult.success(recordMoney);
        } catch (Exception e) {
            // 如果发生异常，返回包含异常信息的错误响应
            return ResponseResult.fail(500, e.getMessage());
        }

    }

    @Override
    @Transactional
    public ResponseResult queryWithdrawal(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            UserInfoVO userInfo = dto.getUser();
            // 发送人id
            String userId = userInfo.getUserId();
            String userName = userInfo.getUserName();
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 收款方
            String payee = (String) map.get("payee");
            // 银行名称
            String bankName = (String) map.get("bankName");
            // 银行卡号
            String bankNum = (String) map.get("bankNum");
            // 支行所在地
            String bankAddress = (String) map.get("bankAddress");
            // 提现金额
            String money = (String) map.get("money");
            if (StringUtils.isBlank(money) || new BigDecimal(money).compareTo(new BigDecimal(0)) <= 0) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }
            // 类型转换
            BigDecimal userMoney = new BigDecimal(0);
            // 判断是否是全部提现
            Integer type = (Integer) map.get("type");
            // 结算单号
            String statementNo = null;
            List<String> no = new ArrayList<String>();
            List<String> list = new ArrayList<String>();
            @SuppressWarnings("unused")
            // 提现说明
            String withdrawalInstructions = (String) map.get("withdrawalInstructions");
            // 手机验证码
            String phoneValidation = (String) map.get("phoneValidation");
            // 获取存储手机验证码
            String phoneCode = RedisUtil.get(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
            String con = "商家提现";
            // 提现订单号
            String orderId = "W" + TimeStampUtil.getTimeStamp() + TimeStampUtil.randomLengthByTwo();
            String recordDate = TimeStampUtil.getYear() + "-" + TimeStampUtil.getMonth() + "-"
                    + TimeStampUtil.getDay();
            String recordYear = TimeStampUtil.getYear();
            String recordMonth = TimeStampUtil.getMonth();
            String recordDay = TimeStampUtil.getDay();
            String recordTitle = "提现";
            String recordDesc = con;
            Integer tradeTypeId = 4;
            Integer userType = 1;
            Integer recordStatus = 8;
            // 商家用户提现，判断验证是否正确
            if (!phoneValidation.equals(phoneCode)) {
                return ResponseResult.fail(10015, "验证码错误！");
            }
            String createTime = TimeStampUtil.getNowDate();
            ShopUserMessageResultVO shopUserMessageResult = merchantServiceClient.queryUserId(shopId);
            String shopName = null;
            if (shopUserMessageResult != null) {
                shopName = shopUserMessageResult.getShopName();
            }
            boolean isUpdateSuccess = false;
            if (type == 1) {
                statementNo = (String) map.get("statementNo");
                String str[] = statementNo.split(",");
                list = Arrays.asList(str);
                userMoney = this.baseMapper.queryCalculateRecordMoney(list, userId);
                // 更新结算单表
                isUpdateSuccess = this.baseMapper.updateRecordWithdraw(list);
            } else {
                // 查询所有结算单
                no = this.baseMapper.queryAllStatementNo(userId);
                list = no;
                userMoney = this.baseMapper.queryCalculateRecordMoney(list, userId);
                // 更新结算单表
                isUpdateSuccess = this.baseMapper.updateRecordWithdraw(no);
            }
            String recordPayorder = statementNo;

            Map<String,Object> paraMap = new HashMap<>();
            //通用参数
            paraMap.put("userId",userId);
            paraMap.put("userName",userName);
            paraMap.put("orderId",orderId);
            paraMap.put("money",userMoney);
            //withdraw参数
            paraMap.put("createTime",createTime);
            paraMap.put("con",con);
            paraMap.put("bankName",bankName);
            paraMap.put("bankAddress",bankAddress);
            paraMap.put("bankNum",bankNum);
            paraMap.put("payee",payee);
            paraMap.put("statementNo",statementNo);
            paraMap.put("shopName",shopName);
            paraMap.put("settleType",1);
            //record参数
            paraMap.put("recordDate",recordDate);
            paraMap.put("recordYear",recordYear);
            paraMap.put("recordMonth",recordMonth);
            paraMap.put("recordDay",recordDay);
            paraMap.put("recordTitle",recordTitle);
            paraMap.put("recordDesc",recordDesc);
            paraMap.put("tradeTypeId",tradeTypeId);
            paraMap.put("userType",userType);
            paraMap.put("recordStatus",recordStatus);
            paraMap.put("recordPayorder",recordPayorder);
            // 插入withdraw表记录
            boolean isInsertSuccess = this.baseMapper.insertConsumeWithdrawBySeller(paraMap);
            // 插入record提现明细
            boolean isInsertRecordSuccess = this.baseMapper.insertRecord(paraMap);
            if (!(isInsertSuccess && isUpdateSuccess && isInsertRecordSuccess)) {
                return ResponseResult.fail(2, "提现申请提交失败！");
            }
            //更新我的余额明细
            Map<String, Object> assetDetailMap = new HashMap<>();
            assetDetailMap.put("assetDirection", "提现");
            assetDetailMap.put("detailType", 4);
            assetDetailMap.put("assetType", 2);
            assetDetailMap.put("payType", 1);
            assetDetailMap.put("assetAmount", money);
            assetDetailMap.put("userId", userId);
            assetDetailMap.put("orderId", orderId);
            this.baseMapper.insertDetail(assetDetailMap);
            return ResponseResult.success(1, "提现申请已提交，等待审核中！");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    @Override
    public ResponseResult queryUserInfo(Map<String, Object> map) {
        // 从map中获取token字符串
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在，如果不存在则返回token失效的信息
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 从Redis中获取token对应的用户信息字符串
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            // 将用户信息字符串反序列化为UserInfoDTO对象
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            // 从UserInfoDTO中获取UserInfoVO对象
            UserInfoVO userInfo = dto.getUser();
            // 根据用户店铺ID查询店铺银行信息
            ShopBankMessageVO sbmr = merchantServiceClient.getShopBankMessage(userInfo.getShopId());
            if (sbmr == null) {
                return ResponseResult.fail(0, "无法获取店铺银行信息");
            }
            // 组合完整的银行地址
            String completeBankAddress = getAddress(sbmr.getBankProvince() + "", sbmr.getBankCity() + "", sbmr.getBankArea() + "");
            // 如果银行地址为空，则设置为空字符串
            if (sbmr.getBankAddress() == null) {
                sbmr.setBankAddress("");
            }
            // 更新完整的银行地址到sbmr对象中，避免“nullnullnull”这样的组合
            completeBankAddress = completeBankAddress + sbmr.getBankAddress();
            if (completeBankAddress.equals("nullnullnull")) {
                completeBankAddress = "";
            }
            sbmr.setBankAddress(completeBankAddress);
            // 返回成功响应，包含更新后的店铺银行信息
            return ResponseResult.success(sbmr);
        } catch (Exception e) {
            // 如果出现异常，则返回失败响应，包含异常信息
            return ResponseResult.fail(500, e.getMessage());
        }

    }
    /**
     * 根据省、市、区ID获取对应的地址信息。
     *
     * @param provinceId 省份ID
     * @param cityId 城市ID
     * @param districtId 区县ID
     * @return 组合后的地址信息，如果ID都为空，则返回空字符串
     */
    private String getAddress(String provinceId, String cityId, String districtId) {
        // 检查省、市、区ID是否为空，如果都为空，则直接返回空字符串
        // 检查省、市、区ID是否均为空，如果都为空，则直接返回空字符串
        if ((provinceId == null || provinceId.equals("") || provinceId.equals("null"))
                && (cityId == null || cityId.equals("") || cityId.equals("null"))
                && (districtId == null || districtId.equals("") || districtId.equals("null"))) {
            return "";
        }

        // 通过基础服务客户端根据ID查询省、市、区的名称
        // 根据省ID查询省名
        String provinceName = baseServiceClient.queryProvinceport(provinceId);
        // 根据市ID查询市名
        String cityName = baseServiceClient.queryProvinceport(cityId);
        // 根据区ID查询区名
        String districtName = baseServiceClient.queryProvinceport(districtId);

        // 返回省、市、区名称的组合字符串
        // 返回省、市、区名的组合字符串
        return provinceName + cityName + districtName;

    }


    @Override
    public ResponseResult queryBusiness(Map<String, Object> map) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            String userId = userInfo.getUserId();
            // 获取商家店铺主账号用户id
            if (null != userInfo.getShopId()) {
                userId = usermsServiceClient.getUserIDByShopId(userInfo.getShopId());
            }
            Integer type = (Integer) map.get("type");
            String allStatus = null;
            Integer otherStatus = null;
            if (type == 1) {
                // 查看全部
                allStatus = "''";
            } else if (type == 2) {
                // 待审核
                otherStatus = 0;
            } else if (type == 3) {
                // 通过
                otherStatus = 3;
            } else if (type == 4) {
                // 打款完成
                otherStatus = 2;
            } else if (type == 5) {
                // 不通过
                otherStatus = 4;
            } else if (type == 6) {
                // 进行中
                otherStatus = 1;
            }
            // 初始化页面信息并根据map中的页码和大小创建Page对象
            map.put("allStatus", allStatus);
            map.put("otherStatus", otherStatus);
            map.put("userId", userId);
            Page<WithdrawRecordVO> page = new Page<>(Integer.parseInt(map.get("page") + ""), Integer.parseInt(map.get("limit") + ""));

            // 使用baseMapper查询提现记录
            IPage<WithdrawRecordVO> withdrawMessageList = this.baseMapper.queryWithdrawRecord(page, map);

            // 初始化总金额和已支付金额
            BigDecimal sumAmount = BigDecimal.ZERO;
            BigDecimal sumPayAmount = BigDecimal.ZERO;

            // 遍历提现记录，计算总金额和已支付金额
            for (WithdrawRecordVO list : withdrawMessageList.getRecords()) {
                // 根据提现状态调整已支付金额
                if(list.getIsSucceed().equals(CommonType.isSucceed.PAYMENT_COMPLETED.getCode())){
                    list.setPayAmount(list.getAmount().subtract(list.getFee()));
                }
                if(list.getIsSucceed().equals(CommonType.isSucceed.NO_PASS.getCode())){
                    list.setPayAmount(new BigDecimal(0));
                }
                sumAmount = sumAmount.add(list.getAmount());
                sumPayAmount = sumPayAmount.add(null == list.getPayAmount() ? BigDecimal.ZERO : list.getPayAmount());
            }

            // 初始化分页信息VO，设置分页列表、总金额和已支付金额
            PageInfoVO infoVO = new PageInfoVO();
            infoVO.setPageList(PageUtils.getPage(withdrawMessageList));
            infoVO.setSumAmount(sumAmount);
            infoVO.setSumPayAmount(sumPayAmount);

            // 返回成功响应，包含分页信息VO
            return ResponseResult.success(infoVO);
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪并返回失败响应，包含错误代码和消息
            e.printStackTrace();
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    @Override
    public ResponseResult queryBusinessCheck(Map<String, Object> map) {
        // 从传入的map中获取token字符串
        String token = (String) map.get("token");

        // 检查token在Redis中的存在性，判断用户会话是否有效
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            // 如果token不存在，返回错误响应，提示用户token失效
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 从map中获取id，转换为Integer类型
            Integer id = (Integer) map.get("id");

            // 根据id查询支付、消费或提现实体
            PayConsumeWithdrawEntity payConsumeWithdrawEntity = this.baseMapper.selectById(id);

            // 返回查询成功的响应，包含实体信息
            return ResponseResult.success(payConsumeWithdrawEntity);
        } catch (Exception e) {
            // 捕获查询过程中可能出现的异常，返回错误响应，包含异常信息
            return ResponseResult.fail(500, e.getMessage());
        }

    }

    @Override
    public ResponseResult platformExecl(ConsumeWithdrawDTO consumeWithdrawDto) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(consumeWithdrawDto.getToken());
        if (null != userInfo) {
            List<WithdrawRecordVO> withdrawRecordList = this.baseMapper.queryWithdrawRecordListExcel(consumeWithdrawDto);
            String[] headers = {"状态", "添加时间", "结算单号", "会员名称", "店铺名称", "提现金额", "手续费", "佣金金额",
                    "实付金额", "银行流水", "开户名称", "开户银行", "银行账号", "平台审核备注", "操作时间"};
            List<Map<String, String>> listExcel = Lists.newArrayList();
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            for (WithdrawRecordVO withdrawRecord : withdrawRecordList) {
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(withdrawRecord, WithdrawRecordVO.class), withdrawRecord);
                }
                HashMap<String, String> map = Maps.newHashMap();
                if (withdrawRecord.getIsSucceed() != null) {
                    switch (withdrawRecord.getIsSucceed()) {
                        //是否成功 0 待审核 1 进行中 2 打款完成 3 打款中 4 不通过
                        case 0:
                            withdrawRecord.setIsSucceedStr("待审核");
                            break;
                        case 1:
                            withdrawRecord.setIsSucceedStr("进行中");
                            break;
                        case 2:
                            withdrawRecord.setIsSucceedStr("打款完成");
                            break;
                        case 3:
                            withdrawRecord.setIsSucceedStr("打款中");
                            break;
                        case 4:
                            withdrawRecord.setIsSucceedStr("不通过");
                            break;

                    }
                }
                map.put("状态", withdrawRecord.getIsSucceedStr());

                map.put("添加时间", DateUtils.formateForSdfCn(withdrawRecord.getAddTime()));
                map.put("结算单号", withdrawRecord.getSettlementOrderId());
                map.put("会员名称", withdrawRecord.getUserName());
                map.put("店铺名称", withdrawRecord.getShopName());
                map.put("提现金额", withdrawRecord.getAmount().toString());
                map.put("手续费", withdrawRecord.getFee().toString());
                map.put("佣金金额", withdrawRecord.getCommission().toString());
                map.put("实付金额", withdrawRecord.getPayAmount().toString());
                map.put("银行流水", withdrawRecord.getBankflow());
                map.put("开户名称", withdrawRecord.getCardname());
                map.put("开户银行", withdrawRecord.getBank());
                map.put("银行账号", withdrawRecord.getCardno());
                map.put("平台审核备注", withdrawRecord.getRemark());
                map.put("操作时间", DateUtils.formateForSdfCn(withdrawRecord.getCheckTime()));
                listExcel.add(map);
            }
            // excel文件名
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("合并数据", listExcel);
            String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询平台详情信息
     * 该方法用于根据传入的消费或提现DTO，查询相应的详细信息。首先，它会尝试根据令牌获取用户信息。
     * 如果用户信息有效，它将根据DTO中的ID查询消费或提现实体，并返回成功响应。如果用户信息无效，
     * 则返回令牌非法的错误响应。
     *
     * @param consumeWithdrawDto 消费或提现DTO，包含令牌和要查询的ID
     * @return 返回查询结果的响应对象。如果查询成功，包含消费或提现实体；如果令牌非法，返回错误信息。
     */
    @Override
    public ResponseResult platformDetail(ConsumeWithdrawDTO consumeWithdrawDto) {
        // 尝试根据令牌获取平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(consumeWithdrawDto.getToken());
        if (null != userInfo) {
            // 如果用户信息有效，根据DTO中的ID查询消费或提现信息
            PayConsumeWithdrawEntity withdrawMessage = this.baseMapper.selectById(consumeWithdrawDto.getId());
            return ResponseResult.success(withdrawMessage);
        }
        // 如果用户信息无效，返回令牌非法的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult withdrawalAudit(Map<String, Object> map) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            Integer id = Integer.valueOf(map.get("id").toString());
            // 审核状态
            Integer status =Integer.valueOf(map.get("status").toString());
            String time = DateUtils.getNowDateToString();
            // 付款金额
            String payMoney = (String) map.get("payMoney");
            if (StringUtils.isBlank(payMoney)) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }
            BigDecimal money = new BigDecimal(payMoney);
            if (money.compareTo(new BigDecimal(0)) <= 0) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }

            // 操作人
            String operator = (null != map.get("operator")) ? (String) map.get("operator") : userInfo.getUserRealname();
            // 银行流水
            String accountStatement = (String) map.get("accountStatement");
            // 备注
            String remark = (String) map.get("remark");
            // 结算单号
            String settlementsId = (String) map.get("settlementsId");
            // 手续费
            String fee = (String) map.get("fee");
            if (StringUtils.isBlank(fee)) {
                fee = "0.00";
            }
            if (new BigDecimal(fee).compareTo(new BigDecimal(0)) < 0) {
                return ResponseResult.fail(BizCodeEnume.WRONG_AMOUNT);
            }
            // 佣金
            String commission = (String) map.get("commission");
            if (StringUtils.isNotBlank(commission)) {
                commission = "0.00";
            }
            List<String> list = new ArrayList<String>();
            if (settlementsId != null) {
                String[] str = settlementsId.split(",");
                list = Arrays.asList(str);
            } else {
                settlementsId = "";
            }
            Integer checkWithrawal = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<PayConsumeWithdrawEntity>()
                    .eq(PayConsumeWithdrawEntity::getId, id)
                    .eq(PayConsumeWithdrawEntity::getIsSucceed, status)));
            if (checkWithrawal >= 1) {
                return ResponseResult.success(1,"提现审核已完成!");
            }
            Map<String, Object> mapAudit = new HashMap<>();
            mapAudit.put("id", id);
            mapAudit.put("status", status);
            mapAudit.put("fee", fee);
            mapAudit.put("time", time);
            mapAudit.put("payMoney", payMoney);
            mapAudit.put("operator", operator);
            mapAudit.put("accountStatement", accountStatement);
            mapAudit.put("remark", remark);
            mapAudit.put("commission", commission);
            // 更新审核
            boolean isUpdateSuccess = this.baseMapper.updateWithdrawAudit(mapAudit);
            PayConsumeWithdrawEntity entity= this.baseMapper.selectById(id);
            String payUserId = entity.getPayUid();
            Integer code = null;
            String msg = null;
            if (status == 2) {
                boolean isInsertSuccess = false;
                // 打款中
                if (null == settlementsId || settlementsId.length() > 0) {
                    isInsertSuccess = payConsumeRecordMapper.update(null,new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                                     .eq(PayConsumeRecordEntity::getTradeTypeId,4)
                                     .eq(PayConsumeRecordEntity::getRecordPayorder,settlementsId)
                                     .set(PayConsumeRecordEntity::getRecordWithdraw,CommonType.commonStatus.NO.getCode())
                                     .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CONFIRM_RECEIPT.getCode()))>0;
                } else {
                    String orderId = entity.getOrderid();
                    isInsertSuccess = payConsumeRecordMapper.update(null,new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                                     .eq(PayConsumeRecordEntity::getTradeTypeId,4)
                                     .eq(PayConsumeRecordEntity::getOrderId,orderId)
                                     .set(PayConsumeRecordEntity::getRecordWithdraw,CommonType.commonStatus.NO.getCode())
                                     .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CONFIRM_RECEIPT.getCode()))>0;
                }
                if (isInsertSuccess) {
                    code = 1;
                    msg = "打款成功！";
                } else {
                    code = 2;
                    msg = "打款失败！";
                }
            } else if (status == 3) {
                // 审核通过
                if (isUpdateSuccess) {
                    code = 1;
                    msg = "审核成功！";

                    if(CollectionUtils.isNotEmpty(list)){
                        orderServiceClient.updateOrderSettlementStatus(list);
                    }

                } else {
                    code = 2;
                    msg = "审核失败！";
                }
            } else if (status == 4) {
                // 审核不通过需要将骑手提现金额 回退到骑手余额中
                if(entity.getOrderid().startsWith("W-R")){
                    // 是骑手申请单 退回金额到骑手余额中
                    payUserResourceMapper.updateRiderMoney(entity);
                }
                // 提现审核不通过
                // 提现审核不通过，还原金额
                boolean isUpdateUserMoney = payUserResourceMapper.updateDeductUserMoney(payUserId, money);
                // 更新record表提现状态
                boolean isUpdateByWithdraw = true;
                if (!"".equals(settlementsId)) {
                    String[] settlementsIds=settlementsId.split(",");
                    isUpdateByWithdraw = this.baseMapper.updateOrderRecordWithdraw(settlementsIds);
                }
                // 更新record表结算状态
                boolean isUpdateBySettlements = true;
                if (list.size() > 0) {
                    isUpdateBySettlements = payConsumeRecordMapper.updateOrderRecordSettlements(list);
                }

                if (isUpdateByWithdraw && isUpdateBySettlements && isUpdateSuccess && isUpdateUserMoney) {
                    code = 1;
                    msg = "提现审核不通过！";
                    if (list.size() > 0) {
                        orderServiceClient.updateSettlementStatus(list);
                    }

                    //更新我的余额明细
                    Map<Object, Object> assetDetailMap = new HashMap<>();
                    assetDetailMap.put("assetDirection", "平台");
                    assetDetailMap.put("detailType", 3);
                    assetDetailMap.put("assetType", 1);
                    assetDetailMap.put("payType", 1);
                    assetDetailMap.put("assetAmount", money);
                    assetDetailMap.put("userId", payUserId);
                    String orderId = entity.getOrderid();
                    assetDetailMap.put("orderId", orderId);
                    payAssetDetailsMapper.insertDetail(assetDetailMap);

                }else {
                    code = 2;
                    msg = "失败";
                }
            }
            return ResponseResult.success(code, msg);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询提现记录列表
     * @param consumeWithdrawDto 提现请求的DTO，包含分页信息和查询条件
     * @return 返回查询结果的响应对象
     */
    @Override
    public ResponseResult withdrawDepositList(ConsumeWithdrawDTO consumeWithdrawDto) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(consumeWithdrawDto.getToken());
        // 检查用户信息是否有效
        if (null != userInfo) {
            try {
                // 将DTO对象转换为Map，以便后续的查询操作
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(consumeWithdrawDto);
                // 执行查询操作，获取提现记录的分页结果
                //分页查询数据
                IPage<WithdrawRecordVO> page = this.baseMapper.queryWithdrawRecordList(new Query<WithdrawRecordVO>().getPage(data), consumeWithdrawDto);
                // 检查查询结果是否为空
                if(CollectionUtils.isNotEmpty(page.getRecords())){
                    // 获取脱敏开关的配置值
                    String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                    // 遍历查询结果，根据脱敏开关的配置进行数据脱敏处理
                    for (WithdrawRecordVO vo:page.getRecords()) {
                        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                            BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, WithdrawRecordVO.class), vo);
                        }
                        // 根据提现状态，计算实际支付金额
                        if(vo.getIsSucceed().equals(CommonType.isSucceed.PAYMENT_COMPLETED.getCode())){
                            vo.setPayAmount(vo.getAmount().subtract(vo.getFee()));
                        }
                        if(vo.getIsSucceed().equals(CommonType.isSucceed.NO_PASS.getCode())){
                            vo.setPayAmount(new BigDecimal(0));
                        }
                    }
                }
                // 构建并返回查询成功的响应结果
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 处理异常，打印堆栈信息
                e.printStackTrace();
            }
        }
        // 如果用户信息无效，返回相应的错误响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult supplierWithdrawDeposit(Map<String, Object> map) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 验证用户是否有权限进行查询，非商家或非管理员将返回权限不足错误
        // 验证用户是否为商家且有权限
        if (null == ui || ui.getShopId() == null ||
                (!CommonType.commonStatus.YES.getCode().equals(ui.getIsShop()) && ui.getUserAccount() != null
                        && (!ui.getUserAccount().equals("admin")))) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        PageInfoVO pi = new PageInfoVO();
        String userId = ui.getUserId();
        // 如果是商家，通过商家ID查询商家主账号ID
        // 获取商家店铺主账号用户id
        if (null != ui.getShopId()) {
            userId = supplierServiceClient.queryShopId(ui.getShopId().toString());
        }
        // 从查询参数中获取提现单号、查询起始时间和结束时间
        String statementNo = (String) map.get("statementNo");
        String startTime = (String) map.get("startTime");
        String endTime = (String) map.get("endTime");
        // 获取查询页码和每页记录数
        // 当前页数
        Integer currentPage = Integer.parseInt(map.get("page") + "");
        // 每页显示的记录数
        Integer pageSize = Integer.parseInt(map.get("limit") + "");
        Integer type = (Integer) map.get("type");
        Integer settleType = null == map.get("settleType") ? null : (Integer) map.get("settleType");
        // 根据查询类型设置查询状态，支持查询全部、待审核、通过、打款完成、不通过和进行中等状态
        String allStatus = null;
        Integer otherStatus = null;
        if (type == 1) {
            // 查看全部
            allStatus = "";
        } else if (type == 2) {
            // 待审核
            otherStatus = 0;
        } else if (type == 3) {
            // 通过
            otherStatus = 3;
        } else if (type == 4) {
            // 打款完成
            otherStatus = 2;
        } else if (type == 5) {
            // 不通过
            otherStatus = 4;
        } else if (type == 6) {
            // 进行中
            otherStatus = 1;
        }
        // 构建分页对象
        Page<WithdrawRecordVO> page = new Page<>(currentPage, pageSize);
        // 准备查询条件
        Map<String, Object> mapRecord = new HashMap<>();
        mapRecord.put("userId", userId);
        mapRecord.put("statementNo", statementNo);
        mapRecord.put("settleType", settleType);
        mapRecord.put("allStatus", allStatus);
        mapRecord.put("otherStatus", otherStatus);
        mapRecord.put("startTime", startTime);
        mapRecord.put("endTime", endTime);
        // 执行查询
        IPage<WithdrawRecordVO> withdrawMessageList = this.baseMapper.queryWithdrawRecord(page, mapRecord);
        // 计算查询结果的总金额和已支付金额
        BigDecimal sumAmount = BigDecimal.ZERO;
        BigDecimal sumPayAmount = BigDecimal.ZERO;
        for (WithdrawRecordVO list : withdrawMessageList.getRecords()) {
            sumAmount = sumAmount.add(list.getAmount());
            sumPayAmount = sumPayAmount.add(null == list.getPayAmount() ? BigDecimal.ZERO : list.getPayAmount());
        }
        // 如果查询有结果，设置分页信息、总金额和已支付金额，并返回查询结果
        if (!withdrawMessageList.getRecords().isEmpty()) {
            pi.setPageList(PageUtils.getPage(withdrawMessageList));
            pi.setSumAmount(sumAmount);
            pi.setSumPayAmount(sumPayAmount);
            return ResponseResult.success(pi);
        }
        // 如果查询无结果，返回空数据提示
        return ResponseResult.success(BizCodeEnume.SELECT_DATA_NULL.getCode(), BizCodeEnume.SELECT_DATA_NULL.getMsg());
    }

    @Override
    public ResponseResult fkPayForSupplierWithdrawal(Map<Object, Object> map) {
        // 获取当前登录的用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 验证用户是否有权限进行商家提现操作
        // 验证用户是否为商家且有权限
        if (null == ui || ui.getShopId() == null ||
                (!CommonType.commonStatus.YES.getCode().equals(ui.getIsShop()) && ui.getUserAccount() != null
                        && (!ui.getUserAccount().equals("admin")))) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        PageInfoVO pageInfoVO = new PageInfoVO();
        String userId = ui.getUserId();
        // 如果商家有店铺ID，则获取店铺主账号的用户ID
        // 获取商家店铺主账号用户id
        if (null != ui.getShopId()) {
            userId = supplierServiceClient.queryShopId(ui.getShopId());
        }
        // 获取当前页码和每页大小
        Integer currentPage = (Integer) map.get("page");
        Integer pageSize = (Integer) map.get("limit");
        // 获取结算类型，如果未传入则为null
        Integer settleType = null == map.get("settleType") ? null : (Integer) map.get("settleType");
        // 创建分页对象
        Page<OrderSettlementVO> page = new Page<>(currentPage, pageSize);
        IPage<OrderSettlementVO> settlementList;
        // 根据是否有结算类型，查询不同的提现记录
        if (null != settleType) {
            settlementList = payConsumeRecordMapper.fkPayForBusinessWithdrawalByShopType(page, userId, settleType);
        } else {
            settlementList = payConsumeRecordMapper.fkPayForBusinessWithdrawal(page, userId);
        }
        // 查询商家所有提现记录的总金额
        BigDecimal sumRecordMoney = payConsumeRecordMapper.getSumRecordMoney(userId);
        // 设置分页信息和总金额到返回结果中
        pageInfoVO.setPageList(PageUtils.getPage(settlementList));
        pageInfoVO.setSumRecordMoney(sumRecordMoney);
        // 返回处理成功的结果
        return ResponseResult.success(pageInfoVO);
    }

    @Override
    public ResponseResult supplierForMerchantsWithdrawal(Map<Object, Object> map) {
        // 从请求参数中获取用户令牌
        String token = map.get("token").toString();
        // 根据令牌获取用户信息
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(token);
        // 如果获取用户信息失败，则直接返回错误结果
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 解析用户信息
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        // 获取商家用户ID
        // 发送人id
        String userId = ui.getUserId();
        // 如果存在商家ID，通过商家ID获取商家用户ID
        // 获取商家店铺主账号用户id
        if (null != ui.getShopId()) {
            userId = supplierServiceClient.queryShopId(ui.getShopId());
            // 根据商家用户ID查询用户信息
            ResponseResult userResult = usermsServiceClient.queryUserInfo(userId);
            ui = JSON.parseObject(JSON.toJSONString(userResult.getData()), UserInfoVO.class);
        }
        // 获取商家用户名
        String userName = ui.getUserName();
        // 获取商家ID
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 从请求参数中获取收款方信息
        // 收款方
        String payee = (String) map.get("payee");
        // 从请求参数中获取银行相关信息
        // 银行名称
        String bankName = (String) map.get("bankName");
        // 银行卡号
        String bankNum = (String) map.get("bankNum");
        // 支行所在地
        String bankAddress = (String) map.get("bankAddress");
        // 从请求参数中获取提现金额
        // 类型转换
        BigDecimal userMoney;
        BigDecimal commission;
        // 判断是否为全部提现
        Integer type = Integer.valueOf(map.get("type").toString());
        // 结算单号
        String statementNo = null;
        List<String> no;
        List<String> list;
        // 手机验证码
        String phoneValidation = (String) map.get("phoneValidation");
        // 从Redis中获取存储的手机验证码
        String phoneCode = RedisUtil.get(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
        // 商家提现标识
        String con = "商家提现";
        // 生成提现订单号
        // 提现订单号
        String orderId = "W" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
        // 记录提现日期
        String recordDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
        String recordYear = DateUtils.getYear();
        String recordMonth = DateUtils.getMonth();
        String recordDay = DateUtils.getDay();
        String recordTitle = "提现";
        Integer tradeTypeId = 4;
        Integer userType = 1;
        Integer recordStatus = 8;
        // 验证手机验证码是否正确
        // 商家用户提现，判断验证是否正确
        if (phoneValidation.equals(phoneCode)) {
            String createTime = DateUtils.getTime();
            // 获取商家名称
            String shopName =  supplierServiceClient.selectSupplierShopName(shopId);
            boolean isUpdateSuccess;
            // 根据提现类型处理提现业务逻辑
            if (type == 1) {
                statementNo = (String) map.get("statementNo");
                String[] str = statementNo.split(",");
                list = Arrays.asList(str);
                userMoney = payConsumeRecordMapper.queryCalculateRecordMoney(list, userId);
                commission = payConsumeRecordMapper.queryCalculateRecordCommission(list, userId);
                // 更新结算单为已提现状态
                // 更新结算单表
                isUpdateSuccess = payConsumeRecordMapper.updateRecordWithdraw(list);
            } else {
                // 查询所有待提现的结算单
                // 查询所有结算单
                no = payConsumeRecordMapper.queryAllStatementNo(userId);
                list = no;
                userMoney = payConsumeRecordMapper.queryCalculateRecordMoney(list, userId);
                commission = payConsumeRecordMapper.queryCalculateRecordCommission(list, userId);
                // 更新结算单为已提现状态
                // 更新结算单表
                isUpdateSuccess = payConsumeRecordMapper.updateRecordWithdraw(no);
            }
            // 记录的支付单号
            String recordPayorder = statementNo;
            // 校验结算单金额是否正确
            // 判断结算单金额
            boolean checkSettlementAmount =orderServiceClient.checkSettlementAmount(shopId.toString());
            // 如果校验失败，则回滚事务并返回错误信息
            if (checkSettlementAmount) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(BizCodeEnume.UNCONFIRMED_SETTLEMENT__DOCUMENTS);
            }
            // 提交提现申请至第三方支付平台
            Map<String, Object> mapSeller = new HashMap<>();
            mapSeller.put("userId", userId);
            mapSeller.put("userName", userName);
            mapSeller.put("orderId", orderId);
            mapSeller.put("money", userMoney);
            mapSeller.put("createTime", createTime);
            mapSeller.put("con", con);
            mapSeller.put("bankName", bankName);
            mapSeller.put("bankAddress", bankAddress);
            mapSeller.put("bankNum", bankNum);
            mapSeller.put("payee", payee);
            mapSeller.put("statementNo", statementNo);
            mapSeller.put("shopName", shopName);
            mapSeller.put("withdrawType", 1);
            mapSeller.put("commission", commission);
            // 插入商家提现记录
            // 插入withdraw表记录
            boolean isInsertSuccess = this.baseMapper.insertConsumeWithdrawBySeller(mapSeller);
            // 插入商家提现明细记录
            Map<String, Object> mapRecordSuccess = new HashMap<>();
            mapRecordSuccess.put("orderId", orderId);
            mapRecordSuccess.put("userId", userId);
            mapRecordSuccess.put("userName", userName);
            mapRecordSuccess.put("userMoney", userMoney);
            mapRecordSuccess.put("recordDate", recordDate);
            mapRecordSuccess.put("recordYear", recordYear);
            mapRecordSuccess.put("recordMonth", recordMonth);
            mapRecordSuccess.put("recordDay", recordDay);
            mapRecordSuccess.put("recordTitle", recordTitle);
            mapRecordSuccess.put("con", con);
            mapRecordSuccess.put("createTime", createTime);
            mapRecordSuccess.put("tradeTypeId", tradeTypeId);
            mapRecordSuccess.put("userType", userType);
            mapRecordSuccess.put("recordStatus", recordStatus);
            mapRecordSuccess.put("recordPayorder", recordPayorder);
            // 插入record提现明细
            boolean isInsertRecordSuccess = payConsumeRecordMapper.insertPayConsumeRecord(mapRecordSuccess);
            // 如果所有操作都成功，则返回成功结果
            if (isInsertSuccess && isUpdateSuccess && isInsertRecordSuccess) {
                return ResponseResult.success();
            }
            // 如果操作失败，则返回失败结果
            return ResponseResult.fail(BizCodeEnume.WITHDRAWAL_REQUEST_FAILED);
        }
        // 如果验证码验证失败，则返回错误结果
        return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
    }
}




