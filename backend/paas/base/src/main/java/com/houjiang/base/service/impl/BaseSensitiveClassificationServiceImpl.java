package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseSensitiveClassificationDTO;
import com.houjiang.base.entity.BaseSensitiveClassificationEntity;
import com.houjiang.base.entity.BaseSensitiveEntity;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseSensitiveClassificationMapper;
import com.houjiang.base.mapper.BaseSensitiveMapper;
import com.houjiang.base.service.BaseSensitiveClassificationService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BaseSensitiveClassificationServiceImpl
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSensitiveClassificationServiceImpl extends ServiceImpl<BaseSensitiveClassificationMapper, BaseSensitiveClassificationEntity>
        implements BaseSensitiveClassificationService {

    @Autowired
    private BaseSensitiveMapper baseSensitiveMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BaseSensitiveServiceImpl baseSensitiveService;

    /**
     * 根据条件查询敏感分类信息。
     *
     * @param param 查询条件DTO，包含分页信息和查询条件。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult querySensitiveClassification(BaseSensitiveClassificationDTO param) {
        // 初始化分页对象
        Page<BaseSensitiveClassificationEntity> page = new Page<>(param.getPage(), param.getLimit());
        // 执行查询，返回IPage结果
        IPage<BaseSensitiveClassificationEntity> result = this.baseMapper.querySensitiveClassification(page,param);
        // 将查询结果转换为响应结果返回
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 查询所有启用的敏感分类。
     *
     * @return 返回所有启用的敏感分类的响应对象。
     */
    @Override
    public ResponseResult querySensitiveClassificationAll() {
        // 使用LambdaQueryWrapper查询所有启用状态的敏感分类，并按添加时间降序排序
        List<BaseSensitiveClassificationEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSensitiveClassificationEntity>()
                .eq(BaseSensitiveClassificationEntity::getClassificationState, CommonType.commonStatus.YES.getCode())
                .orderByDesc(BaseSensitiveClassificationEntity::getClassificationAddTime));
        // 将查询结果转换为响应结果返回
        return ResponseResult.success(list);
    }

    /**
     * 添加或更新敏感分类。
     *
     * @param param 添加或更新敏感分类的DTO对象。
     * @return 返回操作结果的响应对象。
     */
    @Override
    public ResponseResult add(BaseSensitiveClassificationDTO param) {
        BaseSensitiveClassificationEntity entity = new BaseSensitiveClassificationEntity();
        // 使用BeanUtils复制DTO属性到实体对象
        BeanUtils.copyProperties(param, entity);
        Boolean flag;
        // 判断是否为新增操作
        if(null == param.getClassificationId()){
            // 新增操作时设置添加时间
            entity.setClassificationAddTime(new Date());
            // 执行插入操作并判断结果
            flag = this.baseMapper.insert(entity) > 0;
        }else{
            // 更新操作时
            flag = this.baseMapper.updateById(entity) > 0;
            // 如果禁用敏感词分类，则需要禁用该分类下的所有敏感词并从ES中删除
            // 查询当前分类下的敏感词ID列表
            List<Integer> sensitiveIdList  = baseSensitiveMapper.selectList(new LambdaQueryWrapper<BaseSensitiveEntity>()
                    .eq(BaseSensitiveEntity::getSensitiveClassificationId, param.getClassificationId()))
                    .stream().map(BaseSensitiveEntity::getId).collect(Collectors.toList());
            // 如果有敏感词ID列表，则执行禁用和删除操作
            if(null != sensitiveIdList && sensitiveIdList.size() > 0){
                // 禁用敏感词
                baseSensitiveMapper.updateSensitiveStart(sensitiveIdList);
                // 从ES中删除敏感词
                goodsClient.deletSensitiveList(sensitiveIdList);

                // 查询要禁用的敏感词
                List<String> sensitive = baseSensitiveMapper.querySensitiveByIdList(sensitiveIdList);
                // 从ES远程词库中删除敏感词
                baseSensitiveService.deleteSensitiveWords(sensitive);
            }
        }
        // 根据操作结果返回响应对象
        return flag ? ResponseResult.success() : ResponseResult.fail();
    }

    /**
     * 删除敏感信息分类。
     *
     * @param param 删除操作的参数对象，包含待删除的敏感信息分类ID。
     * @return 删除操作的结果。如果分类下存在使用中的敏感信息，则操作失败；否则，操作成功。
     */
    @Override
    public ResponseResult delete(BaseSensitiveClassificationDTO param) {
        // 根据分类ID查询所有关联的敏感信息实体
        List<BaseSensitiveEntity> entityList  = baseSensitiveMapper.selectList(new LambdaQueryWrapper<BaseSensitiveEntity>()
                .eq(BaseSensitiveEntity::getSensitiveClassificationId, param.getClassificationId()));

        // 检查是否存在使用中的敏感信息，如果存在则不能删除
        if(null != entityList && entityList.size() > 0 ){
            return ResponseResult.fail(BizCodeEnume.IS_USE_NOT_DELETE.getCode(),BizCodeEnume.IS_USE_NOT_DELETE.getMsg());
        }

        // 删除指定的敏感信息分类
        this.baseMapper.deleteById(param.getClassificationId());

        return ResponseResult.success();
    }
}
