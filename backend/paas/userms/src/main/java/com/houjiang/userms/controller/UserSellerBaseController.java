package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserSellerBaseVO;
import com.houjiang.userms.dto.UserSellerBaseDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserSellerBaseController
 * @Description 卖家用户
 * @Author Amos
 * @Date 2023/6/21 11:38
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/sellerBase")
public class UserSellerBaseController extends  BaseController{


    /**
     * 根据shopId查询卖家信息
     *
     * @param returnMap
     * @return
     */
    @PostMapping("/info")
    public ResponseResult getSellerInfo(@RequestBody Map<String, String> returnMap) {
        ResponseResult result=sellerBaseService.getSellerInfo(returnMap);
        return result;
    }

    /**
     * 添加子账号
     * @param userSellerBase
     * @return
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody UserSellerBaseDTO userSellerBase){
        ResponseResult result=sellerBaseService.add(userSellerBase);
        return result;
    }

    /**
     * 获取子账号
     * @param paraMap
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody Map<String, String> paraMap){
        ResponseResult result=sellerBaseService.get(paraMap);
        return result;
    }

    /**
     * 删除子账号
     * @param paraMap
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, String> paraMap){
        ResponseResult result=sellerBaseService.delete(paraMap);
        return result;
    }

    /**
     * 编辑子账号
     * @param paraMap
     * @return
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, String> paraMap){
        ResponseResult result=sellerBaseService.update(paraMap);
        return result;
    }

    /**
     * 查询是否为商家子账号
     * @param userId
     * @return
     */
    @PostMapping("/queryShopSellerBase")
    public ResponseResult queryShopSellerBase(@RequestParam("userId") String userId){
        ResponseResult result=sellerBaseService.queryShopSellerBase(userId);
        return result;
    }

    /**
     * 根据userId查询卖家主账号userId
     *
     * @param returnMap
     * @return
     */
    @PostMapping("/getSellerAdmin")
    public String getSellerAdmin(@RequestBody Map<String, String> returnMap) {
        return sellerBaseService.getSellerAdmin(returnMap);
    }

    /**
     * 根据shopid查询有商家权限的账号
     *
     * @param shopId
     * @return
     */
    @PostMapping("/getSubAccount")
    public List<UserSellerBaseVO> getSubAccount(@RequestParam("shopId") String shopId) {
        return sellerBaseService.getSubAccount(shopId);
    }

    /**
     * 新增卖家信息
     * @param map
     * @return
     */
    @PostMapping("/feign/insertSellerbase")
    public Integer insertSellerbase(@RequestBody Map<String, Object> map){
        Integer count = sellerBaseService.insertSellerbase(map);
        return count;
    }

    /**
     * 根据店铺ID查询主账号用户ID
     * @param shopId
     * @return
     */
    @PostMapping("/feign/getShopAdmin")
    public String getShopAdmin(@RequestParam("shopId") String shopId){
        String userId = sellerBaseService.getShopAdmin(shopId);
        return userId;
    }

    /**
     * 查询用户Id
     *
     * @param shopId
     * @return
     */
    @GetMapping("/feign/getUserIDByShopId")
    public String getUserIDByShopId(@RequestParam("shopId") String shopId){
        String userId = sellerBaseService.getUserIDByShopId(shopId);
        return userId;
    }
}
