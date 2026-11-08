package com.houjiang.base.controller;

import com.houjiang.base.dto.GdbMerchantAgreementDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName BaseMerchantAgreementController
 * @Description 入驻协议
 * @Author Amos
 * @Date 2023/6/21 13:57
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/base/merchant/agreement")
public class BaseMerchantAgreementController extends BaseController{


    /**
     * 查询入驻协议
     * @param gdbMerchantAgreement
     * @return
     */
    @PostMapping("/selectAll")
    public ResponseResult selectAllAgreement(@RequestBody GdbMerchantAgreementDTO gdbMerchantAgreement) {
        ResponseResult result = baseMerchantAgreementService.selectAllAgreement(gdbMerchantAgreement);
        return result;
    }


    /**
     * 修改或添加入驻协议
     *
     * @param file
     * @param sign
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateAgreement(@RequestParam("file") MultipartFile file, @RequestParam("sign") String sign) throws IOException {
        double fileSize = file.getSize();
        if (fileSize > 2 * 1024 * 1024) {
            return ResponseResult.fail(BizCodeEnume.FILE_SIZE_GT_TWO);
        }
        ResponseResult result = baseMerchantAgreementService.updateAgreement(file, sign);
        return result;
    }

    /**
     * 查询入驻协议
     *
     * @param map
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryAgreementAll(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseMerchantAgreementService.queryAgreement(map);
        return result;
    }
}
