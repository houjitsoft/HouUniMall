package com.houjiang.userms.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.UserAgreementConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户协议同意记录Controller。
 *
 * @author Amos
 */
@RestController
// 必须带 user/ms 前缀：UserServiceFeign 的 @FeignClient 声明了 path = "/user/ms"，
// 与 UserInfoController("user/ms") / UserOrganizationController("/user/ms/organization") 同例。
// 少了 /ms 会让 bff 打到 /user/ms/agreement/consent/*，userms 侧匹配不到 → 404「接口还未加载」。
@RequestMapping("user/ms/agreement/consent")
public class UserAgreementConsentController {

    @Autowired
    private UserAgreementConsentService userAgreementConsentService;

    /**
     * 记录同意行为（供 bff-web 在注册 / 重新同意时调用）。
     *
     * <p>入参：userId、username、consents（[{code, agreementId, agreementName, versionId, versionNo}]）、
     * ip、ua、source、scene。</p>
     */
    @PostMapping("/record")
    public ResponseResult record(@RequestBody Map<String, Object> param) {
        if (param == null) {
            return ResponseResult.error("参数不能为空");
        }
        String userId = str(param.get("userId"));
        String username = str(param.get("username"));
        String ip = str(param.get("ip"));
        String ua = str(param.get("ua"));
        String source = str(param.get("source"));
        String scene = str(param.get("scene"));

        int count = userAgreementConsentService.recordConsents(
                userId, username, normalizeConsents(param.get("consents")), ip, ua, source, scene);
        return ResponseResult.success(count);
    }

    /**
     * 查询某用户的同意记录
     */
    @PostMapping("/listByUser")
    public ResponseResult listByUser(@RequestBody(required = false) Map<String, Object> param) {
        return userAgreementConsentService.listByUser(param);
    }

    /**
     * 后台：同意记录分页查询
     */
    @PostMapping("/admin/page")
    public ResponseResult adminPage(@RequestBody(required = false) Map<String, Object> param) {
        return userAgreementConsentService.adminPage(param);
    }

    /**
     * 后台：同意记录导出（全量筛选结果）
     */
    @PostMapping("/admin/export")
    public ResponseResult adminExport(@RequestBody(required = false) Map<String, Object> param) {
        return userAgreementConsentService.adminExport(param);
    }

    /**
     * 归一化 consents 入参：兼容「已是 List」与「JSON 数组字符串」两种传递方式。
     *
     * <p>注册链路经 {@code UserInfoController#register} 的 Map&lt;String,String&gt; 转换后，
     * consents 会变成字符串，故此处需能解析 JSON 文本。</p>
     */
    private List<Map<String, Object>> normalizeConsents(Object raw) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        if (raw instanceof List) {
            for (Object item : (List<?>) raw) {
                if (item instanceof Map) {
                    JSONObject copy = new JSONObject();
                    for (Map.Entry<?, ?> e : ((Map<?, ?>) item).entrySet()) {
                        if (e.getKey() != null) {
                            copy.put(String.valueOf(e.getKey()), e.getValue());
                        }
                    }
                    result.add(copy);
                }
            }
            return result;
        }
        String text = String.valueOf(raw).trim();
        if (!text.startsWith("[")) {
            return result;
        }
        try {
            JSONArray array = JSON.parseArray(text);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj != null) {
                    result.add(obj);
                }
            }
        } catch (Exception ignored) {
            // 解析失败按「无同意记录」处理，由调用方决定是否阻断
        }
        return result;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
