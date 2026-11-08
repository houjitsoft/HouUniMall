package com.houjiang.mall.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.auth.MallAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mall 管理员 Mapper 接口
 *
 * @author Amos
 */
@Mapper
public interface MallAdminMapper extends BaseMapper<MallAdmin> {
}
