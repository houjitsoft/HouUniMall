package com.houjiang.goods.controller;

import com.houjiang.goods.service.*;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName BaseController
 * @Description 基础controller
 * @Author Amos
 * @Date 2023/5/18 17:39
 * @Version 1.0
 * 
 * 【重要说明】Token验证机制：
 * 1. 前端请求必须携带Authorization头（格式：Bearer {token}）
 * 2. JWT过滤器（JwtAuthenticationTokenFilter）已经验证了token并设置了SecurityContext
 * 3. Controller中直接使用SecurityContextUtils.getCurrentUser()获取用户信息
 * 4. 禁止在Controller中手动验证token或调用ParseTokenUtils.getUserInfoByToken()
 * @author Amos
 */

public class BaseController {

    /**
     * 获取当前登录用户信息
     * 
     * 【重要】统一使用SecurityContextUtils获取用户信息
     * JWT过滤器已经验证了token，Controller直接使用即可
     * 
     * @return UserInfoVO 当前登录用户信息
     * @throws RuntimeException 如果用户未登录
     */
    public UserInfoVO getCurrentUser() {
        UserInfoVO currentUser = SecurityContextUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        return currentUser;
    }

    /**
     * 验证token并获取用户信息（已废弃，请使用getCurrentUser()）
     * 
     * @deprecated 请使用 getCurrentUser() 方法
     * @return UserInfoVO
     */
    @Deprecated
    public UserInfoVO validateTokenAndGetUser() {
        return getCurrentUser();
    }

    @Autowired
    protected ElasticSearchService elasticSearchService;

    @Autowired
    protected GoodsCommonService goodsCommonService;

    @Autowired
    protected GoodsBaseService goodsBaseService;

    @Autowired
    protected GoodsCatService goodsCatService;

    @Autowired
    protected GoodsUserFootprintService goodsUserFootprintService;

    @Autowired
    protected GoodsBrandCatService goodsBrandCatService;

    @Autowired
    protected GoodsBrandService goodsBrandService;

    @Autowired
    protected GoodsCommonAttributeAssociationService goodsCommonAttributeAssociationService;

    @Autowired
    protected GoodsAttributeService goodsAttributeService;

    @Autowired
    protected GoodsArticleInfoService goodsArticleInfoService;

    @Autowired
    protected GoodsArticleRemarkService goodsArticleRemarkService;

    @Autowired
    protected GoodsTagInfoService goodsTagInfoService;

    @Autowired
    protected GoodsTagClassInfoService goodsTagClassInfoService;

    @Autowired
    protected GoodsArticleLikeService goodsArticleLikeService;

    @Autowired
    protected GoodsArticleStarService goodsArticleStarService;

    @Autowired
    protected GoodsArticleUserFollowService goodsArticleUserFollowService;

    @Autowired
    protected GoodsTypeService goodsTypeService;

    @Autowired
    protected GoodsFoodExhibitionHallService goodsFoodExhibitionHallService;

    @Autowired
    protected GoodsDraftService goodsDraftService;
}
