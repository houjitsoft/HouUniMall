package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.houjiang.merchant.service.ISettingCopyrightService;
import com.houjiang.merchant.validate.setting.SettingCopyrightValidate;
import com.houjiang.merchant.vo.setting.SettingCopyrightVo;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.ConfigUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 网站备案服务实现类
 */
@Service
public class SettingCopyrightServiceImpl implements ISettingCopyrightService {

    /**
     * 获取网站备案信息
     *
     * @author Amos
     * @return List<SettingCopyrightVo>
     */
    @Override
    public List<SettingCopyrightVo> getCopyright() {
        String config = ConfigUtils.get("copyright", "config", "[]");
        List<Map<String, String>> copyright = ListUtils.stringToListAsMapStr(config);

        List<SettingCopyrightVo> list = new LinkedList<>();
        for (Map<String, String> map : copyright) {
            SettingCopyrightVo vo = new SettingCopyrightVo();
            vo.setKey(map.getOrDefault("key", ""));
            vo.setValue(map.getOrDefault("value", ""));
            list.add(vo);
        }

        return list;
    }

    /**
     * 保存网站备案信息
     *
     * @author Amos
     * @param copyrightValidate 参数
     */
    @Override
    public void setCopyright(SettingCopyrightValidate copyrightValidate) {
        String s = JSON.toJSONString(copyrightValidate.getConfig());
        ConfigUtils.set("copyright", "config", s);
    }

}
