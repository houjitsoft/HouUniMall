package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserSellerBaseVO;
import com.houjiang.userms.dto.UserSellerBaseDTO;
import com.houjiang.userms.entity.UserSellerBaseEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface UserSellerBaseService extends IService<UserSellerBaseEntity> {

    /**
     * 根据shopId查询卖家信息
     *
     * @param returnMap
     * @return
     */
    ResponseResult getSellerInfo(Map<String, String> returnMap);

    /**
     * 添加子账号
     *
     * @param userSellerBase
     * @return
     */
    ResponseResult add(@RequestBody UserSellerBaseDTO userSellerBase);

    /**
     * 获取子账号
     *
     * @param paraMap
     * @return
     */
    ResponseResult get(@RequestBody Map<String, String> paraMap);

    /**
     * 删除子账号
     *
     * @param paraMap
     * @return
     */
    ResponseResult delete(@RequestBody Map<String, String> paraMap);

    /**
     * 编辑子账号
     *
     * @param paraMap
     * @return
     */
    ResponseResult update(@RequestBody Map<String, String> paraMap);


    /**
     * 查询店铺子账号列表
     *
     * @param shopId
     * @return
     */
    List<UserSellerBaseVO> getSellerBase(Integer shopId);

    /**
     * 查询是否为商家子账号
     * @param userId
     * @return
     */
    ResponseResult queryShopSellerBase(String userId);

    /**
     * 根据userId查询卖家主账号userId
     *
     * @param returnMap
     * @return
     */
    String getSellerAdmin(Map<String, String> returnMap);

    /**
     * 根据shopid查询有商家权限的账号
     *
     * @param shopId
     * @return
     */
    List<UserSellerBaseVO> getSubAccount(String shopId);

    /**
     * 新增卖家信息
     * @param map
     * @return
     */
    Integer insertSellerbase(Map<String, Object> map);


    /**
     * 根据店铺ID查询主账号用户ID
     * @param shopId
     * @return
     */
    String getShopAdmin(String shopId);

    /**
     * 查询用户Id
     *
     * @param shopId
     * @return
     */
    String getUserIDByShopId(String shopId);
}
