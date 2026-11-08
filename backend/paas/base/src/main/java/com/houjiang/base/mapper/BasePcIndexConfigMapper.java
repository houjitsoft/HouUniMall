package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BasePcIndexConfigEntity;
import com.houjiang.base.vo.index.GdbPcIndexConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BasePcIndexConfigMapper
 * @Description
 * @Author Amos
 * @Date 2023-05-24 09:39:22
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BasePcIndexConfigMapper extends BaseMapper<BasePcIndexConfigEntity> {

 /**
  * 查询所有模块数据
  * @param pcIndexConfig
  * @return
  */
 List<GdbPcIndexConfigVO> selectPcIndexConfig(@Param("param")GdbPcIndexConfigVO pcIndexConfig);
}

