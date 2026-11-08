package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseSmsInfoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @Entity com.houjiang.base.entity.BaseSmsInfo
 * @author Amos
 */
@Mapper
public interface BaseSmsInfoMapper extends BaseMapper<BaseSmsInfoEntity> {

    /**
     * 更新短信信息
     *
     * 该方法用于更新短信的相关信息它通过接受一个参数映射来实现，
     * 其中包含需要更新的短信的各种属性和值此方法不返回任何值
     *
     * @param param 包含短信更新信息的键值对映射
     *              键是需要更新的属性名称，值是相应的更新后值
     */
    void updateSMS(Map<String, String> param);
}




