package com.houjiang.goods.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.goods.mall.dto.MallGoodsCommentSearchDTO;
import com.houjiang.goods.mall.entity.MallGoods;
import com.houjiang.goods.mall.entity.MallGoodsComment;
import com.houjiang.goods.mall.mapper.MallGoodsCommentMapper;
import com.houjiang.goods.mall.mapper.MallGoodsMapper;
import com.houjiang.goods.mall.service.MallGoodsCommentService;
import com.houjiang.goods.mall.vo.MallGoodsCommentVO;
import com.houjiang.goods.mall.vo.PageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品评论 Service 实现
 * @author Amos
 */
@Service
public class MallGoodsCommentServiceImpl implements MallGoodsCommentService {

    @Autowired
    private MallGoodsCommentMapper commentMapper;
    @Autowired
    private MallGoodsMapper goodsMapper;

    @Override
    public PageVO<MallGoodsCommentVO> list(MallGoodsCommentSearchDTO s) {
        LambdaQueryWrapper<MallGoodsComment> qw = new LambdaQueryWrapper<>();
        qw.eq(MallGoodsComment::getDeleteTime, 0);
        if (s.getGoodsId() != null) qw.eq(MallGoodsComment::getGoodsId, s.getGoodsId());
        if (s.getShopId() != null) qw.eq(MallGoodsComment::getShopId, s.getShopId());
        if (s.getUserId() != null) qw.eq(MallGoodsComment::getUserId, s.getUserId());
        if (s.getScore() != null) qw.eq(MallGoodsComment::getScore, s.getScore());
        if (s.getIsShow() != null) qw.eq(MallGoodsComment::getIsShow, s.getIsShow());
        qw.orderByDesc(MallGoodsComment::getId);
        Page<MallGoodsComment> p = commentMapper.selectPage(new Page<>(
                s.getPageNo() == null ? 1 : s.getPageNo(),
                s.getPageSize() == null ? 15 : s.getPageSize()), qw);

        // 关联商品信息
        Set<Integer> ids = p.getRecords().stream().map(MallGoodsComment::getGoodsId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, MallGoods> goodsMap = new HashMap<>();
        if (!ids.isEmpty()) {
            List<MallGoods> goods = goodsMapper.selectList(
                    new LambdaQueryWrapper<MallGoods>().in(MallGoods::getId, ids));
            for (MallGoods g : goods) goodsMap.put(g.getId(), g);
        }

        List<MallGoodsCommentVO> rows = new ArrayList<>();
        for (MallGoodsComment c : p.getRecords()) {
            MallGoodsCommentVO vo = new MallGoodsCommentVO();
            BeanUtils.copyProperties(c, vo);
            if (c.getImages() != null && !c.getImages().isEmpty()) {
                vo.setImageList(Arrays.asList(c.getImages().split(",")));
            } else {
                vo.setImageList(Collections.emptyList());
            }
            MallGoods g = goodsMap.get(c.getGoodsId());
            if (g != null) {
                vo.setGoodsName(g.getName());
                vo.setGoodsImage(g.getImage());
            }
            rows.add(vo);
        }
        return new PageVO<>(p.getCurrent(), p.getSize(), p.getTotal(), rows);
    }

    @Override
    public void changeShow(Integer id) {
        MallGoodsComment c = commentMapper.selectById(id);
        if (c == null) return;
        commentMapper.update(null, new LambdaUpdateWrapper<MallGoodsComment>()
                .eq(MallGoodsComment::getId, id)
                .set(MallGoodsComment::getIsShow, c.getIsShow() != null && c.getIsShow() == 1 ? 0 : 1)
                .set(MallGoodsComment::getUpdateTime, (int) (System.currentTimeMillis() / 1000)));
    }

    @Override
    public void reply(Integer id, String content) {
        commentMapper.update(null, new LambdaUpdateWrapper<MallGoodsComment>()
                .eq(MallGoodsComment::getId, id)
                .set(MallGoodsComment::getIsReply, 1)
                .set(MallGoodsComment::getReplyContent, content == null ? "" : content)
                .set(MallGoodsComment::getUpdateTime, (int) (System.currentTimeMillis() / 1000)));
    }

    @Override
    public void del(Integer id) {
        int now = (int) (System.currentTimeMillis() / 1000);
        commentMapper.update(null, new LambdaUpdateWrapper<MallGoodsComment>()
                .eq(MallGoodsComment::getId, id)
                .set(MallGoodsComment::getDeleteTime, now)
                .set(MallGoodsComment::getUpdateTime, now));
    }
}
