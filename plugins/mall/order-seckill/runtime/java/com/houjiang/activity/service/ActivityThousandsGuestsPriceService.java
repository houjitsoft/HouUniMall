package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.ThousandsGuestsPriceConfigDTO;
import com.houjiang.activity.dto.ThousandsGuestsPriceListQueryDTO;
import com.houjiang.activity.entity.ActivityThousandsGuestsPriceEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;

import java.util.Map;

/**
 * 千客千价价格策略 Service
 * @author Amos
 */
public interface ActivityThousandsGuestsPriceService extends IService<ActivityThousandsGuestsPriceEntity> {

    /**
     * 分页查询价格策略
     */
    ResponseResult queryConfigPage(ThousandsGuestsPriceListQueryDTO param);

    /**
     * 新增或更新价格策略
     */
    ResponseResult saveConfig(ThousandsGuestsPriceConfigDTO param, UserInfoVO userInfo);

    /**
     * 删除价格策略
     */
    ResponseResult deleteConfig(Long id);

    /**
     * 更新启用状态
     */
    ResponseResult updateStatus(Long id, Integer status, UserInfoVO userInfo);

    /**
     * 查询用户个性化价格
     */
    ResponseResult queryPersonalPrice(Map<String, Object> param);
}
