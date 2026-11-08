package com.houjiang.job.service.impl;

import com.houjiang.job.dto.ForSettleShopInfoDTO;
import com.houjiang.job.mapper.ForSettleShopMapper;
import com.houjiang.job.service.ForSettleShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName ForSettleShopServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/5 10:54
 * @Version 1.0
 **/
/**
 * 结算店铺服务实现类，提供获取结算通用店铺信息和结算供应商店铺信息的功能。
 * @author Amos
 */
@Service
public class ForSettleShopServiceImpl implements ForSettleShopService {

    /**
     * 注入结算店铺Mapper，用于数据访问和操作。
     */
    @Autowired
    private ForSettleShopMapper forSettleShopMapper;

    /**
     * 获取结算通用店铺信息。
     *
     * @return 返回结算通用店铺信息的列表。
     */
    @Override
    public List<ForSettleShopInfoDTO> getForSettleCommonShopInfo() {
        return forSettleShopMapper.getForSettleCommonShopInfo();
    }

    /**
     * 获取结算供应商店铺信息。
     *
     * @return 返回结算供应商店铺信息的列表。
     */
    @Override
    public List<ForSettleShopInfoDTO> getForSettleSupplyShopInfo(){
        return forSettleShopMapper.getForSettleSupplyShopInfo();
    }
}
