package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.houjiang.base.entity.BaseMerchantPlusThaliEntity;
import com.houjiang.base.feign.ActivityClient;
import com.houjiang.base.feign.UsermsClient;
import com.houjiang.base.mapper.BaseMembershipGradeMapper;
import com.houjiang.base.mapper.BaseMerchantPlusThaliMapper;
import com.houjiang.base.service.BaseMerchantPlusThaliService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.vo.activity.ActivityRedpacketTemplateVO;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Amos
 */
@Service
public class BaseMerchantPlusThaliServiceImpl extends ServiceImpl<BaseMerchantPlusThaliMapper, BaseMerchantPlusThaliEntity>
    implements BaseMerchantPlusThaliService{

    @Autowired
    private UsermsClient usermsClient;

    @Autowired
    private ActivityClient activityClient;

    @Autowired
    private BaseMembershipGradeMapper baseMembershipGradeMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 根据传入的参数查询商家加锁权益信息。
     * @param map 包含查询条件的参数映射。
     * @return 返回查询结果的响应对象。
     * @throws IllegalAccessException 当访问字段失败时抛出此异常。
     */
    @Override
    public ResponseResult queryMerchantPlusThali(Map<String, Object> map) throws IllegalAccessException {
        // 根据传入的thaliType条件查询商家加锁权益信息
        BaseMerchantPlusThaliEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMerchantPlusThaliEntity>()
                .eq(map.containsKey("thaliType"), BaseMerchantPlusThaliEntity::getThaliType, Integer.valueOf(String.valueOf(map.get("thaliType")))));
        // 将查询到的实体转换为Map格式返回
        Map<String,Object> result = ParseMapUtils.beanToMap(entity);

        return ResponseResult.success(result);
    }

    /**
     * 根据传入的参数查询商家加锁权益详情，并填充相关权益数据。
     * @param map 包含查询条件和token的参数映射。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult selectplusthali(Map<String, Object> map) {
        // 从Redis中获取用户信息，验证token的有效性
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token"));
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 根据thaliType查询商家加锁权益信息
        BaseMerchantPlusThaliEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMerchantPlusThaliEntity>()
                .eq(map.containsKey("thaliType"), BaseMerchantPlusThaliEntity::getThaliType, Integer.valueOf(String.valueOf(map.get("thaliType")))));
        if (entity != null && entity.getThaliEquity() != null) {
            // 将权益字符串转换为权益ID列表
            String equity = entity.getThaliEquity();
            String[] split = equity.split(",");
            List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
            entity.setThaliEquityList(appIdList);
            // 查询权益详情
            Object data = usermsClient.selectplusequitys(appIdList).getData();
            List<PlusEquityDTO> selectplusequitys = JSONObject.parseArray(JSON.toJSONString(data), PlusEquityDTO.class);
            if (selectplusequitys != null && selectplusequitys.size() > 0) {
                for (PlusEquityDTO plusEquityEntity : selectplusequitys) {
                    // 根据权益类型填充不同的数据
                    // 获取红包模板
                    if (plusEquityEntity.getEquityType() == 2 || plusEquityEntity.getEquityType() == 3) {
                        String equityValue = plusEquityEntity.getEquityValue();
                        String[] split1 = equityValue.split(",");
                        List<Integer> redIdList = Stream.of(split1).map(Integer::parseInt).collect(Collectors.toList());
                        // 查询红包模板详情
                        Map<String, Object> actMap = new HashMap<>();
                        actMap.put("id", redIdList);
                        ResponseResult result = activityClient.redpacketTemplateList(map);
                        List<ActivityRedpacketTemplateVO> redTemplateEntities = (List<ActivityRedpacketTemplateVO>) result.getData();
                        plusEquityEntity.setRedList(redTemplateEntities);
                    }
                    if (plusEquityEntity.getEquityType() == 1) {
                        // 将权益值转换为分
                        BigDecimal multiply = new BigDecimal(plusEquityEntity.getEquityValue()).multiply(new BigDecimal(10));
                        plusEquityEntity.setEquityValue(multiply.toString());
                    }
                }
                // 设置填充后的权益详情到实体
                entity.setPlusEquitys(selectplusequitys);
            }
        }
        return ResponseResult.success(entity);
    }

/**
 * 更新加权权益。根据传入的加权权益类型查询相应的加权权益信息，处理并返回相关的权益数据。
 * @param map 包含加权权益类型和其他必要信息的映射。
 * @return 返回处理后的加权权益信息。
 */
@Override
public ResponseResult renewplusthali(Map<String, Object> map) {
    // 根据加权权益类型查询加权权益实体
    //判断是否有信息
    String thaliType = map.get("plusOrder").toString();
    BaseMerchantPlusThaliEntity entity=this.baseMapper.selectthalitype1(thaliType);
    if (entity != null && entity.getThaliEquity() != null){
        String equity = entity.getThaliEquity();
        // 将权益字符串转换为整数列表
        String[] split = equity.split(",");
        List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
        entity.setThaliEquityList(appIdList);

        // 根据权益ID列表查询权益详情
        String data = JSON.toJSONString(usermsClient.selectplusequitys(appIdList).getData());
        List<PlusEquityDTO> selectplusequitys = JSONObject.parseArray(data, PlusEquityDTO.class);
        if (selectplusequitys != null && selectplusequitys.size() > 0){
            for (PlusEquityDTO plusEquityEntity : selectplusequitys) {
                // 处理红包权益，查询红包模板
                //获取红包模板
                if (plusEquityEntity.getEquityType() == 2 || plusEquityEntity.getEquityType() == 3){
                    String equityValue = plusEquityEntity.getEquityValue();
                    String[] split1 = equityValue.split(",");
                    List<Integer> redIdList = Stream.of(split1).map(Integer::parseInt).collect(Collectors.toList());
                    //查询红包模板
                    Map<String, Object> actMap = new HashMap<>();
                    actMap.put("id",redIdList);
                    ResponseResult result = activityClient.redpacketTemplateList(actMap);
                    List<ActivityRedpacketTemplateVO> redTemplateEntities= (List<ActivityRedpacketTemplateVO>) result.getData();

                    plusEquityEntity.setRedList(redTemplateEntities);
                }
                // 处理折扣权益，将权益值乘以10
                if (plusEquityEntity.getEquityType() == 1){
                    BigDecimal multiply = new BigDecimal(plusEquityEntity.getEquityValue()).multiply(new BigDecimal(10));
                    plusEquityEntity.setEquityValue(multiply.toString());
                }
            }
        }
        entity.setPlusEquitys(selectplusequitys);
    }
    return ResponseResult.success(entity);
}

/**
 * 设置加权权益。根据传入的映射信息，判断是插入新的加权权益还是更新已存在的加权权益。
 * @param map 包含加权权益信息的映射。
 * @return 返回操作结果，成功或失败。
 */
@Override
public ResponseResult setplusthali(Map<String, Object> map) {
    // 从Redis中验证token的有效性
    String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token"));
    if (null == userInfoStr || userInfoStr.equals("")) {
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }
    // 根据加权权益类型查询加权权益实体
    //判断是否有信息
    BaseMerchantPlusThaliEntity entity=this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMerchantPlusThaliEntity>()
            .eq(map.containsKey("thaliType"),BaseMerchantPlusThaliEntity::getThaliType,Integer.valueOf(String.valueOf(map.get("thaliType")))));
    Integer integer = null;
    if (map.get("thaliEquityList") != null){
        List list = (List) map.get("thaliEquityList");
        String s = Joiner.on(",").join(list);
        map.put("thaliEquity",s);
    }
    if (entity == null){
        integer = this.baseMapper.insertplusthali(map);
    }else{
        integer =  this.baseMapper.updateplusthali(map);
    }
    if (integer > 0){
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }
    return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
}

/**
 * 查询加权折扣信息。查询所有的加权权益套餐，并计算平台最低会员折扣。
 * @return 返回包含加权权益套餐和会员折扣的信息列表。
 */
@Override
public ResponseResult selectplusdiscount() {
    // 查询所有的加权权益套餐
    //查询套餐表
    List<BaseMerchantPlusThaliEntity> plusThaliEntities = this.baseMapper.selectList(new LambdaQueryWrapper<BaseMerchantPlusThaliEntity>()
                                                  .eq(BaseMerchantPlusThaliEntity::getThaliStatus, CommonType.commonStatus.YES.getCode()));
    // 查询平台最低会员折扣
    String memberdiscount = baseMembershipGradeMapper.selectmemberdiscount();
    BigDecimal bigDecimal = new BigDecimal(memberdiscount);
    ArrayList<Object> returnList = new ArrayList<>();
    if (plusThaliEntities != null && plusThaliEntities.size() > 0){
        for (BaseMerchantPlusThaliEntity plusThaliEntity : plusThaliEntities) {

            String thaliEquity = plusThaliEntity.getThaliEquity();
            String[] split = thaliEquity.split(",");
            List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
            plusThaliEntity.setThaliEquityList(appIdList);

            List<PlusEquityEntityVO> selectplusequitys = usermsClient.selectplusequitysFeign(appIdList);
            if (selectplusequitys != null && selectplusequitys.size() > 0){
                for (PlusEquityEntityVO selectplusequity : selectplusequitys) {
                    if (selectplusequity.getEquityType() == 1){
                        HashMap<Object, Object> map = new HashMap<>();
                        String equityValue = selectplusequity.getEquityValue();
                        BigDecimal bigDecimal1 = new BigDecimal(equityValue);
                        BigDecimal subtract = bigDecimal.subtract(bigDecimal1);

                        //用于前端展示
                        BigDecimal multiply = bigDecimal1.multiply(new BigDecimal(10));

                        map.put("thaliType",plusThaliEntity.getThaliType());
                        map.put("equityValue",multiply.toString());
                        map.put("memberDiscount",subtract);
                        returnList.add(map);
                    }
                }
            }
        }
    }
    return ResponseResult.success(returnList);
}

}




