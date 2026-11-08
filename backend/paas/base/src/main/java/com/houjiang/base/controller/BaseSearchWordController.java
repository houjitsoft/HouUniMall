package com.houjiang.base.controller;


import com.houjiang.commons.dto.goods.GoodsMerchantSearchWordDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseSearchWordController
 * @Description 搜索热门词
 * @Author Amos
 * @Date 2023/5/19 18:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/baseSearchWord")
public class BaseSearchWordController extends BaseController {


    /**
     *   查询热搜词 feign
     * @return
     */
    @PostMapping("/queryHotSearchWords")
    public GoodsMerchantSearchWordDTO queryHotSearchWords(@RequestParam String commonName){
        return baseSearchWordService.selectBySearchName(commonName);
    }

    /**
     *   修改热搜词 feign
     * @return
     */
    @PostMapping("/updateHotSearchWords")
    public Integer updateHotSearchWords(@RequestBody GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto){
        return baseSearchWordService.updateByPrimaryKeySelective(goodsMerchantSearchWordDto);
    }

    /**
     *   添加热搜词 feign
     * @return
     */
    @PostMapping("/addHotSearchWords")
    public Integer addHotSearchWords(@RequestBody GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto){
        return baseSearchWordService.insertSelective(goodsMerchantSearchWordDto);
    }


    /**
     * 查询热搜词
     *
     * @param baseSearchWordDtoReq
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult querySearchWord(@RequestBody GoodsMerchantSearchWordDTO baseSearchWordDtoReq) {
        return baseSearchWordService.querySearchWord(baseSearchWordDtoReq);
    }

    /**
     * 移动端查询热搜词
     */
    @PostMapping("/query/keyWords")
    public ResponseResult queryKeyWords(){
        return baseSearchWordService.queryKeyWords();
    }
}

