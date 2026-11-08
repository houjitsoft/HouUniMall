package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseCurrencySymbolDTO;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import com.houjiang.base.vo.BaseCurrencySymbolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (BaseCurrencySymbol)表数据库访问层
 *
 * @author Amos
 * @since 2024-05-24 17:39:20
 */
@Mapper
public interface BaseCurrencySymbolMapper extends BaseMapper<BaseCurrencySymbolEntity> {

    /**
     * 查询货币符号列表
     *
     * 该方法用于分页查询货币符号信息，可以根据不同的条件进行筛选
     *
     * @param page 分页对象，包含分页查询所需的信息，如当前页数、每页大小等
     * @param currencySymbolDTO 查询条件对象，封装了查询货币符号所需的各个条件参数
     * @return 返回一个分页对象，其中包含符合查询条件的货币符号信息
     */
    IPage<BaseCurrencySymbolVO> listCurrencySymbol(Page<BaseCurrencySymbolVO> page, @Param("param") BaseCurrencySymbolDTO currencySymbolDTO);

    /**
     * 根据货币代码获取对应的基础货币符号信息。
     *
     * @param currencyCode 货币代码，用于标识特定的货币类型。
     * @return 返回一个包含基础货币符号信息的BaseCurrencySymbolVO对象。
     */
    BaseCurrencySymbolVO getByCurrencyDetection(@Param("currencyCode") String currencyCode);
}
