package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.vo.order.ShoppingCartVO;
import com.houjiang.order.dto.EntityObjectsDTO;
import com.houjiang.order.entity.OrderUserCartEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderUserCartMapper
 * @Description  购物车表
 * @Author Amos
 * @Date 2023-05-25 13:39:40
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderUserCartMapper extends BaseMapper<OrderUserCartEntity> {
     /**
      * 查询商品在购物车的数量
      * @param userId
      * @param goodsId
      * @return
      */
     Integer selectCurrentCartQuantity(@Param("userId")String userId, @Param("goodsId")Integer goodsId);

    /**
     * 下单成功 删除购物车商品
     * @param userId
     * @param goodsIdList
     */
    void batchDeleteGoods(@Param("userId") String userId, @Param("goodsIdList") List<Integer> goodsIdList,@Param("shopGateId") Integer shopGateId);

    /**
     * 修改购物车商品数量
     * @param userId
     * @param goodsId
     * @param goodsNum
     * @return
     */
    Boolean changeGoodsNum(@Param("userId") String userId, @Param("goodsId") Integer goodsId,
                           @Param("goodsNum") Integer goodsNum,@Param("shopGateId") Integer shopGateId);


    /**
     * 查询店铺名和店铺id
     *
     * @param userId
//     * @param goodsType
/**
     * @return
     */
    List<EntityObjectsDTO> queryShoppingCartByShopName(String userId/*, Integer goodsType*/);

    /**
     * 平台查看用户购物车
     * @param page
     * @param param
     * @return
     */
    IPage<ShoppingCartVO> getShoppongCartList(IPage<ShoppingCartVO> page , @Param("param") ShoppingCartVO param);



    /**
     * 更新购物车
     *
     * @param map
     * @return
     */
    Integer updateOrderUserCarByMap(@Param("param") Map<String, Object> map);


    /**
     * 更新商品数量
     *
     * @param userId
     * @param goodsId
     * @param goodsNum
     * @return
     */
    Boolean updateGoodsNum(String userId, Integer goodsId, Integer goodsNum,String insertCartTime);

    /**
     * 根据用户ID和通用ID选择共同的数量。
     * @param userId 用户ID。
     * @param commonId 通用ID。
     * @return 共同的数量。
     */
    Integer selectCommonNumByUserId(@Param("userId") String userId,@Param("commonId") Integer commonId);

    /**
     * 根据闸机ID和商品ID选择闸机商店的库存。
     * @param shopGateId 闸机ID。
     * @param goodsId 商品ID。
     * @return 闸机商店的库存数量。
     */
    Integer selectpGateShopStock(@Param("shopGateId") Integer shopGateId, @Param("goodsId")Integer goodsId);

    /**
     * 查询购物车中指定商品的存在性。
     * @param userId 用户ID。
     * @param goodsId 商品ID。
     * @param shopGateId 闸机ID。
     * @param shoppingCartStatus 购物车商品状态。
     * @return 购物车中商品的存在性实体。
     */
    OrderUserCartEntity queryShoppingCartGoodsExist(@Param("userId") String userId, @Param("goodsId")Integer goodsId, @Param("shopGateId")Integer shopGateId, @Param("shoppingCartStatus")Integer shoppingCartStatus);

    /**
     * 更新购物车中商品的数量。
     * @param userCartEntity 购物车商品实体，包含需要更新的数量信息。
     * @param shopGateId 闸机ID。
     * @return 更新操作影响的行数。
     */
    int updateShoppingCartGoodsNum(@Param("param") OrderUserCartEntity userCartEntity, @Param("shopGateId") Integer shopGateId);

    /**
     * 根据用户ID、购物车状态和闸机ID查询用户的购物车商品列表。
     * @param userId 用户ID。
     * @param shoppingCartStatus 购物车商品状态。
     * @param shopGateId 闸机ID。
     * @return 用户的购物车商品列表。
     */
    List<OrderUserCartEntity> queryUserShoppingCart(@Param("userId") String userId/*, @Param("goodsType") Integer goodsType*/, @Param("shoppingCartStatus") Integer shoppingCartStatus, @Param("shopGateId") Integer shopGateId);


    /**
     * @param shopId
     */
    void deleteUserCat(@Param("shopId") String shopId);
}

