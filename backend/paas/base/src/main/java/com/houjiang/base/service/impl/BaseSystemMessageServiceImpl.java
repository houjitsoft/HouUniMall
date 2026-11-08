package com.houjiang.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseSystemMessageEntity;
import com.houjiang.base.mapper.BaseSystemMessageMapper;
import com.houjiang.base.service.BaseSystemMessageService;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.base.BaseSystemMessageDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseSystemMessageServiceImpl
 * @Description 系统消息服务实现
 * @Author Amos
 * @Date 2023/7/19 16:02
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSystemMessageServiceImpl extends ServiceImpl<BaseSystemMessageMapper, BaseSystemMessageEntity>
        implements BaseSystemMessageService {

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 查询系统订单消息
     * @param map 包含查询参数和token的映射
     * @return 返回查询结果的响应对象
     */
    @Override
    public ResponseResult querySysOrderNews(Map<String, Object> map) {
        // 从映射中获取token
        String token = (String) map.get("token");
        // 根据token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回token失效的错误响应
        if (userInfo == null) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 将用户的shopId添加到查询参数映射中
            map.put("shopId", userInfo.getShopId());
            // 执行查询，获取分页结果
            IPage<BaseSystemMessageEntity> page = this.baseMapper.querySysOrderMessage(new Query<BaseSystemMessageEntity>().getPage(map), map);
            // 构建成功响应并返回查询的分页数据
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 捕获异常，构建失败响应并返回异常信息
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    /**
     * 标记系统订单消息为已读
     * @param map 包含token和消息ID列表的映射
     * @return 返回操作结果的响应对象
     */
    @Override
    public ResponseResult readSysOrderNews(Map<Object, Object> map) {
        // 从映射中获取token
        String token = (String) map.get("token");
        // 根据token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回token无效的错误响应
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 构建待更新的消息实体
        BaseSystemMessageEntity baseSystemMessageEntity = new BaseSystemMessageEntity();
        baseSystemMessageEntity.setMessageIslook(1);
        UpdateWrapper<BaseSystemMessageEntity> updateWrapper = new UpdateWrapper<>();
        // 根据条件更新消息的已读状态
        updateWrapper.eq("message_user_id", userInfo.getUserId())
                .in(StrUtil.isNotEmpty(map.get("isAllRead").toString()) && map.get("isAllRead").toString().equals("0"),
                        "message_id", (List<Integer>) map.get("messageId"));
        // 执行更新操作
        int isUpdateReadMessage = this.baseMapper.update(baseSystemMessageEntity, updateWrapper);
        // 如果更新条数为0，则返回失败响应
        if (isUpdateReadMessage == 0) {
            return ResponseResult.fail();
        }
        // 返回成功响应
        return ResponseResult.success();
    }

    /**
     * 删除系统订单消息
     * @param map 包含token和消息ID列表的映射
     * @return 返回操作结果的响应对象
     */
    @Override
    @Transactional
    public ResponseResult deleteSysOrderNews(Map<Object, Object> map) {
        // 从映射中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExistToken = redisCache.hasKey(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token不存在，则返回token失效的错误响应
        if (!isExistToken) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        try {
            // 从Redis中获取用户信息
            String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            UserInfoVO userInfo = dto.getUser();
            String userId = userInfo.getUserId();
            // 构建待更新的消息实体
            BaseSystemMessageEntity baseSystemMessageEntity = new BaseSystemMessageEntity();
            baseSystemMessageEntity.setMessageIsdel(1);
            UpdateWrapper<BaseSystemMessageEntity> updateWrapper = new UpdateWrapper<>();
            // 根据条件更新消息的删除状态
            updateWrapper.eq("message_user_id", userId)
                    .in(map.get("isAllDelete") == null||(map.get("isAllDelete") != null && (Integer) map.get("isAllDelete") != 1),
                            "message_id", (List<Integer>) map.get("messageId"));
            // 执行更新操作
            int isdelMessage = this.baseMapper.update(baseSystemMessageEntity, updateWrapper);
            // 如果更新条数为0，则返回失败响应
            if (isdelMessage == 0) {
                return ResponseResult.fail(2, "删除失败!");
            }
            // 返回成功响应
            return ResponseResult.success();
        } catch (Exception e) {
            // 捕获异常，返回失败响应并附带异常信息
            return ResponseResult.fail(500, e.getMessage());
        }
    }

    /**
     * 添加系统订单消息
     * @param baseSystemMessageDTO 包含新消息信息的数据传输对象
     * @return 返回操作结果的响应对象
     */
    @Override
    public ResponseResult addSysOrderNews(BaseSystemMessageDTO baseSystemMessageDTO) {
        // 创建新消息实体
        BaseSystemMessageEntity entity = new BaseSystemMessageEntity();
        // 将数据传输对象的属性复制到实体中
        BeanUtils.copyProperties(baseSystemMessageDTO, entity);
        // 插入新消息实体
        this.baseMapper.insert(entity);
        // 返回成功响应
        return ResponseResult.success();
    }

    @Override
    public void insertSysMessage(Map<String, Object> map) {
        this.baseMapper.insertSysMessage(map);

    }

}
