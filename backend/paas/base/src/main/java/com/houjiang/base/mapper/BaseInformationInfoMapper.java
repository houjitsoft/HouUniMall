package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseInformationInfoDTO;
import com.houjiang.base.entity.BaseInformationInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资讯文章表(BaseInformationInfo)表数据库访问层
 *
 * @author Amos
 * @since 2024-01-18 15:31:10
 */
@Mapper
public interface BaseInformationInfoMapper extends BaseMapper<BaseInformationInfoEntity> {


    /**
     * 根据平台查询信息
     *
     * @param page 分页对象，用于定义查询的页码和每页大小
     * @param param 查询参数对象，封装了查询条件
     * @return 返回一个分页对象，包含查询到的信息实体
     */
    IPage<BaseInformationInfoEntity> queryByPlatform(Page<BaseInformationInfoEntity> page, @Param("param") BaseInformationInfoDTO param);

    /**
     * 根据分类ID列表查询信息数量
     *
     * @param classifyIdList 分类ID列表，用于指定需要统计的分类
     * @return 返回属于指定分类的信息数量
     */
    Integer selectInformationCountByClassifyIds(@Param("param") List<Integer> classifyIdList);

    /**
     * 根据信息ID删除信息
     *
     * @param informationId 信息ID，用于指定需要删除的信息
     */
    void deleteInformationInfoById(@Param("informationId") Integer informationId);

    /**
     * 用户查询信息
     *
     * @return 返回一个包含信息实体的列表，用于展示给用户
     */
    List<BaseInformationInfoEntity> userQueryInformationInfo();

    /**
     * 用户根据ID查询信息
     *
     * @param page 分页对象，用于定义查询的页码和每页大小
     * @param param 查询参数对象，封装了查询条件
     * @return 返回一个分页对象，包含查询到的信息实体
     */
    IPage<BaseInformationInfoEntity> userQueryInformationInfoById(Page<BaseInformationInfoEntity> page, @Param("param") BaseInformationInfoDTO param);

    /**
     * 根据ID更新信息的页面查看次数
     *
     * @param param 更新参数对象，包含需要更新的信息ID和新的页面查看次数
     */
    void updateInformationPageViewById(@Param("param") BaseInformationInfoDTO param);

    /**
     * 查询底部信息
     *
     * @param param 查询参数对象，封装了查询条件
     * @return 返回一个包含信息实体的列表，这些信息将展示在页面底部
     */
    List<BaseInformationInfoEntity> queryInformationInfoBottom(@Param("param") BaseInformationInfoDTO param);
}
