package com.houjiang.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.job.entity.DcenterConsumeDepositEntity;
import com.houjiang.job.vo.DcenterConsumeDepositVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.job.entity.DcenterConsumeDeposit
 * @author Amos
 */
@Mapper
public interface DcenterConsumeDepositMapper extends BaseMapper<DcenterConsumeDepositEntity> {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<DcenterConsumeDepositVO>  getDecenterConsumeDepositVOList(@Param("pageNum") Integer pageNum, @Param("pageSize")Integer pageSize);

}




