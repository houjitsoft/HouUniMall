package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseInformationClassifyDTO;
import com.houjiang.base.entity.BaseInformationClassifyEntity;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 资讯分类表(BaseInformationClassify)表服务接口
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
public interface BaseInformationClassifyService extends IService<BaseInformationClassifyEntity> {

    /**
     * 查询资讯分类
     *
     * @return
     */
    ResponseResult queryAllClassify();

    /**
     * 平台查询资讯分类
     *
     * @return
     */
    ResponseResult queryAllClassifyByPlatform(UserTokenBeanDTO dto);

    /**
     * 添加资讯分类
     *
     * @param param
     * @return
     */
    ResponseResult addInformationClassify(BaseInformationClassifyDTO param);

    /**
     * 删除资讯分类
     *
     * @param param
     * @return
     */
    ResponseResult deleteInformationClassify(BaseInformationClassifyDTO param);


    /**
     * 查询是否是自己经销商品
     *
     * @param param
     * @return
     */
    Integer checkSupplySelf(Map<String, Object> param);

}
