package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.userms.entity.UserPlusEquityTempEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.userms.entity.UserPlusEquityTemp
 * @author Amos
 */
@Mapper
public interface UserPlusEquityTempMapper extends BaseMapper<UserPlusEquityTempEntity> {
    /**
     * 查询plus 会员权益
     * @param id
     * @return
     */
    List<PlusEquityEntityVO> selectplusequitys(@Param("list") List<Integer> id);

}




