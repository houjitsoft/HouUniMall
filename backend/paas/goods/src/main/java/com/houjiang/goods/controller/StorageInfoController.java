package com.houjiang.goods.controller;


import com.houjiang.commons.group.Delete;
import com.houjiang.commons.group.Insert;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.StorageInfoDTO;
import com.houjiang.goods.service.StorageInfoService;
import com.houjiang.goods.vo.StorageInfoVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 商品仓库表(StorageInfo)表控制层
 *
 * @author Amos
 * @since 2024-09-19 14:06:31
 */
@RestController
@RequestMapping("storageInfo")
public class StorageInfoController {

    /**
     * 服务对象
     */
    @Resource
    private StorageInfoService storageInfoService;

    /**
     * 查询仓库信息列表
     * 
     * @param param 仓库信息查询参数，包含分页和过滤条件
     * @return 返回分页的仓库信息列表封装在ResponseResult中
     */
    @PostMapping("/queryList")
    public ResponseResult<PageUtils<StorageInfoVO>> queryStorageInfoList(@RequestBody StorageInfoDTO param) {
        return storageInfoService.queryStorageInfoList(param);
    }

    /**
     * 更新仓储信息
     * 
     * @param param 包含仓储信息的DTO，用于更新仓储信息
     * @return ResponseResult类型的结果，包含更新操作的结果
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    public ResponseResult updateStorageInfo(@Validated({Insert.class}) @RequestBody StorageInfoDTO param) {
        return storageInfoService.updateStorageInfo(param);
    }

    /**
     * 通过POST请求删除仓库信息
     * 
     * @param param 包含要删除的仓库信息的DTO，需要经过验证
     * @return 删除操作的结果，封装在ResponseResult中
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete")
    public ResponseResult deleteStorageInfo(@Validated({Delete.class}) @RequestBody StorageInfoDTO param) {
        return storageInfoService.deleteStorageInfo(param);
    }

    /**
     * 导出仓库信息列表
     * 该方法接收一个StorageInfoDTO对象，该对象包含查询仓库信息列表所需的参数
     * 并返回一个ResponseResult对象，其中包含查询结果的字符串形式的指数
     * 
     * @param param StorageInfoDTO对象，包含查询仓库信息列表所需的参数
     * @return ResponseResult<String>对象，包含查询结果的字符串形式的指数
     */
    @PostMapping("/queryListExp")
    public ResponseResult<String> queryListExp(@RequestBody StorageInfoDTO param) {
        return storageInfoService.queryListExp(param);
    }

    /**
     * 根据仓库id查询仓库信息
     *
     * @param storageId 仓库Id
     * @return 返回仓库信息封装在ResponseResult中
     */
    @GetMapping("/queryDetail")
    public ResponseResult<StorageInfoVO> queryDetail(@RequestParam("storageId") String storageId) {
        return storageInfoService.queryDetail(storageId);
    }

}

