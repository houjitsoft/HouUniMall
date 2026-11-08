package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.service.IChannelOaMenusService;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.plugin.wechat.WxMnpDriver;
import com.houjiang.merchant.common.util.*;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMenuService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpMenuServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ChannelOaMenusServiceImpl implements IChannelOaMenusService {

    /**
     * 菜单详情
     *
     * @author Amos
     * @return JSONArray
     */
    @Override
    public JSONArray detail() {
        String json = ConfigUtils.get("oa_setting", "menus", "[]");
        return JSONArray.parseArray(json);
    }

    /**
     * 菜单保存
     *
     * @author Amos
     * @param objs 参数
     * @param isPublish 是否发布
     */
    @Override
    public void save(List<Object> objs, Boolean isPublish) {
        if (objs.size() > 3) {
            throw new OperateException("一级菜单超出限制(最多3个)");
        }

        List<Map<String, String>> params = new LinkedList<>();
        for (Object o : objs) {
            params.add(MapUtils.objectToMap(o));
        }

        List<WxMenuButton> menuButtons = new LinkedList<>();
        for (Map<String, String> item : params) {
            // 一级菜单
            Assert.notNull(item.get("name"), "一级菜单名称不能为空");
            WxMenuButton wxMenuButton = new WxMenuButton();
            wxMenuButton.setName(item.get("name"));
            if (String.valueOf(item.get("has_menu")).equals("false")) {
                Assert.notNull(item.get("type"), "一级菜单type参数缺失");
                if (item.get("type").equals("miniprogram")) {
                    Assert.notNull(item.get("appid"), "一级菜单appId参数缺失");
                    Assert.notNull(item.get("url"), "一级菜单url数缺失");
                    Assert.notNull(item.get("pagepath"), "一级菜单pagePath数缺失");
                    wxMenuButton.setType(item.get("type"));
                    wxMenuButton.setAppId(item.get("appid"));
                    wxMenuButton.setUrl(item.get("url"));
                    wxMenuButton.setPagePath(item.get("pagepath"));
                } else {
                    Assert.notNull(item.get("url"), "一级菜单url数缺失");
                    wxMenuButton.setType(item.get("type"));
                    wxMenuButton.setUrl(item.get("url"));
                    wxMenuButton.setAppId(item.getOrDefault("appid", ""));
                }
                menuButtons.add(wxMenuButton);
            }

             // 子级菜单
            if (String.valueOf(item.get("has_menu")).equals("true")) {
                Assert.notNull(item.get("sub_button"), "子级菜单不能为空");
                List<Map<String, String>> subButtons = ListUtils.stringToListAsMapStr(item.get("sub_button"));

                List<WxMenuButton> subMenuButtons = new ArrayList<>();
                if (subButtons.size() > 5) {
                    throw new OperateException("子级菜单超出限制(最多5个)");
                }
                for (Map<String, String> subItem : subButtons) {
                    WxMenuButton subMenuButton = new WxMenuButton();
                    Assert.notNull(subItem.get("type"), "子级菜单type参数缺失!");
                    if (subItem.get("type").equals("miniprogram")) {
                        Assert.notNull(subItem.get("appid"), "子级菜单appId参数缺失!");
                        Assert.notNull(subItem.get("url"), "子级菜单url数缺失!");
                        Assert.notNull(subItem.get("pagepath"), "子级菜单pagePath数缺失!");
                        subMenuButton.setType(subItem.get("type"));
                        subMenuButton.setName(subItem.get("name"));
                        subMenuButton.setAppId(subItem.get("appid"));
                        subMenuButton.setUrl(subItem.get("url"));
                        subMenuButton.setPagePath(subItem.get("pagepath"));
                    } else {
                        Assert.notNull(subItem.get("url"), "子级菜单url数缺失");
                        subMenuButton.setType(subItem.get("type"));
                        subMenuButton.setName(subItem.get("name"));
                        subMenuButton.setUrl(subItem.get("url"));
                    }

                    subMenuButtons.add(subMenuButton);

                }
                wxMenuButton.setType("click");
                wxMenuButton.setSubButtons(subMenuButtons);
                menuButtons.add(wxMenuButton);
            }
        }

        ConfigUtils.set("oa_setting", "menus", JSON.toJSONString(objs));

        if (isPublish) {
            try {
                WxMenu wxMenu = new WxMenu();
                wxMenu.setButtons(menuButtons);

                WxMpService wxMpService = WxMnpDriver.oa();
                WxMpMenuService wxMpMenuService = new WxMpMenuServiceImpl(wxMpService);
                wxMpMenuService.menuCreate(wxMenu);
            } catch (WxErrorException e) {
                throw new OperateException(e.getError().getErrorCode() + ": " + e.getError().getErrorMsg());
            }
        }
    }

}
