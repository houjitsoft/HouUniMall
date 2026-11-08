package com.houjiang.merchant.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.admin.IAdminDeptService;
import com.houjiang.merchant.common.entity.admin.AdminDept;
import com.houjiang.merchant.common.mapper.admin.AdminDeptMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统部门关联实现类
 * @author Amos
 */
@Service
public class AdminDeptServiceImpl implements IAdminDeptService {

    @Resource
    AdminDeptMapper adminDeptMapper;
    @Override
    public List<Integer> getDeptIdAttr(Integer adminId) {
        List<Integer> ret = new ArrayList<Integer>();
        List<AdminDept> rolesList = adminDeptMapper.selectList(new QueryWrapper<AdminDept>().eq("admin_id", adminId).select("dept_id"));
        if (rolesList.size() > 0) {
            for (AdminDept adminDept : rolesList) {
                ret.add(adminDept.getDeptId());
            }
        }
        return ret;
    }

    @Override
    public void batchInsert(Integer adminId, List<Integer> deptIds) {
        this.deleteByAdminId(adminId);
        if (deptIds != null && !deptIds.isEmpty()) {
            deptIds.forEach(item-> {
                this.adminDeptMapper.insert(new AdminDept(){{
                    setAdminId(adminId);
                    setDeptId(item);
                }});
            });
        }
    }

    @Override
    public void deleteByAdminId(Integer adminId) {
        this.adminDeptMapper.delete(new QueryWrapper<AdminDept>().eq("admin_id", adminId));
    }
}
