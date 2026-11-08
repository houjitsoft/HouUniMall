package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserSellerGroupController
 * @Description 卖家用户组
 * @Author Amos
 * @Date 2023/7/21 9:09
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/sellerGroup")
public class UserSellerGroupController extends BaseController {

    /**
     * 查询权限组
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody Map<String, String> paraMap) {
        ResponseResult result=userSellerGroupService.get(paraMap);
        return result;
    }

    /**
     * 增加权限组
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, String> paraMap) {
        ResponseResult result=userSellerGroupService.add(paraMap);
        return result;
    }

    /**
     * 删除权限组
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, String> paraMap) {
        ResponseResult result=userSellerGroupService.delete(paraMap);
        return result;
    }

    /**
     * 编辑权限组
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, String> paraMap) {
        ResponseResult result= userSellerGroupService.update(paraMap);
        return result;
    }
}
