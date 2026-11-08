package com.houjiang.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 读取权威用户组织关系，供支付管理接口做租户隔离。 */
@Mapper
public interface PaymentUserOrganizationMapper {

    @Select("SELECT org_id AS org_id, is_primary AS is_primary "
            + "FROM hou_user_organization WHERE user_id = #{userId} "
            + "ORDER BY is_primary DESC, id ASC")
    List<Map<String, Object>> selectOrganizationsByUserId(@Param("userId") String userId);
}
