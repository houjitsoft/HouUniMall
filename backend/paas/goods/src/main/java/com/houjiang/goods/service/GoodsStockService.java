package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.GoodsStockDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.MerchantGoodsStockDTO;
import com.houjiang.goods.entity.GoodsStockEntity;
import com.houjiang.goods.vo.MerchantGoodsStockVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Amos
 */
public interface GoodsStockService extends IService<GoodsStockEntity> {

    /**
     * 查询商品库存分页列表
     * <p>
     * 通过POST请求接收客户端发送的商品库存查询参数，调用服务层方法实现分页查询商品库存信息
     *
     * @param param 商品库存查询参数，用于指定查询条件和分页信息
     * @return 返回商品库存的分页查询结果，包含总记录数和当前页数据列表
     */
    ResponseResult<PageUtils<MerchantGoodsStockVO>> queryGoodsStockPage(MerchantGoodsStockDTO param);

    /**
     * 根据请求参数查询商品库存信息的分页数据
     * <p>
     * 该方法接收一个包含查询条件的DTO，并返回一个封装了字符串响应结果的对象
     * 它通过调用merchantGoodsStockService的服务层方法来执行具体的查询逻辑
     *
     * @param param MerchantGoodsStockDTO类型的请求体，包含了查询所需的参数
     * @return 返回一个ResponseResult对象，其中封装了查询操作的结果
     */
    ResponseResult<String> queryPageExp(MerchantGoodsStockDTO param);

    /**
     * 通过POST请求更新商品库存信息
     *
     * @param param 商品库存DTO，包含需要更新的商品信息
     * @return 返回更新结果的响应对象
     */
    ResponseResult updateGoodsStock(MerchantGoodsStockDTO param);

    /**
     * 下载商品库存模板
     * <p>
     * 通过POST请求下载商品库存模板，商家可以通过此接口获取如何导入商品库存数据的模板
     *
     * @param param MerchantGoodsStockDTO类型的参数，包含下载模板所需的必要信息
     * @return ResponseResult包装的String类型结果，通常包含操作结果或下载链接
     */
    ResponseResult<String> downloadGoodsStockTemplate(MerchantGoodsStockDTO param);

    /**
     * 导入商品库存模板
     *
     * @param file 用户上传的文件
     * @return 返回导入操作的结果
     */
    ResponseResult importGoodsStockTemplate(MultipartFile file,String token);

    /**
     * 查询商品库存分布
     *
     * @param param MerchantGoodsStockDTO类型的请求体，包含查询条件
     * @return ResponseResult类型的结果，包含查询到的库存分布信息
     */
    ResponseResult<MerchantGoodsStockVO> queryStockDistribution(MerchantGoodsStockDTO param);

    /**
     * 添加商品库存数据
     * @param dto 商品库存数据
     * @return 操作结果
     */
    ResponseResult insertGoodsStock(GoodsStockDTO dto);

    /**
     * 同步商品库存
     *
     * @param
     * @return
     */
    ResponseResult syncStock();


    /**
     * 根据商品ID查询商品库存
     * @param goodsId
     * @return
     */
    Integer selectGoodsStock(Integer goodsId);
}
