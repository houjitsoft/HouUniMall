package com.houjiang.userms.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UcenterMenuMapper extends BaseMapper<UcenterMenuEntity> {
    @Select("SELECT * FROM hou_ucenter_menu WHERE is_enabled=1 AND is_show=1 ORDER BY sort DESC, id")
    List<UcenterMenuEntity> findEnabledVisible();

    @Select({"<script>",
            "SELECT * FROM hou_ucenter_menu WHERE is_enabled=1 AND is_show=1 AND code IN",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>#{code}</foreach>",
            "ORDER BY sort DESC, id", "</script>"})
    List<UcenterMenuEntity> findEnabledByCodes(@Param("codes") java.util.Set<String> codes);
}
