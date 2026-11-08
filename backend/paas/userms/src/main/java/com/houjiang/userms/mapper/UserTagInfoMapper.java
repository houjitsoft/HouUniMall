package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.TagInfoPageDTO;
import com.houjiang.userms.entity.UserTagInfoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserTagInfo
 * @author Amos
 */
@Mapper
public interface UserTagInfoMapper extends BaseMapper<UserTagInfoEntity> {

    /**
     * 修改标签信息
     *
     * @param param
     */
    void updateTagInfo(TagInfoPageDTO param);

    /**
     * 新增标签信息
     *
     * @param param
     */
    void addTagInfo(TagInfoPageDTO param);



    /**
     * 修改标签
     *
     * @param tagClass
     */
    void updateTag(Map<String, Object> tagClass);
}




