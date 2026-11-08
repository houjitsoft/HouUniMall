package com.houjiang.auth.login.channel;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方渠道返回的用户身份（渠道无关）
 *
 * <p>各渠道字段名不同，统一收敛到这里三个语义槽位：</p>
 * <ul>
 *   <li>{@code providerUserId} —— 渠道内唯一标识（微信 openid、企业微信 userid、钉钉 unionId…）</li>
 *   <li>{@code unionId} —— 同一开放平台主体下的跨应用标识，可空</li>
 *   <li>{@code nickname}/{@code avatar} —— 展示用，可空</li>
 * </ul>
 *
 * @author Amos
 */
@Data
public class LoginIdentity {

    private String providerUserId;
    private String unionId;
    private String nickname;
    private String avatar;
    /** 渠道原始响应，便于排障；不落库、不外发 */
    private String raw;
    /** userinfo 接口返回的完整字段，便于后续按需扩展 */
    private Map<String, Object> attributes = new HashMap<>();

    public boolean hasProviderUserId() {
        return providerUserId != null && !providerUserId.trim().isEmpty();
    }
}
