package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.BaseSystemPlatformNavDTO;
import com.houjiang.base.entity.BaseSystemPlatformNavEntity;
import com.houjiang.base.vo.index.WebSystemPlatformNavVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseSystemPlatformNavMapper
 * @Description  页面导航表
 * @Author Amos
 * @Date 2023-05-24 09:33:08
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSystemPlatformNavMapper extends BaseMapper<BaseSystemPlatformNavEntity> {

 /**
  * 查询首页导航
  * @param code
  * @return
  */
 List<WebSystemPlatformNavVO> selectPlatformNav(@Param("navProperty")Integer code);



 /**
  * 新增平台导航
  *
  * @param baseSystemPlatformNavDto
  */
 void insertPlatformNav(@Param("param") BaseSystemPlatformNavDTO baseSystemPlatformNavDto);

 /**
  * 修改平台导航
  *
  * @param baseSystemPlatformNavDto
  */
 void updatePlatformNav(@Param("param")BaseSystemPlatformNavDTO baseSystemPlatformNavDto);
}

