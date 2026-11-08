package com.houjiang.job.service;

import com.houjiang.job.dto.ForSettleShopInfoDTO;

import java.util.List;

/**
 * @ClassName ForSettleShopService
 * @Description
 * @Author Amos
 * @Date 2023/9/5 10:53
 * @Version 1.0
 * @author Amos
 */

public interface ForSettleShopService {

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


}
