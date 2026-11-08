package com.houjiang.thirdparty.mapper.oa;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 读取 OA 已同步的企微员工 userid，供考勤拉取在通讯录 API 无可见范围时回退使用。
 * @author Amos
 */
@Mapper
public interface OaWecomStaffUserIdMapper {

    @Select("""
            SELECT DISTINCT userid FROM hou_oa_wecom_staff
            WHERE org_id = #{orgId} AND del_flag = 0
              /**
               * TODO 补充方法说明
               */
              AND userid IS NOT NULL AND TRIM(userid) <> ''
            """)
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> listUserIdsByOrgId(@Param("orgId") String orgId);
}
