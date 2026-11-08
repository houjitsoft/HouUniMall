package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseTagClassEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.base.entity.BaseTagClass
 * @author Amos
 */
@Mapper
public interface BaseTagClassMapper extends BaseMapper<BaseTagClassEntity> {
    /**
     * 查询用户所属登录
     * @param userId
     * @return
     */
    Integer getMemberClassIdByUserId(String userId);


    /**
     * 查询会员等级下会员
     * @param id
     * @return
     */
    List<String> queryByMemberClassId(@Param("id")  Integer id);
}




