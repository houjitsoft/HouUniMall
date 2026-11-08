package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.IntegralDetailDTO;
import com.houjiang.userms.entity.UserPointsLogEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.mapper.UserInfoMapper;
import com.houjiang.userms.mapper.UserPointsLogMapper;
import com.houjiang.userms.service.UserPointsLogService;
import com.houjiang.userms.vo.IntegralValueListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class UserPointsLogServiceImpl extends ServiceImpl<UserPointsLogMapper, UserPointsLogEntity>
    implements UserPointsLogService{

    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private UserInfoMapper userInfoMapper;
    /**
     * 根据传入的参数查询用户的登录退出记录。
     * <p>
     * 该方法主要用于查询用户积分日志表中特定条件下的登录退出记录。通过传入的参数 map，
     * 可以灵活地指定查询条件，包括用户ID、班级ID以及时间范围。
     * </p>
     *
     * @param map 一个包含查询条件的键值对映射表。其中，"userId" 对应用户ID，"classId" 对应班级ID，
     *            "startTime" 和 "endTime" 对应查询的时间范围。
     * @return 返回查询结果的ResponseResult对象，包含查询到的用户登录退出记录。
     */
    @Override
    public ResponseResult queryLoginExit(Map<String, String> map) {
        // 初始化查询条件包装器
        QueryWrapper<UserPointsLogEntity> queryWrapper = new QueryWrapper<>();

        // 如果map不为空且包含"userId"键，添加用户ID作为查询条件
        if (null != map && null != map.get("userId")) {
            queryWrapper.eq("user_id", map.get("userId").toString());
        }
        // 如果map不为空且包含"classId"键，固定查询条件为班级ID为2
        if (null != map && null != map.get("classId")) {
            queryWrapper.eq("class_id", "2");
        }
        // 如果map不为空，且同时包含"startTime"和"endTime"键，添加时间范围作为查询条件
        if (null != map && null != map.get("startTime") && null != map.get("endTime")) {
            queryWrapper.between("points_log_time", map.get("startTime").toString(), map.get("endTime").toString());
        }
        queryWrapper.last("limit 1");
        // 根据构建的查询条件，查询并返回满足条件的第一条用户积分日志记录
        UserPointsLogEntity userPointsLogEntity = this.baseMapper.selectOne(queryWrapper);

        // 构建并返回查询成功的响应结果，包含查询到的用户登录退出记录
        return ResponseResult.success(userPointsLogEntity);
    }

    /**
     * 通过Map插入用户积分日志实体。
     *
     * 此方法接收一个Map对象，其中包含了用户积分日志的属性及其值。
     * 通过将这个Map对象转换为JSON字符串，然后再反序列化为UserPointsLogEntity对象，
     * 最后将该对象插入到数据库中。
     *
     * @param map 包含用户积分日志属性的Map对象。
     * @return 返回一个表示操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult insertByMap(Map<String, Object> map) {
        // 将Map对象转换为JSON字符串，再反序列化为UserPointsLogEntity对象
        UserPointsLogEntity entity = JSON.parseObject(JSON.toJSONString(map), UserPointsLogEntity.class);
        // 调用BaseMapper的insert方法插入实体到数据库
        this.baseMapper.insert(entity);

        // 返回表示操作成功的ResponseResult对象
        return ResponseResult.success();
    }

    /**
     * 查询积分详情列表
     *
     * @param params 查询参数，包含token等信息
     * @return 查询结果，包含积分详情列表和相关规则信息
     */
    @Override
    public ResponseResult queryIntegralDetailList(Map<String, Object> params) {
        // 从参数中获取token
        String token = params.get("token").toString();
        // 检查token是否存在于Redis中，若不存在则返回未登录状态
        if (!RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token)) {
            return ResponseResult.fail(BizCodeEnume.INTEGRAL_DETAIL_MUST_LOGIN);
        }

        // 通过token获取用户信息
        // 获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);

        // 创建积分详情DTO对象
        IntegralDetailDTO integralDetailDto = new IntegralDetailDTO();
        // 将用户ID放入查询参数中
        params.put("userId", userInfo.getUserId());
        // 根据参数查询积分明细
        // 根据userid获取交易明细
        PageUtils pageInfoS = integralDetail(params);

        // 获取查询结果列表
        List<UserPointsLogEntity> logEntityList = (List<UserPointsLogEntity>) pageInfoS.getList();

        // 遍历列表，根据积分类型设置积分变化说明
        for (UserPointsLogEntity udbUcenterPointsLog : logEntityList) {
            if (udbUcenterPointsLog.getPointsLogType() == 2) {
                // 积分减少的情况，设置为负数表示
                String s = "-" + udbUcenterPointsLog.getPointsLogPoints();
                udbUcenterPointsLog.setIntegralChange(s);
            } else {
                // 积分增加的情况，设置为正数表示
                String s = "+" + udbUcenterPointsLog.getPointsLogPoints();
                udbUcenterPointsLog.setIntegralChange(s);
            }
        }

        // 更新查询结果列表
        pageInfoS.setList(logEntityList);

        // 根据用户ID获取用户积分总数
        // 根据userid获取会员积分
        Integer integral = userInfoMapper.selectById(userInfo.getUserId()).getUserIntegral();

        Integer frozenIntegra = userInfoMapper.queryMemberFrozenIntegral(userInfo.getUserId());
        if(null == frozenIntegra){
            frozenIntegra = 0;
        }
        integral = integral - frozenIntegra;
        if(integral < 0) {
            integral = 0;
        }

        // 查询积分规则
        // 查询积分规则
        ResponseResult resultResp = baseServiceClient.queryIntegralRules();
        Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();

        // 设置积分规则信息到DTO中
        integralDetailDto.setLoginIntegral(integralRules.get("ruleLogin"));
        integralDetailDto.setRegisterIntegral(integralRules.get("ruleRegister"));
        integralDetailDto.setRuleConsume(integralRules.get("ruleConsume"));
        // 设置用户积分总数到DTO中
        integralDetailDto.setCountIntegral(integral);
        integralDetailDto.setFrozenIntegra(frozenIntegra);
        // 设置分页信息到DTO中
        integralDetailDto.setPageInfoS(pageInfoS);

        // 返回查询成功的结果，包含积分详情DTO
        return ResponseResult.success(integralDetailDto);
    }

    /**
     * 根据传入的参数查询用户的积分明细。
     *
     * @param params 包含用户令牌(token)和其他查询参数的映射。
     * @return 返回查询结果，如果令牌无效或权限不足，则返回失败结果。
     */
    @Override
    public ResponseResult query(Map<String, Object> params) {
        // 通过令牌获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(params.get("token").toString());
        // 检查用户信息是否有效。
        if (null != userInfo) {
            // 检查用户是否有查询积分明细的权限。
            // 执行积分明细查询。
            // 分页查询数据
            IPage<IntegralValueListVO> page = this.baseMapper.queryIntegralValueListPort(new Query<IntegralValueListVO>().getPage(params), params);
            List<IntegralValueListVO> records = page.getRecords();
            // 遍历查询结果，根据积分类型添加积分操作的正负号。
            for (IntegralValueListVO record : records) {
                //拼接积分类型
                if (null != record.getPointsLogType() && record.getPointsLogType() == 1) {
                    record.setPointsLogPoints2("+" + record.getPointsLogPoints());
                } else {
                    record.setPointsLogPoints2("-" + record.getPointsLogPoints());
                }
            }
            // 更新查询结果集后返回。
            page.setRecords(records);
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 令牌无效，返回失败结果。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询积分详情的分页数据。
     *
     * @param params 查询参数，包含用户ID、班级ID、开始时间、结束时间、操作描述等信息。
     * @return 分页工具类，包含查询结果和分页信息。
     */
    public PageUtils integralDetail(Map<String, Object> params) {
        // 根据传入的参数进行分页查询
        IPage<UserPointsLogEntity> page = this.baseMapper.selectPage(
                new Query<UserPointsLogEntity>().getPage(params),
                new LambdaQueryWrapper<UserPointsLogEntity>()
                    // 查询指定用户的积分记录
                    .eq(UserPointsLogEntity::getUserId, params.get("userId").toString())
                    // 如果班级ID存在，则查询指定班级的积分记录
                    .eq(params.get("classId") != null && !"".equals(params.get("classId")), UserPointsLogEntity::getClassId, params.get("classId"))
                    // 如果有开始时间，则查询开始时间之后的积分记录
                    .ge(params.get("startTime") != null && !"".equals(params.get("startTime")), UserPointsLogEntity::getPointsLogTime, params.get("startTime"))
                    // 如果有结束时间，则查询结束时间之前的积分记录
                    .le(params.get("endTime") != null && !"".equals(params.get("endTime")), UserPointsLogEntity::getPointsLogTime, params.get("endTime"))
                    // 如果有操作描述，则查询包含该描述的积分记录
                    .like(params.get("describe") != null && !"".equals(params.get("describe")), UserPointsLogEntity::getPointsLogOperate, params.get("describe"))
                    // 按积分记录时间降序排序
                    .orderByDesc(UserPointsLogEntity::getPointsLogTime)
        );

        // 将查询结果包装成分页工具类返回
        return PageUtils.getPage(page);
    }

    /**
     * 根据订单号查询订单对应的积分获取记录。
     * 该方法主要用于查询用户通过特定订单获取的积分数量。
     * 积分记录根据订单号、积分日志类型和班级ID进行筛选，确保查询到的积分记录是与特定订单相关的。
     * 如果找不到匹配的积分记录，则返回0，表示该订单未产生积分。
     *
     * @param orderId 订单号，用于查询积分记录的关键标识。
     * @return 返回查询到的积分数量，如果未找到相关记录则返回0。
     */
    @Override
    public Integer queryPointsByOrderId(String orderId) {
        // 使用LambdaQueryWrapper构造查询条件，查询特定订单的积分记录。
        UserPointsLogEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<UserPointsLogEntity>()
                .eq(UserPointsLogEntity::getOrderId, orderId)
                .eq(UserPointsLogEntity::getPointsLogType, 1)
                .eq(UserPointsLogEntity::getClassId, 4)
                .last("limit 1"));
        // 如果查询结果为空，则说明该订单没有对应的积分记录，返回0。
        if(entity == null) {
            return 0;
        }
        // 返回查询到的积分记录中的积分数量。
        return entity.getPointsLogPoints();
    }

}




