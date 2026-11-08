package com.houjiang.mall.controller.home;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.Notice;
import com.houjiang.mall.service.home.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公告快报控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/home/notice")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取首页快报列表（前台接口）
     */
    @GetMapping("/list")
    public ResponseResult getHomeNotices(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return noticeService.getHomeNotices(type, limit);
    }
    
    /**
     * 分页查询公告列表（后台管理）
     */
    @PostMapping("/page")
    public ResponseResult queryPage(@RequestBody Map<String, Object> params) {
        return noticeService.queryPage(params);
    }
    
    /**
     * 根据ID查询公告
     */
    @GetMapping("/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        return noticeService.getById(id);
    }
    
    /**
     * 查看公告详情（增加浏览次数）
     */
    @GetMapping("/view/{id}")
    public ResponseResult viewDetail(@PathVariable Long id) {
        return noticeService.viewDetail(id);
    }
    
    /**
     * 新增公告
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Notice notice) {
        return noticeService.add(notice);
    }
    
    /**
     * 更新公告
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Notice notice) {
        return noticeService.update(notice);
    }
    
    /**
     * 删除公告
     */
    @PostMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable Long id) {
        return noticeService.delete(id);
    }
}
