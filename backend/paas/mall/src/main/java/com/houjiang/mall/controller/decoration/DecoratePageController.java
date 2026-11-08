package com.houjiang.mall.controller.decoration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.decoration.DecoratePage;
import com.houjiang.mall.service.decoration.DecoratePageService;
import com.houjiang.mall.service.decoration.LikeDecoratePageAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 店铺装修 Controller
 * 路径：/mall/decoration/page/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/decoration/page")
@RequiredArgsConstructor
public class DecoratePageController {

    private final DecoratePageService decoratePageService;
    private final LikeDecoratePageAdminService likeDecoratePageAdminService;

    /**
     * 获取页面装修详情（hou_base_decorate_page：id / type / name / data）
     * POST /mall/decoration/page/detail
     */
    @PostMapping("/detail")
    public ResponseResult<Map<String, Object>> getDecoratePages(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取页面装修详情，参数：{}", params);
            return ResponseResult.success(likeDecoratePageAdminService.detail(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取页面装修详情失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存页面装修配置（likeshop 字段：id、type、name、data）
     * POST /mall/decoration/page/save
     */
    @PostMapping("/save")
    public ResponseResult<Boolean> setDecoratePages(@RequestBody Map<String, Object> params) {
        try {
            log.info("保存页面装修配置，参数 keys：{}", params != null ? params.keySet() : null);
            likeDecoratePageAdminService.save(params);
            return ResponseResult.success(true);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("保存页面装修配置失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 获取首页装修列表
     * POST /mall/decoration/home/list
     * 
     * @param params 查询参数
     * @return 首页装修列表
     */
    @PostMapping("/home/list")
    public ResponseResult<Map<String, Object>> getHomeLists(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取首页微页面列表，参数：{}", params);
            return ResponseResult.success(likeDecoratePageAdminService.homeLists(params));
        } catch (Exception e) {
            log.error("获取首页装修列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除装修页面
     * POST /mall/decoration/page/delete
     * 
     * @param params 删除参数（包含 page_id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delPages(@RequestBody Map<String, Object> params) {
        try {
            log.info("删除装修页面，参数：{}", params);
            Integer id = null;
            if (params.containsKey("id")) {
                id = Integer.parseInt(params.get("id").toString());
            } else if (params.containsKey("page_id")) {
                id = Integer.parseInt(params.get("page_id").toString());
            }
            if (id == null) {
                return ResponseResult.error("缺少页面 id");
            }
            likeDecoratePageAdminService.delete(id);
            return ResponseResult.success(true);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除装修页面失败", e);
            return ResponseResult.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取装修页面列表（分页）
     * POST /mall/decoration/page/list
     * 
     * @param params 查询参数
     * @return 装修页面列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<DecoratePage>> getPageList(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询装修页面列表，参数：{}", params);
            IPage<DecoratePage> page = decoratePageService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("查询装修页面列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 设为首页
     * POST /mall/decoration/page/setHome
     * 
     * @param params 查询参数（包含 shop_id 和 page_id）
     * @return 是否成功
     */
    @PostMapping("/setHome")
    public ResponseResult<Boolean> setHomePage(@RequestBody Map<String, Object> params) {
        try {
            log.info("设为首页，参数：{}", params);
            Integer id = null;
            if (params.containsKey("id")) {
                id = Integer.parseInt(params.get("id").toString());
            } else if (params.containsKey("page_id")) {
                id = Integer.parseInt(params.get("page_id").toString());
            }
            if (id == null) {
                return ResponseResult.error("缺少页面 id");
            }
            // 设为首页只在同一端内互斥：pagePlatform 缺省为 mobile（H5/小程序行为不变）
            String platform = params.get("pagePlatform") != null
                    ? params.get("pagePlatform").toString()
                    : (params.get("page_platform") != null
                            ? params.get("page_platform").toString()
                            : LikeDecoratePageAdminService.PLATFORM_MOBILE);
            likeDecoratePageAdminService.setHome(id, platform);
            return ResponseResult.success(true);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("设为首页失败", e);
            return ResponseResult.error("设置失败：" + e.getMessage());
        }
    }

    /**
     * 新增装修页面（likeshop：type、name、data）
     * POST /mall/decoration/page/add
     *
     * @return 新页面 id（前端 addPages 直接用作 data）
     */
    @PostMapping("/add")
    public ResponseResult<Integer> addPage(@RequestBody Map<String, Object> params) {
        try {
            log.info("新增装修页面，参数 keys：{}", params != null ? params.keySet() : null);
            Integer id = likeDecoratePageAdminService.add(params);
            return ResponseResult.success(id);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增装修页面失败", e);
            return ResponseResult.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 模板库（后台「装修管理 → 模板选择」第二步）。
     * POST /mall/decoration/page/template/list
     *
     * @return 模板列表，含 code / name / summary / themeColor / style / columns
     */
    @PostMapping("/template/list")
    public ResponseResult<List<Map<String, Object>>> templateList() {
        return ResponseResult.success(likeDecoratePageAdminService.templates());
    }

    /**
     * 读取某装修页面当前套用的模板 code。
     * POST /mall/decoration/page/template/current
     *
     * @param params { pageId }
     * @return templateCode，未套用为空串
     */
    @PostMapping("/template/current")
    public ResponseResult<String> currentTemplate(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(likeDecoratePageAdminService.currentTemplate(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 为装修页面套用模板（templateCode 传空串表示取消套用）。
     * POST /mall/decoration/page/template/apply
     *
     * <p>写入 hou_base_decorate_page.page_template_code，与 H5 首页同一条记录，保存即生效。
     *
     * @param params { pageId, templateCode }
     * @return 实际生效的 code
     */
    @PostMapping("/template/apply")
    public ResponseResult<String> applyTemplate(@RequestBody Map<String, Object> params) {
        try {
            log.info("套用页面模板，参数：{}", params);
            return ResponseResult.success(likeDecoratePageAdminService.applyTemplate(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("套用页面模板失败", e);
            return ResponseResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 将 Map 参数转换为 DecoratePage 对象
     * TODO: 实际项目中建议使用 BeanUtils.copyProperties() 进行转换
     *
     * @param params 参数 Map
     * @return DecoratePage 实体
     */
    private DecoratePage convertToDecoratePage(Map<String, Object> params) {
        DecoratePage page = new DecoratePage();
        
        // 基础字段
        if (params.containsKey("page_id")) {
            page.setPageId(Long.parseLong(params.get("page_id").toString()));
        }
        if (params.containsKey("shop_id")) {
            page.setShopId(Long.parseLong(params.get("shop_id").toString()));
        }
        if (params.containsKey("page_name")) {
            page.setPageName(params.get("page_name").toString());
        }
        if (params.containsKey("page_type")) {
            page.setPageType(Integer.parseInt(params.get("page_type").toString()));
        }
        if (params.containsKey("page_data")) {
            page.setPageData(params.get("page_data").toString());
        }
        if (params.containsKey("is_home")) {
            page.setIsHome(Integer.parseInt(params.get("is_home").toString()));
        }
        if (params.containsKey("status")) {
            page.setStatus(Integer.parseInt(params.get("status").toString()));
        }
        
        return page;
    }
}
