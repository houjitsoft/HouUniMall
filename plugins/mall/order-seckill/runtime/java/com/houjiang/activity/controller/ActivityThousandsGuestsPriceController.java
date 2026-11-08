package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityThousandsGuestsPriceService;
import com.houjiang.activity.dto.ThousandsGuestsPriceConfigDTO;
import com.houjiang.activity.dto.ThousandsGuestsPriceListQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 千客千价价格策略控制器
 * @author Amos
 */
@RestController
@RequestMapping("/thousandsGuestsPrice")
public class ActivityThousandsGuestsPriceController {


    @Autowired
    private ActivityThousandsGuestsPriceService activityThousandsGuestsPriceService;

    /**
     * 分页查询千客千价配置列表
     */
    @PostMapping("/config/page")
    public ResponseResult queryConfigPage(@RequestBody ThousandsGuestsPriceListQueryDTO param) {
        return activityThousandsGuestsPriceService.queryConfigPage(param);
    }

    /**
     * 查询配置详情
     */
    @PostMapping("/config/detail")
    public ResponseResult getConfigDetail(@RequestBody Map<String, Object> param) {
        if (param.get("id") == null) {
            return ResponseResult.error("id 不能为空");
        }
        Long id = Long.parseLong(String.valueOf(param.get("id")));
        return ResponseResult.success(activityThousandsGuestsPriceService.getById(id));
    }

    /**
     * 新增或更新千客千价配置
     */
    @PostMapping("/config/save")
    public ResponseResult saveConfig(@RequestBody ThousandsGuestsPriceConfigDTO param) {
        UserInfoVO userInfo = ActivitySupport.validateTokenAndGetUser();
        return activityThousandsGuestsPriceService.saveConfig(param, userInfo);
    }

    /**
     * 删除配置
     */
    @PostMapping("/config/delete")
    public ResponseResult deleteConfig(@RequestBody Map<String, Object> param) {
        if (param.get("id") == null) {
            return ResponseResult.error("id 不能为空");
        }
        Long id = Long.parseLong(String.valueOf(param.get("id")));
        return activityThousandsGuestsPriceService.deleteConfig(id);
    }

    /**
     * 更新启用状态
     */
    @PostMapping("/config/status/update")
    public ResponseResult updateStatus(@RequestBody Map<String, Object> param) {
        if (param.get("id") == null || param.get("status") == null) {
            return ResponseResult.error("id 和 status 不能为空");
        }
        Long id = Long.parseLong(String.valueOf(param.get("id")));
        Integer status = Integer.parseInt(String.valueOf(param.get("status")));
        UserInfoVO userInfo = ActivitySupport.validateTokenAndGetUser();
        return activityThousandsGuestsPriceService.updateStatus(id, status, userInfo);
    }

    /**
     * 查询用户个性化价格
     */
    @PostMapping("/price/query")
    public ResponseResult queryPersonalPrice(@RequestBody Map<String, Object> param) {
        return activityThousandsGuestsPriceService.queryPersonalPrice(param);
    }
}
