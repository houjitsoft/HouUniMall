package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.ModularizationdCatDTO;
import com.houjiang.base.dto.ModularizationdCatDataDTO;
import com.houjiang.base.entity.BaseModularizationdCatEntity;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseModularizationdCatMapper;
import com.houjiang.base.service.BaseModularizationdCatService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.goods.ModularizationdGoodsDataDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseModularizationdCatServiceImpl extends ServiceImpl<BaseModularizationdCatMapper, BaseModularizationdCatEntity>
    implements BaseModularizationdCatService{

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    @Autowired
    private GoodsClient goodsClient;
    /**
     * 根据传入的参数查询所有模块化分类信息。
     *
     * @param param 包含查询条件和分页信息的DTO对象。
     * @return 返回查询结果的响应对象。
     * @throws IllegalAccessException 如果访问字段失败抛出此异常。
     */
    @Override
    public ResponseResult selectAll(ModularizationdCatDTO param) throws IllegalAccessException {
        // 从Redis中根据token获取用户信息
        //获取传来的token
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        // 如果用户信息为空，则返回token无效的错误响应
        if(null == userInfoStr){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将DTO对象转换为Map，用于构建查询条件
        Map data= ParseMapUtils.beanToMap(param);
        // 执行查询操作，获取分页结果
        IPage<BaseModularizationdCatEntity> page=this.baseMapper.selectAllModularizationdCat(new Query<BaseModularizationdCatEntity>().getPage(data),
                param);
        // 构建成功响应，并返回分页结果
        return ResponseResult.success(PageUtils.getPage(page));

    }

    /**
     * 添加模块化分类信息。
     *
     * @param param 包含待添加模块化分类信息的DTO对象。
     * @return 返回操作结果的响应对象。
     */
    @Override
    public ResponseResult addModularizationdCat(ModularizationdCatDTO param) {
        // 从Redis中根据token获取用户信息
        //获取传来的token
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        // 如果用户信息为空，则返回token无效的错误响应
        if(null == userInfoStr){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 执行添加操作
        this.baseMapper.addModularizationdCat(param);
        // 构建成功响应，并返回操作成功的提示信息
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新模块化分类信息。
     *
     * @param param 包含待更新模块化分类信息的DTO对象。
     * @return 返回操作结果的响应对象。
     */
    @Override
    public ResponseResult updateModularizationdCat(ModularizationdCatDTO param) {
        // 从Redis中根据token获取用户信息
        //获取传来的token
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        // 如果用户信息为空，则返回token无效的错误响应
        if(null == userInfoStr){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 执行更新操作
        this.baseMapper.updateModularizationdCat(param);
        // 构建成功响应，并返回操作成功的提示信息
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 删除模块化分类
     * @param param 包含分类ID和用户token的信息
     * @return 删除操作的结果，成功或失败
     */
    @Override
    public ResponseResult deleteModularizationdCat(ModularizationdCatDTO param) {
        // 从Redis中获取用户信息，验证token的有效性
        //获取传来的token
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        if(null == userInfoStr){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        this.baseMapper.deleteById(param.getId());
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新页面应用状态
     * @param param 包含页面ID、起始页等信息
     * @return 更新操作的结果，成功或失败
     */
    @Override
    public ResponseResult updatePageApply(ModularizationdCatDTO param) {
        // 从Redis中获取用户信息，验证token的有效性
        //获取传来的token
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + param.getToken());
        if(null == userInfoStr){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 将所有页面状态设置为使用中
        //修改所有页面为为使用状态
        this.baseMapper.update(null,new LambdaUpdateWrapper<BaseModularizationdCatEntity>()
                                .set(BaseModularizationdCatEntity::getPageStart,2));
        // 将指定页面状态设置为传入的起始页
        //将前端传入的页面设置为使用中
        //页面状态 1 使用中2 未使用

        this.baseMapper.update(null,new LambdaUpdateWrapper<BaseModularizationdCatEntity>()
                                 .set(BaseModularizationdCatEntity::getPageStart,param.getPageStart())
                                 .eq(BaseModularizationdCatEntity::getId,param.getId()));
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据ID查询页面数据
     * @param ModularizationdCatDTO 包含要查询的页面ID
     * @return 查询到的页面数据
     */
    @Override
    public ResponseResult querPageDataById(ModularizationdCatDTO ModularizationdCatDTO) {
        BaseModularizationdCatEntity modularizationdCat =this.baseMapper.selectById(ModularizationdCatDTO.getId());
        return ResponseResult.success(modularizationdCat);
    }

    /**
     * 分析页面数据
     * @param ModularizationdCatDTO 包含页面及其子页面数据的信息
     * @return 分析后的页面数据
     */
    @Override
    public ResponseResult analysisPageData(ModularizationdCatDataDTO ModularizationdCatDTO) {
        if(null != ModularizationdCatDTO ){
            // 解析商品数据
            //解析补充 商品数据
            analysisGoodsData(ModularizationdCatDTO);
            // 如果有子页面，递归分析子页面数据
            //判断是否有下级 补充下级商品数据
            if(null != ModularizationdCatDTO.getModularizationdCatDataList() && ModularizationdCatDTO.getModularizationdCatDataList().size() > 0){
                List<ModularizationdCatDataDTO> modularizationdCatDataList = ModularizationdCatDTO.getModularizationdCatDataList();
                for (ModularizationdCatDataDTO modularizationdCatData : modularizationdCatDataList) {
                    analysisGoodsData(modularizationdCatData);
                }
            }
        }
        return ResponseResult.success(ModularizationdCatDTO);
    }

    /**
     * 查询已应用的页面
     * @return 第一个起始状态为1的页面
     */
    @Override
    public ResponseResult queryApplyPage() {
        BaseModularizationdCatEntity entity=this.baseMapper.selectOne(new LambdaQueryWrapper<BaseModularizationdCatEntity>()
                                                .eq(BaseModularizationdCatEntity::getPageStart,1));
        return ResponseResult.success(entity);
    }

    /**
     * 分析商品数据，填充商品详细信息
     * @param modularizationdCatParam 包含商品数据的信息
     */
    public void analysisGoodsData(ModularizationdCatDataDTO modularizationdCatParam) {
        // 如果存在商品数据，则查询每个商品的详细信息并填充
        //查询商品对象中 商品数据
        if (null != modularizationdCatParam.getGoodsDataList() && modularizationdCatParam.getGoodsDataList().size() > 0) {
            List<ModularizationdGoodsDataDTO> goodsDataList = modularizationdCatParam.getGoodsDataList();
            for (ModularizationdGoodsDataDTO modularizationdGoodsData : goodsDataList) {
                ModularizationdGoodsDataDTO modularizationdGoodsInfo = goodsClient.queryGoodsInfo(modularizationdGoodsData);
                modularizationdGoodsData.setCommonId(modularizationdGoodsInfo.getCommonId());
                modularizationdGoodsData.setGoodsId(modularizationdGoodsInfo.getGoodsId());
                modularizationdGoodsData.setCommonImage(modularizationdGoodsInfo.getCommonImage());
                modularizationdGoodsData.setCommonName(modularizationdGoodsInfo.getCommonName());
                modularizationdGoodsData.setCommonPrice(modularizationdGoodsInfo.getCommonPrice());
            }
            // 数据回填
            modularizationdCatParam.setGoodsDataList(goodsDataList);
        }
    }
}




