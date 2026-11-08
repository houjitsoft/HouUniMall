package com.houjiang.goods.controller;


import com.houjiang.commons.dto.goods.GoodsStockDTO;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.MerchantGoodsStockDTO;
import com.houjiang.goods.service.GoodsStockService;
import com.houjiang.goods.vo.MerchantGoodsStockVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

/**
 * 商品库存表(MerchantGoodsStock)表控制层
 *
 * @author Amos
 * @since 2024-09-19 16:39:25
 */
@RestController
@RequestMapping("merchantGoodsStock")
public class GoodsStockController {

    /**
     * 服务对象
     */
    @Resource
    private GoodsStockService goodsStockService;

    /**
     * 查询商品库存分页列表
     * <p>
     * 通过POST请求接收客户端发送的商品库存查询参数，调用服务层方法实现分页查询商品库存信息
     *
     * @param param 商品库存查询参数，用于指定查询条件和分页信息
     * @return 返回商品库存的分页查询结果，包含总记录数和当前页数据列表
     */
    @PostMapping("/queryPage")
    public ResponseResult<PageUtils<MerchantGoodsStockVO>> queryGoodsStockPage(@RequestBody MerchantGoodsStockDTO param) {
        return goodsStockService.queryGoodsStockPage(param);
    }

    /**
     * 根据请求参数查询商品库存信息的分页数据
     * <p>
     * 该方法接收一个包含查询条件的DTO，并返回一个封装了字符串响应结果的对象
     * 它通过调用merchantGoodsStockService的服务层方法来执行具体的查询逻辑
     *
     * @param param MerchantGoodsStockDTO类型的请求体，包含了查询所需的参数
     * @return 返回一个ResponseResult对象，其中封装了查询操作的结果
     */
    @PostMapping("/queryPageExp")
    public ResponseResult<String> queryPageExp(@RequestBody MerchantGoodsStockDTO param) {
        return goodsStockService.queryPageExp(param);
    }

    /**
     * 通过POST请求更新商品库存信息
     *
     * @param param 商品库存DTO，包含需要更新的商品信息
     * @return 返回更新结果的响应对象
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/updateGoodsStock")
    public ResponseResult updateGoodsStock(@Validated({Update.class}) @RequestBody MerchantGoodsStockDTO param) {
        return goodsStockService.updateGoodsStock(param);
    }

    /**
     * 下载商品库存模板
     * <p>
     * 通过POST请求下载商品库存模板，商家可以通过此接口获取如何导入商品库存数据的模板
     *
     * @param param MerchantGoodsStockDTO类型的参数，包含下载模板所需的必要信息
     * @return ResponseResult包装的String类型结果，通常包含操作结果或下载链接
     */
    @PostMapping("/downloadGoodsStockTemplate")
    public ResponseResult<String> downloadGoodsStockTemplate(@RequestBody MerchantGoodsStockDTO param) {
        return goodsStockService.downloadGoodsStockTemplate(param);
    }

    /**
     * 导入商品库存模板
     *
     * @param file 用户上传的文件
     * @return 返回导入操作的结果
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/importGoodsStockTemplate")
    public ResponseResult importGoodsStockTemplate(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("token") String token) {
        return goodsStockService.importGoodsStockTemplate(file,token);
    }


    /**
     * 查询商品库存分布
     *
     * @param param MerchantGoodsStockDTO类型的请求体，包含查询条件
     * @return ResponseResult类型的结果，包含查询到的库存分布信息
     */
    @PostMapping("/queryStockDistribution")
    public ResponseResult<MerchantGoodsStockVO> queryStockDistribution(@Validated({Select.class}) @RequestBody MerchantGoodsStockDTO param) {
        return goodsStockService.queryStockDistribution(param);
    }

    /**
     * 添加商品库存数据
     * @param dto 商品库存数据
     * @return 操作结果
     */
    @PostMapping("/insertGoodsStock")
    public ResponseResult insertGoodsStock(@RequestBody GoodsStockDTO dto){
        return goodsStockService.insertGoodsStock(dto);
    }

    /**
     * 同步商品库存
     *
     * @param
     * @return
     */
    @PostMapping("/syncStock")
    public ResponseResult syncStock() {
        return goodsStockService.syncStock();
    }

    /**
     * 根据商品ID查询商品库存
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/selectGoodsStock")
    public Integer selectGoodsStock(@RequestParam("goodsId") Integer goodsId){
        Integer goodsStock = goodsStockService.selectGoodsStock(goodsId);
        return goodsStock;
    }

}

