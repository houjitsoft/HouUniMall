package com.houjiang.merchant.common.mapper.system;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.system.SystemMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统菜单Mapper
 * @author Amos
 */
@Mapper
public interface SystemMenuMapper extends IBaseMapper<SystemMenu> {

    @Select({"<script>",
            " SELECT ",
            " perms ",
            " FROM hou_merchant_system_menu WHERE perms != '' AND id in ",
            "<foreach item='item' index='index' collection='items' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> getPerms(@Param("items") List<Integer> menuIds);




}
