package com.houjiang.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.IChannelOaReplyService;
import com.houjiang.merchant.validate.channel.ChannelRpValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.channel.ChannelRpVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.OfficialAccountReply;
import com.houjiang.merchant.common.mapper.OfficialAccountReplyMapper;
import com.houjiang.merchant.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 公众号默认回复服务实现类
 */
@Service
public class ChannelOaReplyServiceImpl implements IChannelOaReplyService {

    @Resource
    OfficialAccountReplyMapper officialAccountReplyMapper;

    /**
     * 默认回复列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @return PageResult<ChannelOaReplyVo>
     */
    @Override
    public PageResult<ChannelRpVo> list(PageValidate pageValidate, Integer replyType) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        IPage<OfficialAccountReply> iPage = officialAccountReplyMapper.selectPage(new Page<>(pageNo, pageSize),
                new QueryWrapper<OfficialAccountReply>()
                        .eq("reply_type", replyType)
                        .isNull("delete_time")
                        .orderByDesc(Arrays.asList("sort", "id")));

        List<ChannelRpVo> list = new LinkedList<>();
        for (OfficialAccountReply officialAccountReply : iPage.getRecords()) {
            ChannelRpVo vo = new ChannelRpVo();
            BeanUtils.copyProperties(officialAccountReply, vo);
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 默认回复详情
     *
     * @author Amos
     * @param id 主键
     * @return ChannelOaReplyDefaultVo
     */
    @Override
    public ChannelRpVo detail(Integer id) {
        OfficialAccountReply officialAccountReply = officialAccountReplyMapper.selectOne(new QueryWrapper<OfficialAccountReply>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(officialAccountReply, "默认数据不存在!");

        ChannelRpVo vo = new ChannelRpVo();
        BeanUtils.copyProperties(officialAccountReply, vo);
        return vo;
    }

    /**
     * 默认回复新
     *
     * @author Amos
     * @param defaultValidate 参数
     */
    @Override
    public void add(ChannelRpValidate defaultValidate) {

        if (defaultValidate.getReplyType().equals(3)) {
            if (defaultValidate.getStatus().equals(1)) {
                OfficialAccountReply reply = new OfficialAccountReply();
                reply.setStatus(0);
                officialAccountReplyMapper.update(reply, new QueryWrapper<OfficialAccountReply>().eq("reply_type", 3));
            }
        }

        if (defaultValidate.getReplyType().equals(2)) {
            Assert.isTrue(StringUtils.isNotEmpty(defaultValidate.getKeyword()), "关键词不能为空");
        }


        OfficialAccountReply officialAccountReply = new OfficialAccountReply();
        officialAccountReply.setReplyType(defaultValidate.getReplyType());
        officialAccountReply.setContent(defaultValidate.getContent());
        officialAccountReply.setName(defaultValidate.getName());
        officialAccountReply.setContentType(defaultValidate.getContentType());
        officialAccountReply.setStatus(defaultValidate.getStatus());
        officialAccountReply.setKeyword(defaultValidate.getKeyword());
        officialAccountReply.setMatchingType(defaultValidate.getMatchingType());
        officialAccountReply.setSort(defaultValidate.getSort());
        officialAccountReply.setUpdateTime(System.currentTimeMillis() / 1000);
        officialAccountReply.setCreateTime(System.currentTimeMillis() / 1000);
        officialAccountReplyMapper.insert(officialAccountReply);
    }

    /**
     * 默认回复编辑
     *
     * @author Amos
     * @param defaultValidate 参数
     */
    @Transactional
    @Override
    public void edit(ChannelRpValidate defaultValidate) {
        OfficialAccountReply officialAccountReply = officialAccountReplyMapper.selectOne(new QueryWrapper<OfficialAccountReply>()
                .eq("id", defaultValidate.getId())
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(officialAccountReply, "默认回复数据不存在!");

        if (defaultValidate.getReplyType().equals(3)) {
            if (defaultValidate.getStatus().equals(1)) {
                OfficialAccountReply reply = new OfficialAccountReply();
                reply.setStatus(0);
                officialAccountReplyMapper.update(reply, new QueryWrapper<OfficialAccountReply>().eq("reply_type", officialAccountReply.getReplyType()));
            }
        }

        if (defaultValidate.getReplyType().equals(2)) {
            Assert.isTrue(StringUtils.isNotEmpty(defaultValidate.getKeyword()), "关键词不能为空");
        }

        officialAccountReply.setId(defaultValidate.getId());
        officialAccountReply.setName(defaultValidate.getName());
        officialAccountReply.setContent(defaultValidate.getContent());
        officialAccountReply.setContentType(defaultValidate.getContentType());
        officialAccountReply.setStatus(defaultValidate.getStatus());
        officialAccountReply.setSort(defaultValidate.getSort());
        officialAccountReply.setKeyword(defaultValidate.getKeyword());
        officialAccountReply.setMatchingType(defaultValidate.getMatchingType());
//        officialAccountReply.setReplyType(defaultValidate.getReplyType());
        officialAccountReply.setCreateTime(System.currentTimeMillis() / 1000);
        officialAccountReply.setUpdateTime(System.currentTimeMillis() / 1000);
        officialAccountReplyMapper.updateById(officialAccountReply);
    }

    /**
     * 默认回复删除
     *
     * @author Amos
     * @param id 主键
     */
    @Override
    public void del(Integer id) {
        OfficialAccountReply officialAccountReply = officialAccountReplyMapper.selectOne(new QueryWrapper<OfficialAccountReply>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(officialAccountReply, "默认回复数据不存在了!");

        officialAccountReply.setDeleteTime(System.currentTimeMillis() / 1000);
        officialAccountReplyMapper.updateById(officialAccountReply);
    }

    /**
     * 默认回复状态
     *
     * @author Amos
     * @param id 主键
     */
    @Override
    public void status(Integer id) {
        OfficialAccountReply officialAccountReply = officialAccountReplyMapper.selectOne(new QueryWrapper<OfficialAccountReply>()
                .isNull("delete_time")
                .eq("id", id)
                .last("limit 1"));

        Assert.notNull(officialAccountReply, "默认回复数据不存在!");

        int status = officialAccountReply.getStatus() == 1 ? 0 : 1;
        if (status == 1) {
            OfficialAccountReply reply = new OfficialAccountReply();
            reply.setStatus(0);
            reply.setUpdateTime(System.currentTimeMillis() / 1000);
            officialAccountReplyMapper.update(reply, new QueryWrapper<OfficialAccountReply>()
                    .eq("reply_type", officialAccountReply.getReplyType()));
        }

        officialAccountReply.setId(id);
        officialAccountReply.setStatus(status);
        officialAccountReply.setUpdateTime(System.currentTimeMillis() / 1000);
        officialAccountReplyMapper.updateById(officialAccountReply);
    }

}
