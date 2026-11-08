package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.activity.dto.GetAvailableVcBaseDTO;
import com.houjiang.activity.dto.UserGetVoucherParamDTO;
import com.houjiang.activity.dto.UserVcBaseDTO;
import com.houjiang.activity.dto.VoucherTemplateDTO;
import com.houjiang.activity.entity.ActivityVoucherBaseEntity;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.vo.activity.VoucherBaseVO;
import com.houjiang.commons.vo.activity.HouVoucherBaseVO;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import com.houjiang.commons.vo.shop.ShopUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityVoucherBaseMapper
 * @Description 代金券表
 * @Author Amos
 * @Date 2023-05-25 16:55:50
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityVoucherBaseMapper extends BaseMapper<ActivityVoucherBaseEntity> {

    /**
     * 根据店铺id查询代金券信息
     * @param param
     * @return
     */
    List<HouVoucherTemplateVO> selectVoucherTempByShopId(@Param("param") GetAvailableVcBaseDTO param);

    /**
     * 判断代金券是否领取过
     * @param param
     * @return
     */
    Integer checkReceivedOrNot(@Param("param") GetAvailableVcBaseDTO param);

    /**
     * 代金卷优惠
     *
     * @param map
     * @return
     */
    VoucherBaseVO getVoucherDetail(@Param("param") Map<String, Object> map);
    /**
     * 查询goodsid
     *
     * @param voucherId
     * @return
     */
    List<Integer> selectAvaliableGoods(@Param("voucherId") Integer voucherId);

    /**
     * 更新优惠券状态
     * @param voucherMap
     * @return
     */
    Integer updateVoucherDetail(Map<String, String> voucherMap);

    /**
     * 查询优惠券
     * @param checkMap
     * @return
     */
    List<HouVoucherBaseVO> selectVcBase(@Param("param")Map<String, Object> checkMap);

    /**
     * 判断优惠券是否领取过
     * @param param
     * @return
     */
    Integer checkVoucherOrNot(@Param("param")UserGetVoucherParamDTO param);
    /**
     * 查询优惠券信息
     */
    UserGetVoucherUpdateParamVO selectVcTemById(@Param("param")UserGetVoucherParamDTO param);

    /**
     * 插入用户领取信息
     * @param voucherTemplate
     */
    void insertyfVoucherBase(@Param("param")UserGetVoucherUpdateParamVO voucherTemplate);

    /**
     * 更新优惠券数量
     * @param param
     */
    void updateVoucherGiveout(@Param("param") UserGetVoucherParamDTO param);

    /**
     * 查询基础店铺
     *
     * @param page
     * @param param
     * @return
     */
    IPage<VoucherBaseVO> selectVcBaseByShopBase(Page<VoucherBaseVO> page, @Param("param") UserVcBaseDTO param);

    /**
     * 查询优惠券列表
     * @param param
     * @return
     */
    List<Integer> selectVoucherTIds(@Param("param")GetAvailableVcBaseDTO param);

    /**
     * 查询红包领取状态
     * @param param
     * @return
     */
    List<CheckReceivedVcOrNotResultVO> checkReceivedVcOrNot(@Param("param")GetAvailableVcBaseDTO param);

    /**
     * 查询优惠券信息
     * @param paraMap
     * @return
     */
    IPage<QueryShopVoucherTempResultVO> queryShopVoucherTempPort(IPage<QueryShopVoucherTempResultVO> page, @Param("param")Map<String, Object> paraMap);

    /**
     * 删除基础代金券
     *
     * @param paraMap
     */
    void deleteVoucherBase(Map<String, String> paraMap);

    /**
     * 根据优惠券ID查询商品列表
     *
     * @param voucherTId 优惠券模板ID
     * @return 返回包含商品信息的列表
     */
    List<GdsListQueryResultVO> queryShopVoucherGoodsById(Integer voucherTId);

    /**
     * 查询商品优惠券基础信息
     *
     * @param commonId 商品公共ID
     * @param goodOrderPrice 商品订单价格
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @param voucherIdList 优惠券ID列表
     * @return 返回包含优惠券基础信息的列表
     */
    List<HouVoucherBaseVO> selectGoodsVcBase(@Param("commonId") Integer commonId,@Param("goodOrderPrice") Double goodOrderPrice,
                                            @Param("userId") String userId,@Param("shopId") String shopId,
                                            @Param("voucherIdList") List<Integer> voucherIdList);
    /**
     * 根据用户信息  插入用户兑换券信息
     * @param userList
     * @param voucherBase
     */
    void addProductVoucherList(@Param("userList") List<VoucherTemplateDTO> userList, @Param("param") ActivityVoucherBaseEntity voucherBase);

    /**
     * 失效优惠券
     * @param paraMap
     */
    void updateVoucherstart(@Param("param") Map<String, String> paraMap);

    /**
     * 查询兑换券 会员信息
     * @param voucherTId
     * @return
     */
    List<ShopUserVO> queryShopVoucherUser(@Param("voucherTId") Integer voucherTId);

    /**
     *  查询当前 用户是否允许使用兑换券
     * @param voucherTId
     * @param userId
     * @return
     */
    Integer queryUserroductVoucherNum(@Param("voucherTId") Integer voucherTId, @Param("userId") String userId);

    /**
     * 查询用户可用的兑换券id
     * @param voucherTId
     * @param userId
     * @return
     */
    Integer queryUserVoucherId(@Param("voucherTId") Integer voucherTId, @Param("userId")String userId);

    /**
     * 修改商品兑换券状态
     * @param paraMap
     */
    void updateProductVoucher(@Param("param") Map<String, Object> paraMap);

    /**
     * 更新基础状态
     *
     * @param param
     */
    void updateVoucherBaseState(@Param("commonParam") Map<String, Object> param);
}
