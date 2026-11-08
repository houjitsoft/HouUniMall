package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.goods.dto.GoodsTypeDTO;
import com.houjiang.goods.dto.MerchantGoodsTypeDTO;
import com.houjiang.goods.entity.GoodsTypeBrandEntity;
import com.houjiang.goods.entity.GoodsTypeEntity;
import com.houjiang.goods.mapper.GoodsBrandMapper;
import com.houjiang.goods.mapper.GoodsTypeBrandMapper;
import com.houjiang.goods.mapper.GoodsTypeMapper;
import com.houjiang.goods.service.GoodsTypeService;
import com.houjiang.goods.vo.GoodsAddTypeVO;
import com.houjiang.goods.vo.GoodsBrandByCatIdVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsTypeServiceImpl extends ServiceImpl<GoodsTypeMapper, GoodsTypeEntity>
        implements GoodsTypeService {

    @Autowired
    private GoodsTypeBrandMapper goodsTypeBrandMapper;

    @Autowired
    private GoodsBrandMapper goodsBrandMapper;

    /**
     * 根据条件查询商品类型列表。
     *
     * @param merchantGoodsTypeReq 商品类型查询条件
     * @return 查询结果
     */
    @Override
    public ResponseResult queryGoodsTypeList(MerchantGoodsTypeDTO merchantGoodsTypeReq) {
        try {
            if (merchantGoodsTypeReq != null) {
                // 将查询条件转换为Map格式
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(merchantGoodsTypeReq);
                // 如果存在分页条件
                if (merchantGoodsTypeReq.getPage() != null && merchantGoodsTypeReq.getLimit() != null) {
                    // 执行分页查询
                    //分页查询商品类型数据
                    IPage<GoodsTypeEntity> page = this.baseMapper.queryGoodsTypePage(new Query<GoodsTypeEntity>().getPage(data), merchantGoodsTypeReq);
                    return ResponseResult.success(PageUtils.getPage(page));
                } else {
                    // 执行不分页查询
                    List<GoodsTypeEntity> list = this.baseMapper.queryGoodsTypeList(merchantGoodsTypeReq);
                    return ResponseResult.success(list);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 根据ID查询商品类型及其关联品牌信息。
     *
     * @param merchantGoodsTypeReq 商品类型查询条件，包含商品类型ID
     * @return 商品类型及其关联品牌信息
     */
    @Override
    public ResponseResult queryGoodsTypeById(MerchantGoodsTypeDTO merchantGoodsTypeReq) {
        if (merchantGoodsTypeReq != null) {
            // 查询商品类型信息
            // 查询商品类型
            GoodsTypeEntity goodsType = this.baseMapper.selectTypeById(merchantGoodsTypeReq.getTypeId());
            // 查询关联品牌信息
            // 查询选中品牌
            List<GoodsTypeBrandEntity> typeBrands = goodsTypeBrandMapper.selectTypeBrand(merchantGoodsTypeReq.getTypeId());
            // 查询所有品牌信息
            // 查询所有的品牌
            List<GoodsBrandByCatIdVO> allBrandList = goodsBrandMapper.selectAllBrand();
            // 给所有品牌匹配关联的类型品牌ID
            for (GoodsBrandByCatIdVO allBrand : allBrandList) {
                for (GoodsTypeBrandEntity typeBrand : typeBrands) {
                    if (allBrand.getBrandId().equals(typeBrand.getBrandId())) {
                        allBrand.setTypeBrandId(typeBrand.getTypeBrandId());
                    }
                }
            }
            // 构建返回结果，包含商品类型和品牌信息
            Map<String, Object> map = new HashMap<>();
            map.put("goodsType", goodsType);
            map.put("typeBrandList", allBrandList);
            return ResponseResult.success(map);
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 添加或更新商品类型及其关联品牌信息。
     *
     * @param goodsTypeAddparam 商品类型添加或更新参数
     * @return 添加或更新结果
     */
    @Override
    public ResponseResult addGoodsType(GoodsTypeDTO goodsTypeAddparam) {
        if (goodsTypeAddparam != null) {
            if (goodsTypeAddparam.getTypeId() == null) {
                // 检查类型名称是否已存在
                //查询类型名称是否存在
                Integer count = this.baseMapper.selectByEmptyName(goodsTypeAddparam.getTypeName());
                if (count == null || count == 0) {
                    // 新增商品类型
                    this.baseMapper.insertType(goodsTypeAddparam);
                    Integer typeId = goodsTypeAddparam.getTypeId();
                    // 新增关联品牌信息
                    if (!CollectionUtils.isEmpty(goodsTypeAddparam.getTypeBrandList())) {
                        for (GoodsTypeBrandEntity brand : goodsTypeAddparam.getTypeBrandList()) {
                            brand.setTypeId(typeId);
                        }
                        goodsTypeBrandMapper.insertTypeBrandBatch(goodsTypeAddparam.getTypeBrandList());
                    }
                } else {
                    return ResponseResult.fail(BizCodeEnume.TYPE_ERROR.getCode(), BizCodeEnume.TYPE_ERROR.getMsg());
                }
            } else if (200 >= goodsTypeAddparam.getTypeDisplayorder()) {
                // 更新商品类型及其关联品牌信息
                // 更新类型
                this.baseMapper.updateType(goodsTypeAddparam);
                // 更新类型品牌
                updateTypeBrand(goodsTypeAddparam);
            } else {
                return ResponseResult.fail(BizCodeEnume.DISPLAYORDER_ERROR.getCode(), BizCodeEnume.DISPLAYORDER_ERROR.getMsg());
            }
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 更新商品类型品牌信息。
     * 该方法用于处理商品类型品牌列表的新增、更新操作。首先，通过传入的参数获取类型品牌列表。
     * 遍历列表，对每个品牌实体设置类型ID，然后根据品牌ID是否存在来区分是新增还是更新操作。
     * 新增品牌列表和更新品牌列表分别被收集，并且需要被删除的品牌ID列表也被构建。
     * 最后，删除已存在的品牌信息，然后根据需要插入新的品牌信息。
     *
     * @param param 商品类型数据传输对象，包含要更新的品牌信息。
     */
    private void updateTypeBrand(GoodsTypeDTO param) {
        List<GoodsTypeBrandEntity> typeBrandList = param.getTypeBrandList();
        // 更新规格类型表
        if (!CollectionUtils.isEmpty(typeBrandList)) {
            List<Integer> removeSpecIds = Lists.newArrayList();
            List<GoodsTypeBrandEntity> addBrands = Lists.newArrayList();
            List<GoodsTypeBrandEntity> updateBrands = Lists.newArrayList();
            for (GoodsTypeBrandEntity typeBrand : typeBrandList) {
                typeBrand.setTypeId(param.getTypeId());
                if (null == typeBrand.getTypeBrandId()) {
                    addBrands.add(typeBrand);
                } else {
                    updateBrands.add(typeBrand);
                    removeSpecIds.add(typeBrand.getTypeBrandId());
                }
            }
            //删除直接已经存在的品牌信息配置
            goodsTypeBrandMapper.delectTypeSpecBrand(param.getTypeId());
            if (!CollectionUtils.isEmpty(addBrands)) {
                goodsTypeBrandMapper.insertTypeBrandBatch(addBrands);
            }
        }
    }

    /**
     * 查询商品类型及品牌信息。
     * 该方法用于查询所有商品规格和品牌信息，以供添加商品时选择使用。
     * 查询所有规格和品牌信息，并将它们封装在一个响应结果对象中返回。
     *
     * @return 包含所有商品规格和品牌信息的响应结果对象。
     */
    @Override
    public ResponseResult addTypeQuery() {
        GoodsAddTypeVO result = new GoodsAddTypeVO();
        // 查询所有品牌
        List<GoodsBrandByCatIdVO> goodsBrandList = this.baseMapper.selectAllBrand();
        result.setGoodsBrandList(goodsBrandList);

        return ResponseResult.success(result);

    }

}




