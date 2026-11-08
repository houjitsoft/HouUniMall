package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseTagClassEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName BaseTagClassService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface BaseTagClassService extends IService<BaseTagClassEntity> {

    /**
     * 根据用户id查询会员等级id
     * @param userId
     * @return
     */
    ResponseResult getMemberClassIdByUserId(String userId);


    /**
     * 更新会员等级
     *
     * @param map
     * @return
     */
    ResponseResult updateTagClassByUserId(Map<String, Object> map);


    /**
     * 注册修改会员等级
     *
     * @param map
     * @return
     */
    ResponseResult saveTagClass(Map<String, Object> map);

    /**
     * 根据用户id查询会员等级名称
     * @param userId
     * @return
     */
    String queryMemberNameByUserId(String userId);
}
