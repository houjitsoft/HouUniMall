package com.houjiang.bffweb.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Merchant商户服务Feign客户端
/**
 * 
 * @description 调用paas中台Merchant商户管理服务(merchant)的接口
 *              由于merchant内部使用AjaxResult（非ResponseResult），返回类型统一用Object避免反序列化冲突
 * @author Amos
 * @date 2026-04-28
 */
@FeignClient(name = "merchant", path = "/", fallback = MerchantServiceFeignFallback.class, contextId = "merchant")
public interface MerchantServiceFeign {

    // ================= 登录认证接口 =================

    /**
     * 系统登录
/**
     * 
     * @param params 登录参数，包含账号密码等
     * @return Object 登录结果，包含token和用户信息
     */
    @PostMapping("/merchant/login/login")
    Object login(@RequestBody Map<String, Object> params);

    /**
     * 退出登录
/**
     * 
     * @param token 用户token
     * @return Object 退出结果
     */
    @PostMapping("/merchant/login/out")
    Object logout(@RequestHeader(value = "Authorization", required = false) String token);

    /** 平台后台查询商家中心店铺。 */
    @PostMapping("/merchant/internal/shop-provision/list")
    Object listProvisionedShops(@RequestBody(required = false) Map<String, Object> params);

    /** 平台后台查询单个商家店铺资料。 */
    @PostMapping("/merchant/internal/shop-provision/detail")
    Object getProvisionedShopDetail(@RequestBody Map<String, Object> params);

    /** 平台后台同时开通店铺与商家中心主账号。 */
    @PostMapping("/merchant/internal/shop-provision/create")
    Object createProvisionedShop(@RequestBody Map<String, Object> params);

    /** 平台后台新增商家表单选项。 */
    @PostMapping("/merchant/internal/shop-provision/options")
    Object provisionShopOptions();

    /** 平台后台新增店铺时可绑定的已有统一账号。 */
    @PostMapping("/merchant/internal/shop-provision/account-options")
    Object provisionShopAccountOptions(@RequestBody(required = false) Map<String, Object> params);

    /** 同步更新店铺状态与商家主账号状态。 */
    @PostMapping("/merchant/internal/shop-provision/status")
    Object updateProvisionedShopStatus(@RequestBody Map<String, Object> params);

    /** 平台后台编辑商家店铺资料。 */
    @PostMapping("/merchant/internal/shop-provision/update")
    Object updateProvisionedShop(@RequestBody Map<String, Object> params);

    /** Explicit bcenter advertising contract; merchant derives the trusted shop identity. */
    @PostMapping("/merchant/advertising/{action}")
    Object advertising(@PathVariable("action") String action,
                       @RequestBody(required = false) Map<String, Object> params);

    // ================= 通用转发接口 =================

    /**
     * 通用POST请求转发
/**
     * 用于处理未单独定义的merchant服务POST接口
     * 
     * @param module 模块名（如 mall.goods, order 等）
     * @param action 操作名（如 lists, detail, add 等）
     * @param params 请求参数
     * @return Object 响应结果
     */
    @PostMapping("/merchant/{module}/{action}")
    Object forwardPost(@PathVariable("module") String module,
                       @PathVariable("action") String action,
                       @RequestBody(required = false) Map<String, Object> params);

    /**
     * 通用GET请求转发
/**
     * 用于处理未单独定义的merchant服务GET接口
     * 
     * @param module 模块名（如 mall.goods, order 等）
     * @param action 操作名（如 lists, detail 等）
     * @param params 请求参数
     * @return Object 响应结果
     */
    @GetMapping("/merchant/{module}/{action}")
    Object forwardGet(@PathVariable("module") String module,
                      @PathVariable("action") String action,
                      @RequestParam(required = false) Map<String, Object> params);

    // ================= /adminapi 专用转发（覆盖 like-shop 迁移过来的 auth/dept/article/crontab/decorate/channel/setting/notice/recharge 等） =================

    /**
     * /adminapi POST 转发
/**
     *
     * <p>路径模板：/adminapi/{module}/{action}。
     * <p>module 可含小点（如 auth.menu / dept.dept / article.articleCate 等）。
     */
    @PostMapping("/adminapi/{module}/{action}")
    Object forwardAdminapiPost(@PathVariable("module") String module,
                                @PathVariable("action") String action,
                                @RequestBody(required = false) Map<String, Object> params);

    /**
     * /adminapi GET 转发
/**
     *
     * <p>路径模板：/adminapi/{module}/{action}。
     */
    @GetMapping("/adminapi/{module}/{action}")
    Object forwardAdminapiGet(@PathVariable("module") String module,
                               @PathVariable("action") String action,
                               @RequestParam(required = false) Map<String, Object> params);

    // ================= 文件上传接口（multipart/form-data） =================

    /**
     * 通用上传转发（image/file/video）
/**
     *
     * <p>修复 415：上传接口必须声明 consumes = multipart/form-data 并使用 @RequestPart，
     * 不能走通用 forwardPost（@RequestBody Map）。
     *
     * @param type 上传类型（image/file/video）
     * @param file 上传文件
     * @param cid  分类 ID（bff 层已兜底为 "0"）
     * @return Object merchant 端 AjaxResult
     */
    @PostMapping(value = "/merchant/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object uploadFile(@PathVariable("type") String type,
                      @RequestPart("file") MultipartFile file,
                      @RequestPart("cid") String cid,
                      @RequestHeader("Authorization") String authorization);
}
