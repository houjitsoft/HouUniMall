package com.houjiang.base.controller;


import com.houjiang.commons.dto.base.BasePlatformBrankDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BasePlatformBrankController
 * @Description  平台收款银行卡表
 * @Author Amos
 * @Date 2023-07-03 09:56:25
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/platformBrank")
public class BasePlatformBrankController extends BaseController{
    /**
     *  获取平台收款银行卡信息
     * @return
     */
    @PostMapping("/queryBrank")
    public ResponseResult queryBrank(@RequestBody UserTokenBeanDTO param){
        return basePlatformBrankService.queryBrank(param);
    }


    /**
     * 更新或添加 银行卡信息
     *
     * @param basePlatformBrankDto
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateBrank(@RequestBody BasePlatformBrankDTO basePlatformBrankDto) {
        ResponseResult result=basePlatformBrankService.updateBrank(basePlatformBrankDto);
        return result;
    }


    /**
     * 查看上传支付凭证 (获取收款银行卡信息）加密返回
     * @return
     */
    @PostMapping("/queryBrankEncryption")
    public ResponseResult queryBrankEncryption(@RequestBody UserTokenBeanDTO param){
        ResponseResult result=basePlatformBrankService.queryBrankEncryption(param);
        return result;
    }

    /**
     * 平台查询银行卡信息 后端feign调用
     *
     * @return
     */
    @PostMapping("/feign/query")
    public ResponseResult queryBrankFeign() {
        return basePlatformBrankService.queryBrankFeign();
    }
}
