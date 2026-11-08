package com.houjiang.job.mapper;

import com.houjiang.commons.vo.payment.PayFxRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName PayFxRecordMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:48
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayFxRecordMapper {

    /**
     * 批量插入支付记录。
     *
     * @param payFxRecordList 需要插入的支付记录列表，每个支付记录包含支付相关的详细信息。
     * @return 返回插入操作影响的行数。这可以用于确认有多少记录被成功插入。
     */
    int insertBatch(@Param("records") List<PayFxRecordVO> payFxRecordList);
}

