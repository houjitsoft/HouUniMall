package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.AdminDepartmentDTO;
import com.houjiang.userms.entity.UserAdminDepartmentEntity;
import com.houjiang.userms.mapper.UserAdminDepartmentMapper;
import com.houjiang.userms.service.UserAdminDepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户管理服务实现类，负责用户与行政部门关联的业务操作。
 * @author Amos
 */
@Service
public class UserAdminDepartmentServiceImpl extends ServiceImpl<UserAdminDepartmentMapper, UserAdminDepartmentEntity>
    implements UserAdminDepartmentService{

    /**
     * 删除行政部门。
     * 验证用户令牌有效性后，检查待删除部门是否被使用，如果未被使用则删除部门。
     *
     * @param adminDepartmentDto 包含部门ID和用户令牌的信息
     * @return 删除操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult deleteDepartment(AdminDepartmentDTO adminDepartmentDto) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(adminDepartmentDto.getToken());
        if (null != userInfo) {
            // 判断当前部门是否有使用
            Integer result = this.baseMapper.selectDepartmentIsUser(adminDepartmentDto.getId());
            if (result > 0) {
                return ResponseResult.fail(BizCodeEnume.DELETE_ADMIN_DEPARTMENT_USER_ERROR);
            }
            // 删除部门
            this.baseMapper.deleteById(adminDepartmentDto.getId());
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询所有行政部门。
     * 验证用户令牌有效性后，返回所有部门列表。
     *
     * @param map 包含用户令牌的信息
     * @return 所有部门的列表，或失败原因
     */
    @Override
    public ResponseResult queryDepartmentAll(Map<String, Object> map) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            List<UserAdminDepartmentEntity> list = this.baseMapper.selectList(new QueryWrapper<>());
            return ResponseResult.success(list);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 添加或修改行政部门。
     * 验证用户令牌有效性后，执行添加或修改部门的操作。
     *
     * @param adminDepartmentDto 包含部门信息和用户令牌
     * @return 添加或修改操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult addDepartment(AdminDepartmentDTO adminDepartmentDto) {
        // 通过解析Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(adminDepartmentDto.getToken());
        // 如果用户信息不为空，则进行部门的新增或修改操作
        if (null != userInfo) {
            // 新增或修改部门
            this.baseMapper.addDepartment(adminDepartmentDto);
            // 返回操作成功的响应结果
            return ResponseResult.success();
        }
        // 如果用户信息为空，表示Token可能无效或已过期，返回操作失败的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }
}
