package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.dto.yiyatong.CategoryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.SupplierYiyatongCatEntity;
import com.houjiang.goods.mapper.SupplierYiyatongCatMapper;
import com.houjiang.goods.service.SupplierYiyatongCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Amos
 */
@Service
public class SupplierYiyatongCatServiceImpl extends ServiceImpl<SupplierYiyatongCatMapper, SupplierYiyatongCatEntity>
    implements SupplierYiyatongCatService{

    @Autowired
    private YiYaTongServiceImpl yiYaTongService;
    @Override
    public ResponseResult synchronousCategorys(CategoryDTO categoryDTO) {


        ResponseResult catResult=yiYaTongService.getCategorys(categoryDTO);
        if(null!=catResult.getData()){
            //1级分类
            List<SupplierYiyatongCatEntity>  list1=JSONArray.parseArray(JSON.toJSONString(catResult.getData()),SupplierYiyatongCatEntity.class);
            if(list1.size()>0){
                for (SupplierYiyatongCatEntity entity1:list1){
                    this.baseMapper.insert(entity1);
                   //1级分类id
                   String level1=entity1.getId();
                    CategoryDTO categoryDTO2=new CategoryDTO();
                    categoryDTO2.setId(level1);
                    ResponseResult catResult2=yiYaTongService.getCategorys(categoryDTO2);
                    //2级分类
                    List<SupplierYiyatongCatEntity>  list2=JSONArray.parseArray(JSON.toJSONString(catResult2.getData()),SupplierYiyatongCatEntity.class);
                    if(list2.size()>0){
                        for (SupplierYiyatongCatEntity entity2:list2){
                            this.baseMapper.insert(entity2);
                            //2级分类id
                            String level2=entity2.getId();
                            CategoryDTO categoryDTO3=new CategoryDTO();
                            categoryDTO3.setId(level2);
                            ResponseResult catResult3=yiYaTongService.getCategorys(categoryDTO3);
                            //3级分类
                            List<SupplierYiyatongCatEntity>  list3=JSONArray.parseArray(JSON.toJSONString(catResult3.getData()),SupplierYiyatongCatEntity.class);
                            if(list3.size()>0){
                                for (SupplierYiyatongCatEntity entity3:list3){
                                    this.baseMapper.insert(entity3);
                                }
                            }
                        }
                    }
                }
            }


        }


        return ResponseResult.success();
    }
}




