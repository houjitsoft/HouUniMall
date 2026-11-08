package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseCurrencySymbolDTO;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import com.houjiang.base.mapper.BaseCurrencySymbolMapper;
import com.houjiang.base.service.BaseCurrencySymbolService;
import com.houjiang.base.vo.BaseCurrencySymbolVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisPrefixUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * (BaseCurrencySymbol)表服务实现类
 *
 * @author Amos
 * @since 2024-05-24 17:39:21
 */
@Service
public class BaseCurrencySymbolServiceImpl extends ServiceImpl<BaseCurrencySymbolMapper, BaseCurrencySymbolEntity>
        implements BaseCurrencySymbolService {

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 查询货币符号列表
     *
     * @param currencySymbolDTO 货币符号查询条件，包含分页信息和查询条件
     * @return 返回查询结果，其中包含分页信息和查询到的货币符号列表
     */
    @Override
    public ResponseResult<PageUtils<BaseCurrencySymbolVO>> listCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO) {
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = currencySymbolDTO.getPage() != null ? currencySymbolDTO.getPage() : 1;
        Integer limit = currencySymbolDTO.getLimit() != null ? currencySymbolDTO.getLimit() : 10;
        
        // 初始化分页查询参数
        Page<BaseCurrencySymbolVO> pageObj = new Page<>(page, limit);
        // 执行分页查询
        IPage<BaseCurrencySymbolVO> result = this.baseMapper.listCurrencySymbol(pageObj, currencySymbolDTO);
        // 构造并返回成功响应结果，包含分页数据
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 获取并打开货币符号
     * 该方法首先尝试从Redis中获取货币符号，如果不存在，则从数据库中查询一个状态为1的货币符号，并将其存储到Redis中。
     *
     * @return ResponseResult 包含货币符号的成功响应结果
     */
    @Override
    public ResponseResult<BaseCurrencySymbolEntity> openCurrencySymbol() {
        // 尝试从Redis获取货币符号
        String currencySymbol = (String) redisCache.getCacheObject(RedisPrefixUtils.currencySymbol);
        // 如果Redis中没有货币符号，则从数据库中查询
        if (StringUtils.isEmpty(currencySymbol)) {
            // 从数据库中选择状态为1的货币符号实体
            BaseCurrencySymbolEntity entity = this.baseMapper.selectOne(new QueryWrapper<BaseCurrencySymbolEntity>()
                    .eq("status", 1));
            // 将获取到的货币符号存储到Redis中
            redisCache.setCacheObject(RedisPrefixUtils.currencySymbol, JSON.toJSONString(entity));
            return ResponseResult.success(entity);
        }
        // 返回成功响应结果，包含货币符号
        return ResponseResult.success(JSON.parseObject(currencySymbol,BaseCurrencySymbolEntity.class));
    }

    /**
     * 插入货币符号信息
     *
     * @param currencySymbolDTO 包含货币符号及相关属性的DTO对象
     * @return 返回操作结果，成功则返回成功信息，失败则返回错误信息
     */
    @Override
    public ResponseResult insertCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO){
        try {
            // 检查货币符号参数是否为空
            if(StringUtils.isEmpty(currencySymbolDTO.getCurrencySymbol())){
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }
            BaseCurrencySymbolEntity currencySymbolEntity = new BaseCurrencySymbolEntity();
            // 复制DTO对象的属性到实体对象
            BeanUtils.copyProperties(currencySymbolDTO, currencySymbolEntity);
            // 尝试插入实体到数据库，并判断操作是否成功
            boolean success = this.baseMapper.insert(currencySymbolEntity) > 0;
            if (success){
                // 插入成功，返回成功结果
                return ResponseResult.success();
            }
            // 插入失败，返回错误结果
            return ResponseResult.fail(BizCodeEnume.ADD_ERROR);
        } catch (Exception e) {
            log.error("插入货币符号信息失败", e);
            // 返回通用错误信息，或者根据异常类型返回更具体的错误信息
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR);
        }
    }

    /**
     * 更新货币符号信息
     *
     * @param currencySymbolDTO 货币符号数据传输对象，包含需要更新的货币符号信息
     * @return 返回操作结果，成功则返回带有成功标识的ResponseResult，失败则返回带有错误码的ResponseResult
     */
    @Override
    public ResponseResult updateCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO){
        try {
            // 检查货币符号参数是否为空
            Integer primaryKey = currencySymbolDTO.getId();
            if(primaryKey < 0){
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
            }
            // 检查货币符号参数是否为空
            if(StringUtils.isEmpty(currencySymbolDTO.getCurrencySymbol())){
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }
            BaseCurrencySymbolEntity baseCurrencySymbolEntity  = this.baseMapper.selectById(primaryKey);
            // 检查基础货币符号实体和货币符号DTO的状态是否满足特定条件。
            if (baseCurrencySymbolEntity.getStatus() == 1 && currencySymbolDTO.getStatus() == 0){
                return ResponseResult.fail(BizCodeEnume.IS_USE_NOT_UPDATE_STATUS);
            }
            // status状态为1的情况只能为一条记录，如果更新字段中有状态字段，则需要判断状态是否为1
            if (currencySymbolDTO.getStatus() == 1){
                // 创建 UpdateWrapper 并设置条件为始终为真
                UpdateWrapper<BaseCurrencySymbolEntity> updateWrapper = new UpdateWrapper<>();
                // 这里的 "1=1" 相当于 SQL 中的 WHERE 1=1，匹配所有记录
                updateWrapper.eq("1", "1");
                updateWrapper.set("status", 0);
                this.baseMapper.update(null, updateWrapper);
                redisCache.setCacheObject(RedisPrefixUtils.currencySymbol, JSON.toJSONString(currencySymbolDTO));
            }
            BaseCurrencySymbolEntity currencySymbolEntity = new BaseCurrencySymbolEntity();
            // 复制DTO对象的属性到实体对象
            BeanUtils.copyProperties(currencySymbolDTO,currencySymbolEntity);
            // 尝试更新实体到数据库，并判断操作是否成功
            boolean success = this.baseMapper.updateById(currencySymbolEntity) > 0;
            if (success){
                // 更新成功，返回成功结果
                return ResponseResult.success();
            }
            // 更新失败，返回错误结果
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        } catch (Exception e) {
            // 捕获异常，返回通用错误信息，或者根据异常类型返回更具体的错误信息
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR);
        }
    }

    /**
     * 删除货币符号信息
     *
     * @param currencySymbolDTO 货币符号数据传输对象，包含需要更新的货币符号信息
     * @return 返回操作结果，成功则返回带有成功标识的ResponseResult，失败则返回带有错误码的ResponseResult
     */
    @Override
    public ResponseResult deleteCurrencySymbol(BaseCurrencySymbolDTO currencySymbolDTO){
        // 检查货币符号参数是否为空
        Integer primaryKey = currencySymbolDTO.getId();
        if(primaryKey < 0){
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }
        BaseCurrencySymbolEntity baseCurrencySymbolEntity  = this.baseMapper.selectById(primaryKey);
        // 如果状态为1，则不能删除
        if (baseCurrencySymbolEntity.getStatus() == 1){
            return ResponseResult.fail(BizCodeEnume.IS_USE_NOT_DELETE);
        }
        boolean success = this.baseMapper.deleteById(primaryKey) > 0;
        if (success){
            // 删除，返回成功结果
            return ResponseResult.success();
        }
        // 更新失败，返回错误结果
        return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);

    }

    /**
     * 根据给定的货币标识符查询对应的货币信息。
     *
     * 该函数通过调用数据库查询方法，获取与指定货币标识符匹配的货币信息，
     * 并将查询结果封装在ResponseResult对象中返回。
     *
     * @param currencyCode 货币标识符，用于在数据库中查找对应的货币信息。
     * @return ResponseResult 包含查询结果的响应对象，若查询成功则返回货币信息，否则返回空或错误信息。
     */
    @Override
    public ResponseResult getByCurrencyDetection(String currencyCode) {
        // 查询数据库中与给定货币标识符匹配的货币信息
        BaseCurrencySymbolVO entity = this.baseMapper.getByCurrencyDetection(currencyCode);

        // 返回查询结果，使用ResponseResult的success方法封装查询结果
        return ResponseResult.success(entity);
    }
}
