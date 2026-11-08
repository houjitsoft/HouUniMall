package com.houjiang.base.controller;


import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseSensitiveRecordController
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseSensitiveRecord")
public class BaseSensitiveRecordController extends BaseController{
    /**
     * 平台查询敏感词命中记录
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/query")
    public ResponseResult querybaseSensitiveRecord(@RequestBody BaseSensitiveRecordDTO param) {
        return baseSensitiveRecordService.querybaseSensitiveRecord(param);
    }

    /**
     * 添加敏感词命中记录
     * @param param
     * @return
     */
    @PostMapping("/addFeign")
    public ResponseResult addbaseSensitiveRecord(@RequestBody BaseSensitiveRecordDTO param) {
        return baseSensitiveRecordService.addbaseSensitiveRecord(param);
    }
}
