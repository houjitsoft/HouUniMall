package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseCountryDTO;
import com.houjiang.base.entity.BaseCurrencySymbolEntity;
import com.houjiang.base.mapper.BaseCountryMapper;
import com.houjiang.base.entity.BaseCountryEntity;
import com.houjiang.base.mapper.BaseCurrencySymbolMapper;
import com.houjiang.base.service.BaseCountryService;
import com.houjiang.base.vo.BaseCountryVO;
import com.houjiang.base.vo.CountryAndCurrencyVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PinyinUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import io.seata.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName BaseCountryServiceImpl
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
@Service
public class BaseCountryServiceImpl extends ServiceImpl<BaseCountryMapper, BaseCountryEntity> 
        implements BaseCountryService {

    @Autowired
    private BaseCurrencySymbolMapper baseCurrencySymbolMapper;

    /**
     * 查询所有国家信息
     * <p>
     * 该方法用于分页查询国家信息，根据传入的国家信息查询参数和分页参数，返回查询结果
     *
     * @param baseCountryDTO 国家信息查询参数对象，包含需要查询的国家信息和分页参数
     * @return 返回查询结果，包括国家信息列表和总记录数等分页信息
     */
    @Override
    public ResponseResult queryCountryAll(BaseCountryDTO baseCountryDTO) {
        // 创建分页对象，传入页面和每页大小
        Page<BaseCountryVO> page = new Page<>(baseCountryDTO.getPage(), baseCountryDTO.getLimit());
        // 调用Mapper层方法，执行分页查询
        IPage<BaseCountryVO> result = this.baseMapper.queryCountryAll(page, baseCountryDTO);
        // 返回查询结果，使用PageUtils工具类处理分页数据
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 更新国家信息
     *
     * 根据提供的国家信息DTO，更新或插入国家信息如果国家ID存在，则更新国家信息；
     * 否则，插入新的国家信息此方法首先验证用户信息，然后根据DTO中的数据更新数据库
     *
     * @param baseCountryDTO 包含国家信息的DTO对象，用于更新或插入国家信息
     * @return 返回更新结果如果更新成功，返回成功响应；否则，返回失败响应
     */
    @Override
    public ResponseResult updateCountry(BaseCountryDTO baseCountryDTO) {
        // 获取用户信息，以验证令牌有效性
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseCountryDTO.getToken());
        baseCountryDTO.setCountryInitial(PinyinUtils.ToFirstChar2(baseCountryDTO.getCountryName()));
        // 如果用户信息不为空，继续处理国家信息更新请求
        if (null != userInfo) {
            // 判断是否提供了国家ID，以决定是插入新记录还是更新现有记录
            if(null == baseCountryDTO.getId()){
                // 如果没有ID，创建一个新的国家实体，并从DTO复制属性，然后插入数据库
                BaseCountryEntity baseCountryEntity = new BaseCountryEntity();
                BeanUtils.copyProperties(baseCountryDTO,baseCountryEntity);
                this.baseMapper.insert(baseCountryEntity);
            }else{
                // 如果有ID，根据DTO中的信息更新数据库中的国家信息
                this.baseMapper.update(null,new LambdaUpdateWrapper<BaseCountryEntity>()
                        .set(null != baseCountryDTO.getCountryName(),BaseCountryEntity::getCountryName,baseCountryDTO.getCountryName())
                        .set(null != baseCountryDTO.getInternationalDialingCode(),BaseCountryEntity::getInternationalDialingCode,baseCountryDTO.getInternationalDialingCode())
                        .set(null != baseCountryDTO.getCountryInitial(),BaseCountryEntity::getCountryInitial,baseCountryDTO.getCountryInitial())
                        .set(null != baseCountryDTO.getCountryCommonUse(), BaseCountryEntity::getCountryCommonUse,baseCountryDTO.getCountryCommonUse())
                        .eq(BaseCountryEntity::getId,baseCountryDTO.getId()));
            }
            return ResponseResult.success();
        }
        // 如果令牌无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 删除国家信息
     *
     * 本方法通过验证用户令牌来确保操作的安全性如果令牌有效，则执行删除国家信息的操作；
     * 否则，返回一个表示令牌非法的错误响应这有效地防止了未授权的删除操作
     *
     * @param baseCountryDTO 包含待删除国家ID和用户令牌的传输对象
     * @return 返回一个ResponseResult对象，表示删除操作的结果如果删除成功，返回成功响应；
     *         如果令牌无效，返回一个表示令牌非法的错误响应
     */
    @Override
    public ResponseResult deleteCountry(BaseCountryDTO baseCountryDTO) {
        // 获取用户信息，以验证令牌有效性
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseCountryDTO.getToken());
        // 如果用户信息不为空，继续处理国家信息更新请求
        if (null != userInfo) {
            // 执行删除操作
            this.baseMapper.deleteById(baseCountryDTO.getId());
            // 返回成功响应
            return ResponseResult.success();
        }
        // 如果令牌无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 查询国家和货币信息
     *
     * 本方法用于获取所有国家和货币的信息，并将其封装到CountryAndCurrencyVO对象中
     * @return ResponseResult 包含国家和货币信息的响应结果对象
     */
    @Override
    public ResponseResult queryCountryAndCurrency() {
        // 实例化CountryAndCurrencyVO对象，用于存储查询结果
        CountryAndCurrencyVO countryAndCurrencyVO = new CountryAndCurrencyVO();

        // 查询所有国家信息
        List<BaseCountryEntity> baseCountryEntities = this.baseMapper.selectList(new QueryWrapper<>());

        // 查询所有货币信息
        List<BaseCurrencySymbolEntity> baseCurrencySymbolEntities = baseCurrencySymbolMapper.selectList(new QueryWrapper<>());

        // 将查询到的国家和货币信息设置到CountryAndCurrencyVO对象中
        countryAndCurrencyVO.setBaseCountryEntities(baseCountryEntities);
        countryAndCurrencyVO.setBaseCurrencySymbolEntities(baseCurrencySymbolEntities);

        // 返回包含查询结果的响应结果对象
        return ResponseResult.success(countryAndCurrencyVO);
    }

    /**
     * 查询运输模板中使用的货币信息
     *
     * 该方法用于从数据库中获取运输模板中使用的所有货币符号实体
     * 它通过调用BaseCurrencySymbolMapper中的同名方法来实现
     *
     * @return ResponseResult 包含货币符号实体列表的响应结果对象
     */
    @Override
    public ResponseResult transportTemplateQueryCurrency() {
        // 从数据库中查询并获取运输模板中使用的货币符号实体列表
        List<BaseCountryEntity> baseCurrencySymbolEntities = this.baseMapper.transportTemplateQueryCurrency();
        // 返回包含查询结果的成功响应
        return ResponseResult.success(baseCurrencySymbolEntities);
    }

    /**
     * 根据国家ID查询国家信息
     *
     * @param countryId 国家ID，用于查询特定国家的信息
     * @return ResponseResult 包含查询结果的响应对象
     */
    @Override
    public ResponseResult queryCountryById(Integer countryId) {
        // 调用baseMapper中的方法查询指定国家ID的国家信息
        Integer count = this.baseMapper.queryCountryById(countryId);
        // 返回包含查询结果的响应对象
        return ResponseResult.success(count);
    }

    /**
     * 查询国家名称 feign
     * @param countryIds
     * @return
     */
    @Override
    public List<String> queryCountryName(List<Integer> countryIds) {
        return this.baseMapper.queryCountryName(countryIds);
    }

    /**
     * 按常用、国家首字母排序查询国家
     * @param baseCountryDTO
     * @return
     */
    @Override
    public ResponseResult queryCountryByInitial(BaseCountryDTO baseCountryDTO) {
        // 首字母
        List<String> countryInitial = new ArrayList<>();
        // 国家
        List<List<BaseCountryEntity>> notCommonUseCountry = new ArrayList<>();
        Map<String,Object> result = new HashMap<>();
        // 查询常用国家 按国家首字母排序
        List<BaseCountryEntity> commonUseCountry = this.baseMapper.selectList(new LambdaQueryWrapper<BaseCountryEntity>()
                .eq(BaseCountryEntity :: getCountryCommonUse,1)
                .like(StringUtils.isNotBlank(baseCountryDTO.getCountryName()),BaseCountryEntity :: getCountryName,baseCountryDTO.getCountryName())
                .orderByAsc(BaseCountryEntity :: getCountryInitial));
        if(null != commonUseCountry && commonUseCountry.size() > 0){
            countryInitial.add("★");
            notCommonUseCountry.add(commonUseCountry);
        }
        // 查询所有国家 国家首字母 分组 按字母排序
        List<BaseCountryEntity> allNotCommonUseCountry =  this.baseMapper.queryAllCountry(baseCountryDTO);
        // 使用 Stream API 按 countryInitial 分组，并按字母顺序排序
        // 使用 TreeMap 自动排序键
        Map<String, List<BaseCountryEntity>> groupedByInitial = new TreeMap<>(
                allNotCommonUseCountry.stream()
                        // 先分组
                        .collect(Collectors.groupingBy(BaseCountryEntity::getCountryInitial))
        );
        groupedByInitial.forEach((initial, countries) -> {
            countryInitial.add(initial);
            notCommonUseCountry.add(countries);
        });
        result.put("countryInitial",countryInitial);
        result.put("notCommonUseCountry",notCommonUseCountry);
        return ResponseResult.success(result);
    }
}

