package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.userms.entity.UserPlusEquityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserPlusEquity
 * @author Amos
 */
@Mapper
public interface UserPlusEquityMapper extends BaseMapper<UserPlusEquityEntity> {
    /**
     * 查询plus 会员权益
     * @return
     */
    List<PlusEquityEntityVO> selectplusequitys(@Param("list") List<Integer> id);

    /**
     * 查询套餐权益
     * @param type
     * @return
     */
    List<PlusEquityEntityVO> selectplusequity(@Param("list")List<Integer> type);
    /**
     * 查询plus 会员权益
     * @return
     */
    List<PlusEquityEntityVO> selectplusequitysTmp(@Param("list")List<Integer> appIdList);
    /**
     * 添加会员权益
     * @param param
     * @return
     */
    Integer insertPlusEquity(@Param("param") PlusEquityDTO param);


    /**
     * 将信息备份
     * @param param
     * @return
     */
    Integer insertplusequityTemp(@Param("param") UserPlusEquityEntity param);


    /**
     * 查询plus套餐
     * @return
     */
    List<String> selectthalitypes();

    /**
     * @param page
     * @param param
     * @return
     */
    IPage<UserPlusEquityEntity> selectPlusEquityByPage(IPage<UserPlusEquityEntity> page , @Param("param") Map<String, Object> param);
}




