package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.WxChatIdParamDTO;
import com.houjiang.base.entity.BaseWxLiveHostEntity;
import com.houjiang.base.vo.QuLiveHostStateResultVO;
import com.houjiang.base.vo.WxHostsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseWxLiveHostMapper
 * @Description
 * @Author Amos
 * @Date 2023/7/24 13:39
 * @Version 1.0
 **/
@Mapper
public interface BaseWxLiveHostMapper extends BaseMapper<BaseWxLiveHostEntity> {

    /**
     * 查询直播间
     *
     * @param shopId
     * @return
     */
    QuLiveHostStateResultVO queryLiveHostState(@Param("shopId") Integer shopId);


    /**
     * @title 审核之后 更改主播状态
     * @description
     * @author Amos
     * @updateTime 2021/8/18 10:32
     * @throws
     */
    Integer updateHostState(@Param("id")Integer id,@Param("state") Integer state,@Param("cause") String cause);


    /**
     * 分页查询地址
     *
     * @param page
     * @param param
     * @return
     */
    IPage<WxHostsVO> queryHosts(IPage<WxHostsVO> page, @Param("param") WxChatIdParamDTO param);
}
