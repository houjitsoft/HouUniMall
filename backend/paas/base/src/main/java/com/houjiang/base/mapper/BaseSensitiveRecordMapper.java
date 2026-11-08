package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.base.entity.BaseSensitiveRecordEntity;
import com.houjiang.base.vo.BaseSensitiveRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseSensitiveRecordMapper
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSensitiveRecordMapper extends BaseMapper<BaseSensitiveRecordEntity> {


    /**
     * 查询基础敏感记录
     *
     * 该方法用于分页查询基础敏感记录信息它接受一个分页对象和一个参数对象作为参数，
     * 并返回一个分页对象，其中包含查询到的基础敏感记录该方法有助于减轻数据库查询负担，
     * 提高查询效率，特别是在处理大量数据时
     *
     * @param page 分页对象，包含分页信息如当前页码、每页大小等
     * @param param 查询参数对象，包含查询条件如时间范围、类型等
     * @return 返回一个包含基础敏感记录的分页对象
     */
    IPage<BaseSensitiveRecordVO> querybaseSensitiveRecord(Page<BaseSensitiveRecordDTO> page, @Param("param") BaseSensitiveRecordDTO param);
}
