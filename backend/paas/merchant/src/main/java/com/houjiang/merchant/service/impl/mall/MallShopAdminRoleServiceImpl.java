package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopAdminJob;
import com.houjiang.merchant.common.entity.mall.MallShopAdminRole;
import com.houjiang.merchant.common.entity.mall.MallShopRole;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminRoleMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.service.mall.IMallShopAdminRoleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统角色服务实现类
 * @author Amos
 */
@Service
public class MallShopAdminRoleServiceImpl implements IMallShopAdminRoleService {

    @Resource
    MallShopAdminRoleMapper mallShopAdminRoleMapper;
    @Resource
    MallShopRoleMapper mallShopRoleMapper;
    @Override
    public List<Integer> getRoleIdAttr(Integer adminId) {
        List<Integer> ret = new ArrayList<>();
        List<MallShopAdminRole> rolesList = mallShopAdminRoleMapper.selectList(new QueryWrapper<MallShopAdminRole>().eq("admin_id", adminId).select("role_id"));
        if (rolesList.size() > 0) {
            for (MallShopAdminRole adminRole : rolesList) {
                if (ret.contains(adminRole.getRoleId()) == false) {
                    ret.add(adminRole.getRoleId());
                }
            }
        }
        return ret;
    }

    @Override
    public List<String> getRoleNameAttr(Integer adminId) {
        List<String> ret = new ArrayList<String>();
        List<Integer> ids = getRoleIdAttr(adminId);
        if (ids.size() == 0) {
            return ret;
        } else {
            List<MallShopRole> rolesList = mallShopRoleMapper.selectList(new QueryWrapper<MallShopRole>().in("id", ids).select("name"));
            if (rolesList.size() > 0) {
                for (MallShopRole role : rolesList) {
                    ret.add(role.getName());
                }
            }
            return ret;
        }
    }

    @Override
    public List<MallShopAdminRole> getAdminIdByRoleId(Integer roleId) {
        List<MallShopAdminRole> result = mallShopAdminRoleMapper.selectList(new QueryWrapper<MallShopAdminRole>().eq("role_id", roleId).select("admin_id"));
        return result;
    }

    @Override
    public List<Integer> getAdminIdsByRoleId(Integer roleId) {
        List<MallShopAdminRole> retList = this.getAdminIdByRoleId(roleId);
        List<Integer> ret = new ArrayList<Integer>();
        if (retList.size() > 0) {
            for (MallShopAdminRole item : retList) {
                ret.add(item.getAdminId());
            }
        }
        return ret;
    }

    @Override
    public void batchInsert(Integer adminId, List<Integer> roleIds, Integer shopId) {
        this.deleteByAdminId(adminId);
        if (roleIds != null && !roleIds.isEmpty()) {
            roleIds.forEach(item-> {
                mallShopAdminRoleMapper.insert(new MallShopAdminRole(){{
                    setAdminId(adminId);
                    setRoleId(item);
                    setShopId(shopId);
                    setCreateTime(System.currentTimeMillis() / 1000);
                    setUpdateTime(System.currentTimeMillis() / 1000);
                    setDeleteTime(0L);
                }});
            });
        }
    }

    @Override
    public void deleteByAdminId(Integer adminId) {
        mallShopAdminRoleMapper.delete(new QueryWrapper<MallShopAdminRole>().eq("admin_id", adminId));
    }

    @Override
    public Long getCountByRoleId(Integer roleId) {
        return mallShopAdminRoleMapper.selectCount(new QueryWrapper<MallShopAdminRole>().eq("role_id", roleId).eq("delete_time", 0));

    }

    @Override
    public Boolean shopAdminUse(Integer roleId) {
        MallShopAdminRole role = mallShopAdminRoleMapper.selectOne(new QueryWrapper<MallShopAdminRole>().eq("role_id", roleId).eq("delete_time", 0).last("limit 1"));
        return StringUtils.isNull(role) ? true : false;
    }
}
