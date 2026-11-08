package com.houjiang.mall.service.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.Notice;
import com.houjiang.mall.mapper.home.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公告快报服务类
 * 
 * @author Amos
 */
@Service
public class NoticeService {
    
    @Autowired
    private NoticeMapper noticeMapper;
    
    /**
     * 获取首页快报列表（前台接口）
     * 
     * @param noticeType 类型：1-快报 2-公告 3-通知
     * @param limit 限制数量
     * @return 公告列表
     */
    public ResponseResult getHomeNotices(Integer noticeType, Integer limit) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1);
        
        if (noticeType != null) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        
        wrapper.and(w -> w.isNull(Notice::getStartTime)
                       .or().le(Notice::getStartTime, LocalDateTime.now()))
               .and(w -> w.isNull(Notice::getEndTime)
                       .or().ge(Notice::getEndTime, LocalDateTime.now()))
               .orderByDesc(Notice::getIsTop)
               .orderByDesc(Notice::getCreateTime);
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        
        List<Notice> notices = noticeMapper.selectList(wrapper);
        return ResponseResult.success(notices);
    }
    
    /**
     * 分页查询公告列表（后台管理）
     * 
     * @param params 查询参数
     * @return 分页结果
     */
    public ResponseResult queryPage(Map<String, Object> params) {
        int pageNum = params.get("pageNum") != null ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;
        
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        
        if (params.get("status") != null) {
            wrapper.eq(Notice::getStatus, params.get("status"));
        }
        if (params.get("noticeType") != null) {
            wrapper.eq(Notice::getNoticeType, params.get("noticeType"));
        }
        if (params.get("shopId") != null) {
            wrapper.eq(Notice::getShopId, params.get("shopId"));
        }
        if (params.get("keyword") != null) {
            wrapper.like(Notice::getTitle, params.get("keyword"));
        }
        
        wrapper.orderByDesc(Notice::getIsTop).orderByDesc(Notice::getCreateTime);
        
        Page<Notice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResponseResult.success(page);
    }
    
    /**
     * 根据ID查询公告
     * 
     * @param id 公告ID
     * @return 公告信息
     */
    public ResponseResult getById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return ResponseResult.fail(404, "公告不存在");
        }
        return ResponseResult.success(notice);
    }
    
    /**
     * 查看公告详情（增加浏览次数）
     * 
     * @param id 公告ID
     * @return 公告信息
     */
    public ResponseResult viewDetail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return ResponseResult.fail(404, "公告不存在");
        }
        // 增加浏览次数
        notice.setViewCount(notice.getViewCount() + 1);
        noticeMapper.updateById(notice);
        return ResponseResult.success(notice);
    }
    
    /**
     * 新增公告
     * 
     * @param notice 公告实体
     * @return 操作结果
     */
    public ResponseResult add(Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        if (notice.getViewCount() == null) {
            notice.setViewCount(0);
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        noticeMapper.insert(notice);
        return ResponseResult.success(notice.getId());
    }
    
    /**
     * 更新公告
     * 
     * @param notice 公告实体
     * @return 操作结果
     */
    public ResponseResult update(Notice notice) {
        if (notice.getId() == null) {
            return ResponseResult.fail(400, "ID不能为空");
        }
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return ResponseResult.success();
    }
    
    /**
     * 删除公告
     * 
     * @param id 公告ID
     * @return 操作结果
     */
    public ResponseResult delete(Long id) {
        noticeMapper.deleteById(id);
        return ResponseResult.success();
    }
}
