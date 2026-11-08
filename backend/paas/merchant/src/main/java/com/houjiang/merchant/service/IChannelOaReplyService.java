package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.channel.ChannelRpValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.channel.ChannelRpVo;
import com.houjiang.merchant.common.core.PageResult;

/**
 * 公众号默认回复服务接口类
 */
public interface IChannelOaReplyService {

    /**
     * 默认回复列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @return PageResult<ChannelRpDefaultVo>
     */
    PageResult<ChannelRpVo> list(PageValidate pageValidate, Integer replyType);

    /**
     * 默认回复详情
     *
     * @author Amos
     * @param id 主键
     * @return ChannelRpDefaultVo
     */
    ChannelRpVo detail(Integer id);

    /**
     * 默认回复新增
     *
     * @author Amos
     * @param defaultValidate 参数
     */
    void add(ChannelRpValidate defaultValidate);

    /**
     * 默认回复编辑
     *
     * @author Amos
     * @param defaultValidate 参数
     */
    void edit(ChannelRpValidate defaultValidate);

    /**
     * 默认回复删除
     *
     * @author Amos
     * @param id 主键
     */
    void del(Integer id);

    /**
     * 默认回复状态
     *
     * @author Amos
     * @param id 主键
     */
    void status(Integer id);

}
