package com.houjiang.job.mapper;

import com.houjiang.job.vo.QuWxGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName WxLiveHostMapper
 * @Description 微信主机
 * @Author Amos
 * @Date 2023/5/17 9:54
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface WxLiveHostMapper {

    /**
     * 上传商品验证
     * @param isverify
     * @param goodsIds
     * @param cause
     * @return
     */
    Integer upLiveGoodsiIsverify(@Param("isverify") Integer isverify, @Param("goodsIds") List<QuWxGoodsVO> goodsIds, @Param("cause") String cause);

}
