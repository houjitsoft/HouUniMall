package com.houjiang.mall.service.notice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.notice.Announcement;
import com.houjiang.mall.mapper.notice.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 系统公告 Service
 * 
 * @author Amos
 */
@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {
    
    /**
     * 分页查询公告列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<Announcement> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer type = params.get("type") != null ? Integer.parseInt(params.get("type").toString()) : null;
        
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 按类型筛选
        wrapper.eq(type != null, Announcement::getType, type);
        
        // 置顶优先，然后按发布时间降序
        wrapper.orderByDesc(Announcement::getIsTop)
               .orderByDesc(Announcement::getPublishTime)
               .orderByDesc(Announcement::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 获取最新公告
     * @return 最新的公告
     */
    public Announcement getLatest() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 置顶优先，然后按发布时间降序
        wrapper.orderByDesc(Announcement::getIsTop)
               .orderByDesc(Announcement::getPublishTime)
               .orderByDesc(Announcement::getCreateTime);
        
        // 只取第一条
        List<Announcement> list = this.list(wrapper);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
    
    /**
     * 根据 ID 查询公告详情
     * @param id 公告 ID
     * @return 公告实体
     */
    public Announcement getDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 保存公告（新增或编辑）
     * 根据 id 是否存在自动区分创建 / 更新
     * @param announcement 公告实体
     * @return 是否成功
     */
    public boolean saveAnnouncement(Announcement announcement) {
        return this.saveOrUpdate(announcement);
    }

    /**
     * 删除公告（物理删除，表无 @TableLogic）
     * @param id 公告 ID
     * @return 是否成功
     */
    public boolean removeAnnouncement(Long id) {
        return this.removeById(id);
    }
}
