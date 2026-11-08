package com.houjiang.base.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseSensitiveClassificationDTO;
import com.houjiang.base.entity.BaseSensitiveClassificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseSensitiveClassificationMapper
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSensitiveClassificationMapper extends BaseMapper<BaseSensitiveClassificationEntity> {


    /**
     * 查询敏感分类信息
     *
     * 该方法用于分页查询敏感分类信息，可以根据不同的参数进行筛选
     *
     * @param page 分页对象，包含分页查询所需的信息，如当前页数、每页大小等
     * @param param 查询参数对象，包含查询条件，如分类名称、创建时间等，用于筛选查询结果
     * @return 返回一个分页对象，包含查询到的敏感分类信息
     */
    IPage<BaseSensitiveClassificationEntity> querySensitiveClassification(Page<BaseSensitiveClassificationEntity> page, @Param("param") BaseSensitiveClassificationDTO param);
}
