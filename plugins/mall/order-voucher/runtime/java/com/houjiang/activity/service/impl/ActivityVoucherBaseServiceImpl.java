package com.houjiang.activity.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.houjiang.activity.dto.GetAvailableVcBaseDTO;
import com.houjiang.activity.dto.UserGetVoucherParamDTO;
import com.houjiang.activity.dto.UserVcBaseDTO;
import com.houjiang.activity.entity.ActivityVoucherBaseEntity;
import com.houjiang.activity.entity.ActivityVoucherTemplateEntity;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.UserServiceClient;
import com.houjiang.activity.mapper.ActivityVoucherBaseMapper;
import com.houjiang.activity.mapper.ActivityVoucherTemplateMapper;
import com.houjiang.activity.service.ActivityVoucherBaseService;
import com.houjiang.activity.vo.CheckReceivedVcOrNotResultVO;
import com.houjiang.activity.vo.QueryShopVoucherTempResultVO;
import com.houjiang.activity.vo.UserGetVoucherUpdateParamVO;
import com.houjiang.activity.vo.HouVoucherTemplateVO;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.VoucherBaseVO;
import com.houjiang.commons.vo.activity.HouVoucherBaseVO;
import com.houjiang.commons.vo.goods.GoodsIdsQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName ActivityVoucherBaseServiceImpl
 * @Description 代金券表
 * @Author Amos
 * @Date 2023-05-25 16:55:50
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityVoucherBaseServiceImpl extends ServiceImpl<ActivityVoucherBaseMapper, ActivityVoucherBaseEntity>
        implements ActivityVoucherBaseService {

    private final static Logger log = LoggerFactory.getLogger(ActivityVoucherBaseServiceImpl.class);

    /**
     * 红包状态更新走 userms 宿主（红包是用户权益，数据不跨插件共享）。
     */
    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ActivityVoucherTemplateMapper activityVoucherTemplateMapper;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    /**
     * 根据店铺ID查询可用代金券模板
     *
     * @param param 查询参数，包含店铺ID和用户token等信息
     * @return 返回代金券模板信息的结果，如果用户未登录或店铺没有代金券，则返回失败结果
     */
    @Override
    public ResponseResult selectVoucherTempByShopId(GetAvailableVcBaseDTO param) {
        // 根据店铺ID查询代金券模板信息
        List<HouVoucherTemplateVO> houVoucherTemplates = this.baseMapper.selectVoucherTempByShopId(param);

        // 如果查询结果为空，则初始化为空列表
        if (CollectionUtils.isEmpty(houVoucherTemplates)) {
            houVoucherTemplates = Lists.newArrayList();
        }

        // 如果查询结果仍为空，则返回失败结果，表示该店铺没有代金券
        if (CollectionUtils.isEmpty(houVoucherTemplates)) {
            return ResponseResult.fail(0, "该店铺没有代金券", houVoucherTemplates);
        }

        // 检查用户是否登录，未登录则返回失败结果
        // 判断店铺的优惠券该用户是否领取过0
        if (StringUtils.isBlank(param.getToken())) {
            return ResponseResult.fail(0, "未登录状态下查看", houVoucherTemplates);
        }
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 如果用户信息不为空，设置查询参数中的用户ID和通用ID
        if (userInfo != null) {
            param.setVoucherOwnerId(userInfo.getUserId());
            param.setCommonId(param.getCommonId());

            // 遍历代金券模板列表，检查每张代金券是否已被用户领取
            for (HouVoucherTemplateVO houVoucherTemplate : houVoucherTemplates) {
                // 根据代金券模板ID查询代金券详细信息
                // 判断代金券是否领取过
                param.setVoucherTId(houVoucherTemplate.getVoucherTId());
                ActivityVoucherTemplateEntity voucherTemplate = activityVoucherTemplateMapper.selectById(param.getVoucherTId());
                // 如果代金券信息为空，则设置为未领取状态
                if (null == voucherTemplate) {
                    houVoucherTemplate.setReceiveOrNot(0);
                } else if (voucherTemplate.getVoucherTGiveout() >= voucherTemplate.getVoucherTTotal()) {
                    // 如果代金券已发放完毕，则设置为已领取状态
                    houVoucherTemplate.setReceiveOrNot(1);
                } else {
                    // 检查用户是否已领取该代金券，如果已领取满足领取限制，则设置为已领取状态
                    // 判断代金券是否领取过
                    Integer voucherIdCount = this.baseMapper.checkReceivedOrNot(param);
                    if (voucherIdCount != null && voucherTemplate.getVoucherTEachlimit() != 0
                            && voucherIdCount >= voucherTemplate.getVoucherTEachlimit()) {
                        houVoucherTemplate.setReceiveOrNot(1);
                    } else {
                        houVoucherTemplate.setReceiveOrNot(0);
                    }
                }
            }
            // 返回查询到的代金券模板信息，表示查询成功
            return ResponseResult.success(houVoucherTemplates);
        } else {
            // 如果用户信息为空，表示用户未登录，返回失败结果
            return ResponseResult.fail(0, "请先登录");
        }

    }

    /**
     * 根据传入的参数获取代金券的详细信息。
     *
     * @param map 包含代金券查询条件的参数映射。
     * @return VoucherBaseVO 代金券的详细信息对象，包括代金券可用的商品列表。
     */
    @Override
    public VoucherBaseVO getVoucherDetail(Map<String, Object> map) {
        // 通过baseMapper查询代金券的详细信息
        VoucherBaseVO voucherDetail = this.baseMapper.getVoucherDetail(map);

        // 查询该代金券可用的商品ID列表
        List<Integer> avaliableGoods = this.baseMapper.selectAvaliableGoods(voucherDetail.getVoucherId());
        // 将可用商品ID列表设置到代金券详细信息对象中
        voucherDetail.setAvaliableGoods(avaliableGoods);

        // 返回包含代金券详细信息及可用商品列表的对象
        return voucherDetail;
    }

    /**
     * 更新销售参数。
     *
     * @param paraMap 参数映射表，包含各种更新所需的参数。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateSalesPrm(Map<String, Object> paraMap) {
        // 使用ObjectMapper来处理JSON转换
        ObjectMapper objectMapper = new ObjectMapper();
        // 提取红包ID和平台优惠券代码
        String redpacketId = (String) paraMap.get("redpacketId");
        // 将voucherId参数转换为字符串数组
        String[] voucherIds = objectMapper.convertValue(paraMap.get("voucherId"), String[].class);
        String orderId = null;

        // 获取操作类型
        // 退款与否1新人下单2退款红包状态修改
        String type = paraMap.get("type") != null ? paraMap.get("type").toString() : null;
        // 根据类型更新红包状态
        if (type != null) {
            Integer state = type.equals("1") ? 2 : 1;
            orderId = (String) paraMap.get("orderId");
            // 如果存在红包ID，更新红包状态
            if (null != redpacketId && !redpacketId.isEmpty()) {
                Boolean redpacketUpdated = userServiceClient.updateRedpacketState(
                        redpacketId, state, orderId);
                // 如果更新失败，返回失败结果
                if (Boolean.FALSE.equals(redpacketUpdated)) {
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), CommonStatusEnum.FAIL.getValue());
                }
            }
            // 更新优惠券状态
            // 代金券
            if (null != voucherIds) {
                for (String voucherId : voucherIds) {
                    String[] voucherNoList = voucherId.split("-");
                    for (String voucherNo : voucherNoList) {
                        Map<String, Object> voucherMap = new HashMap<>();
                        // 设置优惠券状态
                        // 代金券状态(1-未用,2-已用,3-过期,4-收回)',
                        voucherMap.put("voucherState", state);
                        voucherMap.put("voucherId", voucherNo);
                        // 如果存在订单ID且状态为2（退款），则添加订单ID到券详情中
                        if (null != paraMap.get("orderId") && state == 2) {
                            voucherMap.put("orderId", orderId);
                        }
                        // 更新券详情
                        ResponseResult voucherTorf = updateVoucherDetail(voucherMap);
                        // 如果更新失败，返回失败结果
                        if (voucherTorf.getCode() == CommonStatusEnum.FAIL.getCode()) {
                            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), CommonStatusEnum.FAIL.getValue());
                        }
                    }
                }
            }
        }
        // 操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据传入的参数查询可用的优惠券信息。
     *
     * @param paraMap 包含token、shopId和goodsInfo等参数的映射。
     * @return 返回查询到的可用优惠券信息的结果。
     */
    @Override
    public ResponseResult selectAvailableVcBase(Map<String, Object> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token").toString());

        // 初始化查询条件映射
        Map<String, Object> checkMap = new HashMap<>();
        // 设置优惠券主人的用户ID
        checkMap.put("voucherOwnerId", userInfo.getUserId());
        // 设置优惠券状态为可用
        checkMap.put("voucherState", "1");
        // 设置优惠券适用的店铺ID
        checkMap.put("voucherShopId", paraMap.get("shopId").toString());

        // 初始化店铺代金券金额
        Double shopOrder = 0.00;
        // 从参数映射中获取商品信息列表
        List<Map<String, Object>> goodsInfo = (List<Map<String, Object>>) paraMap.get("goodsInfo");
        // 初始化通用ID列表、商品优惠券基础信息列表和优惠券ID列表
        List<Integer> commonList = new ArrayList<>();
        List<HouVoucherBaseVO> goodsVoucherBaseList = new ArrayList<>();
        List<Integer> voucherList = new ArrayList<>();
        Map<Integer, Double> commonGoodsMap = new HashMap<>();
        // 遍历商品信息列表
        for (Map<String, Object> goodInfo : goodsInfo) {
            // 获取并转换商品 ID、商品价格和商品数量
            Integer goodsId = safeParseInteger(goodInfo.get("goodsId"));
            Double goodsPrice = new BigDecimal(goodInfo.get("goodsPrice").toString()).doubleValue();
            Integer goodsNum = safeParseInteger(goodInfo.get("goodsNum"));
            if (goodsId == null || goodsPrice == null || goodsNum == null) {
                return ResponseResult.fail(BizCodeEnume.NO_GOODS);
            }
            // 根据商品ID查询商品基础信息
            GoodsIdsQueryResultVO goodsBase = goodsServiceClient.selectGoodsIdList(goodsId);
            // 如果商品基础信息为空，则返回错误结果
            if (null == goodsBase) {
                return ResponseResult.fail(BizCodeEnume.NO_GOODS);
            }
            Integer commonId = goodsBase.getCommonId();
            commonList.add(commonId);
            // 计算商品订单金
            Double goodOrder = goodsPrice * goodsNum;
            // 累加店铺订单金额
            shopOrder += goodOrder;
            // 将商品订单金额添加到通用商品订单金额映射中
            commonGoodsMap.merge(commonId, goodOrder, Double::sum);
        }
        for (Map.Entry<Integer, Double> map : commonGoodsMap.entrySet()) {
            // 根据商品基础信息、商品订单金额等查询商品适用的优惠券基础信息
            List<HouVoucherBaseVO> goodsVoucherList = this.baseMapper.selectGoodsVcBase(map.getKey(), map.getValue(),
                    userInfo.getUserId(), paraMap.get("shopId").toString(), voucherList);
            if (CollectionUtil.isNotEmpty(goodsVoucherList)) {
                // 提取优惠券ID列表
                List<Integer> voucherIdList = goodsVoucherList.stream().map(HouVoucherBaseVO::getVoucherId).toList();
                // 将优惠券ID列表添加到全局优惠券ID列表
                voucherList.addAll(voucherIdList);
                // 将商品优惠券基础信息添加到全局商品优惠券基础信息列表
                goodsVoucherBaseList.addAll(goodsVoucherList);
            }
        }
        // 如果存在通用ID列表，则将其添加到查询条件映射中
        if (!commonList.isEmpty()) {
            checkMap.put("commonList", commonList);
        }
        // 设置订单金额为查询条件，如果订单金额为0，则设置为null
        checkMap.put("order", shopOrder == 0.00 ? null : shopOrder.toString());
        // 设置当前时间作为查询条件
        checkMap.put("now", "now");

        // 根据查询条件映射查询优惠券基础信息
        List<HouVoucherBaseVO> houVoucherBases = this.baseMapper.selectVcBase(checkMap);
        // 如果查询结果为空，则初始化为空列表
        houVoucherBases = CollectionUtil.isNotEmpty(houVoucherBases) ? houVoucherBases : new ArrayList<>();
        // 将商品优惠券基础信息列表合并到查询结果列表中
        if (CollectionUtil.isNotEmpty(goodsVoucherBaseList)) {
            houVoucherBases.addAll(goodsVoucherBaseList);
        }

        // 处理优惠券列表，准备返回结果
        List<HouVoucherBaseVO> voucherBases = new ArrayList<>(houVoucherBases);
        if (CollectionUtil.isNotEmpty(houVoucherBases)) {
            // 初始化处理后的优惠券列表、通用图片列表、优惠券模板ID列表和通用ID列表
            List<HouVoucherBaseVO> houVoucherBaseVOList = new ArrayList<>();
            List<String> commonImageList = new ArrayList<>();
            List<Integer> voucherTIdList = new ArrayList<>();
            List<Integer> commonIds = new ArrayList<>(commonList);
            List<Integer> chooseCommonIds = new ArrayList<>();
            StringBuilder voucherNo = new StringBuilder();
            BigDecimal voucherPrice = new BigDecimal(0);
            Double voucherLimit = 0.0;
            int index = 0;

            // 遍历优惠券列表，进行筛选和处理
            for (HouVoucherBaseVO houVoucherBase : voucherBases) {
                // 设置优惠券编号
                houVoucherBases.get(index).setVoucherNo(houVoucherBase.getVoucherId().toString());
                // 根据优惠券模板ID查询适用的商品ID列表
                List<Integer> list = activityVoucherTemplateMapper.selectGoodsIdByVoucherCommon(houVoucherBase.getVoucherTId());

                // 根据优惠券类型和适用商品列表，判断优惠券是否适用当前商品
                if (houVoucherBase.getIsShopVoucher() == 1 && !list.isEmpty() && !new HashSet<>(list).containsAll(commonList)) {
                    houVoucherBases.remove(houVoucherBase);
                    continue;
                }
                if (houVoucherBase.getIsShopVoucher() == 2 && !commonIds.isEmpty()) {
                    boolean flag = true;
                    for (Integer commonId : list) {
                        if (chooseCommonIds.contains(commonId)) {
                            flag = false;
                            break;
                        }
                    }
                    for (Integer commonId : commonIds) {
                        if (list.contains(commonId)) {
                            chooseCommonIds.add(commonId);
                            commonIds.remove(commonId);
                            break;
                        }
                    }
                    if (flag) {
                        // 组合优惠券信息
                        if (voucherNo.isEmpty()) {
                            voucherNo.append(houVoucherBase.getVoucherId());
                        } else {
                            voucherNo.append("-").append(houVoucherBase.getVoucherId());
                        }
                        commonImageList.add(houVoucherBase.getCommonImage());
                        voucherTIdList.add(houVoucherBase.getVoucherTId());
                        houVoucherBaseVOList.add(houVoucherBase);
                        voucherPrice = voucherPrice.add(houVoucherBase.getVoucherPrice());
                        ;
                        voucherLimit = voucherLimit + houVoucherBase.getVoucherLimit();
                    }
                }
                index++;
            }

            // 如果处理后的优惠券列表不为空且包含多个优惠券，则添加组合优惠券信息
            if (!houVoucherBaseVOList.isEmpty() && houVoucherBaseVOList.size() > 1) {
                voucherBases.add(new HouVoucherBaseVO("组合优惠", voucherPrice, voucherLimit, voucherNo,
                        voucherTIdList, houVoucherBaseVOList, commonImageList));
            }
            // 对优惠券列表进行排序
            sortVoucherList(voucherBases);
        } else {
            // 如果优惠券列表为空，则返回提示信息和空列表
            return ResponseResult.success("未查询到可以用代金券", new ArrayList<>());
        }
        // 返回查询到的可用优惠券信息结果
        return ResponseResult.success(voucherBases);
    }

    /**
     * 安全地将对象转换为 Integer。
     *
     * @param value 待转换的对象
     * @return 转换后的 Integer 值，若转换失败则返回 null
     */
    private Integer safeParseInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析为整数: {}", value);
            }
        }
        return null;
    }

    /**
     * 安全地将对象转换为 Double。
     *
     * @param value 待转换的对象
     * @return 转换后的 Double 值，若转换失败则返回 null
     */
    private Double safeParseDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析为浮点数: {}", value);
            }
        }
        return null;
    }

    /**
     * 对优惠券列表进行排序。
     * <p>
     * 此方法通过对传入的优惠券列表进行排序，实现了根据优惠券面额和使用限制金额进行排序的功能。
     * 排序逻辑是首先比较优惠券的面额，如果面额相同，则根据使用限制金额进行排序。
     * 这样的排序目的是为了确保优惠券列表在使用时能够按照面额和使用条件的综合优劣进行排序展示，便于用户选择。
     *
     * @param voucherList 优惠券列表，包含待排序的优惠券信息。
     *                    列表中的每个元素都是HouVoucherBaseVO类型的对象，包含了优惠券的面额和使用限制金额等信息。
     */
    public void sortVoucherList(List<HouVoucherBaseVO> voucherList) {
        // 使用lambda表达式和sort方法对列表进行排序
        voucherList.sort((vo1, vo2) -> {
            // 首先比较优惠券面额
            // 根据大小进行排序，这里以某个字段为例（假设是amount字段）
            if (vo1.getVoucherPrice().compareTo(vo2.getVoucherPrice()) == 0) {
                // 如果面额相同，则比较使用限制金额
                return Double.compare(vo1.getVoucherLimit(), vo2.getVoucherLimit());
            } else {
                // 如果面额不同，则根据面额进行反向排序（面额高的排在前面）
                return vo2.getVoucherPrice().compareTo(vo1.getVoucherPrice());
            }
        });
    }


    /**
     * 插入代金券基础信息接口。
     * 通过传入的参数，尝试为用户领取代金券，并在数据库中插入相应的记录。
     * 使用Redis锁来保证并发领取代金券的线程安全。
     *
     * @param param 用户领取代金券的参数，包含代金券ID、用户令牌等信息。
     * @return 领取代金券的结果，成功或失败以及相应的信息。
     */
    @Override
    public ResponseResult insertVcBase(UserGetVoucherParamDTO param) {
        // 获取Redis连接
        // 获取jedis实例
        Jedis jedis = RedisUtil.getJedis();
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 创建Redis锁，用于并发控制
        RedisLock redisLock = new RedisLock(jedis, "user-vc-" + userInfo.getUserId() + param.getVoucherTId(), 10000);
        try {
            // 尝试获取锁，如果获取失败，则代金券领取失败
            // 用户锁
            if (redisLock.lock()) {
                // 设置代金券的拥有者ID
                param.setVoucherOwnerId(userInfo.getUserId());
                // 根据代金券ID查询代金券模板信息
                UserGetVoucherUpdateParamVO voucherTemplate = this.baseMapper.selectVcTemById(param);
                // 如果代金券模板不存在，则领取失败
                if (null == voucherTemplate) {
                    return ResponseResult.fail(0, "该代金券不存在");
                }
                // 如果代金券已领完，则领取失败
                if (voucherTemplate.getVoucherTGiveout() >= voucherTemplate.getVoucherTTotal()) {
                    return ResponseResult.fail(0, "该代金券已领取完，看看其他的代金券");
                }
                // 检查用户是否已领取达到上限
                // 判断代金券是否领取过
                Integer voucherIdCount = this.baseMapper.checkVoucherOrNot(param);
                if (voucherIdCount != null && voucherTemplate.getVoucherTEachlimit() != 0
                        && voucherIdCount >= voucherTemplate.getVoucherTEachlimit()) {
                    return ResponseResult.fail(0, "代金券已领取到达上限");
                }
                // 设置代金券的拥有者信息，并标记为已领取
                // 获取用户信息
                voucherTemplate.setVoucherOwnerId(userInfo.getUserId());
                voucherTemplate.setVoucherOwnerName(userInfo.getUserName());
                voucherTemplate.setVoucherState(1);
                // 插入代金券领取记录
                this.baseMapper.insertyfVoucherBase(voucherTemplate);
                // 更新代金券的领取数量
                this.baseMapper.updateVoucherGiveout(param);
            } else {
                // 如果未能获取锁，则领取失败
                return ResponseResult.fail(0, "代金券领取失败");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放Redis锁
            redisLock.unLock();
            // 关闭Redis连接
            jedis.close();
        }
        // 领取代金券成功
        return ResponseResult.success();
    }

    /**
     * 根据用户券基础信息查询条件，查询用户券信息。
     *
     * @param param 用户券查询条件DTO，包含用户券的查询条件和分页信息。
     * @return 返回查询结果，如果查询失败，返回错误信息。
     */
    @Override
    public ResponseResult selectVcBase(UserVcBaseDTO param) {
        // 通过token解析获取用户信息，用于后续的用户券主人设置
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，表示token解析失败，返回非法token错误信息
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 设置用户券主人ID，用于查询用户自己的券信息
        param.setVoucherOwnerId(userInfo.getUserId());
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<VoucherBaseVO> pageObj = new Page<>(page, limit);
        // 根据查询条件和分页信息，查询用户的券基础信息
        IPage<VoucherBaseVO> houVoucherBases = this.baseMapper.selectVcBaseByShopBase(pageObj, param);
        // 如果查询结果为空，表示未找到可用的券，返回未找到券的错误信息
        if (CollectionUtils.isEmpty(houVoucherBases.getRecords())) {
            return ResponseResult.fail(BizCodeEnume.NO_USABLE_VOUCHERS_FOUND);
        }
        List<VoucherBaseVO> records = houVoucherBases.getRecords();
        for (VoucherBaseVO record : records) {
            if (CommonType.isShopVoucher.PRODUCT_WEIGHING.getCode().equals(record.getIsShopVoucher())) {
                //设置二维码路径（商品详情页面）
                String path = "/h5/#/activities/exchangeCouponList/exchangeCouponList?id=" + record.getVoucherTId();
                //调用二维码工具
                String base64URLString = QrCodeUtil.getBase64URLString(path);
                record.setQrUrl(base64URLString);
            }
        }
        // 如果查询成功，返回查询结果的分页信息
        return ResponseResult.success(PageUtils.getPage(houVoucherBases));
    }

    /**
     * 更新代金券详情信息。
     * 该方法主要用于根据传入的代金券映射对象更新代金券的基本状态和订单ID。
     * 如果代金券状态被设置为已使用，则还会更新对应的代金券模板的使用次数。
     *
     * @param voucherMap 代金券信息的映射对象，包含代金券ID、状态和订单ID等信息。
     * @return 返回操作结果，通常表示操作成功。
     */
    @Override
    public ResponseResult updateVoucherDetail(Map<String, Object> voucherMap) {
        // 更新代金券基础信息，如状态和订单ID
        this.baseMapper.update(null, new LambdaUpdateWrapper<ActivityVoucherBaseEntity>()
                .eq(voucherMap.get("voucherId") != null, ActivityVoucherBaseEntity::getVoucherId, voucherMap.get("voucherId"))
                .set(voucherMap.get("voucherState") != null, ActivityVoucherBaseEntity::getVoucherState, voucherMap.get("voucherState"))
                .set(voucherMap.containsKey("orderId"), ActivityVoucherBaseEntity::getVoucherOrderId, voucherMap.containsKey("orderId")));

        // 如果代金券状态被设置为已使用，则更新代金券模板的使用次数
        if (voucherMap.get("voucherState") != null && voucherMap.get("voucherState") == CommonType.redpacketState.USED.getCode()) {
            ActivityVoucherBaseEntity base = this.baseMapper.selectById(Integer.parseInt(voucherMap.get("voucherId").toString()));
            if (base != null) {
                ActivityVoucherTemplateEntity entity = activityVoucherTemplateMapper.selectById(base.getVoucherTId());
                activityVoucherTemplateMapper.update(null, new LambdaUpdateWrapper<ActivityVoucherTemplateEntity>()
                        .set(ActivityVoucherTemplateEntity::getVoucherTUsed, entity.getVoucherTUsed() + 1)
                        .eq(ActivityVoucherTemplateEntity::getVoucherTId, entity.getVoucherTId()));
            }
        }

        // 返回操作成功的结果
        return ResponseResult.success();
    }

    /**
     * 查询可领取的满减优惠券列表
     *
     * @param param 查询参数，包含用户信息和筛选条件
     * @return 返回查询结果，如果无券可领，则返回失败信息
     */
    @Override
    public ResponseResult queryFullReduceVcList(GetAvailableVcBaseDTO param) {
        // 根据查询条件获取可用优惠券模板的ID列表
        List<Integer> voucherTIds = this.baseMapper.selectVoucherTIds(param);

        // 如果有可用的优惠券模板ID，则进一步查询优惠券详情
        List<HouVoucherTemplateVO> houVoucherTemplates = null;
        if (!CollectionUtils.isEmpty(voucherTIds)) {
            param.setVoucherTIds(voucherTIds);
            houVoucherTemplates = activityVoucherTemplateMapper.selectFullReduceVcList(param);
        }

        // 如果查询结果为空，则表示无优惠券可领取，返回失败信息
        if (CollectionUtils.isEmpty(houVoucherTemplates)) {
            return ResponseResult.fail(0, "暂无优惠券可领取");
        }

        // 如果用户token存在，则标记用户是否已领取优惠券
        if (StringUtils.isNotBlank(param.getToken())) {
            houVoucherTemplates = setUserReceiveOrNot(param, houVoucherTemplates);
        }

        // 返回查询成功的优惠券列表
        return ResponseResult.success(houVoucherTemplates);
    }

    /**
     * 根据用户领取状态设置代金券模板信息
     *
     * @param param              用户请求参数，包含代金券相关标识信息
     * @param houVoucherTemplates 代金券模板列表
     * @return 经过用户领取状态处理后的代金券模板列表
     */
    private List<HouVoucherTemplateVO> setUserReceiveOrNot(GetAvailableVcBaseDTO param,
                                                          List<HouVoucherTemplateVO> houVoucherTemplates) {
        // 初始化未领取和已领取代金券列表
        List<HouVoucherTemplateVO> noUserVouchers = Lists.newArrayList();
        List<HouVoucherTemplateVO> alreadyUserVouchers = Lists.newArrayList();

        // 通过token获取用户信息
        // 获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置代金券所有者ID为用户ID
        param.setVoucherOwnerId(userInfo.getUserId());

        // 查询用户代金券领取状态
        List<CheckReceivedVcOrNotResultVO> receivedVcList = this.baseMapper.checkReceivedVcOrNot(param);
        if (!CollectionUtils.isEmpty(receivedVcList)) {
            for (HouVoucherTemplateVO houVoucherTemplate : houVoucherTemplates) {
                boolean flag = true;
                for (CheckReceivedVcOrNotResultVO receivedVc : receivedVcList) {
                    // 代金券状态映射处理
                    // 代金券状态(1-未用,2-已用,3-过期,4-收回)'
                    // ReceiveOrNot:// 0未领取1未用2不可领取3已用4过期5回收
                    Integer voucherState = receivedVc.getVoucherState();
                    if (houVoucherTemplate.getVoucherTId().intValue() == receivedVc.getVoucherTId().intValue()) {
                        flag = false;
                        // 根据原始状态设置新的代金券状态
                        if (voucherState.intValue() == 1) {
                            voucherState = 1;
                        } else if (voucherState.intValue() == 2) {
                            voucherState = 3;
                        } else if (voucherState.intValue() == 3) {
                            voucherState = 4;
                        } else {
                            voucherState = 5;
                        }

                        // 更新代金券模板的领取状态
                        houVoucherTemplate.setReceiveOrNot(voucherState);
                        // 根据状态添加到对应的列表
                        if (voucherState.intValue() == 1) {
                            noUserVouchers.add(houVoucherTemplate);
                        } else {
                            alreadyUserVouchers.add(houVoucherTemplate);
                        }
                        break;
                    }
                }
                // 如果没有匹配到领取状态，则认为是未领取，添加到未领取列表
                if (flag) {
                    noUserVouchers.add(houVoucherTemplate);
                }
            }
        } else {
            // 如果没有查询到领取状态信息，认为所有代金券都是未领取状态
            noUserVouchers.addAll(houVoucherTemplates);
        }
        // 合并已领取和未领取的代金券列表
        noUserVouchers.addAll(alreadyUserVouchers);
        return noUserVouchers;
    }


    /**
     * 根据传入的参数查询店铺优惠券模板信息。
     *
     * @param paraMap 包含查询参数的映射，其中可能包含用户的token信息。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryShopVoucherTempPort(Map<String, Object> paraMap) {
        // 通过token获取用户信息，用于后续查询中过滤用户相关的数据
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token").toString());
        if (null != userInfo) {
            // 如果用户信息存在，则将用户ID添加到查询参数中
            paraMap.put("userId", userInfo.getUserId());
        }
        // 执行查询，通过基础Mapper接口和分页插件进行数据检索
        // 分页查询数据
        IPage<QueryShopVoucherTempResultVO> page = this.baseMapper.queryShopVoucherTempPort(new Query<QueryShopVoucherTempResultVO>().getPage(paraMap), paraMap);

        // 将查询结果包装成响应对象返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult updateProductVoucher(Map<String, Object> paraMap) {
        this.baseMapper.updateProductVoucher(paraMap);
        return ResponseResult.success();
    }

    @Override
    public void updateVoucherBaseState(Map<String, Object> param) {
        this.baseMapper.updateVoucherBaseState(param);
    }
}
