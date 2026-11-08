package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsComment;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSearchValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetIsShowValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetReplyValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCommentListedVo;

/**
 * 商品评论服务接口类
 * @author Amos
 */
public interface IMallGoodsCommentService {

    /**
     * 商品评论列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCommentListedVo>
     */
    PageResult<MallGoodsCommentListedVo> list(PageValidate pageValidate, MallGoodsCommentSearchValidate searchValidate);


    /**
     * 评论回复
     * @param setReplyValidate
     */
    void setReply(MallGoodsCommentSetReplyValidate setReplyValidate);

    /**
     * 设置是否显示
     * @param setReplyValidate
     */
    void setIsShow(MallGoodsCommentSetIsShowValidate setReplyValidate);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallGoodsComment getById(Integer id);
}
