package com.houjiang.base.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName BaseTagClassController
 * @Description 会员标签等级
 * @Author Amos
 * @Date 2023/5/18 17:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/tagClass")
public class BaseTagClassController extends BaseController{


    /**
     * 根据用户id查询会员等级id
     * @param userId
     * @return
     */
    @PostMapping("/queryByUserId")
   public ResponseResult getMemberClassIdByUserId(@RequestParam("userId") String userId){
       ResponseResult result=baseTagClassService.getMemberClassIdByUserId(userId);
       return result;
    }


    /**
     * 更新会员等级
     *
     * @param map
     * @return
     */
    @PostMapping("/updateTagClassByUserId")
    public ResponseResult updateTagClassByUserId(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseTagClassService.updateTagClassByUserId(map);;
        return result;
    }




    /**
     * 注册修改会员等级
     * @param map
     * @return
     */
    @PostMapping("/saveTagClass")
    public ResponseResult saveTagClass(@RequestBody Map<String, Object> map) {
        ResponseResult result = baseTagClassService.saveTagClass(map);

        return result;
    }
    /**
     * 根据用户id查询会员等级名称 feign
     * @param userId
     * @return
     */
    @PostMapping("/queryMemberName/ByUserId")
    public String queryMemberNameByUserId(@RequestParam("userId") String userId){
        return baseTagClassService.queryMemberNameByUserId(userId);
    }
}
