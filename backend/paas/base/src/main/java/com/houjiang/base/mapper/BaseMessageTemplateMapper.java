package com.houjiang.base.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseMessageTemplateEntity;
import com.houjiang.commons.vo.base.BaseMessageTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseMessageTemplateMapper
 * @Description  消息模板表
 * @Author Amos
 * @Date 2024-09-11 14:19:51
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseMessageTemplateMapper extends BaseMapper<BaseMessageTemplateEntity> {

    /**
     * 根据模板类型选择信息模板
     *
     * @param templateId 模板类型，用于筛选信息模板
     * @return 返回一个信息模板的列表，如果无匹配结果，则返回空列表
     */
    BaseMessageTemplateVO selectListByTemplateId(@Param("templateId")Integer templateId);
}
