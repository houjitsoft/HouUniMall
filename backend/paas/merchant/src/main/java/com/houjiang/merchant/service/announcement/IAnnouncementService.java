package com.houjiang.merchant.service.announcement;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.announcement.AnnouncementSearchValidate;
import com.houjiang.merchant.validate.article.ArtCateCreateValidate;
import com.houjiang.merchant.validate.article.ArtCateSearchValidate;
import com.houjiang.merchant.validate.article.ArtCateUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.announcement.AnnouncementDetailVo;
import com.houjiang.merchant.vo.article.ArticleCateVo;

import java.util.List;

/**
 * 平台公告接口务类
 */
public interface IAnnouncementService {

    /**
     * 分类所有
     *
     * @author Amos
     * @return List<CategoryVo>
     */
    AnnouncementDetailVo newest();

    /**
     * @notes 获取平台公告详情
     * @param $params
     * @return array
     * @author Amos
     * @date 2024/12/21 15:04
     */
    AnnouncementDetailVo detail(Integer id);

    /**
     * @notes 获取平台公告列表
     * @return array
     * @author Amos
     * @date 2024/12/21 15:04
     */
    PageResult<AnnouncementDetailVo> list(PageValidate pageValidate, AnnouncementSearchValidate searchValidate);
}
