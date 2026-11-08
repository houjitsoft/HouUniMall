package com.houjiang.payment.controller;

import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.PayCardRecordDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName PayCardRecordController
 * @Description 生成充值卡信息记录表
 * @Author Amos
 * @Date 2023-05-31 15:35:49
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/cardRecord")
public class PayCardRecordController extends BaseController {

    /**
     * 平台 查询充值卡生成记录表
     *
     * @param payCardRecordDto
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody PayCardRecordDTO payCardRecordDto) {
        ResponseResult result = payCardRecordService.query(payCardRecordDto);
        return result;
    }

    /**
     * 平台 生成充值卡
     *
     * @param payCardRecordDto
     * @return
     */
    @PostMapping("add")
    public ResponseResult add(@RequestBody PayCardRecordDTO payCardRecordDto) {
        if (payCardRecordDto.getCardNumber() < 0) {
            return ResponseResult.fail(BizCodeEnume.CARD_NUMBER_MUST_GT_ZERO);
        }
        ResponseResult result = payCardRecordService.add(payCardRecordDto);
        return result;
    }

}
