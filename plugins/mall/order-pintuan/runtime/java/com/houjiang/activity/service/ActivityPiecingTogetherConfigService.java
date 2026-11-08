package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.PiecingTogetherConfigDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.activity.entity.ActivityPiecingTogetherConfigEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivityPiecingTogetherConfigService extends IService<ActivityPiecingTogetherConfigEntity> {

    /**
     * 删除拼团配置
     * @param param
     * @return
     */
     ResponseResult h5ConfigModelDelete(PiecingTogetherConfigDTO param);

    /**
     * 新增拼团配置
     * @param param
     * @return
     */
    ResponseResult h5ConfigModelAdd(PiecingTogetherConfigDTO param);

    /**
     * 修改拼团配置
     * @param param
     * @return
     */
    ResponseResult updateH5Config(PiecingTogetherConfigDTO param);

    /**
     * 查询拼团配置
     * @param param
     * @return
     */
    ResponseResult queryH5Config(Map<String,Object> param);

    /**
     * h5 获取拼团主页
     * @param param
     * @return
     */
    ResponseResult queryH5PinTuanIndex(WebPinTuanParamDTO param);

    /**
     * h5 获取更多拼团商品
     * @param param
     * @return
     */
    ResponseResult queryPinTuanGoodsAll(WebPinTuanParamDTO param);
}
