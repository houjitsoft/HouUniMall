package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseSensitiveRecordEntity;
import com.houjiang.base.mapper.BaseSensitiveRecordMapper;
import com.houjiang.base.service.BaseSensitiveRecordService;
import com.houjiang.base.vo.BaseSensitiveRecordVO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @ClassName BaseSensitiveRecordServiceImpl
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSensitiveRecordServiceImpl extends ServiceImpl<BaseSensitiveRecordMapper, BaseSensitiveRecordEntity>
        implements BaseSensitiveRecordService {

    /**
     * 根据条件查询基础敏感记录信息。
     *
     * @param param 查询条件，包含分页信息和敏感记录的详细条件。
     * @return 返回查询结果，包含分页信息和查询到的敏感记录。
     */
    @Override
    public ResponseResult querybaseSensitiveRecord(BaseSensitiveRecordDTO param) {
        // 初始化分页对象
        // 分页查询
        Page<BaseSensitiveRecordDTO> page = new Page<>(param.getPage(), param.getLimit());
        // 调用Mapper进行条件查询，并返回分页结果
        IPage<BaseSensitiveRecordVO> result = this.baseMapper.querybaseSensitiveRecord(page,param);
        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 添加基础敏感记录。
     *
     * @param param 添加的敏感记录信息。
     * @return 如果添加成功，则返回成功的响应结果；否则返回失败的响应结果。
     */
    @Override
    public ResponseResult addbaseSensitiveRecord(BaseSensitiveRecordDTO param) {
        // 创建实体对象
        BaseSensitiveRecordEntity entity = new BaseSensitiveRecordEntity();
        // 将DTO的属性复制到实体对象
        BeanUtils.copyProperties(param, entity);
        // 插入实体对象到数据库，并判断插入是否成功
        Boolean flag;
        flag = this.baseMapper.insert(entity) > 0;
        // 根据插入结果返回相应的响应结果
        return flag ? ResponseResult.success() : ResponseResult.fail();
    }
}
