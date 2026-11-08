package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.UserPlatformMessageDTO;
import com.houjiang.userms.entity.UserPlatformMessageEntity;
import com.houjiang.userms.mapper.UserPlatformMessageMapper;
import com.houjiang.userms.service.UserPlatformMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户平台消息服务实现类
 * @author Amos
 */
@Service
public class UserPlatformMessageServiceImpl extends ServiceImpl<UserPlatformMessageMapper, UserPlatformMessageEntity>
    implements UserPlatformMessageService{

    /**
     * 发送平台消息
     * @param param 包含消息记录和token的Map
     * @return 成功返回消息ID，失败返回错误信息
     */
    @Override
    public ResponseResult sendPlatformMessage(Map<String, Object> param) {
        // 校验token是否存在
        if (null == param.get("token") || StringUtils.isEmpty((String) param.get("token"))) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
        // 将消息记录转为JSON字符串，再解析为UserPlatformMessageDTO对象
        String jsonString = JSONObject.toJSONString(param.get("record"));
        UserPlatformMessageDTO record = JSONObject.parseObject(jsonString, UserPlatformMessageDTO.class);
        // 如果未指定collection，获取用户信息并设置消息创建者
        if (param.get("collection") == null) {
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());
            if (null == userInfo) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            }
            record.setCreater(userInfo.getUserRealname());
        }
        // 插入消息记录，成功返回消息ID，失败返回错误信息
        int result = this.baseMapper.insertSelective(record);
        if (result > 0) {
            return ResponseResult.success(record.getId());
        }
        return ResponseResult.fail(BizCodeEnume.ADD_ERROR);
    }
    /**
     * 发送平台消息
     * @param param 包含消息记录的Map
     * @return 成功返回
     */
    @Override
    public ResponseResult sendPlatformMessageFeign(Map<String, Object> param) {
        this.baseMapper.insertmessage(param);
        return ResponseResult.success();
    }
    /**
     * 查询平台消息
     * @param param 包含查询条件和token的Map
     * @return 成功返回分页查询结果，失败返回错误信息
     */
    @Override
    public ResponseResult queryPlatformMessage(Map<String, Object> param) {
        String token = param == null || param.get("token") == null ? null : param.get("token").toString();
        UserInfoVO user = resolveUserByToken(token);
        if (user == null || user.getUserId() == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 根据用户ID查询消息记录，按阅读状态和创建时间倒序排列
        IPage<UserPlatformMessageEntity> page = this.baseMapper.selectPage(new Query<UserPlatformMessageEntity>().getPage(param),
                new LambdaQueryWrapper<UserPlatformMessageEntity>()
                        .eq(UserPlatformMessageEntity::getUserId, user.getUserId())
                        .last("order by is_reader,create_time desc"));

        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 标记消息为已读
     * @param param 包含token和消息ID列表的Map
     * @return 成功返回空结果，失败返回错误信息
     */
    @Override
    public ResponseResult readPlatformMessage(Map<String, Object> param) {
        String token = param == null || param.get("token") == null ? null : param.get("token").toString();
        UserInfoVO user = resolveUserByToken(token);
        if (user == null || user.getUserId() == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 校验消息ID列表是否存在
        List<String> platformMessageIds = (List<String>) param.get("platformMessageIds");
        if (null == platformMessageIds || platformMessageIds.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        // 更新消息的阅读状态
        for (String platformMessageId : platformMessageIds) {
            UserPlatformMessageEntity record = this.baseMapper.selectById(Long.parseLong(platformMessageId));
            // 判断信息有没有阅读，==0 是没有阅读,需要更新
            if (record.getIsReader() == 0) {
                String updater = StringUtils.isNotEmpty(user.getUserName()) ? user.getUserName() : user.getUserNickname();
                this.baseMapper.update(null, new LambdaUpdateWrapper<UserPlatformMessageEntity>()
                        .eq(UserPlatformMessageEntity::getId, record.getId())
                        .set(UserPlatformMessageEntity::getUpdater, updater)
                        .set(UserPlatformMessageEntity::getIsReader, CommonType.commonStatus.YES.getCode()));
            }
        }
        return ResponseResult.success();
    }

    /**
     * 删除平台消息
     * @param param 包含token和消息ID列表的Map
     * @return 成功返回空结果，失败返回错误信息
     */
    @Override
    public ResponseResult deletePlatformMessage(Map<String, Object> param) {
        String token = param == null || param.get("token") == null ? null : param.get("token").toString();
        UserInfoVO user = resolveUserByToken(token);
        if (user == null || user.getUserId() == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        // 校验消息ID列表是否存在
        List<String> platformMessageIds = (List<String>) param.get("platformMessageIds");
        if (null == platformMessageIds || platformMessageIds.size() == 0) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        // 删除指定的消息记录
        for (String platformMessageId : platformMessageIds) {
            this.baseMapper.deleteById(Long.parseLong(platformMessageId));
        }
        return ResponseResult.success();
    }

    /**
     * 与 JWT 过滤器同一套 Redis 解析（优先 hou-user-token-user_）。
     */
    private UserInfoVO resolveUserByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
    }
}
