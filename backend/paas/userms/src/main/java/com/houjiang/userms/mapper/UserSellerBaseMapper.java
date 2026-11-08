package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.user.UserSellerBaseVO;
import com.houjiang.userms.dto.UserSellerBaseDTO;
import com.houjiang.userms.entity.UserSellerBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserSellerBase
 * @author Amos
 */
@Mapper
public interface UserSellerBaseMapper extends BaseMapper<UserSellerBaseEntity> {

    /**
     * 获取卖家账号
     * @param paraMap
     * @return
     */
    List<UserSellerBaseDTO> getSellerAccount(@Param("param") Map<String, String> paraMap);


    /**
     * 查询店铺子账号列表
     *
     * @param shopId
     * @return
     */
    List<UserSellerBaseVO> getSellerBase(Integer shopId);

    /**
     * 是否绑定门店
     * @param userMobile
     * @return
     */
    Integer selectShopGateCount(@Param("userMobile") String userMobile);

    /**
     * 根据shopid查询有商家权限的账号
     *
     * @param shopId
     * @return
     */
    List<UserSellerBaseVO> getSubAccount(String shopId);

    /**
     * @param map
     * @return
     */
    Integer insertSellerbase(@Param("param") Map<String, Object> map);


    /**
     * 根据店铺ID查询主账号用户ID
     * @param shopId
     * @return
     */
    String getShopAdmin(@Param("shopId") String shopId);

    /**
     * 查询用户Id
     *
     * @param shopId
     * @return
     */
    String getUserIDByShopId(@Param("shopId") String shopId);
}




