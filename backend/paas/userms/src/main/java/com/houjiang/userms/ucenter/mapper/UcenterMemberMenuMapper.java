package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UcenterMemberMenuMapper extends BaseMapper<UcenterMemberMenuEntity> {
    @Select("SELECT menu_id FROM hou_ucenter_member_menu WHERE member_id=#{memberId}")
    Set<Long> findGrantedMenuIds(@Param("memberId") Long memberId);

    @Select("SELECT m.code FROM hou_ucenter_member_menu g JOIN hou_ucenter_menu m ON m.id=g.menu_id WHERE g.member_id=#{memberId} ORDER BY m.sort DESC, m.id")
    Set<String> findGrantedMenuCodes(@Param("memberId") Long memberId);

    @Delete("DELETE FROM hou_ucenter_member_menu WHERE member_id=#{memberId}")
    int deleteByMemberId(@Param("memberId") Long memberId);
}
