package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.CommissionSettingVO;
import com.houjiang.base.entity.BaseVkSetEntity;
import com.houjiang.base.feign.OrderClient;
import com.houjiang.base.mapper.BaseVkSetMapper;
import com.houjiang.base.service.BaseVkSetService;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.Constants;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName BaseVkSetServiceImpl
 * @Description 微客配置表
 * @Author Amos
 * @Date 2023-06-05 10:48:40
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseVkSetServiceImpl extends ServiceImpl<BaseVkSetMapper, BaseVkSetEntity>
        implements BaseVkSetService {
    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询商品设置信息。
     * 优先从Redis中获取，如果不存在，则从数据库中查询并存入Redis。
     */
    @Override
    public GoodsVkSetVO queryGoodsVkSet() {
        // 从Redis中获取商品设置信息
        String data = (String) redisCache.getCacheObject(Constants.FX_GOODS_SET);
        GoodsVkSetVO goodsVkSet;
        // 如果Redis中不存在，则从数据库中查询
        if (StringUtils.isBlank(data)) {
            goodsVkSet = this.baseMapper.selectByPrimaryKey(GoodsVkSetVO.VK_SET_ID);
            // 如果数据库中也不存在，则创建一个新的商品设置对象
            if (null == goodsVkSet) {
                goodsVkSet = new GoodsVkSetVO(0);
            }
            // 将查询到的商品设置信息存入Redis
            redisCache.setCacheObject(Constants.FX_GOODS_SET, JSON.toJSONString(goodsVkSet));
            redisCache.expire(Constants.FX_GOODS_SET, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            // 如果Redis中存在，则直接反序列化为对象
            goodsVkSet = JSON.parseObject(data, GoodsVkSetVO.class);
        }
        return goodsVkSet;
    }

    /**
     * 添加或更新佣金设置。
     * 根据数据库中是否存在佣金设置来决定是添加还是更新，并且无论操作是否成功，都会清除Redis中的佣金设置数据。
     */
    @Override
    public ResponseResult addOrUpdateCommissionSetting(CommissionSettingVO param) {
        // 查询数据库中是否存在佣金设置
        //查询分佣设置列表
        BaseVkSetEntity result = this.baseMapper.selectOne(new LambdaQueryWrapper<>());
        if (null == result) {
            // 如果不存在，则插入新的佣金设置
            //如果为空，就是插入新数据
            Integer results = this.baseMapper.insertCommissionSetting(param);
            if (results >= 1) {
                // 插入成功后清除Redis中的佣金设置数据
                //插入成功后把redis数据删除掉
                redisCache.deleteObject(Constants.FX_GOODS_SET);
            }
        } else {
            // 如果存在，则更新佣金设置
            //如果不为空，就是更新新数据
            Integer results = this.baseMapper.updateCommissionSetting(param);
            if (results >= 1) {
                // 更新成功后清除Redis中的佣金设置数据
                //更新成功后，把redis数据删掉
                redisCache.deleteObject(Constants.FX_GOODS_SET);
            }
        }
        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询佣金设置。
     * 如果数据库中不存在佣金设置，则返回失败响应；否则，将佣金设置存入Redis并返回成功响应。
     */
    @Override
    public ResponseResult commissionSetting() {
        // 查询数据库中是否存在佣金设置
        //查询分佣设置列表
        BaseVkSetEntity result = this.baseMapper.selectOne(new LambdaQueryWrapper<>());
        if (null == result) {
            // 如果不存在，则返回失败响应
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        } else {
            // 如果存在，则将佣金设置存入Redis并返回成功响应
            redisCache.setCacheObject(Constants.FX_GOODS_SET, JSON.toJSONString(result));
            redisCache.expire(Constants.FX_GOODS_SET, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
            return ResponseResult.success(result);
        }
    }

    /**
     * 更新售后时间。
     * 如果订单佣金信息存在，则根据商品设置中的售后天数更新订单的售后时间。
     */
    @Override
    public ResponseResult updateOrderAfterSale(String orderId) {
        // 查询订单佣金信息
        OrderCommissionVO orderCommission = orderClient.selectOrderCommission(orderId);
        if (null != orderCommission) {
            // 将佣金状态更改为已冻结
            orderClient.updateOrderCommissionState(orderId);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }
}
