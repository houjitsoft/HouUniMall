package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.TokenUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.entity.OrderInvoiceAddressEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderInvoiceAddressMapper;
import com.houjiang.order.service.OrderInvoiceAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 发票地址管理表服务实现类
 * @ClassName OrderInvoiceAddressServiceImpl
 * @Description 发票地址管理表
 * @Author Amos
 * @Date 2023-07-04 14:05:35
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderInvoiceAddressServiceImpl extends ServiceImpl<OrderInvoiceAddressMapper, OrderInvoiceAddressEntity>
        implements OrderInvoiceAddressService {

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 查询发票地址列表
     * @param param 查询参数，包含分页信息和token
     * @return 查询结果
     */
    @Override
    public ResponseResult list(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 若token无效，返回错误响应
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        try {
            // 将用户ID添加到查询参数中
            param.put("userId",userInfo.getUserId());
            // 分页查询数据
            IPage<OrderInvoiceAddressEntity> page = this.baseMapper.list(new Query<OrderInvoiceAddressEntity>().getPage(param),param);
            List<OrderInvoiceAddressEntity> records = page.getRecords();
            // 格式化地址信息
            for (OrderInvoiceAddressEntity record : records) {
                String completeAddress = baseServiceClient.getAddressString(record.getAddressProvince(),record.getAddressCity(),record.getAddressArea()).getData().toString();
                record.setPreCompleteAddress(completeAddress);
                completeAddress = completeAddress + record.getAddressDetail();
                record.setCompleteAddress(completeAddress);
            }
            // 返回成功响应，包含分页信息
            return ResponseResult.success(PageUtils.getPage(page));

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 系统错误，返回失败响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 添加或更新发票地址信息
     * @param param 添加或更新参数，包含token和发票地址信息
     * @return 操作结果
     */
    @Override
    public ResponseResult add(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 若token无效，返回错误响应
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        try {
            // 将用户ID添加到参数中
            param.put("userId",userInfo.getUserId());
            // 检查是否已达到发票地址模板数量上限
            if (param.get("invoiceTmpId") == null) {
                Integer count = this.baseMapper.queryAddressTmpListCount(param);
                if(null !=count && count >= 10){
                    return ResponseResult.fail(BizCodeEnume.INVOICE_ADDRESS_TEMPLATES_HAS_REACHED_THE_UPPER_LIMIT.getCode(),BizCodeEnume.INVOICE_ADDRESS_TEMPLATES_HAS_REACHED_THE_UPPER_LIMIT.getMsg());
                }
            }
            // 处理默认地址逻辑
            int isDefault = (Integer) param.get("isDefault");
            if (1 == isDefault) {
                this.baseMapper.updateAddressDefault(param);
            }
            // 插入或更新发票地址模板
            if (param.get("invoiceTmpId") == null) {
                this.baseMapper.insertAddressTmp(param);
            } else {
                this.baseMapper.updateAddressTmp(param);
            }
            // 返回成功响应
            return ResponseResult.success();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // 系统错误，返回失败响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 删除发票地址模板
     * @param param 删除参数，包含token和地址ID
     * @return 删除结果
     */
    @Override
    public ResponseResult deleteAddressTmp(Map<String, Object> param) {
        // 校验token
        String token = (String) param.get("token");
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据用户ID和地址ID删除记录
            this.baseMapper.delete(new LambdaQueryWrapper<OrderInvoiceAddressEntity>()
                    .eq(OrderInvoiceAddressEntity::getUserId, param.get("userId").toString())
                    .eq(OrderInvoiceAddressEntity::getAddressId, param.get("addressId").toString()));
            // 返回成功响应
            return ResponseResult.success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据ID查询发票地址详情
     * @param param 查询参数，包含token和地址ID
     * @return 查询结果
     */
    @Override
    public ResponseResult queryById(Map<String, Object> param) {
        // 校验token
        String token = (String) param.get("token");
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据用户ID和地址ID查询记录
            OrderInvoiceAddressEntity addressTmpEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<OrderInvoiceAddressEntity>()
                    .eq(OrderInvoiceAddressEntity::getUserId, param.get("userId").toString())
                    .eq(OrderInvoiceAddressEntity::getAddressId, param.get("addressId").toString()));
            // 格式化地址信息
            String addressInfo = baseServiceClient.getAddressString(addressTmpEntity.getAddressProvince(),addressTmpEntity.getAddressCity(),addressTmpEntity.getAddressArea()).getData().toString();
            addressTmpEntity.setAddressInfo(addressInfo);
            // 返回查询结果
            return ResponseResult.success(addressTmpEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseResult.fail(e.getMessage());
        }
    }
}
