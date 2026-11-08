package com.houjiang.job.mapper;

import com.houjiang.job.dto.ForSettleShopInfoDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @ClassName ForSettleShopMapper
 * @Description 结算商店
 * @Author Amos
 * @Date 2023/5/17 16:08
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ForSettleShopMapper {

    /**
     * 获取结算结算商店信息
     * @return
     */
    List<ForSettleShopInfoDTO> getForSettleShopInfo();

    /**
     * 获取用于结算的普通店铺信息列表。
     *
     * @return List<ForSettleShopInfoDTO> 返回一个包含普通店铺结算信息的列表。
     */
    List<ForSettleShopInfoDTO> getForSettleCommonShopInfo();

    /**
     * 获取用于结算的供应商店铺信息列表。
     *
     * @return List<ForSettleShopInfoDTO> 返回一个包含供应商店铺结算信息的列表。
     */
    List<ForSettleShopInfoDTO> getForSettleSupplyShopInfo();

    /**
     * 获取结算结算webPos门店信息
     * @return
     */
    List<ForSettleShopInfoDTO> getGateShopInfo();

    /**
     * 获取结算结算商店信息
     * @return
     */
    List<ForSettleShopInfoDTO> getForSettleSupplierShopInfo();
}
