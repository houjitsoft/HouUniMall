package com.houjiang.mall.mapper.market;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.market.MarketPayConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 插件支付配置 Mapper。
 *
 * <p>表未创建时查询会抛异常，由 {@code PluginPayConfigStore} 捕获后回落 Nacos/yml 基线，
 * 不影响服务启动——即「先跑代码、后建表」也是安全的。
 *
 * @author Amos
 */
@Mapper
public interface MarketPayConfigMapper extends BaseMapper<MarketPayConfig> {
}
