package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.VkQuerySettingDTO;
import com.houjiang.userms.entity.UserVkSetEntity;

/**
 * @ClassName UserVkSetService
 * @Description
 * @Author Amos
 * @Date 2023/7/14 10:22
 * @Version 1.0
 * @author Amos
 */

public interface UserVkSetService extends IService<UserVkSetEntity> {

    /**
     * 开启或者关闭微客
     *
     * @param dto
     * @return
     */
    ResponseResult enableVk(VkQuerySettingDTO dto);

    /**
     * 添加或者修改微客设置
     */
    ResponseResult addOrUpdateVkQuerySetting(VkQuerySettingDTO param);

    /**
     * 查询微客的设置
     */
    ResponseResult vkQuerySetting();
}
