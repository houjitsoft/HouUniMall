package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.order.dto.GoodsParaDTO;
import com.houjiang.order.dto.RiderEvaluationDTO;
import com.houjiang.order.dto.ShopParaDTO;
import com.houjiang.order.entity.OrderGoodsEvaluationEntity;
import com.houjiang.order.vo.CommentsEntityVO;
import com.houjiang.order.vo.GoodsCommentsVO;
import com.houjiang.order.vo.ShopCommentsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName OrderGoodsEvaluationMapper
 * @Description  商品评论表
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderGoodsEvaluationMapper extends BaseMapper<OrderGoodsEvaluationEntity> {

     /**
      *  查询评论总数
      * @param commonId
      * @return
      */
     Integer queryCommentsCount(Integer commonId);


    /**
     * 查询评论
     * @param page
     * @param map
     * @return
     */
    IPage<CommentsEntityVO> commentsQuery(IPage<CommentsEntityVO> page,@Param("map")Map<String, Object> map);

    /**
     * 根据条件查询评论
     * @param commonId
     * @param evaluation
     * @return
     */
    Integer getEvaluationTotalCount(@Param("commonId") Integer commonId, @Param("evaluation") String evaluation);

    /**
     * 注释按商品插入
     *
     * @param mapGoods
     * @return
     */
    boolean commentsInsertByGoods(Map<String, Object> mapGoods);

    /**
     * 平台查询评论
     */
    IPage<GoodsCommentsVO> selectGoodsComments(IPage<GoodsCommentsVO> page,@Param("param") GoodsParaDTO goodsPara);

    /**
     * 店铺动态评价
     */
    IPage<ShopCommentsVO> selectShopComments(IPage<ShopCommentsVO> page,@Param("param") ShopParaDTO shopPara);

    /**
     * 删除店铺动态评价
     */
    void deleteShopComments(@Param("map")Map<String,String> map);

    /**
     * 商家回复
     * @param map
     * @return
     */
    boolean updateForReply(@Param("param") Map<String, Object> map);

    /**
     * 商家回复追加评论
     * @param map
     * @return
     */
    boolean updateForAdditionalReply(@Param("param")  Map<String, Object> map);

    /**
     * 插入骑手评价信息。
     *
     * 本方法用于将骑手的评价信息插入到数据库中。通过接收一个RiderEvaluationDTO对象作为参数，
     * 将其中包含的评价信息存储到相应的数据表中。这有助于跟踪骑手的表现和服务质量，
     * 为平台提供数据支持以进行骑手的管理和激励。
     *
     * @param riderEvaluationDTO 包含骑手评价信息的数据传输对象。该对象应包含所有必要的评价细节，
     *                           如骑手ID、评价分数、评价评论等。
     * @return 返回插入操作的影响行数。通常情况下，如果插入成功，返回值为1；
     *         如果出现错误或插入失败，则返回值可能为0或其他指示错误的值。
     */
    int insertRiberEvaluated(@Param("param") RiderEvaluationDTO riderEvaluationDTO);
}
