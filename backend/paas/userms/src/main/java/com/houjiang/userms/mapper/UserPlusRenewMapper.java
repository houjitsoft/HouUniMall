package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.entity.UserPlusRenewEntity;
import com.houjiang.userms.vo.PlusRenewEntityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserPlusRenewMapper
 * @Description
 * @Author Amos
 * @Date 2023-06-28 18:14:55
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserPlusRenewMapper extends BaseMapper<UserPlusRenewEntity> {

    /**
     * 查询续费信息
     * @param userId
     * @return
     */
    PlusRenewEntityVO selectRenewNew(String userId);

    /**
     * 插入续费记录
     * @param map
     * @return
     */
    Integer insertplusrenew(@Param("param") Map<String,Object> map);

    /**
     * 查询是否有续费权益
     * @param userId
     * @return
     */
    Integer selectthaliIdnext(@Param("userId") String userId);

    /**
     * 查询权益最后到期时间
     * @param userId
     * @return
     */
    Map selectthaliIdnexttime(@Param("userId") String userId);

    /**
     * 更新续费状态
     * @param renewId
     */
    void updaterenew(Integer renewId);
}
