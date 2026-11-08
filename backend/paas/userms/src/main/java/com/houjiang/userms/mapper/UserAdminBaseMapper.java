package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserAdminBaseEntity;
import com.houjiang.userms.vo.UserPersonalBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserAdminBaseMapper
 * @Description  后台用户基础信息表
 * @Author Amos
 * @Date 2023-06-27 14:05:30
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserAdminBaseMapper extends BaseMapper<UserAdminBaseEntity> {

    /**
     * 查询平台用户信息
     * @param paraMap
     * @return
     */
    UserInfoVO getPlatformUserInfo(Map<String, String> paraMap);

    /**
     * 添加管理员
     *
     * @param param
     */
    void addAdminUser(@Param("paraMap") Map<String, String> param);


    /**
     * 修改管理员
     *
     * @param paraMap
     */
    Integer updateAdminUser(@Param("paraMap") Map<String, String> paraMap);


    /**
     * 查询管理员列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<UserPersonalBaseVO> queryAdminUserList(Page<UserPersonalBaseVO> page, @Param("param") UserPersonalBaseVO param);
}
