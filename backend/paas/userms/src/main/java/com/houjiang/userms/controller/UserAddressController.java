package com.houjiang.userms.controller;


import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserAddressController
 * @Description  用户地址表
 * @Author Amos
 * @Date 2023-06-16 15:26:49
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/address")
public class UserAddressController extends BaseController {

    /**
     * 查询用户收货地址
     */
    @PostMapping("/getDef")
    public ResponseResult getDef(@RequestBody Map<String, String> paraMap) {
        ResponseResult result =userAddressService.getDef(paraMap);
        return result;
    }

    /**
     * 查询我的收货地址
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userAddressService.get(paraMap);
        return result;
    }

    /**
     * 更新收货地址
     *
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"userAddressContact","userAddressAddress"})
    @PostMapping("/update")
    @SysLog("更新收货地址")
    public ResponseResult update(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userAddressService.update(paraMap);
        return result;
    }

    /**
     * 删除收货地址
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/delete")
    @SysLog("删除收货地址")
    public ResponseResult delete(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userAddressService.delete(paraMap);
        return result;
    }

    /**
     * 新增收货地址
     *
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"userAddressContact","userAddressAddress"})
    @PostMapping("/add")
    @SysLog("新增收货地址")
    public ResponseResult add(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userAddressService.add(paraMap);
        return result;
    }

    /**
     * 设置默认地址
     * @param paraMap
     * @return
     */
    @PostMapping("/setDefault")
    public ResponseResult setDefault(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userAddressService.setDefault(paraMap);
        return result;
    }
}
