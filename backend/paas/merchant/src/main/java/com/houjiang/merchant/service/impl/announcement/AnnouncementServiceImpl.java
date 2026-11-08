package com.houjiang.merchant.service.impl.announcement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.announcement.Announcement;
import com.houjiang.merchant.common.entity.article.Article;
import com.houjiang.merchant.common.entity.article.ArticleCate;
import com.houjiang.merchant.common.enums.AnnouncementEnum;
import com.houjiang.merchant.common.mapper.announcement.AnnouncementMapper;
import com.houjiang.merchant.common.mapper.article.ArticleCateMapper;
import com.houjiang.merchant.common.mapper.article.ArticleMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.IArtCateService;
import com.houjiang.merchant.service.announcement.IAnnouncementService;
import com.houjiang.merchant.validate.announcement.AnnouncementSearchValidate;
import com.houjiang.merchant.validate.article.ArtCateCreateValidate;
import com.houjiang.merchant.validate.article.ArtCateSearchValidate;
import com.houjiang.merchant.validate.article.ArtCateUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.announcement.AnnouncementDetailVo;
import com.houjiang.merchant.vo.article.ArticleCateVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文章分类服务实现类
 * @author Amos
 */
@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

    @Resource
    AnnouncementMapper announcementMapper;

    @Override
    public AnnouncementDetailVo newest() {
        Announcement announcement = announcementMapper.selectOne(new QueryWrapper<Announcement>().eq("delete_time", 0).orderByDesc("id").last("limit 1"));
        AnnouncementDetailVo vo = new AnnouncementDetailVo();
        if (announcement == null) {
            return vo;
        }
        BeanUtils.copyProperties(announcement, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(announcement.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(announcement.getUpdateTime()));
        vo.setPriorityName(AnnouncementEnum.getPriorityTextArr(announcement.getPriority()));
        return vo;
    }

    @Override
    public AnnouncementDetailVo detail(Integer id) {
        Announcement announcement = announcementMapper.selectOne(new QueryWrapper<Announcement>().eq("id", id).eq("delete_time", 0));
        Assert.notNull(announcement, "公告不存在");
        AnnouncementDetailVo vo = new AnnouncementDetailVo();
        BeanUtils.copyProperties(announcement, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(announcement.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(announcement.getUpdateTime()));
        vo.setPriorityName(AnnouncementEnum.getPriorityTextArr(announcement.getPriority()));
        return vo;
    }

    @Override
    public PageResult<AnnouncementDetailVo> list(PageValidate pageValidate, AnnouncementSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0)
                .orderByDesc(Arrays.asList("id"));

        announcementMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:title:str"
        });

        IPage<Announcement> iPage = announcementMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        List<AnnouncementDetailVo> list = new ArrayList<>();
        for (Announcement item : iPage.getRecords()) {
            AnnouncementDetailVo vo = new AnnouncementDetailVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setPriorityName(AnnouncementEnum.getPriorityTextArr(item.getPriority()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }
}
