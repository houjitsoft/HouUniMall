package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseDashboardPrefDTO;
import com.houjiang.base.entity.BaseDashboardPrefEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * Dashboard 用户偏好 Service
 *
 * @author Amos
 * @since 2026-05-13
 */
public interface BaseDashboardPrefService extends IService<BaseDashboardPrefEntity> {

    /**
     * 查询当前登录用户的 Dashboard 偏好（无记录则返回默认）
     *
     * @param token 登录 token
     * @return 偏好 VO
     */
    ResponseResult getPref(String token);

    /**
     * 保存当前登录用户的 Dashboard 偏好（全量覆盖明细）
     *
     * @param token 登录 token
     * @param dto   偏好 DTO
     * @return 保存结果
     */
    ResponseResult savePref(String token, BaseDashboardPrefDTO dto);

    /**
     * 重置当前登录用户的 Dashboard 偏好（清空明细并恢复默认列/刷新）
     *
     * @param token 登录 token
     * @return 操作结果
     */
    ResponseResult resetPref(String token);
}
