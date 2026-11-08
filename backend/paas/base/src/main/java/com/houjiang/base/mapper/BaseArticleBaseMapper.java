package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.BaseArticleBaseDTO;
import com.houjiang.base.entity.BaseArticleBaseEntity;
import com.houjiang.base.vo.index.WebArticleBaseNameVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseArticleBaseMapper
 * @Description  网站初始化内容设置
 * @Author Amos
 * @Date 2023-05-22 18:54:37
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseArticleBaseMapper extends BaseMapper<BaseArticleBaseEntity> {


  /**
   * 根据文章分组ID和页面大小选择文章基本信息列表
   * 此方法用于从数据库中筛选出属于特定文章分组的文章，并按照指定的数量返回
   * 主要解决的问题是，如何在大量文章中快速定位到特定分组的文章，以及如何控制查询结果的数量，以优化性能和用户体验
   *
   * @param articleGroupId 文章分组ID，用于指定需要查询的文章分组
   * @param pageSize 页面大小，用于限制查询结果的最大数量
   * @return 返回一个包含文章基本信息的列表，这些文章属于指定的分组，并且数量不超过页面大小的限制
   */
  List<WebArticleBaseNameVO> selectByGroupId(@Param("articleGroupId")Integer articleGroupId, @Param("pageSize")Integer pageSize);


    /**
     * 文章插入
     *
     * @param param
     */
    void articleInsert(@Param("param") BaseArticleBaseDTO param);

    /**
     * 查询文章基础
     *
     * @param page
     * @param param
     * @return
     */
    IPage<BaseArticleBaseEntity> queryArticleBase(IPage<BaseArticleBaseEntity> page, @Param("param") BaseArticleBaseDTO param);
 }

