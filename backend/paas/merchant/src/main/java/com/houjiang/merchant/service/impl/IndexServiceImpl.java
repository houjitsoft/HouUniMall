package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.*;
import com.houjiang.merchant.common.enums.AuditEnum;
import com.houjiang.merchant.common.enums.OrderEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.mall.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.IIndexService;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.vo.admin.AdminAccountDetailVo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 主页服务实现类
 */
@Service
public class IndexServiceImpl implements IIndexService {

    @Resource
    IMallGoodsService iMallGoodsService;
    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    IMallShopAdminService iMallShopAdminService;
    @Resource
    IMallShopService iMallShopService;
    @Resource
    IMallShopAdminMenuService iMallShopAdminMenuService;

    @Resource
    MallOrderMapper mallOrderMapper;

    @Resource
    MallGoodsMapper mallGoodsMapper;
    @Resource
    MallGoodsCommentMapper mallGoodsCommentMapper;
    @Resource
    MallShopVisitMapper mallShopVisitMapper;
    @Resource
    MallGoodsVisitMapper mallGoodsVisitMapper;

    /**
     * 控制台数据
     *
     * @author Amos
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> index() {

        JSONObject waitWork = new JSONObject();
        waitWork.put("wait_send", mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).eq("status", OrderEnum.STATUS_WAIT_SEND.getCode())));
        waitWork.put("wait_audit", mallGoodsMapper.selectCount(new QueryWrapper<MallGoods>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).eq("audit_status", AuditEnum.WAIT.getCode())));
        waitWork.put("wait_reply", mallGoodsCommentMapper.selectCount(new QueryWrapper<MallGoodsComment>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).eq("is_reply", YesNoEnum.NO.getCode())));
        waitWork.put("goods_stock_warning", mallGoodsMapper.selectCount(new QueryWrapper<MallGoods>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).gt("stock_warning", 0).apply("stock_warning >= total_stock")));

        JSONObject todayData = new JSONObject();
        // 今日订单
        List<Long> todayTime = TimeUtils.today();
        List<Long> yesterdayTime = TimeUtils.yesterday();
        // 今日订单
        todayData.put("today_orders", mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", todayTime.get(0), todayTime.get(1))));
        // 昨日订单
        todayData.put("yesterday_orders", mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", yesterdayTime.get(0), yesterdayTime.get(1))));
        // 今日收入
        todayData.put("today_sale", mallOrderMapper.sum("pay_money", new QueryWrapper<MallOrder>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", todayTime.get(0), todayTime.get(1))));
        // 昨日收入
        todayData.put("yesterday_sale", mallOrderMapper.sum("pay_money", new QueryWrapper<MallOrder>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", yesterdayTime.get(0), yesterdayTime.get(1))));
        // 今日进店人数
        todayData.put("today_shop_visit", mallShopVisitMapper.sum("num", new QueryWrapper<MallShopVisit>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", todayTime.get(0), todayTime.get(1))));
        // 昨日进店人数
        todayData.put("yesterday_shop_visit", mallShopVisitMapper.sum("num", new QueryWrapper<MallShopVisit>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", yesterdayTime.get(0), yesterdayTime.get(1))));
        // 今日商品浏览
        todayData.put("today_goods_visit", mallGoodsVisitMapper.sum("num", new QueryWrapper<MallGoodsVisit>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", todayTime.get(0), todayTime.get(1))));
        // 昨日商品浏览
        todayData.put("yesterday_goods_visit", mallGoodsVisitMapper.sum("num", new QueryWrapper<MallGoodsVisit>().eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", yesterdayTime.get(0), yesterdayTime.get(1))));

        // 7天营业额
        todayData.put("sale_7day", iMallOrderService.saleDay(7, MerchantThreadLocal.getShopId()));

        //商品销售额排行
        todayData.put("goods_sale_rank", iMallGoodsService.getGoodsSaleRank(10, MerchantThreadLocal.getShopId()));

        Map<String, Object> console = new LinkedHashMap<>();
        console.put("now", TimeUtils.timestampToDate(System.currentTimeMillis() / 1000));
        console.put("version", GlobalConfig.version);
        console.put("often_use", new JSONArray());
        console.put("shop", iMallShopService.detail(MerchantThreadLocal.getShopId()));
        console.put("wait_work", waitWork);
        console.put("today_data", todayData);
        return console;
    }

    /**
     * 公共配置
     *
     * @author Amos
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> config() {
        Map<String, String> website   = ConfigUtils.get("website");
        String copyright = ConfigUtils.get("copyright", "config", "");

        String captchaStatus = YmlUtils.get("merchant.captcha.status");

        Map<String, Object> map = new LinkedHashMap<>();
        // 文件域名
        map.put("oss_domain", UrlUtils.domain());
        //map.put("loginCaptcha", StringUtils.isNotNull(captchaStatus) && captchaStatus.equals("true"));
        // 网站名称
        map.put("web_name", website.getOrDefault("name", ""));
        // 网站图标
        map.put("web_favicon", UrlUtils.toShopAbsoluteUrl(website.getOrDefault("web_favicon", "")));
        // 网站logo
        map.put("web_logo", UrlUtils.toShopAbsoluteUrl(website.getOrDefault("web_logo", "")));
        // 登录页
        map.put("login_image", UrlUtils.toShopAbsoluteUrl(website.getOrDefault("login_image", "")));
        // 版权信息
        map.put("copyright_config", ListUtils.stringToListAsMapStr(copyright));
        return map;
    }

    @Override
    public JSONObject mySelf() {
        JSONObject ret = new JSONObject();
        AdminAccountDetailVo accountDetailVo = iMallShopAdminService.accountDetail(MerchantThreadLocal.getAdminId());
        ret.put("menu", iMallShopAdminMenuService.getShopAdminMenuList(MerchantThreadLocal.getAdminId()));
        ret.put("account", accountDetailVo);
        ret.put("access", iMallShopAdminMenuService.getAdminAccessList(MerchantThreadLocal.getAdminId()));
        return ret;
    }

}
