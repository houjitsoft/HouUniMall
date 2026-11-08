package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.ISettingPaymentService;
import com.houjiang.merchant.validate.setting.SettingPayConfigValidate;
import com.houjiang.merchant.vo.setting.SettingPaymentMethodVo;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.entity.setting.MallPayScene;
import com.houjiang.merchant.common.enums.PaymentEnum;
import com.houjiang.merchant.common.mapper.setting.MallPayMapper;
import com.houjiang.merchant.common.mapper.setting.MallPaySceneMapper;
import com.houjiang.merchant.common.util.UrlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 支付配置服务实现类
 */
@Service
public class SettingPaymentServiceImpl implements ISettingPaymentService {

    @Resource
    MallPayMapper mallPayMapper;

    @Resource
    MallPaySceneMapper mallPaySceneMapper;

    /**
     * 支付配置方式
     *
     * @author Amos
     * @return List<List<SettingPaymentMethodVo>>
     */
    public HashMap getPayWay() {
        List<MallPayScene> lists = mallPaySceneMapper.selectList(null);

        //List<List<SettingPaymentMethodVo>> result = new LinkedList<>();
        HashMap result = new LinkedHashMap<Integer, String>();
        List<SettingPaymentMethodVo> mnp = new LinkedList<>();
        List<SettingPaymentMethodVo> oa = new LinkedList<>();
        List<SettingPaymentMethodVo> h5 = new LinkedList<>();

        for (MallPayScene devPayWay : lists) {
            MallPay devPayConfig = mallPayMapper.selectOne(
                    new QueryWrapper<MallPay>()
                        .eq("id", devPayWay.getMallPayId())
                        .last("limit 1"));

            SettingPaymentMethodVo vo = new SettingPaymentMethodVo();
            BeanUtils.copyProperties(devPayWay, vo);
            vo.setPayWayName(PaymentEnum.getPayWayMsg(devPayConfig.getPayType()));
            vo.setIcon(UrlUtils.toShopAbsoluteUrl(devPayConfig.getIcon()));

            switch (devPayWay.getScene()) {
                // 微信小程序
                case 1:
                    mnp.add(vo);
                    break;
                // 微信公众号
                case 2:
                    oa.add(vo);
                    break;
                case 3:
                    h5.add(vo);
                    break;
            }
        }

        result.put(1, mnp);
        result.put(2, oa);
        result.put(3, h5);
        return result;
    }

    /**
     * 支付配置列表
     *
     * @author Amos
     * @return List<DevPayConfig>
     */
    @Override
    public JSONObject list() {
        List<MallPay> devPayConfigs = mallPayMapper.selectList(
                new QueryWrapper<MallPay>()
                    .orderByDesc(Arrays.asList("sort", "id")));

        for (MallPay dev : devPayConfigs) {
            dev.setPayTypeName(PaymentEnum.getPayWayMsg(dev.getPayType()));
            dev.setName(dev.getName());
            dev.setConfig(JSONObject.parse("{}"));
            dev.setIcon(UrlUtils.toShopAbsoluteUrl(dev.getIcon()));
        }

        JSONObject result = new JSONObject() {{
            put("lists", devPayConfigs);
        }};
        return result;
    }

    /**
     * 支付配置详情
     *
     * @author Amos
     * @param id 主键
     * @return DevPayConfig
     */
    @Override
    public MallPay getConfig(Integer id) {
        MallPay devPayConfig = mallPayMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("id", id)
                        .last("limit 1"));

        devPayConfig.setName(PaymentEnum.getPayWayMsg(devPayConfig.getPayType()));
        devPayConfig.setPayTypeName(devPayConfig.getName());
        devPayConfig.setConfig(JSONObject.parse(devPayConfig.getConfig().toString()));
        devPayConfig.setIcon(UrlUtils.toShopAbsoluteUrl(devPayConfig.getIcon()));
        return devPayConfig;
    }

    /**
     * 支付配置编辑
     *
     * @author Amos
     * @param configValidate 参数
     */
    @Override
    public void setConfig(SettingPayConfigValidate configValidate) {
        MallPay devPayConfig = mallPayMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("id", configValidate.getId())
                        .last("limit 1"));

        Assert.notNull(devPayConfig, "数据不存在!");

        devPayConfig.setName(configValidate.getName());
        devPayConfig.setIcon(UrlUtils.toShopRelativeUrl(configValidate.getIcon()));
        devPayConfig.setSort(configValidate.getSort());
        devPayConfig.setRemark(configValidate.getRemark());
        if (devPayConfig.getPayType().equals(1)) {
            devPayConfig.setConfig("{}");
        } else {
            devPayConfig.setConfig(configValidate.getConfig().toJSONString());
        }
        mallPayMapper.updateById(devPayConfig);
    }

    /**
     * 支付方式编辑
     *
     * @author Amos
     * @param data 参数
     */
    @Override
    @Transactional
    public void setPayWay(HashMap<Integer, List<SettingPaymentMethodVo>> data) {
        for (Integer key : data.keySet()) {
            List<SettingPaymentMethodVo> list = data.get(key);
            for (SettingPaymentMethodVo vo : list) {
                MallPayScene way = new MallPayScene();
                way.setIsDefault(vo.getIsDefault());
                way.setStatus(vo.getStatus());
                mallPaySceneMapper.update(way, new QueryWrapper<MallPayScene>().eq("id", vo.getId()));
            }
        }
    }

}
