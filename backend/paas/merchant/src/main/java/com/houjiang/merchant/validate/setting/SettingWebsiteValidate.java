package com.houjiang.merchant.validate.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingWebsiteValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "网站名称")
    private String name = "";
    @Schema(description = "网站logo")
    private String webLogo = "";
    @Schema(description = "网站图标")
    private String webFavicon = "";
    @Schema(description = "h5图标")
    private String h5Favicon = "";
    @Schema(description = "登录页广告图")
    private String loginImage = "";
    @Schema(description = "商城名称")
    private String shopName = "";
    @Schema(description = "商城LOGO")
    private String shopLogo = "";
    @Schema(description = "PC端LOGO")
    private String pcLogo = "";
    @Schema(description = "网站标题")
    private String pcTitle = "";
    @Schema(description = "网站图标")
    private String pcIco = "";
    @Schema(description = "网站描述")
    private String pcDesc = "";
    @Schema(description = "网站关键词")
    private String pcKeywords = "";

}
