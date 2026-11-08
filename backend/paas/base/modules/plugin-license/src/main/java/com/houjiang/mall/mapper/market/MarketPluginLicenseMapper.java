package com.houjiang.mall.mapper.market;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import org.apache.ibatis.annotations.*;

/**
 * 插件市场-授权许可 Mapper。
 *
 * @author Amos
 */
@Mapper
public interface MarketPluginLicenseMapper extends BaseMapper<MarketPluginLicense> {
    // A stable row exists even for the first purchase. The update obtains an exclusive InnoDB lock.
    @Insert("INSERT INTO hou_service_entitlement_lock(instance_id,plugin_code) VALUES(#{instance},#{code}) ON DUPLICATE KEY UPDATE plugin_code=VALUES(plugin_code)")
    int lockEntitlement(@Param("code") String code,@Param("instance") String instance);

}
