package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import com.houjiang.commons.vo.shop.RetentionShopInfoVO;
import com.houjiang.commons.vo.shop.ShopEvaluationVO;
import com.houjiang.order.entity.OrderComplaintsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderComplaintsMapper
 * @Description  用户投诉表
 * @Author Amos
 * @Date 2023-08-23 09:38:14
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderComplaintsMapper extends BaseMapper<OrderComplaintsEntity> {

    /**
     * 查询发货时效
     * @param commonid
     * @return
     */
    List<Map> querydeliverytime(@Param("list") List commonid);

    /**
     * 获取商家质保金余额
     * @param shopId
     * @return
     */
    Integer selectshop(Integer shopId);

    /**
     * 修改账户余额
     * @param manageMoney
     * @return
     */
    Integer updateCardMemberDetailPort(Map<String, Object> manageMoney);

    /**
     * 新增支付明细
     * @param assetDetailMap
     * @return
     */
    Integer insertDetail(@Param("param")Map<String, Object> assetDetailMap);

    /**
     * 判断当前订单是否属于当前用户 1 是 2 否
     * @param comRecord
     * @return
     */
    Integer insertConsumeRecordport(Map<String, Object> comRecord);

    /**
     * 查询质保金信息
     * @param shopId
     * @return
     */
    RetentionShopInfoVO selectretention(Integer shopId);

    /**
     * 查询质保金信息
     * @param shopId
     * @return
     */
    RetentionShopInfoVO selectretention2(Integer shopId);

    /**
     * 发送站内信
     * @param messageOrderId
     * @param sellerId
     * @param sellerName
     * @param messageType
     * @param messageTitle
     * @param messageContent
     * @param shopId
     * @return
     */
     boolean insertSysMessage1(@Param("messageOrderId") String messageOrderId,@Param("sellerId") String sellerId, @Param("sellerName") String sellerName,
                                     @Param("messageType") Integer messageType, @Param("messageTitle") String messageTitle,
                                     @Param("messageContent") String messageContent,@Param("shopId") String shopId);

    /**
     * 更新shopbase表
     * @param map
     */
    Integer updateshopbase(@Param("param") Map<Object, Object> map);

    /**
     * 更新投诉
     * @param map
     * @return
     */
    Integer updatecomplaints(@Param("param")Map<String, Object> map);

    /**
     * 添加投诉
     * @param map
     * @return
     */
    Integer addcomplaints(@Param("param")Map<String, Object> map);

    /**
     * 查询投诉单列表
     * @param page
     * @param map
     * @return
     */
    IPage<OrderComplaintsEntity> queryComplaints(IPage<OrderComplaintsEntity> page, @Param("param")Map<String, Object> map);

    /**
     * 查询投诉详情
     * @param map
     * @return
     */
    OrderComplaintsEntity complaintsdetail(@Param("param")Map<String, Object> map);

    /**
     * 撤销投诉单
     * @param userId
     * @param complaintsId
     * @return
     */
    Integer revokeComplaints(@Param("userId")String userId, @Param("complaintsId")String complaintsId);

    /**
     * 查询订单是否投诉过
     * @param map
     * @return
     */
    Map<String,Object> isComplaints(@Param("param") Map<String, Object> map);


    /**
     * 查询投诉列表
     * @param page
     * @param param
     * @return
     */
    IPage<OrderComplaintsEntity> queryComplaintsByPlatform(IPage<OrderComplaintsEntity> page , @Param("param") Map<String, Object> param);


    /**
     * 查询投诉详情
     * @param map
     * @return
     */
    OrderComplaintsEntity querycomplaintsdetail(@Param("param") Map<String,Object> map);


    /**
     *查询用户投诉信息
/**
     * @param id
     * @return
     */
    OrderComplaintsEntity querycomplaintsdetail1(@Param("param") String id);

    /**
     * 查询商品信息
     * @param map
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList(@Param("param") Map<String, Object> map);

    /**
     * 查询店铺评分
     * @param shopId
     * @return
     */
    ShopEvaluationVO ShopScore(@Param("shopId") Integer shopId);


    /**
     * 修改质保金余额
     * @param map
     * @return
     */
    Integer handlingcomplaints(@Param("param") Map<String, Object> map);

    /**
     * 查询订单撤销投诉的数量。
     * 本函数通过调用数据库查询特定订单和用户下的撤销投诉数量。
     * @param orderId 订单标识符，用于确定查询的订单。
     * @param userId 用户标识符，用于确定查询的用户。
     * @return 返回Integer类型的结果，表示撤销投诉的数量。
     */
    Integer queryOrderRevokeComplaints(@Param("orderId") String orderId,@Param("userId")  String userId);


    /**
     * 查询订单撤销投诉的数量。
     * @param map
     * @return
     */
    String querymonthFine(@Param("param") Map<String, Object> map);


    /**
     * 添加投诉
     * @param map
     * @return
     */

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer addComplaints(@Param("param") Map<String,Object> map);


    /**
     * 修改投诉单赔付状态
     * @param id
     */
    void updateispay(@Param("id")Integer id);


    /**
     * 修改投诉单赔付状态
     * @param map
     * @return
     */
    Integer handlingComplaints(@Param("param") Map<String, Object> map);


    /**
     * 查询未赔付投诉单
     * @param shopId
     * @return
     */
    List<OrderComplaintsVO> unpaidcomplaints(@Param("shopId")String shopId);

    /**
     *  查询未赔付投诉单总数
     * @param shopId
     * @param complaintsStatus
     * @return
     */
    Integer selectretentiontotal(@Param("shopId") String shopId,@Param("complaintsStatus")Integer complaintsStatus);


    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    OrderComplaintsVO queryComplaintsId(@Param("complaintsId") String complaintsId);


    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    OrderComplaintsVO queryComplaintsId1(@Param("complaintsId") Integer complaintsId);


    /**
     * 查询
     * @param complaintsId
     * @return
     */
    Map<String, Object> selectOrderId(@Param("complaintsId")  String complaintsId);


    /**
     * 查询
     * @param shopId
     * @return
     */
    String queryAllFine(@Param("shopId") String shopId);
}
