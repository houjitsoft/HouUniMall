package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseInformationClassifyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 资讯分类表(BaseInformationClassify)表数据库访问层
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
@Mapper
public interface BaseInformationClassifyMapper extends BaseMapper<BaseInformationClassifyEntity> {


    /**
     * 根据分类ID选择分类级别
     *
     * @param classifyId 分类ID，用于查询分类级别
     * @return 返回一个整数列表，表示该分类ID对应的级别
     */
    List<Integer> selectClassifyLevel(@Param("classifyId") Integer classifyId);

    /**
     * 根据分类ID列表删除相关记录
     *
     * @param classifyIdList 分类ID列表，用于指定要删除的记录
     */
    void deleteByClassifyIds(@Param("classifyIdList") List<Integer> classifyIdList);

    /**
     * 查询是否是自己经销商品
     *
     * @param param
     * @return
     */
    Integer checkSupplySelf(@Param("param") Map<String, Object> param);
}
