package com.houjiang.payment.controller;


import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName PayConsumeTradeController
 * @Description 交易订单表-强调唯一订单-充值则先创建充值订单
 * @Author Amos
 * @Date 2023-05-31 16:13:57
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/consumeTrade")
public class PayConsumeTradeController extends BaseController {
    /**
     * 插入交易记录
     *
     * @param ctp
     * @return
     */
    @PostMapping("insert")
    public boolean insert(@RequestBody ConsumeTradeParaDTO ctp) {
        boolean isInsertSuccess = payConsumeTradeService.insert(ctp);
        return isInsertSuccess;
    }
}
