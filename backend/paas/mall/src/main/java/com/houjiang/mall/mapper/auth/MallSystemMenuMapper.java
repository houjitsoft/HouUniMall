package com.houjiang.mall.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.auth.MallSystemMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mall 系统菜单 Mapper 接口
 * @author Amos
 */
@Mapper
public interface MallSystemMenuMapper extends BaseMapper<MallSystemMenu> {

    /**
     * 根据管理员ID查询路由菜单（M/C，通过角色关联）
     */
    List<MallSystemMenu> selectRouteMenusByAdminId(@Param("adminId") Integer adminId);

    /**
     * 根据管理员ID查询按钮权限标识（A 类型）
     */
    List<String> selectPermsByAdminId(@Param("adminId") Integer adminId);
}
