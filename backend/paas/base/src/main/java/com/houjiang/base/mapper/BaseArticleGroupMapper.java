package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.BaseArticleGroupDTO;
import com.houjiang.base.entity.BaseArticleGroupEntity;
import com.houjiang.base.vo.index.WebIndexArticleGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseArticleGroupMapper
 * @Description  网站初始化内容分组表
 * @Author Amos
 * @Date 2023-05-22 18:54:37
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseArticleGroupMapper extends BaseMapper<BaseArticleGroupEntity> {


  /**
   * 选择所有文章分组信息
   *
   * 此方法用于获取数据库中所有的文章分组信息它返回一个包含所有文章分组的列表
   * 主要用于需要展示或处理所有文章分组信息的场景
   *
   * @return 包含所有文章分组的列表如果数据库中没有数据，则返回空列表
   */
  List<WebIndexArticleGroupVO> selectAllGroup();

  /**
   * 根据文章分组ID选择文章分组信息
   *
   * 此方法通过文章分组ID来查询特定的文章分组信息，返回匹配的WebIndexArticleGroupVO对象
   * 如果没有找到匹配的记录，则返回null主要用于需要根据特定分组ID获取详细信息的场景
   *
   * @param articleGroupId 文章分组ID，用于查询特定的文章分组信息
   * @return 匹配的文章分组信息对象如果没有找到匹配的记录，则返回null
   */
  WebIndexArticleGroupVO selectByArticleGroupId(@Param("articleGroupId")Integer articleGroupId);


    /**
     * 新增文章分组
     *
     * @param baseArticleGroupReq
     */
    void insertArticleGroup(@Param("param") BaseArticleGroupDTO baseArticleGroupReq);
 }

