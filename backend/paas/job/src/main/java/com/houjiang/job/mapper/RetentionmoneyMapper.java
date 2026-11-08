package com.houjiang.job.mapper;

import com.houjiang.commons.dto.goods.PlatFormGoodsStateDTO;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.goods.PlatFormGoodsCatsVO;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import com.houjiang.commons.vo.payment.CardMemberservicesVO;
import com.houjiang.commons.vo.shop.RetentionShopInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName RetentionmoneyMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/4 13:20
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface RetentionmoneyMapper {

    /**
     * 查询商家未处理投诉单
     * @param date
     * @return
     */
    List<OrderComplaintsVO> querycomplaintsstatus(@Param("date") Date date);


    /**
     * 发送短信通知
     * @param messageOrderId
     * @param sellerId
     * @param sellerName
     * @param messageType
     * @param messageTitle
     * @param messageContent
     * @param shopId
     * @return
     */
    boolean insertSysMessage(@Param("messageOrderId") String messageOrderId,@Param("sellerId") String sellerId, @Param("sellerName") String sellerName,
                             @Param("messageType") Integer messageType, @Param("messageTitle") String messageTitle
            , @Param("messageContent") String messageContent,@Param("shopId") String shopId);


    /**
     * 商家超时未处理投诉单
     * @return
     */
    List<OrderComplaintsVO> merchantprocessingouttime();


    /**
     * 查询质保金信息
     * @param shopId
     * @return
     */
    RetentionShopInfoVO selectretention(Integer shopId);

    /**
     *修改投诉单赔付状态
/**
     * @param status
     * @param id
     * @return
     */
    Integer updateispay1(@Param("status") Integer status,@Param("id") Integer id);

    /**
     * 修改账户余额
     * @param manageMoney
     * @return
     */
    Integer updateCardMemberDetailPort(Map<String, Object> manageMoney);

    /**
     * 插入详细信息。
     *
     * @param map 包含详细信息的映射表，其中键值对代表了特定字段及其对应的值。
     * @return 插入操作的标识，通常为受影响的行数。
     */
    Integer insertDetail(@Param("param") Map<String, Object> map);

    /**
     * 插入消费记录。
     *
     * @param comRecord 包含消费记录信息的映射表，用于记录消费细节。
     * @return 插入操作的标识，通常为受影响的行数。
     */
    Integer insertConsumeRecordport(Map<String, Object> comRecord);

    /**
     * 更新shopbase表
     * @param map
     * @return
     */
    Integer updateshopbase(@Param("param") Map<Object, Object> map);

    /**
     *查询商家是否产生交易订单
/**
     * @param start
     * @param end
     * @return
     */
    List<Map<String,Object>> selectorder(@Param("starttime") Date start,@Param("endtime") Date end);


    /**
     * 根据配置类型查询系统配置信息。
     *
     * 本方法通过查询特定类型的系统配置，为系统提供动态配置的获取能力。
     * 通过配置类型作为查询条件，可以精确获取到该类型下的所有配置项，便于系统根据配置调整行为。
     *
     * @param configType 配置的类型，用于筛选查询的配置项。
     * @return 返回匹配配置类型的系统配置信息列表。
     */
    List<BaseSysConfigsVO> selectByConfigType(@Param("configType") String configType);

    /**
     *查询店铺订单商品分类信息
/**
     * @param shopId
     * @param start
     * @param end
     * @return
     */
    List<PlatFormGoodsCatsVO> selectordergoods(@Param("shopId")Integer shopId, @Param("starttime")Date start, @Param("endtime") Date end);

    /**
     * 查看分类父类质保金额度是否为null
     * @param catId
     * @return
     */
    List<Integer>  selectcat(@Param("catId") Integer catId);


    /**
     * 查询所有保留店铺的基础信息。
     *
     * @return 返回一个包含所有保留店铺基础信息的列表。
     */
    List<RetentionShopInfoVO> selectshopbase();

    /**
     * 根据用户ID查询卡会员服务端口信息。
     *
     * @param userId 用户的唯一标识ID。
     * @return 返回卡会员服务端口的相关信息。
     */
    CardMemberservicesVO queryCardMemberservicesPort(String userId);

    /**
     * 查询未赔付投诉单
     * @param shopId
     * @return
     */
    List<OrderComplaintsVO> unpaidcomplaints(@Param("shopId") Integer shopId);

    /**
     * 修改投诉单赔付状态
     * @param id
     * @return
     */
    Integer updateispay(@Param("id") Integer id);


    /**
     * 充值记录插入
     * @param map
     * @return
     */
    Integer insertretentionmoney(@Param("param") Map<Object, Object> map);

    /**
     * @param shopId
     * @return
     */
    List<Integer> selectshopgoods(@Param("param")Integer shopId);

    /**
     * 下架店铺商品
     * @param shopId
     * @return
     */
    Integer updateshopgoods(@Param("param")Integer shopId);



    /**
     * 更新商品基础状态。
     * <p>
     * 通过传递 PlatFormGoodsStateDTO 对象来更新商品的基础状态信息。
     * 具体更新的内容和逻辑由 PlatFormGoodsStateDTO 决定。
     *
     * @param param 包含商品状态更新信息的数据传输对象。
     */
    void updateGoodsBaseState(@Param("param") PlatFormGoodsStateDTO param);

    /**
     * 查询指定时间范围内的店铺ID。
     * <p>
     * 根据开始时间和结束时间查询店铺的ID列表。
     * 返回一个店铺ID的列表。
     *
     * @param start 查询的开始时间。
     * @param end 查询的结束时间。
     * @return 店铺ID的列表。
     */
    List<Integer> selectshopid(@Param("starttime") Date start, @Param("endtime") Date end);

    /**
     * 更新首次留存率。
     * <p>
     * 根据店铺ID更新首次留存率的相关信息。
     * 具体的更新逻辑和计算方式未详细说明。
     *
     * @param shopId 需要更新首次留存率的店铺ID。
     */
    void updateFirstRetention(@Param("shopId") Integer shopId);
}
