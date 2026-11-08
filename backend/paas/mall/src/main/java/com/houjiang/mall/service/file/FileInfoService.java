package com.houjiang.mall.service.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.file.FileInfo;
import com.houjiang.mall.mapper.file.FileInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 文件信息 Service
 * 
 * @author Amos
 */
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> {
    
    /**
     * 分页查询文件列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<FileInfo> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long cateId = params.get("cateId") != null ? Long.parseLong(params.get("cateId").toString()) : null;
        String fileType = (String) params.get("fileType");
        
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        
        // 按店铺 ID 筛选
        wrapper.eq(shopId != null, FileInfo::getShopId, shopId);
        
        // 按分类 ID 筛选
        wrapper.eq(cateId != null, FileInfo::getCateId, cateId);
        
        // 按文件类型筛选
        wrapper.eq(StringUtils.hasText(fileType), FileInfo::getFileType, fileType);
        
        // 按创建时间降序排序
        wrapper.orderByDesc(FileInfo::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 根据 ID 查询文件详情
     * @param fileId 文件 ID
     * @return 文件实体
     */
    public FileInfo getFileDetail(Long fileId) {
        return this.getById(fileId);
    }
    
    /**
     * 添加文件
     * @param fileInfo 文件对象
     * @return 是否成功
     */
    public boolean addFile(FileInfo fileInfo) {
        return this.save(fileInfo);
    }
    
    /**
     * 编辑文件信息
     * @param fileInfo 文件对象
     * @return 是否成功
     */
    public boolean editFile(FileInfo fileInfo) {
        return this.updateById(fileInfo);
    }
    
    /**
     * 删除文件
     * @param fileId 文件 ID
     * @return 是否成功
     */
    public boolean deleteFile(Long fileId) {
        // TODO: 如果是 OSS 文件，需要同步删除 OSS 上的文件
        
        return this.removeById(fileId);
    }
}
