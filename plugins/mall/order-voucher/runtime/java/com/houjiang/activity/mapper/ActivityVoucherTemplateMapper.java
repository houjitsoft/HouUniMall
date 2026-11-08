package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.activity.dto.GetAvailableVcBaseDTO;
import com.houjiang.activity.dto.JoinVoucherActivityDTO;
import com.houjiang.activity.dto.VoucherGoodsListDTO;
import com.houjiang.activity.dto.VoucherTemplateUpdateDTO;
import com.houjiang.activity.entity.ActivityVoucherTemplateEntity;
import com.houjiang.activity.vo.PlatformVoucherQueryVO;
import com.houjiang.activity.vo.ProductVoucherVO;
import com.houjiang.activity.vo.VoucherGoodsQueryVO;
import com.houjiang.activity.vo.HouVoucherTemplateVO;
import com.houjiang.commons.vo.goods.ProductVoucherGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.activity.entity.ActivityVoucherTemplate
 * @author Amos
 */
@Mapper
public interface ActivityVoucherTemplateMapper extends BaseMapper<ActivityVoucherTemplateEntity> {


    /**
     * 商家后台：查询优惠券信息
     *
     */
    IPage<HouVoucherTemplateVO> selectVoucherTemp(IPage<HouVoucherTemplateVO> page , @Param("param") Map<String, Object> paraMap);

    /**
     * 商家后台：查询优惠券信息总数
     *
     */
    Integer selectVoucherTempCount(Map<String, Object> paraMap);

    /**
     * 商家后台：删除代金券信息 1.1将删除代金券改为修改代金券状态
     * @param paraMap
     * @return
     */
    Integer deleteVoucherTemp(Map<String, String> paraMap);

    /**
     * 商家后台：插入代金券模板
     * @param param
     * @return
     */
    Integer insertVoucherTemplate(@Param("param") VoucherTemplateUpdateDTO param);

    /**
     * 查询可报名的优惠券活动
     * @return
     */
    List<PlatformVoucherQueryVO> selectPlatformVoucherList(@Param("shopId") Integer shopId);

    /**
     * 查询代金券商品数量
     * @param param
     * @return
     */
    Integer selectVoucherGoodsCount(@Param("param") JoinVoucherActivityDTO param);

    /**
     * 查询代金券店铺数量
     * @param param
     * @return
     */
    Integer selectVoucherShopCount(@Param("param") JoinVoucherActivityDTO param);

    /**
     * 新增代金券店铺
     * @param param
     */
    void insertVoucherShop(@Param("param") JoinVoucherActivityDTO param);

    /**
     * 修改代金券活动
     * @param goodsList
     */
    void updateVoucherActivity(@Param("param") List<VoucherGoodsListDTO> goodsList);

    /**
     * 批量修改代金券
     * @param goodsList
     */
    void updateVoucherTIdsBatch(@Param("param") List<VoucherGoodsListDTO> goodsList);

    /**
     * 分页查询代金券商品信息
     * @param page
     * @param param
     * @return
     */
    IPage<VoucherGoodsQueryVO> selectVoucherGoods(Page<VoucherGoodsQueryVO> page,@Param("param") JoinVoucherActivityDTO param);

    /**
     * 查询优惠券信息
     * @param param
     * @return
     */
    List<HouVoucherTemplateVO> selectFullReduceVcList(@Param("param")GetAvailableVcBaseDTO param);


    /**
     * 分页查询销售促销优惠券列表
     *
     * @param page
     * @param paraMap
     * @return
     */
    IPage<ActivityVoucherTemplateEntity> salesPromotionVoucherList(Page<ActivityVoucherTemplateEntity> page, @Param("param") Map<String, Object> paraMap);

    /**
     * 根据通用优惠券ID选择商品ID列表
     * 此方法用于查询与特定通用优惠券相关联的所有商品ID
     * 它通常用于购物车或商品筛选场景，以找出可以使用特定优惠券的商品
     *
     * @param voucherTId 通用优惠券ID，用于标识特定的优惠券
     * @return 返回一个包含商品ID的列表，这些商品与给定的通用优惠券相关联
     */
    List<Integer> selectGoodsIdByVoucherCommon(@Param("voucherTId") Integer voucherTId);

    /**
     * 查询兑换券活动列表
     * @param page
     * @param param
     * @return
     */
    IPage<ProductVoucherVO> queryProductVoucher(Page<ProductVoucherVO> page, @Param("param") Map<String, Object> param);
    /**
     * 查询兑换券活动列表
     * @param page
     * @param param
     * @return
     */
    IPage<ProductVoucherVO> queryAllProductVoucherActivity(Page<ProductVoucherVO> page, @Param("param") Map<String, Object> param);

    /**
     * 查询商品兑换券 详情
     * @param voucherTId
     * @return
     */
    ProductVoucherVO queryProductVoucherDetail(@Param("voucherTId") Integer voucherTId);

    /**
     * 查询商品兑换券 商品信息
     * @param page
     * @param param
     * @return
     */
    IPage<ProductVoucherGoodsVO> queryProductVoucherGoodsDetail(Page<ProductVoucherGoodsVO> page, @Param("param") VoucherTemplateUpdateDTO param);

    /**
     * 检查 兑换券是否有效
     * @param voucherTId
     * @return
     */
    Integer queryvoucherTemplateNum(@Param("voucherTId") Integer voucherTId);

    /**
     * 检查 兑换券是否适用当前商品
     * @param voucherTId
     * @param commonId
     * @return
     */
    Integer queryVoucherTemplateGoodsNum(@Param("voucherTId") Integer voucherTId, @Param("commonId")Integer commonId);


    /**
     * 更新状态
     *
     * @param param
     */
    void updateVoucherState(@Param("commonParam") Map<String, Object> param);
}




