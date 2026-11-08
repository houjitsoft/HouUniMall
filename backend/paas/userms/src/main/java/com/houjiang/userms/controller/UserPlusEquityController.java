package com.houjiang.userms.controller;

import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserPlusEquityController
 * @Description 会员权益
 * @Author Amos
 * @Date 2023/5/18 18:05
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/plusEquity")
public class UserPlusEquityController extends BaseController {


    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    @PostMapping("/query")
    public ResponseResult selectPlusEquitys(@RequestBody List<Integer> id) {
        ResponseResult result = userPlusEquityService.selectPlusEquitys(id);

        return result;
    }

    /**
     * 添加会员权益
     * @param dto
     * @return
     */
    @PostMapping("/insert")
    public ResponseResult insertPlusEquity(@RequestBody PlusEquityDTO dto) {
        ResponseResult result = userPlusEquityService.insertPlusEquity(dto);

        return result;
    }

    /**
     *删除会员权益
/**
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deletePlusEquity(@RequestBody PlusEquityDTO dto) {
        ResponseResult result = userPlusEquityService.deletePlusEquity(dto);

        return result;
    }

    /**
     *更新会员权益状态
/**
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updatePlusEquity(@RequestBody PlusEquityDTO dto) {
        ResponseResult result = userPlusEquityService.updatePlusEquity(dto);

        return result;
    }


    /**
     *查询会员权益
/**
     * @param map
     * @return
     */
    @PostMapping("/select")
    public ResponseResult selectPlusEquity(@RequestBody Map<String, Object> map) {
        ResponseResult result = userPlusEquityService.selectPlusEquity(map);

        return result;
    }

    /**
     * 查询会员权益
     * @param id
     * @return
     */
    @PostMapping("/feign/selectplusequitys")
    public List<PlusEquityEntityVO> selectplusequitysFeign(@RequestBody  List<Integer> id){
        List<PlusEquityEntityVO> selectplusequitys = userPlusEquityService.selectplusequitys(id);
        return selectplusequitys;
    }
}
