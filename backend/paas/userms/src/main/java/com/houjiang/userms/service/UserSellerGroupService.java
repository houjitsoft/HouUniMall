package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserSellerGroupEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserSellerGroupService extends IService<UserSellerGroupEntity> {

    /**
     * 查询权限组
     *
     * @param paraMap
     * @return
     */
    ResponseResult get(Map<String, String> paraMap);

    /**
     * 增加权限组
     *
     * @param paraMap
     * @return
     */
    ResponseResult add(Map<String, String> paraMap);

    /**
     * 删除权限组
     *
     * @param paraMap
     * @return
     */
    ResponseResult delete(Map<String, String> paraMap);

    /**
     * 编辑权限组
     *
     * @param paraMap
     * @return
     */
    ResponseResult update(Map<String, String> paraMap);

}
