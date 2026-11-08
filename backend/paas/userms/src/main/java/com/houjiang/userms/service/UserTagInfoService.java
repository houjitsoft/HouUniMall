package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.MakeTagDTO;
import com.houjiang.userms.dto.TagInfoPageDTO;
import com.houjiang.userms.entity.UserTagInfoEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserTagInfoService extends IService<UserTagInfoEntity> {

    /**
     * 平台  删除会员标签
     *
     * @param dto
     * @return
     */
    ResponseResult deleteTagInfo(TagInfoPageDTO dto);


    /**
     * 平台  修改会员标签
     *
     * @param dto
     * @return
     */
    ResponseResult updateTagInfo(TagInfoPageDTO dto);


    /**
     * 平台  添加会员标签
     *
     * @param dto
     * @return
     */
    ResponseResult addTagInfo(TagInfoPageDTO dto);

    /**
     * 平台给 会员打标签
     *
     * @param dto
     * @return
     */
    ResponseResult makeUserTag(MakeTagDTO dto);


    /**
     * 平台查询会员标签
     *
     * @param map
     * @return
     */
    ResponseResult queryTagInfo(Map<String, Object> map);

}
