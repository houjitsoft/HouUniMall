package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserBusinessInfoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


/**
* @author Amos
*/
public interface UserBusinessInfoService extends IService<UserBusinessInfoEntity> {

    /**
     * 判断是否为未通过企业用户
     *
     * @param map
     * @return
     */
    ResponseResult isPassedEnt(Map<String, String> map);

    /**
     * 查找是否为企业 fegin
     *
     * @param userId
     * @return
     */
    boolean isBusinessFegin(String userId);

    /**
     * 判断是否企业用户
     *
     * @param
     * @return
     */
    ResponseResult isBusiness(Map<String, Object> param);

    /**
     * 查询企业用户详细信息
     *
     * @param
     * @return
     */
    ResponseResult queryInfo(Map<String, Object> param);

    /**
     * 根据管理员找到企业
     *
     * @param userId
     * @return
     */
    ResponseResult queryBusiness(String userId);



    /**
     * 更改企业用户信息
     *
     * @param map
     * @return
     */
    ResponseResult updateBusinessInfo(Map<String, Object> map);


    /**
     * 查询企业用户信息
     *
     * @param
     * @return
     */
     ResponseResult queryList(Map<String, Object> param);

    /**
     * 注册企业用户
     *
     * @param map
     * @return
     */
    ResponseResult registerBusiness(Map<String, Object> map);

    /**
     * 校验企业名称是否存在
     * @param paraMap
     * @return
     */
    ResponseResult selectBusinessUser(Map<String, String> paraMap);


    /**
     * 查询部门用户信息
     * @param param
     * @return
     */
    BusinessUserInfoDTO queryInfoFeign(@Param("param") Map<String, Object> param);


}
