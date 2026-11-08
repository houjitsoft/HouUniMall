package com.houjiang.base.controller;

import com.houjiang.base.vo.ManageExpressQueryVO;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseExpressController
 * @Description 快递
 * @Author Amos
 * @Date 2023/6/19 16:29
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/base/express")
public class BaseExpressController extends BaseController{

    /**
     * 查询快递公司
     *
     * @return
     */
    @PostMapping("/query/noToken")
    public ResponseResult queryNoToken() {

        return baseExpressService.queryNoToken();
    }

    /**
     * 查询快递公司列表
     *
     * @param map
     * @return
     */
    @PostMapping("/list/query")
    public ResponseResult queryExpressList(@RequestBody Map<String, Object> map) {

        return baseExpressService.queryExpressList(map);
    }

    /**
     * 根据id查询快递信息
     *
     * @param expressId
     * @return
     */
    @GetMapping("/getInfoById")
    public ResponseResult getBaseExpressInfoById(@RequestParam("expressId") Integer expressId) {

        return baseExpressService.getBaseExpressInfoById(expressId);
    }

    /**
     * 查询快递公司 feign
     *
     * @return
     */
    @PostMapping("/query/noTokenTwo")
    public List<ManageExpressQueryVO> queryNoTokenTwo() {
        return baseExpressService.queryNoTokenTwo();
    }


    /**
     * 新增快递公司
     *
     * @param baseExpressReq
     * @return
     */
    @PostMapping("/add")
    public ResponseResult addExpress(@RequestBody BaseExpressDTO baseExpressReq) {
        if (baseExpressReq == null) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        } else if (baseExpressReq.getExpressName().length() > 30) {
            // 旧写法返回 BizCodeEnume.EXPRESS_NAME_PING_LIMIT（10086）。10086 已认领为 AUTH 域
            // 30801「登录失败，请重新登录」，前端据此清 token 跳登录页 —— 用户改个快递名会被踢下线。
            // 这里改成 VALIDATION 域参数校验码，「名称/首字母需小于 30 个字符」的具体规则不再出屏。
            return ResponseResult.fail(ValidationErrorCode.PARAM_INVALID.code(),
                    ValidationErrorCode.PARAM_INVALID.meta().userMessage());
        } else if (baseExpressReq.getExpressPinyin().length() > 30) {
            // 同上：快递首字母超长是参数校验问题，不是登录态问题
            return ResponseResult.fail(ValidationErrorCode.PARAM_INVALID.code(),
                    ValidationErrorCode.PARAM_INVALID.meta().userMessage());
        }
        ResponseResult result=baseExpressService.addExpress(baseExpressReq);
        return result;
    }

    /**
     * 修改快递公司
     *
     * @param baseExpressReq
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateExpress(@RequestBody BaseExpressDTO baseExpressReq) {
        ResponseResult result=baseExpressService.updateExpress(baseExpressReq);
        return result;
    }

    /**
     * 删除快递公司
     *
     * @param baseExpressReq
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteExpress(@RequestBody BaseExpressDTO baseExpressReq) {

        ResponseResult result=baseExpressService.deleteExpress(baseExpressReq);
        return result;
    }

    /**
     * 查询快递设置
     * @param paraMap
     * @return
     */
    @PostMapping("/queryByShopDpexpress")
    public ResponseResult queryByShopDpexpress(@RequestBody Map<String, Object> paraMap) {
        return baseExpressService.queryByShopDpexpress(paraMap);
    }


    /**
     * 根据物流公司Id查找名称
     * @param orderShippingExpressId
     * @return
     */
    @GetMapping("/queryBaseExpressById")
    public String queryBaseExpressById(@RequestParam("orderShippingExpressId") Integer orderShippingExpressId){
        return baseExpressService.queryBaseExpressById(orderShippingExpressId);
    }
}
