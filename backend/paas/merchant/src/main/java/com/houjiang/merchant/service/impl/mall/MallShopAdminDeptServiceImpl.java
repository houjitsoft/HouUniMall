package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopAdminDept;
import com.houjiang.merchant.common.entity.mall.MallShopAdminJob;
import com.houjiang.merchant.common.entity.mall.MallShopDept;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminDeptMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopDeptMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.service.mall.IMallShopAdminDeptService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统部门关联实现类
 * @author Amos
 */
@Service
public class MallShopAdminDeptServiceImpl implements IMallShopAdminDeptService {

    @Resource
    MallShopAdminDeptMapper mallShopAdminDeptMapper;
    @Resource
    MallShopDeptMapper mallShopDeptMapper;
    @Override
    public List<Integer> getDeptIdAttr(Integer adminId) {
        List<Integer> ret = new ArrayList<Integer>();
        List<MallShopAdminDept> rolesList = mallShopAdminDeptMapper.selectList(new QueryWrapper<MallShopAdminDept>().eq("admin_id", adminId).select("dept_id"));
        if (rolesList.size() > 0) {
            for (MallShopAdminDept adminDept : rolesList) {
                if (ret.contains(adminDept.getDeptId()) == false) {
                    ret.add(adminDept.getDeptId());
                }
            }
        }
        return ret;
    }

    @Override
    public List<String> getDeptNameAttr(Integer adminId) {
        List<String> ret = new ArrayList<String>();
        List<Integer> ids = getDeptIdAttr(adminId);
        if (ids.size() == 0) {
            return ret;
        } else {
            List<MallShopDept> deptsList = mallShopDeptMapper.selectList(new QueryWrapper<MallShopDept>().in("id", ids).select("name"));
            if (deptsList.size() > 0) {
                for (MallShopDept dept : deptsList) {
                    ret.add(dept.getName());
                }
            }
            return ret;
        }
    }

    @Override
    public void batchInsert(Integer adminId, List<Integer> deptIds, Integer shopId) {
        this.deleteByAdminId(adminId);
        if (deptIds != null && !deptIds.isEmpty()) {
            deptIds.forEach(item-> {
                mallShopAdminDeptMapper.insert(new MallShopAdminDept(){{
                    setAdminId(adminId);
                    setDeptId(item);
                    setShopId(shopId);
                    setCreateTime(System.currentTimeMillis() / 1000);
                    setUpdateTime(System.currentTimeMillis() / 1000);
                }});
            });
        }
    }

    @Override
    public void deleteByAdminId(Integer adminId) {
        mallShopAdminDeptMapper.delete(new QueryWrapper<MallShopAdminDept>().eq("admin_id", adminId));
    }

    @Override
    public Boolean deptAdminUse(Integer deptId) {
        MallShopAdminDept dept = mallShopAdminDeptMapper.selectOne(new QueryWrapper<MallShopAdminDept>().eq("dept_id", deptId).eq("delete_time", 0).last("limit 1"));
        return StringUtils.isNull(dept) ? true : false;
    }
}
