package com.houjiang.base.controller;

import com.houjiang.base.dto.fileimport.DraftCreateRequest;
import com.houjiang.base.dto.fileimport.DraftFileRequest;
import com.houjiang.base.dto.fileimport.ImportItemUpdateRequest;
import com.houjiang.base.security.BaseOrganizationResolver;
import com.houjiang.base.service.FileRecognizeCatalog;
import com.houjiang.base.service.FileRecognizeDraftService;
import com.houjiang.base.service.FileRecognizeService;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 小匠帮你传 - 文件智能识别
 * @author Amos
 */
@RestController
@RequestMapping("/base/file")
public class FileRecognizeController {

    @Autowired
    private FileRecognizeService fileRecognizeService;

    @Autowired
    private FileRecognizeDraftService fileRecognizeDraftService;

    @Autowired
    private FileRecognizeCatalog fileRecognizeCatalog;

    @Autowired
    private BaseOrganizationResolver organizationResolver;

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult recognize(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "description", required = false) String description,
        @RequestHeader(value = "Authorization", required = false) String token
    ) {
        return fileRecognizeService.recognize(file, description, token);
    }

    @PostMapping("/confirm")
    public ResponseResult confirm(@RequestBody Map<String, Object> params) {
        return fileRecognizeService.confirm(params);
    }

    @GetMapping("/import/catalog")
    public ResponseResult importCatalog() {
        return ResponseResult.success(fileRecognizeCatalog.entries());
    }

    @PostMapping(value = "/import/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult recognizeForImport(
            @RequestPart("file") MultipartFile file,
            @RequestParam("systemKey") String systemKey,
            @RequestParam("description") String description,
            @RequestParam(value = "draftNo", required = false) String draftNo,
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("fileHash") String fileHash,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();

        DraftCreateRequest draft = new DraftCreateRequest();
        draft.setDraftNo(draftNo);
        draft.setSystemKey(systemKey);
        draft.setDescription(description);
        draft.setUserId(user.getUserId());
        draft.setOrgId(orgId);
        DraftFileRequest draftFile = new DraftFileRequest();
        draftFile.setFileName(file == null ? null : file.getOriginalFilename());
        draftFile.setFileHash(fileHash);
        draftFile.setFileUrl(fileUrl);
        draftFile.setFileSize(file == null ? 0 : file.getSize());
        draftFile.setContentType(file == null ? null : file.getContentType());
        draftFile.setStatus("uploaded");

        ResponseResult created = fileRecognizeDraftService.createOrUpdate(draft, draftFile);
        if (created.getCode() != 1) return created;
        Map<String, Object> createdData = castMap(created.getData());
        String storedDraftNo = String.valueOf(createdData.get("draftNo"));
        Long itemId = Long.valueOf(String.valueOf(createdData.get("itemId")));
        ResponseResult recognized = fileRecognizeService.recognizeForImport(file, systemKey, description, token);
        if (recognized.getCode() != 1) return recognized;
        ResponseResult saved = fileRecognizeDraftService.saveRecognition(storedDraftNo, itemId,
                castMap(recognized.getData()), user.getUserId(), orgId);
        if (saved.getCode() != 1) return saved;
        return fileRecognizeDraftService.findDraft(storedDraftNo, user.getUserId(), orgId);
    }

    @GetMapping("/import/draft/resume")
    public ResponseResult resumeDraft(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();
        return fileRecognizeDraftService.resume(user.getUserId(), orgId);
    }

    @GetMapping("/import/draft/{draftNo}")
    public ResponseResult getDraft(@PathVariable String draftNo,
                                   @RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();
        return fileRecognizeDraftService.findDraft(draftNo, user.getUserId(), orgId);
    }

    @PatchMapping("/import/item/{itemId}")
    public ResponseResult updateItem(@PathVariable Long itemId,
                                     @RequestBody ImportItemUpdateRequest request,
                                     @RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        request.setItemId(itemId);
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();
        return fileRecognizeDraftService.updateItem(request, user.getUserId(), orgId);
    }

    @GetMapping("/import/item/{itemId}/prepare")
    public ResponseResult prepareConfirm(@PathVariable Long itemId,
                                         @RequestHeader(value = "Authorization", required = false) String token,
                                         @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();
        return fileRecognizeDraftService.prepareConfirm(itemId, user.getUserId(), orgId);
    }

    @PostMapping("/import/item/{itemId}/result")
    public ResponseResult completeItem(@PathVariable Long itemId, @RequestBody Map<String, Object> request,
                                       @RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestHeader(value = "X-Org-Id", required = false) String selectedOrgId) {
        UserInfoVO user = currentUser(token);
        if (user == null || user.getUserId() == null) return ResponseResult.fail(401, "请先登录");
        String orgId = organization(user, selectedOrgId);
        if (orgId == null) return organizationFailure();
        return fileRecognizeDraftService.completeItem(itemId, String.valueOf(request.get("status")),
                stringOrNull(request.get("businessRecordId")), stringOrNull(request.get("detailUrl")),
                stringOrNull(request.get("attachmentUrl")), stringOrNull(request.get("errorMessage")),
                user.getUserId(), orgId);
    }

    /**
     * base 服务的安全链历史上允许内部接口直接调用，没有注册 JWT 过滤器。
     * 智能入库需要用户隔离，因此先兼容未来可能存在的安全上下文，再按 base
     * 现有约定从 BFF 转发的 Authorization 中校验 Token 并读取当前用户。
     */
    private UserInfoVO currentUser(String authorization) {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user != null && user.getUserId() != null && !user.getUserId().isBlank()) return user;

        if (authorization == null || authorization.isBlank()) return null;
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) token = token.substring(7).trim();
        if (token.isEmpty()) return null;
        try {
            user = ParseTokenUtils.getUserInfoByToken(token);
            return user != null && user.getUserId() != null && !user.getUserId().isBlank() ? user : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 组织未确定时区分原因，避免一律抛出「无权访问」这种误导性文案：
     * 账号没有任何组织成员关系，与选了不属于自己的组织，是两种完全不同的处置方式。
     */
    private ResponseResult organizationFailure() {
        if (organizationResolver == null) {
            return noMembershipFailure();
        }
        return organizationResolver.hasAnyMembership()
                ? ResponseResult.fail(422, "当前选择的组织无效，请重新选择组织")
                : noMembershipFailure();
    }

    private ResponseResult noMembershipFailure() {
        return ResponseResult.fail(422, "当前账号未加入任何组织，不能使用智能入库");
    }

    private String organization(UserInfoVO user, String selectedOrgId) {
        if (organizationResolver != null) {
            String resolved = organizationResolver.resolveCurrentOrgId();
            return resolved == null || resolved.isBlank() ? null : resolved;
        }
        return selectedOrgId == null || selectedOrgId.isBlank() ? user.getOrgId() : selectedOrgId.trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    private String stringOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
