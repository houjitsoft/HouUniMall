package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.GdbMerchantUploadAlbumDTO;
import com.houjiang.userms.dto.ManageImageGroupDeleteDTO;
import com.houjiang.userms.entity.UserUploadAlbumEntity;
import com.houjiang.userms.mapper.UserUploadAlbumMapper;
import com.houjiang.userms.service.UserUploadAlbumService;
import com.houjiang.userms.vo.ManageQueryGroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户相册服务实现类
 * @ClassName UserUploadAlbumServiceImpl
 * @Description 用户相册服务，提供查询、创建和删除相册分组的功能
 * @Author Amos
 * @Date 2023/6/28 16:47
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserUploadAlbumServiceImpl extends ServiceImpl<UserUploadAlbumMapper, UserUploadAlbumEntity> implements UserUploadAlbumService {

    @Resource
    private UserUploadAlbumMapper userUploadAlbumMapper;

    /**
     * 查询相册分组
     * @param param 包含用户令牌和相册类型的参数
     * @return 查询结果，包含相册分组信息
     */
    @Override
    public ResponseResult queryImageGroup(UserTokenBeanDTO param) {
        List<ManageQueryGroupVO> queryGroups = new ArrayList<>();
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            Map<String, Object> map = new HashMap<>();
            map.put("shopId", userPermissionsResult.getShopId());
            map.put("albumType", param.getAlbumType());
            queryGroups = userUploadAlbumMapper.selectByShopIdAndType(map);
            // 添加默认分组
            ManageQueryGroupVO queryGroup = new ManageQueryGroupVO();
            queryGroup.setAlbumId(0);
            queryGroup.setAlbumDesc("默认分组");
            queryGroups.add(0, queryGroup);
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success(queryGroups);
    }

    /**
     * 创建或更新相册分组
     * @param param 包含相册信息的参数，如果相册ID为空，则为新建相册
     * @return 创建或更新结果
     */
    @Override
    @Transactional
    public ResponseResult createGroup(GdbMerchantUploadAlbumDTO param) {
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            // 设置用户和店铺ID
            param.setUserId(userPermissionsResult.getUserId());
            param.setShopId(userPermissionsResult.getShopId());
            // 根据相册ID判断是插入还是更新
            if (param.getAlbumId() == null) {
                userUploadAlbumMapper.insertSelective(param);
            } else {
                userUploadAlbumMapper.updateByPrimaryKeySelective(param);
            }
        } else {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        return ResponseResult.success();
    }

    /**
     * 删除相册分组
     * @param param 包含要删除的相册ID的参数
     * @return 删除结果
     */
    @Override
    public ResponseResult deleteImageGroup(ManageImageGroupDeleteDTO param) {
        // 判断用户权限
        // 根据用户权限判断是否允许删除相册
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            // 具备权限，执行删除操作
            userUploadAlbumMapper.deleteByPrimaryKey(param.getAlbumId());
        } else {
            // 权限不足，返回失败响应
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 删除操作成功或权限检查失败后，返回成功响应
        return ResponseResult.success();

    }
}
