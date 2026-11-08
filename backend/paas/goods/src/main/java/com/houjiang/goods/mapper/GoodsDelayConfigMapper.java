package com.houjiang.goods.mapper;

import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName GoodsDelayConfigMapper
 * @Description 经销商品配置信息数据
 * @Author Amos
 * @Date 2023/6/27 9:36
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsDelayConfigMapper {

    /**
     * 保存经销商品配置信息数据
     * @param record
     * @return
     */
    int insertSelective(BaseGoodsConfigVO record);

    /**
     * 查询
     * @param commonId
     * @return
     */
    BaseGoodsConfigVO selectByPrimaryKey(Integer commonId);

    /**
     * 修改
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(BaseGoodsConfigVO record);
}
