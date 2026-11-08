package com.houjiang.mall.controller.file;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.file.FileCategory;
import com.houjiang.mall.entity.file.FileInfo;
import com.houjiang.mall.service.file.FileCategoryService;
import com.houjiang.mall.service.file.FileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件管理 Controller
 * 路径：/mall/file/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/file")
@RequiredArgsConstructor
public class FileController {

    private final FileCategoryService fileCategoryService;
    private final FileInfoService fileInfoService;

    // ==================== 文件分类管理 ====================

    /**
     * 获取文件分类列表
     * POST /mall/file/cate/list
     * 
     * @param params 查询参数
     * @return 分类列表
     */
    @PostMapping("/cate/list")
    public ResponseResult<List<FileCategory>> fileCateLists(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取文件分类列表，参数：{}", params);
            List<FileCategory> list = fileCategoryService.list(params);
            return ResponseResult.success(list);
        } catch (Exception e) {
            log.error("获取文件分类列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加文件分类
     * POST /mall/file/cate/add
     * 
     * @param params 分类信息
     * @return 是否成功
     */
    @PostMapping("/cate/add")
    public ResponseResult<Boolean> fileCateAdd(@RequestBody Map<String, Object> params) {
        try {
            log.info("添加文件分类，参数：{}", params);
            
            FileCategory fileCategory = convertToFileCategory(params);
            boolean result = fileCategoryService.addCategory(fileCategory);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("添加文件分类失败", e);
            return ResponseResult.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑文件分类
     * POST /mall/file/cate/edit
     * 
     * @param params 分类信息（包含 id）
     * @return 是否成功
     */
    @PostMapping("/cate/edit")
    public ResponseResult<Boolean> fileCateEdit(@RequestBody Map<String, Object> params) {
        try {
            log.info("编辑文件分类，参数：{}", params);
            
            if (!params.containsKey("id")) {
                return ResponseResult.error("缺少分类 ID 参数");
            }
            
            FileCategory fileCategory = convertToFileCategory(params);
            boolean result = fileCategoryService.editCategory(fileCategory);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("编辑文件分类失败", e);
            return ResponseResult.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件分类
     * POST /mall/file/cate/delete
     * 
     * @param params 删除参数（包含 id）
     * @return 是否成功
     */
    @PostMapping("/cate/delete")
    public ResponseResult<Boolean> fileCateDelete(@RequestBody Map<String, Object> params) {
        try {
            log.info("删除文件分类，参数：{}", params);
            
            if (!params.containsKey("id")) {
                return ResponseResult.error("缺少分类 ID 参数");
            }
            
            Long cateId = Long.parseLong(params.get("id").toString());
            boolean result = fileCategoryService.deleteCategory(cateId);
            
            if (!result) {
                return ResponseResult.error("删除失败，分类可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("删除文件分类失败", e);
            return ResponseResult.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 文件管理 ====================

    /**
     * 获取文件列表（分页）
     * POST /mall/file/list
     * 
     * @param params 查询参数
     * @return 文件列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<FileInfo>> fileLists(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取文件列表，参数：{}", params);
            IPage<FileInfo> page = fileInfoService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件
     * POST /mall/file/upload
     * 
     * @param params 文件信息
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseResult<FileInfo> fileUpload(@RequestBody Map<String, Object> params) {
        try {
            log.info("上传文件，参数：{}", params);
            
            // TODO: 实际文件上传需要处理 MultipartFile
            // 这里仅实现数据库记录保存
            
            FileInfo fileInfo = convertToFileInfo(params);
            boolean result = fileInfoService.addFile(fileInfo);
            
            if (!result) {
                return ResponseResult.error("上传失败");
            }
            
            return ResponseResult.success(fileInfo);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ResponseResult.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 编辑文件信息
     * POST /mall/file/edit
     * 
     * @param params 文件信息（包含 file_id）
     * @return 是否成功
     */
    @PostMapping("/edit")
    public ResponseResult<Boolean> fileEdit(@RequestBody Map<String, Object> params) {
        try {
            log.info("编辑文件信息，参数：{}", params);
            
            if (!params.containsKey("file_id")) {
                return ResponseResult.error("缺少文件 ID 参数");
            }
            
            FileInfo fileInfo = convertToFileInfo(params);
            boolean result = fileInfoService.editFile(fileInfo);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("编辑文件信息失败", e);
            return ResponseResult.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     * POST /mall/file/delete
     * 
     * @param params 删除参数（包含 file_id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> fileDelete(@RequestBody Map<String, Object> params) {
        try {
            log.info("删除文件，参数：{}", params);
            
            if (!params.containsKey("file_id")) {
                return ResponseResult.error("缺少文件 ID 参数");
            }
            
            Long fileId = Long.parseLong(params.get("file_id").toString());
            boolean result = fileInfoService.deleteFile(fileId);
            
            if (!result) {
                return ResponseResult.error("删除失败，文件可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ResponseResult.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 Map 参数转换为 FileCategory 对象
     */
    private FileCategory convertToFileCategory(Map<String, Object> params) {
        FileCategory category = new FileCategory();
        
        if (params.containsKey("id")) {
            category.setCateId(Long.parseLong(params.get("id").toString()));
        }
        if (params.containsKey("shop_id")) {
            category.setShopId(Long.parseLong(params.get("shop_id").toString()));
        }
        if (params.containsKey("cate_name")) {
            category.setCateName(params.get("cate_name").toString());
        }
        if (params.containsKey("sort")) {
            category.setSort(Integer.parseInt(params.get("sort").toString()));
        }
        
        return category;
    }

    /**
     * 将 Map 参数转换为 FileInfo 对象
     */
    private FileInfo convertToFileInfo(Map<String, Object> params) {
        FileInfo fileInfo = new FileInfo();
        
        if (params.containsKey("file_id")) {
            fileInfo.setFileId(Long.parseLong(params.get("file_id").toString()));
        }
        if (params.containsKey("shop_id")) {
            fileInfo.setShopId(Long.parseLong(params.get("shop_id").toString()));
        }
        if (params.containsKey("cate_id")) {
            fileInfo.setCateId(Long.parseLong(params.get("cate_id").toString()));
        }
        if (params.containsKey("file_name")) {
            fileInfo.setFileName(params.get("file_name").toString());
        }
        if (params.containsKey("file_path")) {
            fileInfo.setFilePath(params.get("file_path").toString());
        }
        if (params.containsKey("file_size")) {
            fileInfo.setFileSize(Long.parseLong(params.get("file_size").toString()));
        }
        if (params.containsKey("file_type")) {
            fileInfo.setFileType(params.get("file_type").toString());
        }
        
        return fileInfo;
    }
}
