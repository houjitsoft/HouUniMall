package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.AdLiveRoomParamDTO;
import com.houjiang.base.dto.WxEstablishLiveRoomDTO;
import com.houjiang.base.entity.BaseWxLiveStudioEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName BaseWxLiveStudioMapper
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:57
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseWxLiveStudioMapper extends BaseMapper<BaseWxLiveStudioEntity> {

    /**
     * 新增直播间
     * @param param
     * @return
     */
    Integer addLiveRoom(@Param("param") AdLiveRoomParamDTO param);

    /**
     * 查询直播间
     * @param id
     * @return
     */
    WxEstablishLiveRoomDTO queryLiveRoom(@Param("id") Integer id);

    /**
     * 通过直播间id查询
     * @param shopId
     * @return
     */
    List<Integer> queryLiveRoomId(@Param("shopId") Integer shopId);
}
