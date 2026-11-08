package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.goods.dto.StorageInfoDTO;
import com.houjiang.goods.entity.StorageInfoEntity;
import com.houjiang.goods.vo.StorageInfoVO;

/**
 * 商品仓库表(StorageInfo)表服务接口
 *
 * @author Amos
 * @since 2024-09-19 14:06:31
 */
public interface StorageInfoService extends IService<StorageInfoEntity> {

    /**
     * 查询仓库信息列表
     *
     * @param param 仓库信息查询参数，包含分页和过滤条件
     * @return 返回分页的存储信息列表封装在ResponseResult中
     */
    ResponseResult<PageUtils<StorageInfoVO>> queryStorageInfoList(StorageInfoDTO param);

    /**
     * 更新仓储信息
     *
     * @param param 包含仓储信息的DTO，用于更新仓储信息
     * @return ResponseResult类型的结果，包含更新操作的结果
     */
    ResponseResult updateStorageInfo(StorageInfoDTO param);

    /**
     * 通过POST请求删除仓库信息
     *
     * @param param 包含要删除的仓库信息的DTO，需要经过验证
     * @return 删除操作的结果，封装在ResponseResult中
     */
    ResponseResult deleteStorageInfo(StorageInfoDTO param);

    /**
     * 导出仓库信息列表
     * 该方法接收一个StorageInfoDTO对象，该对象包含查询仓库信息列表所需的参数
     * 并返回一个ResponseResult对象，其中包含查询结果的字符串形式的指数
     *
     * @param param StorageInfoDTO对象，包含查询仓库信息列表所需的参数
     * @return ResponseResult<String>对象，包含查询结果的字符串形式的指数
     */
    ResponseResult<String> queryListExp(StorageInfoDTO param);

    /**
     * @param storageId 库存主键
     * @return 处理结果
     */
    ResponseResult<StorageInfoVO> queryDetail(String storageId);
}
