package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.vo.decorate.DecorateDataArticleVo;

import java.util.List;

/**
 * 装修数据服务接口类
 */
public interface IDecorateDataService {

    /**
     * 获取文章数据
     *
     * @author Amos
     * @param limit 条数
     * @return List<DecorateArticleDataVo>
     */
    List<DecorateDataArticleVo> article(Integer limit);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    JSONObject pc();
}
