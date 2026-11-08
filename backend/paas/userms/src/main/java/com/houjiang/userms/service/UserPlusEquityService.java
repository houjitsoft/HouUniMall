package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.userms.entity.UserPlusEquityEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface UserPlusEquityService extends IService<UserPlusEquityEntity> {

    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    ResponseResult selectPlusEquitys(List<Integer> id);

    /**
     * 添加会员权益
     * @param dto
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param dto TODO
     * @return TODO
     */
    ResponseResult insertPlusEquity(PlusEquityDTO dto);


    /**
     *删除会员权益
/**
     * @param dto
     * @return
     */
     ResponseResult deletePlusEquity(PlusEquityDTO dto);


    /**
     *更新会员权益状态
/**
     * @param dto
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param dto TODO
     * @return TODO
     */
    ResponseResult updatePlusEquity(PlusEquityDTO dto);

    /**
     *查询会员权益
/**
     * @param map
     * @return
     */
    ResponseResult selectPlusEquity(Map<String, Object> map);

    /**
     * 查询会员权益
     * @param id
     * @return
     */
    List<PlusEquityEntityVO> selectplusequitys(List<Integer> id);
}
