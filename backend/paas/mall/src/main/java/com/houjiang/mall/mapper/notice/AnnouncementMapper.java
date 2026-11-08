package com.houjiang.mall.mapper.notice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.notice.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告 Mapper 接口
 * 
 * @author Amos
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
