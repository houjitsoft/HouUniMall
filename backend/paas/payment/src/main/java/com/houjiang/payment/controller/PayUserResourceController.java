package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CardMemberDetailDTO;
import io.seata.core.exception.TransactionException;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @ClassName PayUserResourceController
 * @Description 用户资源
 * @Author Amos
 * @Date 2023/6/19 13:56
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("payment/userResource")
public class PayUserResourceController extends  BaseController{


    /**
     * 钱包余额展示
     *
     * @param map
     * @return
     */
    @PostMapping("/balanceDisplay")
    public ResponseResult balanceDisplay(@RequestBody Map<String, Object> map) {
        ResponseResult result = payUserResourceService.balanceDisplay(map);
        return result;
    }

    /**
     * 增加卖家金额
     *
     * @param map
     * @return
     */
    @PostMapping("/addMoney")
    public ResponseResult addMoney(@RequestBody Map<String, Object> map) {
        ResponseResult isUpdateSuccess = payUserResourceService.addMoney(map);
        return isUpdateSuccess;
    }

    /**
     * 用户余额提现
     *
     * @param map
     * @param request
     * @return
     */
    @PostMapping("/withdrawal")
    public ResponseResult withdrawal(@RequestBody Map<Object, Object> map, HttpServletRequest request) {
        ResponseResult result = payUserResourceService.withdrawal(map, request);
        return result;
    }

    /**
     * 用户转账
     *
     * @param map
     * @return
     */
    @PostMapping("/transfer")
    public ResponseResult transfer(@RequestBody Map<Object, Object> map) {
        ResponseResult result = payUserResourceService.transfer(map);
        return result;
    }

    /**
     * 平台查询 会员充值
     *
     * @param param
     * @return
     * @throws TransactionException
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody CardMemberDetailDTO param) throws TransactionException {
        ResponseResult result = payUserResourceService.updateCardMemberDetailPort(param);
        return result;
    }


    /**
     * 平台查询 会员充值列表
     *
     * @param map
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody Map<String, Object> map) {
        ResponseResult result = payUserResourceService.query(map);
        return result;
    }

    /**
     * 修改个人余额
     * @param userId
     * @param rechargeAmount
     * @return
     */
    @PostMapping("/updateUserBalance")
    public ResponseResult updateUserBalance(@RequestParam("userId") String userId, @RequestParam("rechargeAmount") BigDecimal rechargeAmount) {
        ResponseResult result = payUserResourceService.updateUserBalance(userId, rechargeAmount);
        return result;
    }

    /**
     * 查询个人余额
     * @param userId
     * @return
     */
    @GetMapping("/feign/queryAccountBalance")
    public String queryAccountBalance(@RequestParam("userId") String userId){
        String result = payUserResourceService.queryAccountBalance(userId);
        return result;
    }

    /**
     * 修改会员余额
     * @param manageMoney
     */
    @PostMapping("/feign/updateCardMemberDetailPort")
    public void updateCardMemberDetailPort(@RequestBody Map<String, Object> manageMoney){
        payUserResourceService.updateCardMemberDetailPortFeign(manageMoney);
    }

    /**
     * 会员余额修改
     * @param manageMoney
     * @return
     */
    @PostMapping("/feign/updateCardMemberDetailPort2")
    public Integer updateCardMemberDetailPort2(@RequestBody Map<String, Object> manageMoney){
        Integer integer = payUserResourceService.updateCardMemberDetailPort2(manageMoney);
        return integer;
     }

}
