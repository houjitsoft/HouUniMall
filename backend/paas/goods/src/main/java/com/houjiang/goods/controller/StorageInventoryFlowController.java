package com.houjiang.goods.controller;


import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.group.Insert;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.service.StorageInventoryFlowService;
import com.houjiang.goods.vo.StorageInventoryFlowVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 仓库库存流水表(StorageInventoryFlow)表控制层
 *
 * @author Amos
 * @since 2024-09-20 16:51:29
 */
@RestController
@RequestMapping("storageInventoryFlow")
public class StorageInventoryFlowController {

    /**
     * 服务对象
     */
    @Resource
    private StorageInventoryFlowService storageInventoryFlowService;

    /**
     * 插入出库/入库/调拨单接口
     * <p>
     * 通过POST请求接收客户端发送的库存流水信息，并委托服务层插入数据库
     *
     * @param param 经过校验的库存流水数据传输对象，用于接收客户端请求参数
     * @return ResponseResult 包含插入结果的响应对象
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/insert")
    public ResponseResult insertStorageInventoryFlow(@Validated({Insert.class}) @RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.insertStorageInventoryFlow(param);
    }

    /**
     * 查询出库/入库/调拨单列表
     * <p>
     * 通过POST请求接收客户端的查询请求，并将查询参数进行校验和解析，最终调用服务层方法进行库存流水的查询操作
     *
     * @param param 请求体，包含查询条件和分页参数，经过Select组的校验
     * @return 返回库存流水的分页查询结果，封装在ResponseResult中
     * 该方法首先将HTTP请求中的JSON数据解析为StorageInventoryFlowDTO对象，然后调用storageInventoryFlowService
     * 的queryStorageInventoryFlow方法执行查询操作，将查询到的库存流水分页结果封装后返回给客户端
     */
    @PostMapping("/query")
    public ResponseResult<PageUtils<StorageInventoryFlowVO>> queryStorageInventoryFlow(@Validated({Select.class}) @RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.queryStorageInventoryFlow(param);
    }

    /**
     * 查询出库/入库/调拨单详情
     * <p>
     * 通过POST请求接收客户端发送的库存流水查询参数，以查询特定的库存流水详情
     * 此方法委托给storageInventoryFlowService中的queryDetailsInventoryFlow方法来处理具体的业务逻辑
     *
     * @param param 包含查询条件的对象，遵循Select组的验证规则，确保传入的参数符合预期格式
     * @return 返回包含库存流水详细信息的响应结果，使用StorageInventoryFlowVO作为返回类型
     */
    @PostMapping("/queryDetails")
    public ResponseResult<StorageInventoryFlowVO> queryDetailsInventoryFlow(@Validated({Select.class}) @RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.queryDetailsInventoryFlow(param);
    }

    /**
     * 导出入库存流水的列表
     *
     * @param param 包含仓储库存流水支出详细信息的请求体
     * @return 包含操作结果的响应对象
     */
    @PostMapping("/putStorageInventoryFlowExp")
    public ResponseResult<String> putStorageInventoryFlowExp(@RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.putStorageInventoryFlowExp(param);
    }

    /**
     * 导出出库库存流水的列表
     *
     * @param param 包含导出条件的请求体，类型为StorageInventoryFlowDTO，用于指定导出的条件和范围
     * @return ResponseResult<String> 返回一个包含操作结果的响应对象，字符串类型，可能包含成功或失败的信息
     * <p>
     * 该方法的作用是通过POST请求，根据提供的导出条件（如时间范围、商品类型等），生成并导出相应的出库库存流水数据
     * 具体的导出逻辑交由storageInventoryFlowService中的outStorageInventoryFlowExp方法实现
     */
    @PostMapping("/outStorageInventoryFlowExp")
    public ResponseResult<String> outStorageInventoryFlowExp(@RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.outStorageInventoryFlowExp(param);
    }

    /**
     * 导出调拨库存流水的列表
     *
     * @param param 包含流转信息的参数对象，描述了库存的变化细节
     * @return 返回操作结果的响应对象，包含操作状态和可能的消息
     */
    @PostMapping("/transferStorageInventoryFlowExp")
    public ResponseResult<String> transferStorageInventoryFlowExp(@RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowService.transferStorageInventoryFlowExp(param);
    }

}

