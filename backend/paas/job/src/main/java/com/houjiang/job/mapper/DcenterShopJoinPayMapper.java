package com.houjiang.job.mapper;

import com.houjiang.job.entity.DcenterShopJoinPayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.vo.DcenterShopJoinPayVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterShopJoinPay
 * @author Amos
 */
@Mapper
public interface DcenterShopJoinPayMapper extends BaseMapper<DcenterShopJoinPayEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterShopJoinPayVO> getDecenterShopJoinPayVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




