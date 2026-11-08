package com.houjiang.goods.mapper;

import com.houjiang.goods.dto.GdbNavSortLogoDTO;
import com.houjiang.goods.dto.WebNavLogoConbfigDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseSysConfigsMapper
 * @Author Amos
 * @Date 2023/6/26 17:31
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSysConfigsMapper {

    /**
     * 配置
     * @param gdbNavSortLogoDTO
     * @return
     */
    List<WebNavLogoConbfigDTO> quertNavLogoByConfigType(@Param("param") GdbNavSortLogoDTO gdbNavSortLogoDTO);

}
