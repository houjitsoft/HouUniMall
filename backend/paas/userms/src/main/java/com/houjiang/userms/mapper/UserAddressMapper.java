package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.entity.UserAddressEntity;
import com.houjiang.userms.vo.UserAddressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserAddressMapper
 * @Description  用户地址表
 * @Author Amos
 * @Date 2023-06-16 15:26:49
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {

    /**
     * 获取用户默认收货地址
     * @param paraMap
     * @return
     */
    UserAddressVO getDefAddresses(Map<String, String> paraMap);

    /**
     * 查询用户收货地址
     * @param paraMap
     * @return
     */
    List<UserAddressVO> getAddresses(Map<String, String> paraMap);

    /**
     * 修改地址
     *
     * @param paraMap
     * @return
     */
    Integer updateAddress(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 新增地址
     *
     * @param paraMap
     * @return
     */
    Integer addAddress(Map<String, String> paraMap);
}
