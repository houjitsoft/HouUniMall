package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsComment;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.server.Sys;
import com.houjiang.merchant.common.enums.MallGoodsCommentEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallGoodsCommentMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsCommentService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSearchValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetIsShowValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetReplyValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCommentJoinListedVo;
import com.houjiang.merchant.vo.mall.MallGoodsCommentListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 商品评论实现类
 * @author Amos
 */
@Service
public class MallGoodsCommentServiceImpl implements IMallGoodsCommentService {
        
    @Resource
    MallGoodsCommentMapper mallGoodsCommentMapper;

    /**
     * 商品评论列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCommentListedVo>
     */
    @Override
    public PageResult<MallGoodsCommentListedVo> list(PageValidate pageValidate, MallGoodsCommentSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        MPJQueryWrapper<MallGoodsComment> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.setAlias("gc");
        mpjQueryWrapper.leftJoin("?_user u ON u.id=gc.user_id".replace("?_", GlobalConfig.tablePrefix));
        mpjQueryWrapper.leftJoin("?_mall_order_goods og ON og.id=gc.order_goods_id".replace("?_", GlobalConfig.tablePrefix));
        mpjQueryWrapper.eq("gc.delete_time", 0);
        mpjQueryWrapper.eq("gc.shop_id", MerchantThreadLocal.getShopId());
        mpjQueryWrapper.orderByDesc("gc.id");

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                mpjQueryWrapper.ge("gc.create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                mpjQueryWrapper.le("gc.create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        if (StringUtils.isNotNull(searchValidate.getIs_show())) {
            mpjQueryWrapper.eq("gc.is_show", searchValidate.getIs_show());
        }

        if (StringUtils.isNotNull(searchValidate.getKey_word_type()) && StringUtils.isNotEmpty(searchValidate.getKey_word())) {
            // 商品名称
            if (searchValidate.getKey_word_type().equals(1)) {
                mpjQueryWrapper.like("og.goods_name", searchValidate.getKey_word());
            }
            // 用户编号
            if (searchValidate.getKey_word_type().equals(2)) {
                mpjQueryWrapper.like("u.sn", searchValidate.getKey_word());
            }
            // 用户昵称
            if (searchValidate.getKey_word_type().equals(3)) {
                mpjQueryWrapper.like("u.nickname", searchValidate.getKey_word());
            }
        }

        if (StringUtils.isNotEmpty(searchValidate.getLevel())) {
            List<Integer> score = MallGoodsCommentEnum.getScoreByLevel(searchValidate.getLevel());
            mpjQueryWrapper.ge("gc.score", score.get(0));
            if (score.size() == 2) {
                mpjQueryWrapper.le("gc.score", score.get(1));
            }
        }

        MPJQueryWrapper<MallGoodsComment> hasReplyWrapper = mpjQueryWrapper.clone();
        MPJQueryWrapper<MallGoodsComment> isReplyWrapper = mpjQueryWrapper.clone();
        MPJQueryWrapper<MallGoodsComment> allReplyWrapper = mpjQueryWrapper.clone();

        if (StringUtils.isNotNull(searchValidate.getIs_reply())) {
            mpjQueryWrapper.eq("gc.is_reply", searchValidate.getIs_reply());
        }
        Long allReply = mallGoodsCommentMapper.selectJoinCount(allReplyWrapper);
        Long hasReply = mallGoodsCommentMapper.selectJoinCount(hasReplyWrapper.eq("is_reply", 1));
        Long isReply = mallGoodsCommentMapper.selectJoinCount(isReplyWrapper.eq("is_reply", 0));
        mpjQueryWrapper.select("gc.id, gc.goods_id, gc.goods_sku_id, gc.create_time, gc.score, gc.content, gc.is_reply, gc.reply_content, gc.is_show, gc.describe_score, gc.service_score, gc.logistics_score, gc.images,u.nickname, u.sn as user_sn, og.goods_name, og.goods_image, og.goods_sku_value");
        IPage<MallGoodsCommentJoinListedVo> iPage = mallGoodsCommentMapper.selectJoinPage(new Page<>(page, limit), MallGoodsCommentJoinListedVo.class, mpjQueryWrapper);
        List<MallGoodsCommentListedVo> list = new LinkedList<>();
        for(MallGoodsCommentJoinListedVo item : iPage.getRecords()) {
            MallGoodsCommentListedVo vo = new MallGoodsCommentListedVo();
            BeanUtils.copyProperties(item, vo);
            JSONArray imagesArray = new JSONArray();
            for (Object imageItem : JSONArray.parseArray(item.getImages())) {
                imagesArray.add(UrlUtils.toShopAbsoluteUrl(imageItem.toString()));
            }
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setGoodsSkuValue(JSONArray.parseArray(item.getGoodsSkuValue()));
            vo.setImages(imagesArray);
            vo.setLevel(MallGoodsCommentEnum.getLevelAttr(item.getScore()));
            vo.setLevelText(MallGoodsCommentEnum.getLevelTextAttr(item.getScore()));
            list.add(vo);
        }

        Map<String,Object> extend = new HashMap<>();
        extend.put("all", allReply);
        extend.put("has_reply", hasReply);
        extend.put("is_reply", isReply);
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, extend);
    }

    @Override
    @Transactional
    public void setReply(MallGoodsCommentSetReplyValidate setReplyValidate) {
        MallGoodsComment comment = this.getById(setReplyValidate.getId());
        if (StringUtils.isNull(comment)) {
            throw new OperateException("找不到该商品评价信息");
        }
        comment.setIsReply(YesNoEnum.YES.getCode());
        comment.setUpdateTime(System.currentTimeMillis() / 1000);
        comment.setReplyContent(setReplyValidate.getReplyContent());
        mallGoodsCommentMapper.updateById(comment);
    }

    @Override
    public void setIsShow(MallGoodsCommentSetIsShowValidate setReplyValidate) {
        MallGoodsComment comment = this.getById(setReplyValidate.getId());
        if (StringUtils.isNull(comment)) {
            throw new OperateException("找不到该商品评价信息");
        }
        comment.setIsShow(setReplyValidate.getIsShow());
        comment.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGoodsCommentMapper.updateById(comment);
    }

    @Override
    public MallGoodsComment getById(Integer id) {
        MallGoodsComment comment = mallGoodsCommentMapper.selectOne(new QueryWrapper<MallGoodsComment>().eq("id", id).eq("delete_time", 0).eq("shop_id", MerchantThreadLocal.getShopId()));
        return comment;
    }
}
