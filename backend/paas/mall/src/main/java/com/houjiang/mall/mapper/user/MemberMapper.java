package com.houjiang.mall.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.user.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员管理 Mapper 接口
 * 
 * @author Amos
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
