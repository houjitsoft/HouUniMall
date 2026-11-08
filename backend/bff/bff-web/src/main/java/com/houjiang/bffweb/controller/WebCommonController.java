package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.PermissionServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterRegistry;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * 通用功能控制器
/**
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/common")
@Tag(name = "通用功能", description = "通用功能相关接口")
public class WebCommonController {
    @Value("${local.upload.path:}")
    private String configuredUploadPath;

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @Autowired
    private PermissionServiceFeign permissionServiceFeign;

    @Autowired
    private FileImportAdapterRegistry fileImportAdapterRegistry;

    private static final List<String> IMPORT_SYSTEM_KEYS = List.of(
            "mall", "crm", "mes", "pm", "erp", "finance", "supplier",
            "payment", "oa", "geo", "aicode");

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "wps", "et", "dps",
            "odt", "ods", "odp", "odg", "odf", "rtf", "txt", "csv", "tsv", "md",
            "json", "xml", "log", "yaml", "yml", "zip", "rar", "7z", "tar", "gz",
            "bz2", "xz", "tgz", "tbz2", "txz");
    private static final List<String> COMPOUND_FILE_EXTENSIONS = List.of(".tar.gz", ".tar.bz2", ".tar.xz");

    /** 上传文件名白名单：仅允许「字母数字下划线横线 + 扩展名」，防止路径穿越 */
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[A-Za-z0-9_\\-]+\\.(?:[A-Za-z0-9]{1,10}|(?i:tar\\.(?:gz|bz2|xz)))$");

    /**
     * 文件访问接口（供 <img>/<a> 等无 Token 场景直接访问上传文件）
     * 文件名由上传接口生成（时间戳_纳秒.扩展名），不可猜测，等价于一次性访问链接。
     *
     * @param fileName 上传时生成的文件名（不含目录）
     * @return 文件内容；不存在或非法文件名时返回 404
     */
    // 不能写成 "/file/{fileName:.+}"：Spring Boot 3 默认 PathPatternParser 不支持
    // {var:regex} 正则变量，路由会静默匹配不上，表现为所有上传文件一律 404
    // （文件其实存在、权限也正常，2026-09-20 财务中心 4 张图全部 404 即因此）。
    // 扩展名无需正则：{fileName} 默认捕获到下一个 "/" 为止，已含 "."；
    // 路径穿越由下面的 SAFE_FILE_NAME 白名单拦截。
    @GetMapping("/file/{fileName}")
    @Operation(summary = "上传文件访问")
    public ResponseEntity<Resource> serveFile(@PathVariable("fileName") String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(resolveUploadDir(), fileName);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = guessContentType(fileName);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(contentType)
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentLength(file.length());
        if (!"image".equals(contentType.getType()) && !MediaType.APPLICATION_PDF.equals(contentType)) {
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        }
        return response.body(new FileSystemResource(file));
    }

    /**
     * 根据文件扩展名推断 Content-Type
     *
     * @param fileName 文件名（含扩展名）
     * @return 对应的 MediaType，未知扩展名返回 application/octet-stream
     */
    private MediaType guessContentType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".bmp")) {
            return MediaType.parseMediaType("image/bmp");
        }
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * 文件识别接口（转发 base 服务，使用系统默认 AI 模型）
     */
    @PostMapping(value = "/file/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "文件识别")
    public ResponseResult recognizeFile(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "description", required = false) String description
    ) {
        return baseServiceFeign.recognizeFile(file, description);
    }

    /**
     * 确认识别结果并跳转
     */
    @PostMapping("/file/confirm")
    @Operation(summary = "确认识别结果")
    public ResponseResult confirmRecognize(@RequestBody Map<String, Object> params) {
        return baseServiceFeign.confirmRecognize(params);
    }

    @GetMapping("/file/import/catalog")
    @Operation(summary = "获取当前用户可用的智能入库目录")
    public ResponseResult getFileImportCatalog() {
        ResponseResult menuResult = permissionServiceFeign.getUserMenus(Map.of());
        if (menuResult == null || menuResult.getCode() != 1) {
            return menuResult == null ? ResponseResult.fail(503, "权限服务暂时不可用") : menuResult;
        }
        ResponseResult catalogResult = baseServiceFeign.getFileImportCatalog();
        if (catalogResult == null || catalogResult.getCode() != 1) {
            return catalogResult == null ? ResponseResult.fail(503, "智能入库目录服务暂时不可用") : catalogResult;
        }

        Set<String> permitted = permittedSystemKeys(extractList(menuResult.getData()));
        List<Map<String, Object>> catalog = extractList(catalogResult.getData());
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> entry : catalog) {
            String key = text(entry.get("systemKey"));
            if (permitted.contains(key)) grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
        }
        List<Map<String, Object>> systems = new ArrayList<>();
        for (String key : IMPORT_SYSTEM_KEYS) {
            if (!permitted.contains(key)) continue;
            List<Map<String, Object>> objects = grouped.get(key);
            if (objects == null || objects.isEmpty()) continue;
            Map<String, Object> system = new LinkedHashMap<>();
            system.put("key", key);
            system.put("name", text(objects.getFirst().get("systemName")));
            system.put("objects", objects);
            systems.add(system);
        }
        return ResponseResult.success(Map.of("systems", systems));
    }

    @PostMapping(value = "/file/import/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传原文件并开始智能入库识别")
    public ResponseResult recognizeFileImport(
            @RequestPart("file") MultipartFile file,
            @RequestParam("systemKey") String systemKey,
            @RequestParam("description") String description,
            @RequestParam(value = "draftNo", required = false) String draftNo) {
        String normalizedSystem = systemKey == null ? "" : systemKey.trim().toLowerCase();
        String normalizedDescription = description == null ? "" : description.trim();
        if (!IMPORT_SYSTEM_KEYS.contains(normalizedSystem)) return ResponseResult.fail(422, "请选择业务系统");
        if (normalizedDescription.isEmpty()) return ResponseResult.fail(422, "描述不能为空");
        if (normalizedDescription.length() > 500) return ResponseResult.fail(422, "描述不能超过500字");
        if (file == null || file.isEmpty()) return ResponseResult.fail(422, "文件不能为空");
        if (!currentUserCanAccess(normalizedSystem)) return ResponseResult.fail(403, "无权使用该业务系统");

        ResponseResult uploaded = uploadFile(file);
        if (uploaded.getCode() != 1) return uploaded;
        Map<String, Object> uploadedData = castMap(uploaded.getData());
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(file.getBytes()));
            return baseServiceFeign.recognizeFileImport(file, normalizedSystem, normalizedDescription,
                    text(uploadedData.get("url")), hash, draftNo);
        } catch (Exception ex) {
            log.error("计算智能入库文件摘要失败", ex);
            return ResponseResult.fail(500, "读取上传文件失败");
        }
    }

    @GetMapping("/file/import/draft/resume")
    public ResponseResult resumeFileImportDraft() {
        return baseServiceFeign.resumeFileImportDraft();
    }

    @GetMapping("/file/import/draft/{draftNo}")
    public ResponseResult getFileImportDraft(@PathVariable String draftNo) {
        return baseServiceFeign.getFileImportDraft(draftNo);
    }

    @PatchMapping("/file/import/item/{itemId}")
    public ResponseResult updateFileImportItem(@PathVariable Long itemId,
                                               @RequestBody Map<String, Object> request) {
        return baseServiceFeign.updateFileImportItem(itemId, request);
    }

    @PostMapping("/file/import/confirm")
    @Operation(summary = "确认并批量写入识别结果")
    public ResponseResult confirmFileImport(@RequestBody Map<String, Object> request) {
        List<Long> itemIds = extractIds(request == null ? null : request.get("itemIds"));
        if (itemIds.isEmpty()) return ResponseResult.fail(422, "请选择需要入库的识别项");

        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        for (Long itemId : itemIds) {
            Map<String, Object> result = confirmOneItem(itemId);
            results.add(result);
            if ("success".equals(result.get("status"))) successCount++;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", results.size());
        data.put("successCount", successCount);
        data.put("failedCount", results.size() - successCount);
        data.put("results", results);
        return ResponseResult.success(data);
    }

    private Map<String, Object> confirmOneItem(Long itemId) {
        ResponseResult preparedResult = baseServiceFeign.prepareFileImportItem(itemId);
        if (preparedResult == null) return failedResult(itemId, 503, "智能入库草稿服务暂时不可用");
        if (preparedResult.getCode() != 1) {
            return failedResult(itemId, preparedResult.getCode(), preparedResult.getMessage());
        }
        Map<String, Object> item = castMap(preparedResult.getData());
        String systemKey = text(item.get("systemKey"));
        if (!currentUserCanAccess(systemKey)) {
            return finishFailedItem(itemId, 403, "当前用户没有该业务系统的入库权限");
        }

        FileImportAdapterResult imported;
        try {
            imported = fileImportAdapterRegistry.importItem(item, castMap(item.get("attachment")));
        } catch (Exception ex) {
            log.error("智能入库业务适配器执行失败, itemId={}", itemId, ex);
            return finishFailedItem(itemId, 500, "业务入库失败");
        }
        if (imported.code() != 1) {
            return finishFailedItem(itemId, imported.code(), imported.message());
        }

        Map<String, Object> completion = new LinkedHashMap<>();
        completion.put("status", "success");
        completion.put("businessRecordId", imported.businessRecordId());
        completion.put("detailUrl", imported.detailUrl());
        completion.put("attachmentUrl", imported.attachmentUrl());
        completion.put("errorMessage", null);
        ResponseResult saved = baseServiceFeign.completeFileImportItem(itemId, completion);
        if (saved == null || saved.getCode() != 1) {
            String message = saved == null ? "入库结果保存失败" : saved.getMessage();
            Map<String, Object> result = failedResult(itemId, saved == null ? 503 : saved.getCode(), message);
            result.put("businessRecordId", imported.businessRecordId());
            result.put("detailUrl", imported.detailUrl());
            return result;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("status", "success");
        result.put("code", 1);
        result.put("message", imported.message());
        result.put("businessRecordId", imported.businessRecordId());
        result.put("detailUrl", imported.detailUrl());
        result.put("attachmentUrl", imported.attachmentUrl());
        return result;
    }

    private Map<String, Object> finishFailedItem(Long itemId, int code, String message) {
        Map<String, Object> completion = new LinkedHashMap<>();
        completion.put("status", "failed");
        completion.put("businessRecordId", null);
        completion.put("detailUrl", null);
        completion.put("attachmentUrl", null);
        completion.put("errorMessage", message);
        ResponseResult saved = baseServiceFeign.completeFileImportItem(itemId, completion);
        if (saved != null && saved.getCode() != 1 && (message == null || message.isBlank())) {
            message = saved.getMessage();
        }
        return failedResult(itemId, code, message);
    }

    private Map<String, Object> failedResult(Long itemId, int code, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("status", "failed");
        result.put("code", code);
        result.put("message", message == null || message.isBlank() ? "入库失败" : message);
        return result;
    }

    private List<Long> extractIds(Object value) {
        if (!(value instanceof List<?> values)) return List.of();
        List<Long> result = new ArrayList<>();
        for (Object item : values) {
            try {
                long id = item instanceof Number number ? number.longValue() : Long.parseLong(text(item));
                if (id > 0 && !result.contains(id)) result.add(id);
            } catch (Exception ignored) {
                // Invalid identifiers are ignored; an entirely invalid request is rejected below.
            }
        }
        return result;
    }

    private boolean currentUserCanAccess(String systemKey) {
        ResponseResult menuResult = permissionServiceFeign.getUserMenus(Map.of());
        return menuResult != null && menuResult.getCode() == 1
                && permittedSystemKeys(extractList(menuResult.getData())).contains(systemKey);
    }

    private Set<String> permittedSystemKeys(List<Map<String, Object>> menus) {
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        for (Map<String, Object> menu : menus) {
            if (number(menu.get("pid")) != 0 || !"M".equalsIgnoreCase(text(menu.get("type")))
                    || number(menu.get("is_show")) != 1) continue;
            String key = systemKey(text(menu.get("name")), text(menu.get("paths")));
            if (key != null) keys.add(key);
        }
        return keys;
    }

    private String systemKey(String name, String path) {
        String normalizedPath = path == null ? "" : path.trim().toLowerCase();
        for (String key : IMPORT_SYSTEM_KEYS) {
            String root = "/" + key;
            String pluginRoot = "/plugin/" + key;
            if (normalizedPath.equals(root) || normalizedPath.startsWith(root + "/")
                    || normalizedPath.equals(pluginRoot) || normalizedPath.startsWith(pluginRoot + "/")) {
                return key;
            }
        }
        // 旧插件地址仍映射到 payment 独立服务，供智能入库识别历史菜单
        if (normalizedPath.equals("/plugin/unipay") || normalizedPath.startsWith("/plugin/unipay/")
                || normalizedPath.equals("/shell/payment") || normalizedPath.startsWith("/shell/payment/")) {
            return "payment";
        }

        return switch (name == null ? "" : name.trim().toLowerCase()) {
            case "mall", "商城", "商城概览", "商品管理", "订单管理", "用户管理",
                    "营销活动", "店铺管理", "库存管理", "售后服务", "物流管理",
                    "装修管理", "数据统计" -> "mall";
            case "crm" -> "crm";
            case "mes", "智能制造", "生产制造" -> "mes";
            case "pm", "项目管理" -> "pm";
            case "erp" -> "erp";
            case "finance", "财务中心" -> "finance";
            case "supplier", "供应商", "供应商管理" -> "supplier";
            case "payment", "支付中心", "unipay" -> "payment";
            case "oa", "oa办公" -> "oa";
            case "geo" -> "geo";
            case "aicode", "ai 开发", "ai开发" -> "aicode";
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) if (item instanceof Map<?, ?> map) result.add((Map<String, Object>) map);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int number(Object value) {
        if (value instanceof Number number) return number.intValue();
        try { return Integer.parseInt(text(value)); } catch (Exception ignored) { return -1; }
    }

    /**
     * 文件上传接口
/**
     * 用于图片、文档等文件的通用上传
     *
     * @param file 上传的文件
     * @return ResponseResult 包含文件URL的响应结果
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public ResponseResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseResult.error("文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long fileSize = file.getSize();
            
            log.info("上传文件: {}, 类型: {}, 大小: {} bytes", fileName, contentType, fileSize);

            // 验证文件类型
            if (!isAllowedFileType(fileName)) {
                return ResponseResult.error("不支持的文件类型");
            }

            // 验证文件大小（20MB限制）
            if (fileSize > 20 * 1024 * 1024) {
                return ResponseResult.error("文件大小不能超过20MB");
            }

            // 生成唯一文件名
            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                String lowerFileName = fileName.toLowerCase(Locale.ROOT);
                String compoundExtension = COMPOUND_FILE_EXTENSIONS.stream()
                        .filter(lowerFileName::endsWith).findFirst().orElse(null);
                fileExtension = compoundExtension == null
                        ? fileName.substring(fileName.lastIndexOf("."))
                        : fileName.substring(fileName.length() - compoundExtension.length());
            }
            String newFileName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;

            // 保存文件到本地目录（兼容Windows/Linux）
            String uploadDir = resolveUploadDir();
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建上传目录失败: {}", uploadDir);
                return ResponseResult.error("创建上传目录失败");
            }

            File destFile = new File(dir, newFileName);
            file.transferTo(destFile);

            // 返回文件访问URL
            String fileUrl = "/uploads/" + newFileName;

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);
            result.put("fileSize", fileSize);
            result.put("contentType", contentType);

            log.info("文件上传成功: {}", fileUrl);

            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 解析上传目录，兼容 Windows/Linux 的绝对路径和相对路径
/**
     * 规则：
     * 1. 优先使用配置 local.upload.path
     * 2. 未配置时，Linux 默认 /www/wwwroot/houcloud_code/uploads
     * 3. 未配置时，Windows 默认 D:/wwwroot/houcloud/uploads
     * 4. 若配置为相对路径，则相对于 user.dir
     */
    private String resolveUploadDir() {
        String raw = configuredUploadPath == null ? "" : configuredUploadPath.trim();
        if (raw.isEmpty()) {
            boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
            return isWindows ? "D:/wwwroot/houcloud/uploads" : "/www/wwwroot/houcloud_code/uploads";
        }

        if (isAbsolutePath(raw)) {
            return raw;
        }
        return new File(System.getProperty("user.dir"), raw).getAbsolutePath();
    }

    /**
     * 判断是否为绝对路径（支持 Linux、Windows 盘符路径、UNC 路径）
     */
    private boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Linux absolute: /data/uploads
        if (path.startsWith("/")) {
            return true;
        }
        // Windows drive path: C:\xx or D:/xx
        if (path.matches("^[a-zA-Z]:[\\\\/].*")) {
            return true;
        }
        // UNC path: \\server\share\dir
        return path.startsWith("\\\\");
    }

    /**
     * 验证文件类型是否允许
     */
    private boolean isAllowedFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 && ALLOWED_FILE_EXTENSIONS.contains(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
