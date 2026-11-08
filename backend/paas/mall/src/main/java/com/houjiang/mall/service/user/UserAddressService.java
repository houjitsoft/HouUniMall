package com.houjiang.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.user.UserAddress;
import com.houjiang.mall.mapper.user.UserAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户收货地址服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
public class UserAddressService extends ServiceImpl<UserAddressMapper, UserAddress> {

    /**
     * 添加收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(UserAddress address) {
        // 如果设置为默认地址,需要先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelDefaultAddress(address.getUserId());
        }
        return this.save(address);
    }

    /**
     * 更新收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(UserAddress address) {
        // 如果设置为默认地址,需要先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelDefaultAddress(address.getUserId());
        }
        return this.updateById(address);
    }

    /**
     * 删除收货地址
     */
    public boolean deleteAddress(Long id, Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 设置默认地址
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long id, Long userId) {
        // 先取消所有默认地址
        cancelDefaultAddress(userId);
        
        // 设置新的默认地址
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setIsDefault(1);
        return this.updateById(address);
    }

    /**
     * 取消默认地址
     */
    private void cancelDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1);
        
        List<UserAddress> addresses = this.list(wrapper);
        addresses.forEach(addr -> addr.setIsDefault(0));
        if (!addresses.isEmpty()) {
            this.updateBatchById(addresses);
        }
    }

    /**
     * 获取用户地址列表
     */
    public List<UserAddress> getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 获取默认地址
     */
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 获取地址详情
     */
    public UserAddress getAddressById(Long id, Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId);
        return this.getOne(wrapper);
    }

    /**
     * 获取用户地址数量
     */
    public long getAddressCount(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        return this.count(wrapper);
    }
}
