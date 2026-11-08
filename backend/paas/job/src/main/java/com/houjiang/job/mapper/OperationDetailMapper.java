package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName OperationDetailMapper
 * @Description
 * @Author Amos
 * @Date 2023/5/17 14:28
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OperationDetailMapper {

    /**
     * 删除
     */
    void delete();
}
