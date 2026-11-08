package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPlusEquityTempEntity;

import java.util.List;

/**
 * @author Amos
 */
public interface UserPlusEquityTempService extends IService<UserPlusEquityTempEntity> {


    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    ResponseResult selectPlusEquityTemps(List<Integer> id);
}
