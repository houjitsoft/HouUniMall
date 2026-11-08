package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.base.entity.BaseSensitiveEntity;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseSensitiveMapper
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:46:14
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSensitiveMapper extends BaseMapper<BaseSensitiveEntity> {


    /**
     * 查询敏感信息列表
     *
     * @param page 分页对象，包含分页参数如当前页码和每页记录数
     * @param param 查询参数对象，可能包含多种查询条件
     * @return 返回一个分页对象，包含敏感信息的列表
     */
    IPage<BaseSensitiveVO> querySensitive(Page<BaseSensitiveVO> page, @Param("param") BaseSensitiveDTO param);

    /**
     * 查询所有有效敏感词
     * @return
     */
    List<String> queryAllSensitive();

    /**
     * 查询所有敏感信息
     *
     * @return 返回一个列表，包含所有的敏感信息
     */
    List<BaseSensitiveVO> selectAllSensitive();

    /**
     * 更新敏感信息的启用状态
     *
     * @param sensitiveIdList 敏感信息的ID列表，用于指定需要更新的记录
     */
    void updateSensitiveStart(@Param("sensitiveIdList")List<Integer> sensitiveIdList);

    /**
     * 根据敏感信息ID列表查询敏感信息
     *
     * @param idList 敏感信息的ID列表，用于指定需要查询的记录
     * @return 返回一个字符串列表，包含根据ID查询到的敏感信息
     */
    List<String> querySensitiveByIdList(@Param("sensitiveIdList")List<Integer> idList);
}
