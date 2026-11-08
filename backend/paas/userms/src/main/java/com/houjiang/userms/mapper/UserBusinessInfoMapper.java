package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.userms.BusinessEmployeeDetailDTO;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.vo.user.BusinessUserInfoDetailVO;
import com.houjiang.userms.entity.UserBusinessInfoEntity;
import com.houjiang.userms.vo.BusinessUserInfoVO;
import com.houjiang.userms.vo.BusinessUserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


/**
* @Entity com.houjiang.userms.entity.UserBusinessInfo
/**
* @author Amos
*/
@Mapper
public interface UserBusinessInfoMapper extends BaseMapper<UserBusinessInfoEntity> {

    /**
     * 插入企业用户信息
     * @param businessegMap
     */
    boolean insertBusinessUser(Map<String, Object> businessegMap);

    /**
     * 判断是否企业用户
     *
     * @param
     * @return
     */
    int isBusiness(Map<String, Object> param);

    /**
     * 查询企业用户详细信息
     *
     * @param
     * @return
     */
    BusinessUserInfoVO queryInfo(Map<String, Object> param);

    /**
     * 分页查询企业用户信息
     *
     * @param
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<BusinessUserListVO> queryList(IPage<BusinessUserListVO> page , @Param("param") Map<String, Object> param);


    /**
     * 查询企业员工详情
     *
     * @param map
     * @return
     */
    BusinessUserInfoDetailVO queryDetail(Map<String, Object> map);

    /**
     * 校验企业名称是否存在
     * @param paraMap
     * @return
     */
    Integer selectBusinessUser(@Param("param")Map<String, String> paraMap);



    /**
     * 查询部门用户信息
     * @param param
     * @return
     */
    BusinessUserInfoDTO queryInfoFeign(@Param("param") Map<String, Object> param);

}
