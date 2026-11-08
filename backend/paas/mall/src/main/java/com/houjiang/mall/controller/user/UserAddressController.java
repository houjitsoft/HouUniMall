package com.houjiang.mall.controller.user;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.user.UserAddress;
import com.houjiang.mall.service.user.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收货地址Controller
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 添加收货地址
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addAddress(@RequestBody UserAddress address) {
        if (address.getUserId() == null || address.getReceiverName() == null || 
            address.getReceiverPhone() == null || address.getDetailAddress() == null) {
            return ResponseResult.error("参数不完整");
        }
        boolean result = userAddressService.addAddress(address);
        return ResponseResult.success(result);
    }

    /**
     * 更新收货地址
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> updateAddress(@RequestBody UserAddress address) {
        if (address.getId() == null) {
            return ResponseResult.error("地址ID不能为空");
        }
        boolean result = userAddressService.updateAddress(address);
        return ResponseResult.success(result);
    }

    /**
     * 删除收货地址
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteAddress(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = userAddressService.deleteAddress(id, userId);
        return ResponseResult.success(result);
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/set-default")
    public ResponseResult<Boolean> setDefaultAddress(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = userAddressService.setDefaultAddress(id, userId);
        return ResponseResult.success(result);
    }

    /**
     * 获取用户地址列表
     */
    @PostMapping("/list")
    public ResponseResult<List<UserAddress>> getAddressList(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        List<UserAddress> list = userAddressService.getAddressList(userId);
        return ResponseResult.success(list);
    }

    /**
     * 获取默认地址
     */
    @PostMapping("/default")
    public ResponseResult<UserAddress> getDefaultAddress(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        UserAddress address = userAddressService.getDefaultAddress(userId);
        return ResponseResult.success(address);
    }

    /**
     * 获取地址详情
     */
    @PostMapping("/detail")
    public ResponseResult<UserAddress> getAddressById(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        UserAddress address = userAddressService.getAddressById(id, userId);
        return ResponseResult.success(address);
    }

    /**
     * 获取用户地址数量
     */
    @PostMapping("/count")
    public ResponseResult<Long> getAddressCount(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        long count = userAddressService.getAddressCount(userId);
        return ResponseResult.success(count);
    }
}
