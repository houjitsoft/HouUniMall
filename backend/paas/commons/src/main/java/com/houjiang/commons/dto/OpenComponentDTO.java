package com.houjiang.commons.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class OpenComponentDTO {
    /** aeskey */
    public static String aesKey = "tokenkey10tokenkey10tokenkey10tokenkey10123";
    /** 平台设定的token */
    public static String COMPONENT_TOKEN = "tokenkey";
    public static String appId = "wx10635bace7d8d89f";
    public static String secret = "a441ccbee14511e1fd30be77aaca8af0";
    public String pre_auth_code;
    /** 第三方ticket */
    public static String component_verify_ticket = "";
    /** token */
    public static String ACCESS_TOKEN = "";
    public String component_access_token;
    /** url */
    public static String URL = "https://api.weixin.qq.com";

    /** 获取component_access_token 的url */
    public static String component_access_token_url = URL + "/cgi-bin/component/api_component_token";

    /** 获取pre_auth_code 的url */
    public static String pre_auth_code_url = URL + "/cgi-bin/component/api_create_preauthcode";

    /** 根据授权码获取信息url */
    public static String OPEN_URL_AUTHORIZERACCESSTOKEN = URL + "/cgi-bin/component/api_query_auth";

    /** 获取授权帐号调用令牌 */
    public static String authorizer_access_token_url = URL + "/cgi-bin/component/api_authorizer_token";

    /** 获取草稿箱列表 get */
    public static String getTemplatedRaftListUrl = URL + "/wxa/gettemplatedraftlist";

    /** 获取代码模板列表 get */
    public static String gettemplatelistUrl = URL + "/wxa/gettemplatelist";

    /** 上传代码url post */
    public static String commitUrl = URL + "/wxa/commit";

    /** 获取体验二维码 get */
    public static String getqrcodeUrl = URL + "/wxa/get_qrcode";

    /** 上传提审素材  post */
    public static String uploadMediaToCodeAuditUrl = URL + "/wxa/uploadmedia";

    /** 提交代码审核  post */
    public static String submitAuditUrl = URL + "/wxa/submit_audit";

    /** 获取类目名称  get */
    public static String getAllCategoryNameUrl = URL + "/wxa/get_category";

    /** 设置小程序隐私指引 post */
    public static String setPrivacySettingUrl = URL + "/cgi-bin/component/setprivacysetting";

    /** 获取最新一次审核状态 get */
    public static String getLatestAuditStatusUrl = URL + "/wxa/get_latest_auditstatus";

    /** 发布已通过审核的小程序 */
    public static String releaseUrl = URL + "/wxa/release";

    /** 获取基本信息 post */
    public static String selectinfoUrl = URL + "/cgi-bin/account/getaccountbasicinfo";

    /** 获取接口隐藏信息检测结果 */
    public static String privacyUrl = URL + "/wxa/security/get_code_privacy_info";

    /** 上传提审素材 */
    public static String uploadmediaUrl = URL + "/wxa/uploadmedia";

    /** 生成授权码 */
    public static String componentloginpageUrl = "https://mp.weixin.qq.com/cgi-bin/componentloginpage";
}
