package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.MakeTagDTO;
import com.houjiang.userms.dto.TagInfoPageDTO;
import com.houjiang.userms.entity.UserTagInfoEntity;
import com.houjiang.userms.mapper.UserTagInfoMapper;
import com.houjiang.userms.service.UserTagInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户标签信息 service 实现类
 * @author Amos
 */
@Service
public class UserTagInfoServiceImpl extends ServiceImpl<UserTagInfoMapper, UserTagInfoEntity>
    implements UserTagInfoService{

    /**
     * 删除标签信息
     * @param dto 标签信息页DTO
     * @return 删除结果
     */
    @Override
    public ResponseResult deleteTagInfo(TagInfoPageDTO dto) {
        // 通过解析令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        if (null != userInfo) {
            // 当用户信息存在时，更新用户标签信息，将标签状态设置为不活跃，并更新更新时间
            this.baseMapper.update(null,new LambdaUpdateWrapper<UserTagInfoEntity>()
                            .set(UserTagInfoEntity::getTagIsactive,00)
                            .set(UserTagInfoEntity::getTagUpdData,DateUtils.getNowDateToString())
                            .eq(UserTagInfoEntity::getTagId,dto.getTagId()));
            // 返回操作成功的响应
            return ResponseResult.success();
        }
        // 当用户信息不存在时，返回令牌非法的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 更新标签信息
     * @param dto 标签信息页DTO
     * @return 更新结果
     */
    @Override
    public ResponseResult updateTagInfo(TagInfoPageDTO dto) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        if (null != userInfo) {
            // 查询已存在的标签名是否与输入的标签名相同但标签ID不同
            Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<UserTagInfoEntity>()
                    .eq(UserTagInfoEntity::getTagName, dto.getTagName())
                    .ne(UserTagInfoEntity::getTagId, dto.getTagId())));
            if (count > 0) {
                // 如果存在相同的标签名但标签ID不同，则返回失败结果
                return ResponseResult.fail(BizCodeEnume.ADD_TAG_INFO_ERROR);
            }
            // 设置日期格式，用于更新标签的更新时间
            // 设置日期格式
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 获取当前日期并格式化，更新到DTO中
            dto.setTagUpdData(DateUtils.getNowDateToString());
            // 更新标签信息
            this.baseMapper.updateTagInfo(dto);
            // 更新成功，返回成功结果
            return ResponseResult.success();
        }
        // 如果用户信息为空，则返回失败结果，提示Token非法
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 添加标签信息
     * @param dto 标签信息页DTO
     * @return 添加结果
     */
    @Override
    public ResponseResult addTagInfo(TagInfoPageDTO dto) {
        // 通过解析令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        if (userInfo != null) {
            // 检查标签名是否已存在
            // 判断标签名是否存在
            Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<UserTagInfoEntity>()
                    .eq(UserTagInfoEntity::getTagName, dto.getTagName())));
            if (count > 0) {
                // 标签名已存在，返回失败结果
                return ResponseResult.fail(BizCodeEnume.ADD_TAG_INFO_ERROR);
            }
            // 标签名不存在，添加新的标签信息
            this.baseMapper.addTagInfo(dto);
            // 添加成功，返回成功结果
            return ResponseResult.success();
        }
        // 令牌解析失败，返回失败结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 为用户打标签
     * @param dto 打标签DTO
     * @return 打标签结果
     */
    @Override
    public ResponseResult makeUserTag(MakeTagDTO dto) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        if (null != userInfo) {
            // 获取用户ID数组
            String[] userIdArray = dto.getUserIdArray();
            // 遍历用户ID数组，为每个用户添加标签
            //遍历传过来的userid数组判断是否有标签
            for (String userid : userIdArray) {
                // 将标签ID数组用逗号拼接成字符串
                String str = StringUtils.join(dto.getTagIdArray(), ",");
                // 准备更新标签的参数
                Map<String, Object> paraMap = new HashMap<>();
                paraMap.put("memberId",userid);
                paraMap.put("tagId",str);
                // 调用Mapper接口更新用户的标签信息
                this.baseMapper.updateTag(paraMap);
            }
            // 返回操作成功的响应
            return ResponseResult.success();
        }
        // 如果用户信息为空，表示Token非法，返回相应的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 查询标签信息
     * @param map 查询参数映射
     * @return 查询结果
     */
    @Override
    public ResponseResult queryTagInfo(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 初始化分页参数
            // 分页查询数据
            Page<UserTagInfoEntity> page = new Page<>((Integer) map.get("page"), (Integer) map.get("limit"));
            // 根据条件查询用户标签信息，包括激活状态为1，标签名含有指定关键字，并按标签权重升序排序
            IPage<UserTagInfoEntity> iPage = this.baseMapper.selectPage(page, new LambdaQueryWrapper<UserTagInfoEntity>()
                    .eq(UserTagInfoEntity::getTagIsactive, 1)
                    .like(map.get("tagName") != null && !"".equals(map.get("tagName")), UserTagInfoEntity::getTagName, map.get("tagName"))
                    .orderByAsc(UserTagInfoEntity::getTagWeight));
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(iPage));
        }
        // 如果token无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }
}
