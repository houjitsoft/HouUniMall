package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderCancelReasonEntity;
import com.houjiang.order.mapper.OrderCancelReasonMapper;
import com.houjiang.order.service.OrderCancelReasonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 订单取消原因服务实现类
 * <p>
 * 该类继承自ServiceImpl，实现了OrderCancelReasonService接口，
 * 提供了对订单取消原因的增删改查等操作。
 * @author Amos
 */
@Service
public class OrderCancelReasonServiceImpl extends ServiceImpl<OrderCancelReasonMapper, OrderCancelReasonEntity>
    implements OrderCancelReasonService{

    /**
     * 查询订单取消原因列表
     * <p>
     * 根据传入的参数查询订单取消原因列表。
     *
     * @param paraMap 参数映射表，包含查询条件
     * @return 查询结果的ResponseResult对象
     */
    @Override
    public ResponseResult list(Map<String, String> paraMap) {
        // 使用LambdaQueryWrapper构造查询条件，根据参数查询订单取消原因列表
        List<OrderCancelReasonEntity> entities = this.baseMapper.selectList(new LambdaQueryWrapper<OrderCancelReasonEntity>()
                .eq(!paraMap.isEmpty(), OrderCancelReasonEntity::getCancelIdentity, paraMap.get("orderReturnReasonType").toString()));
        // 返回查询结果的ResponseResult对象
        return ResponseResult.success(entities);
    }

    /**
     * 删除订单取消原因
     * <p>
     * 根据传入的参数删除相应的订单取消原因。
     *
     * @param paraMap 参数映射表，包含待删除记录的ID
     * @return 删除操作结果的ResponseResult对象
     */
    @Override
    public ResponseResult delete(Map<String, String> paraMap) {
        // 根据参数删除订单取消原因
        int cancelReasonId = this.baseMapper.deleteById(paraMap.get("cancelReasonId"));
        // 根据删除结果返回相应的ResponseResult对象
        if (cancelReasonId > 0){
            return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
        }
        return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
    }

    /**
     * 添加订单取消原因
     * <p>
     * 根据传入的参数添加新的订单取消原因。
     *
     * @param paraMap 参数映射表，包含新记录的信息
     * @return 添加操作结果的ResponseResult对象
     */
    @Override
    public ResponseResult addCancelreason(Map<String, String> paraMap) {
        // 执行添加操作
        Integer integer = this.baseMapper.addCancelreason(paraMap);
        // 根据添加结果返回相应的ResponseResult对象
        if (integer > 0){
            return ResponseResult.success(BizCodeEnume.ADD_SUCCESS);
        }
        return ResponseResult.fail(BizCodeEnume.ADD_ERROR);
    }

    /**
     * 更新订单取消原因
     * <p>
     * 根据传入的参数更新相应的订单取消原因信息。
     *
     * @param paraMap 参数映射表，包含待更新记录的ID及其新的信息
     * @return 更新操作结果的ResponseResult对象
     */
    @Override
    public ResponseResult updateCancelreason(Map<String, String> paraMap) {
        // 根据参数更新订单取消原因信息
        Integer integer = this.baseMapper.updateCancelreason(paraMap);
        // 根据更新结果返回相应的ResponseResult对象
        if (integer > 0){
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

}
