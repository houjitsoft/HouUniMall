package com.houjiang.userms.controller;


import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.MakeTagDTO;
import com.houjiang.userms.dto.TagInfoPageDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * (UserTagInfo)
 *
 * @since 2023-04-14 15:02:36
 * @author Amos
 */
@RestController
@RequestMapping("user/tagInfo")
public class UserTagInfoController extends BaseController{


    /**
     * 平台  删除会员标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/delete")
    @SysLog("删除会员标签")
    public ResponseResult deleteTagInfo(@RequestBody TagInfoPageDTO dto) {
        ResponseResult result = userTagInfoService.deleteTagInfo(dto);
        return result;
    }

    /**
     * 平台  修改会员标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/update")
    @SysLog("修改会员标签")
    public ResponseResult updateTagInfo(@RequestBody TagInfoPageDTO dto) {
        ResponseResult result = userTagInfoService.updateTagInfo(dto);
        return result;
    }

    /**
     * 平台  添加会员标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/add")
    @SysLog("添加会员标签")
    public ResponseResult addTagInfo(@RequestBody TagInfoPageDTO dto) {
        ResponseResult result = userTagInfoService.addTagInfo(dto);
        return result;
    }

    /**
     * 平台给 会员打标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/makeUserTag")
    @SysLog("给会员打标签")
    public ResponseResult makeUserTag(@RequestBody MakeTagDTO dto) {
        ResponseResult result = userTagInfoService.makeUserTag(dto);
        return result;
    }

    /**
     * 平台查询会员标签
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/query")
    public ResponseResult queryTagInfo(@RequestBody Map<String, Object> map) {
        ResponseResult result = userTagInfoService.queryTagInfo(map);
        return result;
    }
}

