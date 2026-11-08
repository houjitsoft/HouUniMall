package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.activity.entity.ActivityPiecingTogetherBuyerEntity;
import com.houjiang.activity.vo.PintuanMarkMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ActivityPiecingTogetherBuyerMapper
 * @Description  购买人
 * @Author Amos
 * @Date 2023-06-01 11:58:24
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityPiecingTogetherBuyerMapper extends BaseMapper<ActivityPiecingTogetherBuyerEntity> {

     /**
      * 插入购买人
      * @param markMessage
      */
     void insertPintuanBuyer(@Param("param")PintuanMarkMessageVO markMessage);
 }
