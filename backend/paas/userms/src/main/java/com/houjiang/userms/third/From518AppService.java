package com.houjiang.userms.third;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.houjiang.commons.vo.third.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @CreateDate: 2025/7/8 22:44
 * @author Amos
 */
@Slf4j
@Service
public class From518AppService {


    @Value("${from518.baseUrl:https://nscrm-t.jiahaofoods.com:11443}")
    private String baseUrl;

    @Value("${from518.signKey:shop9527@test}")
    private String signKey;

    @Value("${from518.brandId:shop}")
    private String brandId;

    /**
     * 获取用户店铺信息（用户身份）
     *
     * @param shopId 店铺id(跳转第三方会带的)，唯一标识，跳转商城链接会带过来
     * @param userId 用户id(跳转第三方会带的)，唯一标识，跳转商城链接会带过来
     * @return
     */
    public From518ResVO<ThirdUserShopVO> getUserAndShopInfo(Integer shopId, Integer userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("shopId", shopId);
        request.put("userId", userId);

        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/getUserAndShopInfo")
                .body(JSONUtil.toJsonStr(request))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("获取用户店铺信息（用户身份） execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, new TypeReference<From518ResVO<ThirdUserShopVO>>() {
        }, true);
    }


    /**
     * 获取用户积分信息
     *
     * @param shopId 店铺id(跳转第三方会带的)，唯一标识，跳转h5链接会带过来
     * @param userId 用户id(跳转第三方会带的)，唯一标识，跳转h5链接会带过来
     * @return
     */
    public From518ResVO<UserPointsInfoVO> getUserPointsInfo(Integer shopId, Integer userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("shopId", shopId);
        request.put("userId", userId);

        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/getUserPointsInfo")
                .body(JSONUtil.toJsonStr(request))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("获取用户积分信息 execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, new TypeReference<From518ResVO<UserPointsInfoVO>>() {
        }, true);
    }


    /**
     * 获取积分流水
     *
     * @param req 入参
     * @return
     */
    public From518ResVO<PageResVO<PointsFlowDetailResVO>> getPointsFlowDetail(PointsFlowDetailReqVO req) {

        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/getPointsFlowDetail")
                .body(JSONUtil.toJsonStr(req))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("获取积分流水 execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, new TypeReference<From518ResVO<PageResVO<PointsFlowDetailResVO>>>() {
        }, true);
    }

/*
    public static void localMain(String[] args) {
        String body = "{\n" +
                "    \"code\": 200,\n" +
                "    \"msg\": \"请求成功\",\n" +
                "    \"data\": {\n" +
                "        \"total\": 242,\n" +
                "        \"list\": [\n" +
                "            {\n" +
                "                \"id\": 6914544,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"参与活动消耗\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T18:18:23\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914543,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"参与活动消耗\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T17:41:06\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914541,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:39\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914540,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:34\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914539,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:30\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914538,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:25\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914537,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:21\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914536,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:18\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914535,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:09\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 6914534,\n" +
                "                \"flowType\": 2,\n" +
                "                \"flowAmount\": 8.00,\n" +
                "                \"flowTitle\": \"第三方活动扣除\",\n" +
                "                \"flowDesc\": \"用户因【红包活动消耗】，扣除【8嘉币】\",\n" +
                "                \"createTime\": \"2025-02-07T14:54:00\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"pageNum\": 2,\n" +
                "        \"pageSize\": 10,\n" +
                "        \"size\": 10,\n" +
                "        \"startRow\": 11,\n" +
                "        \"endRow\": 20,\n" +
                "        \"pages\": 25,\n" +
                "        \"prePage\": 1,\n" +
                "        \"nextPage\": 3,\n" +
                "        \"isFirstPage\": false,\n" +
                "        \"isLastPage\": false,\n" +
                "        \"hasPreviousPage\": true,\n" +
                "        \"hasNextPage\": true,\n" +
                "        \"navigatePages\": 8,\n" +
                "        \"navigatepageNums\": [\n" +
                "            1,\n" +
                "            2,\n" +
                "            3,\n" +
                "            4,\n" +
                "            5,\n" +
                "            6,\n" +
                "            7,\n" +
                "            8\n" +
                "        ],\n" +
                "        \"navigateFirstPage\": 1,\n" +
                "        \"navigateLastPage\": 8\n" +
                "    },\n" +
                "    \"requestId\": \"162aaa11-8472-4549-904e-ed01c0c05eb9\"\n" +
                "}";
        From518ResVO<PageResVO<PointsFlowDetailResVO>> bean = JSONUtil.toBean(body, new TypeReference<From518ResVO<PageResVO<PointsFlowDetailResVO>>>() {
        }, true);

        System.out.println("bean = " + bean);
    }
*/

    /**
     * 扣减流水
     *
     * @param req 入参
     * @return
     */
    public From518ResVO consumePoints(ConsumePointsReqVO req) {

        log.info("扣减流水 req = {}", req);
        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/consumePoints")
                .body(JSONUtil.toJsonStr(req))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("扣减流水 execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, From518ResVO.class);
    }

    /**
     * 新增流水
     *
     * @param req 入参
     * @return
     */
    public From518ResVO addPoints(AddPointsReqVO req) {

        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/addPoints")
                .body(JSONUtil.toJsonStr(req))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("新增流水  execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, From518ResVO.class);
    }

    /**
     * 获取用户店铺详细信息
     *
     * @param shopId 店铺id(跳转第三方会带的)，唯一标识，跳转商城链接会带过来
     * @param userId 用户id(跳转第三方会带的)，唯一标识，跳转商城链接会带过来
     * @return
     */
    public From518ResVO<ThirdUserShopVO> getUserAndShopDetail(Integer shopId, Integer userId) {

        Map<String, Object> request = new HashMap<>();
        request.put("shopId", shopId);
        request.put("userId", userId);
        HttpResponse execute = HttpUtil.createPost(baseUrl + "/api/user/openapi/third/activity/getUserAndShopDetail")
                .body(JSONUtil.toJsonStr(request))
                .addHeaders(getPublicHeader())
                .execute();

        log.info("获取用户店铺详细信息  execute = {}", execute);
        Assert.isTrue(execute.isOk(), "远程调用异常");
        String body = execute.body();

        return JSONUtil.toBean(body, new TypeReference<From518ResVO<ThirdUserShopVO>>() {
        }, true);
    }

/*    public static void main(String[] args) {
        Map<String, Object> request = new HashMap<>();
        request.put("shopId", "80889");
        request.put("userId", "28916");


        Map<String, String> map = new HashMap<>();
        long currentTime = System.currentTimeMillis() / 1000;
        String signStr = currentTime + "&shop9527@test&shop";
        String sign = DigestUtils.md5DigestAsHex((signStr).getBytes()).toUpperCase();

        map.put("x-sign", sign);
        map.put("x-timestamp", String.valueOf(currentTime));
        map.put("x-nonce", RandomUtil.randomString(8));
        map.put("x-brand-id", "shop");
        HttpResponse execute = HttpUtil.createPost("https://nscrm-t.jiahaofoods.com:11443/api/user/openapi/third/activity/getUserAndShopDetail")
                .body(JSONUtil.toJsonStr(request))
                .addHeaders(map)
                .execute();

        System.out.println("execute = " + execute);
    }*/

    private Map<String, String> getPublicHeader() {
        Map<String, String> map = new HashMap<>();
        long currentTime = System.currentTimeMillis() / 1000;
        String signStr = currentTime + "&" + signKey + "&" + brandId;
        String sign = DigestUtils.md5DigestAsHex((signStr).getBytes()).toUpperCase();

        map.put("x-sign", sign);
        map.put("x-timestamp", String.valueOf(currentTime));
        map.put("x-nonce", RandomUtil.randomString(8));
        map.put("x-brand-id", brandId);
        return map;
    }

}
