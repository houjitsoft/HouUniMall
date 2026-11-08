package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.VkQuerySettingDTO;
import com.houjiang.userms.entity.UserVkSetEntity;
import com.houjiang.userms.vo.VKSetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserVkSetMapper
 * @Description 微客配置表
 * @Author Amos
 * @Date 2023-06-05 15:01:39
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserVkSetMapper extends BaseMapper<UserVkSetEntity> {

    /**
     * 查询微客推广配置
      * @param vkSetId
     * @return
     */
    VKSetVO selectByPrimaryKey(Integer vkSetId);


    /**
     * 开启或者关闭微客设置
     * @param param
     */
    void insertEnableVk(@Param("param") VkQuerySettingDTO param);


    /**
     * TODO 补充方法说明
     */
    void updateEnableVk(@Param("param")VkQuerySettingDTO param);


    /**
     * 查询分佣设置
     */
    Map<String,Object> commissionSetting();

    /**
     * TODO 补充方法说明
     */
    void insertVk(@Param("param")VkQuerySettingDTO param);

    /**
     * TODO 补充方法说明
     */
    void updateVk(@Param("param")VkQuerySettingDTO param);


    /**
     * 插入微客设置
     * @param param
     */
    void insertSetting(@Param("param") VkQuerySettingDTO param);

    /**
     * 更新微客设置
     * @param param
     */
    void updateSetting(@Param("param") VkQuerySettingDTO param);
}
