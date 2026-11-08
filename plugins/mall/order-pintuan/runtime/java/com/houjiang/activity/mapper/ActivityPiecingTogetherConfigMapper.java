package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.activity.dto.PiecingTogetherConfigDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.activity.entity.ActivityPiecingTogetherConfigEntity;
import com.houjiang.activity.vo.GoodsPinTuanResultVO;
import com.houjiang.activity.vo.PinTuanConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.activity.entity.ActivityPiecingTogetherConfig
 * @author Amos
 */
@Mapper
public interface ActivityPiecingTogetherConfigMapper extends BaseMapper<ActivityPiecingTogetherConfigEntity> {

    /**
     * 新增拼团配置
     * @param param
     * @return
     */
    Integer h5ConfigModelAdd(@Param("param") PiecingTogetherConfigDTO param);

    /**
     * 修改拼团配置
     * @param param
     * @return
     */
    Integer updateH5Config(@Param("param")PiecingTogetherConfigDTO param);

    /**
     * 查询拼团配置
     * @return
     */
    List<PinTuanConfigVO> queryH5Config();

    /**
     * 查询拼团商品详情
     * @param commonid
     * @return
     */
    GoodsPinTuanResultVO queryPinTunGoods(@Param("commonId")String commonid);

    /**
     * h5 获取更多拼团商品
     * @param param
     * @return
     */
    IPage<GoodsPinTuanResultVO> queryPinTuanGoodsAll(Page<GoodsPinTuanResultVO> page, @Param("param")WebPinTuanParamDTO param);
}




