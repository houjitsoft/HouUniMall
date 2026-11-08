package com.houjiang.goods.feign;

import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/25 17:45
 */

@FeignClient("userms")
public interface UserServiceClient {


    /**
     * 修改聚宝盆用户表信息
     *
     * @param map
     * @return
     */
    @PostMapping("/user/treasure/update")
    ResponseResult updateTreasureByMap(@RequestBody Map<String, Object> map);

    /**
     * 查询微客的设置
     */
    @GetMapping("/user/vkSet/vkQuerySetting")
    ResponseResult vkQuerySetting();

    /**
     * 根据用户ID查找用户名称
     *
     * @param userId 用户Id
     * @return 用户名称
     */
    @GetMapping("/user/ms/feign/queryUserNameByUserId")
    String queryUserNameByUserId(@RequestParam("userId") String userId);

    /**
     * @param param 参数
     * @return 处理结果
     */
    @PostMapping("/user/upload/base/feign/selectImageSpaceList")
    List<ManageShopVideoDTO> selectImageSpaceList(@RequestBody ManageShopVideoDTO param);
}
