package com.houjiang.job.mapper;

import com.houjiang.job.vo.AppJiPushOrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @ClassName AppJiPushMapper
 * @Description APP推送
 * @Author Amos
 * @Date 2023/5/17 14:04
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface AppJiPushMapper {

    /**
     * 查询订单签收 APP推送
     *
     * @return
     */
    List<AppJiPushOrderVO> queryOrderLogistics();

}
