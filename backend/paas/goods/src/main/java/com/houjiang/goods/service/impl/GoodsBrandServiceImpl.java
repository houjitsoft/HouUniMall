package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.PinyinUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.goods.GoodsBrandVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.GoodsBrandDTO;
import com.houjiang.goods.entity.GoodsBrandCatEntity;
import com.houjiang.goods.entity.GoodsBrandEntity;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.mapper.GoodsBrandCatMapper;
import com.houjiang.goods.mapper.GoodsBrandMapper;
import com.houjiang.goods.mapper.GoodsCommonMapper;
import com.houjiang.goods.service.GoodsBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsBrandServiceImpl extends ServiceImpl<GoodsBrandMapper, GoodsBrandEntity>
    implements GoodsBrandService{

    @Autowired
    private GoodsCommonMapper goodsCommonMapper;
    @Autowired
    private GoodsBrandCatMapper goodsBrandCatMapper;
    /**
     * 查询所有商品品牌及数量
     *
     * @return ResponseResult 包含商品品牌列表及总数的结果对象
     */
    @Override
    public ResponseResult queryBrandGoods() {
        // 初始化一个Map用于存储商品品牌列表和总数
        Map<String,Object> map=new HashMap<>();

        // 查询所有商品品牌
        List<GoodsBrandEntity> goodsBrandList =this.baseMapper.selectList(new LambdaQueryWrapper<GoodsBrandEntity>());
        // 将商品品牌列表和总数存入Map
        map.put("goodsBrandList",goodsBrandList);
        map.put("totalCount",goodsBrandList.size());
        // 返回包含商品品牌列表和总数的结果对象
        return ResponseResult.success(map);
    }

    /**
     * 根据品牌名和品牌ID查询商品品牌
     *
     * @param map 包含品牌名和品牌ID的Map
     * @return ResponseResult 根据查询结果返回成功或失败的结果对象
     */
    @Override
    public ResponseResult selectBrandName(Map<String, Object> map) {
        // 从map中获取品牌名
        String brandName = (String) map.get("brandName");
        // 初始化品牌ID为null
        Integer brandId = null;
        // 如果map中包含品牌ID且不为空，则赋值给brandId
        if(map.get("brandId") != "" && map.get("brandId") != null){
            brandId = (Integer) map.get("brandId");
        }
        // 根据品牌名和品牌ID查询商品品牌
        List<GoodsBrandEntity> goodsBrands=this.baseMapper.selectList(new LambdaQueryWrapper<GoodsBrandEntity>()
                                                    .eq(GoodsBrandEntity::getBrandName,brandName)
                                                    .eq(GoodsBrandEntity::getBrandId,brandId));
        // 如果查询结果为空，则返回成功结果对象
        if(goodsBrands == null || goodsBrands.size() == 0){
           return ResponseResult.success();
        }else{
            // 如果查询结果不为空，则返回品牌已存在失败结果对象
            return ResponseResult.fail(BizCodeEnume.BRAND_NAME_ALREADY_EXISTS);
        }

    }

    /**
     * 删除品牌信息。
     * 该方法首先验证传入的DTO是否为空以及其中的token是否有效。如果有效，它会进一步检查该品牌是否与任何商品关联。
     * 如果品牌与商品有关联，则操作失败，因为不能删除与商品关联的品牌。如果没有关联，方法将删除品牌及其相关分类信息，
     * 并返回操作成功的消息。
     * 如果token验证失败或DTO为空，则操作直接失败，并返回相应的错误消息。
     *
     * @param dto 包含品牌ID和用于验证用户身份的token的信息传输对象。
     * @return 删除操作的结果，成功或失败，并包含相应的错误代码。
     */
    @Override
    public ResponseResult deleteBrand(GoodsBrandDTO dto) {
        // 验证DTO和token是否为空
        if (null != dto && null != dto.getToken()) {
            // 通过token获取用户信息
            String token = dto.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            // 检查用户信息是否存在
            if (null != userInfo) {
                // 查询是否有商品关联该品牌
                Integer num = Math.toIntExact(goodsCommonMapper.selectCount(new LambdaQueryWrapper<GoodsCommonEntity>().eq(GoodsCommonEntity::getBrandId, dto.getBrandId())));
                // 如果品牌与商品有关联，则删除失败
                if (num > 0) {
                    return ResponseResult.fail(BizCodeEnume.BRAND_ASSOCIATED_GOODS);
                }
                // 删除品牌和品牌分类信息
                this.baseMapper.deleteById(dto.getBrandId());
                goodsBrandCatMapper.delete(new LambdaQueryWrapper<GoodsBrandCatEntity>().eq(GoodsBrandCatEntity::getBrandId, dto.getBrandId()));
                // 返回删除成功的消息
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            // 如果token验证失败，返回相应的错误消息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 如果DTO或token为空，返回相应的错误消息
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }


    /**
     * 更新品牌信息。
     *
     * @param dto 品牌数据传输对象，包含品牌相关的信息。
     * @return 返回操作结果，成功或失败，并附带相应的错误码。
     */
    @Override
    public ResponseResult updateBrand(GoodsBrandDTO dto) {
        // 如果shopId为空，则默认设置为0
        if (null == dto.getShopId()) {
            dto.setShopId(0);
        }
        // 设置品牌为启用状态，推荐状态为是，并将品牌名的首字母作为品牌首字母
        dto.setBrandEnable(1);
        dto.setBrandRecommend(1);
        dto.setBrandInitial(PinyinUtils.ToFirstChar2(dto.getBrandName()));

        // 查询是否已存在相同品牌名但品牌ID不同的情况
        Integer integer = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<GoodsBrandEntity>()
                .eq(GoodsBrandEntity::getBrandName, dto.getBrandName())
                .ne(GoodsBrandEntity::getBrandId, dto.getBrandId())));
        // 如果存在相同品牌名，则操作失败并返回相应的错误码
        if (integer > 0) {
            return ResponseResult.fail(BizCodeEnume.BRAND_NAME_ALREADY_EXISTS);
        }

        // 如果品牌ID为空，则插入新的品牌信息
        if (null == dto.getBrandId()) {
            this.baseMapper.insertBrand(dto);
        } else {
            // 如果品牌ID不为空，则更新品牌相关信息
            goodsBrandCatMapper.updateBatchBrandCat(dto);

            // 更新商品通用信息表中的品牌名
            goodsCommonMapper.update(null,new LambdaUpdateWrapper<GoodsCommonEntity>()
                                .eq(GoodsCommonEntity::getBrandId,dto.getBrandId())
                                .set(GoodsCommonEntity::getBrandName,dto.getBrandName()));

            // 更新品牌信息
            this.baseMapper.updateBrand(dto);
        }

        // 操作成功，返回相应的成功错误码
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据条件查询商品品牌列表。
     *
     * @param dto 查询条件DTO，包含分页信息和品牌相关信息。
     * @return 查询结果，成功返回品牌列表，失败返回错误信息。
     */
    @Override
    public ResponseResult queryBrandList(GoodsBrandDTO dto) {
        try {
            // 【重要】优先从SecurityContext获取用户信息（JWT过滤器已设置）
            // 如果请求参数中有token，使用旧的验证方式（向后兼容）
            UserInfoVO userInfo = null;
            if (dto != null && dto.getToken() != null && !dto.getToken().isEmpty()) {
                // 使用旧的token验证方式（向后兼容）
                userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
            } else {
                // 从SecurityContext获取用户信息（新的统一方式）
                try {
                    userInfo = SecurityContextUtils.getCurrentUser();
                } catch (Exception e) {
                    // 如果获取失败，记录日志但不抛出异常，继续使用旧的验证方式
                }
            }
            
            // 验证用户信息
            if (userInfo == null) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }
            
            // 将查询条件DTO转换为Map，用于后续的查询
            Map data = ParseMapUtils.beanToMap(dto);
            // 执行分页查询
            IPage<GoodsBrandEntity> page = this.baseMapper.queryBrandList(new Query<GoodsBrandEntity>().getPage(data), dto);
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "查询品牌列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据品牌ID查询商品品牌详细信息。
     *
     * @param param 查询条件DTO，包含品牌ID和token信息。
     * @return 查询结果，成功返回品牌详细信息，失败返回错误信息。
     */
    @Override
    public ResponseResult queryGoodsBrandById(GoodsBrandDTO param) {
        // 根据token判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (userPermissionsResult.getFlag()) {
            // 权限校验通过，查询品牌详细信息
            GoodsBrandEntity result = this.baseMapper.selectById(param.getBrandId());
            // 返回查询结果
            return ResponseResult.success(result);
        } else {
            // 权限校验失败，返回错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 根据条件查询商品品牌列表。
     *
     * @param param 查询条件，包含品牌相关参数和用户令牌信息。
     * @return 返回查询结果，如果用户权限不足，则返回错误信息。
     */
    @Override
    public ResponseResult queryGoodsBrandList(GoodsBrandDTO param) {
        // 初始化结果列表
        List<GoodsBrandEntity> results = new ArrayList<GoodsBrandEntity>();

        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 如果用户具有足够的权限
        if (userPermissionsResult.getFlag()) {
            // 设置店铺ID，用于后续查询
            param.setShopId(Integer.valueOf(userPermissionsResult.getShopId()));
            // 根据店铺ID查询品牌列表
            results = this.baseMapper.selectByShopId(param);
        } else {
            // 如果用户权限不足，返回错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 返回查询结果成功
        return ResponseResult.success(results);
    }

    /**
     * 根据品牌id集合查询品牌集合
     *
     * @param brandIdList 品牌id集合
     * @return 品牌集合
     */
    @Override
    public List<GoodsBrandVO> selectGoodsBrandList(List<Integer> brandIdList) {
        List<GoodsBrandVO> goodsBrandList = baseMapper.selectGoodsBrandList(brandIdList);
        for (GoodsBrandVO goodsBrandVO : goodsBrandList) {
            // 获取第一个字符
            char firstChar = goodsBrandVO.getBrandName().charAt(0);
            // 检查是否在汉字的 Unicode 范围内
            boolean isChinese = (firstChar >= '\u4e00' && firstChar <= '\u9fa5');
            if (isChinese) {
                goodsBrandVO.setBrandKey(PinyinUtils.ToFirstChar2(goodsBrandVO.getBrandName()));
            } else {
                boolean isEnglish = String.valueOf(firstChar).matches("[a-zA-Z]");
                if (isEnglish) {
                    goodsBrandVO.setBrandKey(String.valueOf(firstChar).toUpperCase());
                } else {
                    goodsBrandVO.setBrandKey("A");
                }
            }
        }
        return goodsBrandList;
    }

    /**
     * 根据名称查询商品品牌列表
     *
     * 本方法重写了父类中的findListByName方法，旨在根据传入的ManageGoodsBrandDTO对象查询相应的商品品牌信息。
     * 通过调用baseMapper的findListByName方法来实现数据的查询，并将查询结果封装在ResponseResult对象中返回。
     *
     * @param param ManageGoodsBrandDTO对象，包含查询商品品牌所需的条件。
     * @return 返回包含查询结果的ResponseResult对象，查询结果为GoodsBrandVO类型的列表。
     */
    @Override
    public ResponseResult findListByName(GoodsBrandDTO param) {
        // 调用baseMapper的findListByName方法查询符合条件的商品品牌列表
        List<GoodsBrandVO> list = this.baseMapper.findListByName(param);
        // 将查询结果封装在ResponseResult对象中返回
        return ResponseResult.success(list);
    }
}




