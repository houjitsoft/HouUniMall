package com.houjiang.merchant.service.impl.admin;

import com.houjiang.merchant.service.admin.IAdminService;
import com.houjiang.merchant.service.admin.IAuthService;
import com.houjiang.merchant.vo.system.SystemAuthAdminDetailVo;
import com.houjiang.merchant.common.mapper.system.SystemMenuMapper;
import com.houjiang.merchant.common.mapper.system.SystemRoleMenuMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限功能类
 * @author Amos
 */
@Service
public class AuthServiceImpl implements IAuthService {

    @Resource
    IAdminService iAdminService;

    @Resource
    SystemMenuMapper systemMenuMapper;

    @Resource
    SystemRoleMenuMapper SystemRoleMenuMapper;

    @Override
    public List<String> getBtnAuthByRoleId(Integer adminId) {
        List<String> ret = new ArrayList<String>();
        SystemAuthAdminDetailVo admin = iAdminService.detail(adminId);
        if (admin.getRoot().equals(1)) {
            ret.add("*");
            return ret;
        } else {
            List<Integer> menuIds = SystemRoleMenuMapper.getMenuIds(adminId);
            ret = this.systemMenuMapper.getPerms(menuIds);
        }
        return ret;
    }
}
