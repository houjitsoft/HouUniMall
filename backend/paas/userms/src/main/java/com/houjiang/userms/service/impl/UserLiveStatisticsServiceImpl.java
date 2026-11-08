package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.userms.entity.UserLiveStatisticsEntity;
import com.houjiang.userms.mapper.UserLiveStatisticsMapper;
import com.houjiang.userms.service.UserLiveStatisticsService;
import com.houjiang.userms.vo.MemberMessageListVO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户直播统计服务实现类
 * @author Amos
 */
@Service
public class UserLiveStatisticsServiceImpl extends ServiceImpl<UserLiveStatisticsMapper, UserLiveStatisticsEntity>
    implements UserLiveStatisticsService{

    /**
     * 保存直播统计数据
     * @param map 包含直播统计信息的Map
     * @return 返回操作结果
     */
    @Override
    public ResponseResult saveLiveStatistics(Map<String, Object> map) {
        // 从请求头中获取token并解析userId
        String userId = RequestTokenUtils.getUserIdFromRequest();
        
        // 如果从token解析出userId，优先使用token中的userId
        if (userId == null) {
            return ResponseResult.fail(401, "用户未登录或token已过期");
        }
        
        // 将Map转换为UserLiveStatisticsEntity对象
        UserLiveStatisticsEntity liveStatisticsEntity = JSON.parseObject(JSONObject.toJSONString(map), UserLiveStatisticsEntity.class);
        
        // 设置从token解析出的userId
        liveStatisticsEntity.setUserId(userId);
        
        // 设置登录时间为当前时间
        if (liveStatisticsEntity.getLoginTime() == null) {
            liveStatisticsEntity.setLoginTime(new Date());
        }
        
        // 设置时间戳为当前时间
        if (liveStatisticsEntity.getTs() == null) {
            liveStatisticsEntity.setTs(new Date());
        }
        
        // 获取当天日期
        String todayTime = DateUtils.getDate();

        // 统计当天该用户在该平台的登录记录数
        int count = this.baseMapper.selectUserLiveStatisticsCount(liveStatisticsEntity,todayTime);

        // 如果当天没有登录记录，则插入新的记录
        if (count == 0) {
            int  result = this.baseMapper.insert(liveStatisticsEntity);
            // 根据操作结果返回成功或失败
            return result== CommonStatusEnum.SUCCESS.getCode()?ResponseResult.success():ResponseResult.fail();
        }
        // 如果当天已有登录记录，直接返回成功
        return ResponseResult.success();
    }

    /**
     * 查询用户观影详情分页数据
     * @param param 查询参数Map
     * @return 返回分页查询结果
     */
    @Override
    public ResponseResult queryUserNikkatsuDetailPort(Map<String, Object> param) {

        // 执行分页查询
        IPage<MemberMessageListVO> page=this.baseMapper.queryUserNikkatsuDetailPort(new Query<MemberMessageListVO>().getPage(param),
                param);
        // 处理查询结果，格式化时间并隐藏手机号中间部分
        for (MemberMessageListVO result : page.getRecords()) {
            if (null != result.getUserRegTime()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long time = Long.valueOf(result.getUserRegTime());
                String d = format.format(time * 1000);
                result.setUserRegTime(d);
            }
            if(null!=result.getUserMobile()&&!("".equals(result.getUserMobile()))){
                String  hidenPhone= result.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
                result.setUserMobile(hidenPhone);
            }
        }
        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询用户观影统计数据
     * @param param 查询参数Map
     * @return 返回查询结果列表
     */
    @Override
    public ResponseResult queryUserNikkatsuPort(Map<String, Object> param) {
        // 执行查询
        List<UserLiveStatisticsEntity> list=this.baseMapper.queryUserNikkatsuPort(param);
        // 返回查询结果列表
        return ResponseResult.success(list);
    }
}
