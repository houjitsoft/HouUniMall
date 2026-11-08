package com.houjiang.mall.controller.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.notice.Announcement;
import com.houjiang.mall.service.notice.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统公告 Controller
 * 路径：/mall/notice/announcement/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/notice/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取最新公告
     * POST /mall/notice/announcement/latest
     * 
     * @param params 查询参数（limit: 数量限制）
     * @return 最新公告
     */
    @PostMapping("/latest")
    public ResponseResult<Announcement> getNotice(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取最新公告，参数：{}", params);
            Announcement announcement = announcementService.getLatest();
            
            if (announcement == null) {
                // 没有公告也返回成功
                return ResponseResult.success(null);
            }
            
            return ResponseResult.success(announcement);
        } catch (Exception e) {
            log.error("获取最新公告失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取公告列表（分页）
     * POST /mall/notice/announcement/list
     * 
     * @param params 查询参数
     * @return 公告列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<Announcement>> NoticeLists(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取公告列表，参数：{}", params);
            IPage<Announcement> page = announcementService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("获取公告列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     * POST /mall/notice/announcement/detail
     * 
     * @param params 公告 ID
     * @return 公告详情
     */
    @PostMapping("/detail")
    public ResponseResult<Announcement> NoticeDetial(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取公告详情，参数：{}", params);
            
            if (!params.containsKey("id")) {
                return ResponseResult.error("缺少公告 ID 参数");
            }
            
            Long id = Long.parseLong(params.get("id").toString());
            Announcement announcement = announcementService.getDetail(id);
            
            if (announcement == null) {
                return ResponseResult.error("公告不存在");
            }
            
            return ResponseResult.success(announcement);
        } catch (Exception e) {
            log.error("获取公告详情失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存公告（新增或编辑）
     * POST /mall/notice/announcement/save
     *
     * @param announcement 公告实体（不带 id 为新增，带 id 为编辑）
     * @return 保存结果
     */
    @PostMapping("/save")
    public ResponseResult saveAnnouncement(@RequestBody Announcement announcement) {
        try {
            log.info("保存公告，参数：{}", announcement);
            boolean result = announcementService.saveAnnouncement(announcement);
            return result ? ResponseResult.success("保存成功") : ResponseResult.error("保存失败");
        } catch (Exception e) {
            log.error("保存公告失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除公告
     * POST /mall/notice/announcement/delete
     *
     * @param params 参数（id）
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteAnnouncement(@RequestBody Map<String, Object> params) {
        try {
            log.info("删除公告，参数：{}", params);
            if (!params.containsKey("id")) {
                return ResponseResult.error("缺少公告 ID 参数");
            }
            Long id = Long.parseLong(params.get("id").toString());
            boolean result = announcementService.removeAnnouncement(id);
            return result ? ResponseResult.success("删除成功") : ResponseResult.error("删除失败");
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return ResponseResult.error("删除失败：" + e.getMessage());
        }
    }
}
