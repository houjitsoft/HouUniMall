package com.houjiang.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.IndexConfigDTO;
import com.houjiang.base.entity.BasePcIndexConfigEntity;
import com.houjiang.base.feign.BusinessClient;
import com.houjiang.base.mapper.BasePcIndexConfigMapper;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.base.mapper.WebIndexMapper;
import com.houjiang.base.service.BasePcIndexConfigService;
import com.houjiang.base.vo.index.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisPrefixUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.PcConfigShopVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName BasePcIndexConfigServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023-05-24 09:39:23
 * @Version 1.0
 * @author Amos
 */
@Service
public class BasePcIndexConfigServiceImpl extends ServiceImpl<BasePcIndexConfigMapper, BasePcIndexConfigEntity>
        implements BasePcIndexConfigService {
    @Autowired
    private WebIndexMapper webIndexMapper;
    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;

    @Value("${system.pcIndexConfig}")
    private String pcIndexConfig;

    @Autowired
    private BusinessClient businessClient;
    private static String pcIndexConfig2;

    @Autowired
    private RedisCache redisCache;


    @PostConstruct
    public String getPcIndexConfig2() {
        return pcIndexConfig2 = this.pcIndexConfig;
    }

    /**
     * 查询GDB PC端索引配置
     * 该方法用于从数据库中查询PC端索引的配置信息，并进行相应的处理和转换，最终返回一个分类好的配置结果集合。
     * @return Map<Integer, List<PcConfigResultVO>> 分类后的PC配置结果集合，键为页面类型，值为该类型下的配置结果列表。
     */
    public Map<Integer, List<PcConfigResultVO>> querygdbPcIndexConfig() {
        // 初始化结果列表
        List<PcConfigResultVO> result = new ArrayList<>();
        // 创建一个空的PC索引配置对象
        GdbPcIndexConfigVO pcIndexConfig = new GdbPcIndexConfigVO();
        // 从数据库中查询所有的PC索引配置
        //查询所有数据 解析
        List<GdbPcIndexConfigVO> gdbPcIndexConfigs = this.baseMapper.selectPcIndexConfig(pcIndexConfig);
        // 遍历查询结果，处理每个配置项
        if (null != gdbPcIndexConfigs && gdbPcIndexConfigs.size() > 0) {
            for (GdbPcIndexConfigVO gdbPcIndexConfig : gdbPcIndexConfigs) {
                // 创建一个PC配置结果对象，用于存储当前配置项的处理结果
                //用与存放 商品 店铺数据
                PcConfigResultVO pcConfigResult = new PcConfigResultVO();
                // 将当前配置项的基本信息赋值给结果对象
                pcConfigResult.setPageType(gdbPcIndexConfig.getPageType());
                pcConfigResult.setPageName(gdbPcIndexConfig.getPageName());
                pcConfigResult.setPageTitle(gdbPcIndexConfig.getPageTitle());
                pcConfigResult.setPageTwoTitle(gdbPcIndexConfig.getPageTwoTitle());
                pcConfigResult.setPageStart(gdbPcIndexConfig.getPageStart());
                pcConfigResult.setPageSort(gdbPcIndexConfig.getPageSort());
                pcConfigResult.setPageId(gdbPcIndexConfig.getId());
                // 解析页面数据，根据类型处理不同的配置数据
                // 获取页面数据
                String data = gdbPcIndexConfig.getPageData();
                if (StrUtil.isNotBlank(data)) {
                    GdbPcIndexConfigDataVO gdbPcIndexConfigData = JSON.parseObject(data, GdbPcIndexConfigDataVO.class);
                    // 根据类型处理不同的配置数据
                    List<Integer> ids = gdbPcIndexConfigData.getData();
                    if (null != gdbPcIndexConfigData.getType() && 1 == gdbPcIndexConfigData.getType()) {
                        // 处理商品类型的配置数据
                        //存放商品信息
                        List<PcConfigGoodsVO> goods = new ArrayList<>();
                        if (null != ids) {
                            for (Integer id : ids) {
                                //根据common 查询商品信息
                                PcConfigGoodsVO pcConfigGoodsParam = webIndexMapper.pcConfigGoods(id);
                                if (null != pcConfigGoodsParam) {
                                    goods.add(pcConfigGoodsParam);
                                }
                            }
                            pcConfigResult.setGoods(goods);
                        }
                    } else if (null != gdbPcIndexConfigData.getType() && 2 == gdbPcIndexConfigData.getType()) {
                        // 处理店铺类型的配置数据
                        //存放店铺信息
                        List<PcConfigShopVO> shops = new ArrayList<>();
                        if (null != ids) {
                            for (Integer id : ids) {
                                //根据shopid
                                PcConfigShopVO pcConfigShopParam = businessClient.pcConfigShop(id);
                                if (null != pcConfigShopParam) {
                                    if (StringUtils.isEmpty(pcConfigShopParam.getCommonImage())) {
                                        String icoShop = baseSysConfigsMapper.selectConfigValue("logo_copywriting_ico_shop");
                                        pcConfigShopParam.setCommonImage(icoShop);
                                    }
                                    shops.add(pcConfigShopParam);
                                }
                            }
                            pcConfigResult.setShops(shops);
                        }
                    } else if (null != gdbPcIndexConfigData.getType() && 3 == gdbPcIndexConfigData.getType()) {
                        // 处理分类类型的配置数据
                        PcConfigCatVO pcConfigCatParam = new PcConfigCatVO();
                        pcConfigCatParam.setCatImage(gdbPcIndexConfigData.getImages());
                        if (null != ids) {
                            for (Integer id : ids) {
                                pcConfigCatParam.setCatUrl(String.valueOf(id));
                            }
                        }
                        pcConfigResult.setCat(pcConfigCatParam);
                    }
                }
                // 将处理好的配置结果添加到结果列表
                result.add(pcConfigResult);
            }
        }
        // 根据页面类型对结果列表进行分类
        //对数据库中数据分组 返回
        return result.stream().collect(Collectors.groupingBy(PcConfigResultVO::getPageType));
    }

    /**
     * 更新PC端索引配置
     * 该方法用于更新PC端索引的配置信息，根据传入的配置对象，判断是更新已有配置还是新增配置，并处理相应的操作。
     * @param pcIndexConfig PC端索引的配置对象，包含需要更新的配置信息。
     * @return ResponseResult 更新操作的结果，成功返回success，失败返回相应的错误信息。
     */
    @Override
    public ResponseResult updatePcIndexConfig(IndexConfigDTO pcIndexConfig) {
        // 检查传入的配置对象是否为空
        if (null != pcIndexConfig) {
            // 判断是否需要更新已有配置还是新增配置
            // 根据id 判断是否是添加
            if (null != pcIndexConfig.getId()) {
                // 更新已有配置
                //修改
                this.baseMapper.update(null, new LambdaUpdateWrapper<BasePcIndexConfigEntity>()
                        .eq(BasePcIndexConfigEntity::getId, pcIndexConfig.getId())
                        .set(BasePcIndexConfigEntity::getPageName, pcIndexConfig.getPageName())
                        .set(BasePcIndexConfigEntity::getPageType, pcIndexConfig.getPageType())
                        .set(BasePcIndexConfigEntity::getPageTitle, pcIndexConfig.getPageTitle())
                        .set(BasePcIndexConfigEntity::getPageTwoTitle, pcIndexConfig.getPageTwoTitle())
                        .set(BasePcIndexConfigEntity::getPageData, pcIndexConfig.getPageData())
                        .set(BasePcIndexConfigEntity::getCreateTime, new Date())
                );
            } else {
                // 新增配置
                BasePcIndexConfigEntity entity = new BasePcIndexConfigEntity();
                BeanUtils.copyProperties(pcIndexConfig, entity);
                this.baseMapper.insert(entity);
            }
            // 删除Redis中的相关缓存
            //删除redis缓存
            redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig);
            redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig2);
            // 返回更新成功的响应
            return ResponseResult.success();
        }
        // 如果配置对象为空，返回失败响应
        return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
    }

}

