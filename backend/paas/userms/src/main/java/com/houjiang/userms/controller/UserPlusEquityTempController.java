package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName UserPlusEquityTempController
 * @Description 会员权益临时表
 * @Author Amos
 * @Date 2023/5/18 18:19
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/plusEquityTemp")
public class UserPlusEquityTempController extends BaseController{


    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    @PostMapping("/query/selectPlusEquityTemps")
    public ResponseResult selectPlusEquityTemps(@RequestBody List<Integer> id) {
        ResponseResult result = userPlusEquityTempService.selectPlusEquityTemps(id);
        return result;
    }
}
