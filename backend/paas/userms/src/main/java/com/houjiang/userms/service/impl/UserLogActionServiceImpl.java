package com.houjiang.userms.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.userms.entity.UserLogActionEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.mapper.UserLogActionMapper;
import com.houjiang.userms.service.UserLogActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 用户操作日志服务实现类
 * @author Amos
 */
@Service
public class UserLogActionServiceImpl extends ServiceImpl<UserLogActionMapper, UserLogActionEntity>
    implements UserLogActionService{

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 查询用户操作日志
     * @param paraMap 查询参数
     * @return 查询结果
     */
    @Override
    public ResponseResult queryUserOperateLog(Map<String, Object> paraMap) {
        // 从Redis中获取用户信息，验证权限
        String token = (String) paraMap.get("token");
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + token);
        if (StrUtil.isBlank(userInfoStr)) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 处理查询参数
        String userName = (String) paraMap.get("userName");
        String userNickName = (String) paraMap.get("userNickName");
        String userMobile = (String) paraMap.get("userMobile");
        paraMap.put("userName", userName);
        paraMap.put("userNickName", userNickName);
        paraMap.put("userMobile", userMobile);

        // 分页查询数据
        // 分页查询数据
        IPage<Map<String, Object>> page = this.baseMapper.queryUserOperateLog(new Query<Map<String, Object>>().getPage(paraMap), paraMap);

        // 格式化日期时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (null != page.getRecords()) {
            try {
                // 根据配置决定是否脱敏
                String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    for (Map<String, Object> map : page.getRecords()) {
                        // 对手机号进行脱敏处理
                        if (map.get("userName") != null && StrUtil.isNotEmpty(map.get("userName").toString())) {
                            boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("userName").toString());
                            if (match) {
                                map.put("userName", DesensitizedUtil.mobilePhone(map.get("userName").toString()));
                            }
                        }
                        if (map.get("userMobile") != null && StrUtil.isNotEmpty(map.get("userMobile").toString())) {
                            map.put("userMobile", DesensitizedUtil.mobilePhone(map.get("userMobile").toString()));
                        }
                    }
                }
                // 格式化日志时间
                for (Map<String, Object> map : page.getRecords()) {
                    if (map.get("log_time") != null) {
                        String dateTime = defaultFormat.format(map.get("log_time"));
                        map.put("log_time", dateTime);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseResult.success(PageUtils.getPage(page));
    }
}
