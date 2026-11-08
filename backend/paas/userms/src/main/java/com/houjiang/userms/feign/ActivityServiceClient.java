package com.houjiang.userms.feign;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("activity")
public interface ActivityServiceClient {

    /**
     * plus 会员 查询红包信息
     */
    @PostMapping("activity/redpacketBase/plus/selectRedPacket")
    List<HouRedpacketTemplateVO> selectNewRedpacketTem(@RequestParam List<Integer> id);

    /**
     * plus 会员 领取红包
     */
    @PostMapping("activity/redpacketBase/plus/insertRedPacket")
    ResponseResult insertXinRenYfRedpacketBase(@RequestBody Map<String, Object> insertMap);

    /**
     * 修改红包领取数量
     *
     * @param updateMap
     */
    @PostMapping("activity/redpacketBase/plus/update")
    ResponseResult updateRedpGiveout(@RequestBody Map<String, String> updateMap);

    /**
     * 查询红包模板
     *
     * @param redIdList
     * @return
     */
    @PostMapping("activity/redpacketBase/plus/selectRedByTemplateId")
    List<RedTemplateEntityVO> selectredtemplateId(@RequestParam("redIdList") List<Integer> redIdList);

    /**
     * 判断红包可用张数
     *
     * @param redIdList
     * @param userId
     * @return
     */
    @PostMapping("activity/redpacketBase/plus/selectUserRedNum")
    Integer selectuserred(@RequestParam("redIdList") List<Integer> redIdList, @RequestParam("userId") String userId);

    /**
     * 查询红包模板信息
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/activity/redpacketTemplate/list")
    ResponseResult redpacketTemplateList(@RequestBody Map<String, Object> paraMap);

    /**
     * 添加红包
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/activity/redpacketBase/insertRedpacketBase")
    ResponseResult insertRedpacketBase(@RequestBody Map<String, Object> paraMap);

}
