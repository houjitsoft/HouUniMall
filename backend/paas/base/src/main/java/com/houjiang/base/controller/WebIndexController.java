package com.houjiang.base.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName BaseFloorAdpageController
 * @Description pc首页装修
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/web/index")
public class WebIndexController extends BaseController {

    /**
     * 查询首页楼层
     *
     * @return
     */
    @GetMapping("/all/adpage/floor")
    public ResponseResult queryAllAdpageFloor() {
        ResponseResult resultResp = webIndexService.queryAllAdpageFloor();
        return resultResp;
    }

    /**
     * 查询热搜词,首页导航,轮播,热销商品
     *
     * @return
     */
    @GetMapping(value = "/query")
    public ResponseResult queryIndex(@RequestParam(value = "integralType", required = false) Integer integralType) {
        return webIndexService.queryIndex(integralType);
    }

    /**
     * pc 首页 天天特价 特色专题 为你推荐 查询解析
     */
    @PostMapping(value = "/selectPcIndexConfigAll")
    public ResponseResult selectPcIndexConfigAll() {
        return webIndexService.selectPcIndexConfigAll();
    }



    /**
     * pc首页查询分类信息
     *
     * @param catId
     * @param goodsType
     * @param token
     * @return
     */
    @GetMapping("/goods/cat/id/query")
    public ResponseResult queryByGoodsCatId(@RequestParam Integer catId, Integer goodsType, String token) {

        return webIndexService.queryByGoodsCatId(catId, goodsType, token);
    }

    /**
     * 查询首页楼层
     *
     * @return
     */
    @PostMapping("/adpage/floor/goods")
    public ResponseResult queryAdpageFloorGoods(@RequestBody Map<String, Object> map) {

        return webIndexService.queryAdpageFloorGoods(map);
    }

    /**
     * 查询商品分类
     *
     * @return
     */
    @GetMapping("/itemcat/query")
    public ResponseResult queryItemCat(@RequestParam("catParentId") Integer catParentId,
                                       @RequestParam(name = "shopId", required = false) Integer shopId) {

        return webIndexService.queryItemCat(catParentId, shopId);
    }

    /**
     * 查询导航
     *
     * @return
     */
    @GetMapping("/platform/nav/query")
    public ResponseResult queryPlatformNav() {

        return webIndexService.queryPlatformNav();
    }

    /**
     * 商品列表头部信息
     *
     * @param param
     * @return
     */
    @PostMapping("/query/header/filter")
    public ResponseResult queryGoodsListHeaderFilter(@RequestBody(required = false) WebGoodsListQueryDTO param) {

        return webIndexService.queryGoodsListHeaderFilter(param);
    }

    /**
     * 获取上传图片配置地址
     *
     * @param configKey
     * @return
     */
    @PostMapping("/image/configuration/address")
    public ResponseResult getImageConfigUrl(@RequestParam String configKey) {
        return webIndexService.getImageConfigUrl(configKey);
    }

    /**
     * 查询首页底部
     *
     * @return
     */
    @GetMapping(value = "/buttom/help")
    public ResponseResult queryButtomHelp() {

        return webIndexService.queryButtomHelp();
    }

	/**
	 * 查询 pc 首页 天天特价 特色专题 为你推荐 楼层装修 智能推荐
	 */
	@SysLog("首页访客")
	@PostMapping(value="/newQueryPcIndex")
	public ResponseResult newQueryPcIndex(@RequestBody Map<String,Object> map){
		return  webIndexService.newQueryPcIndex(map);
	}

	/**
	 * 移动端首页数据查询
	 * 提供轮播图、导航等首页数据
	 */
	@PostMapping(value="/index/list")
	public ResponseResult getIndexList(@RequestBody(required = false) Map<String,Object> params){
		try {
			// 调用现有的首页服务获取数据
			return webIndexService.selectPcIndexConfigAll();
		} catch (Exception e) {
			return ResponseResult.error("获取首页数据失败: " + e.getMessage());
		}
	}

    /**
     * 关闭 或 开启 pc 首页 楼层
     *
     * @param map
     * @return
     */
    @PostMapping("/pcIndexFloor/update")
    public ResponseResult pcIndexFloorUpdate(@RequestBody Map<String, Object> map) {
        ResponseResult result = webIndexService.pcIndexFloorUpdate(map);
        return result;
    }


    /**
     * 关闭 或 开启 pc 首页 天天特技 特色专题 为你推荐 楼层
     *
     * @param map
     * @return
     */
    @PostMapping("/pcIndexConfig/update")
    public ResponseResult pcIndexConfigUpdate(@RequestBody Map<String, Object> map) {
        ResponseResult result = webIndexService.pcIndexConfigUpdate(map);
        return result;
    }

    /**
     * 检测应用中心状态
     *
     * @return
     */
    @RequestMapping("/detectingApplicationStatus")
    public ResponseResult detectingApplicationStatus() {
        return webIndexService.detectingApplicationStatus();
    }

    /**
     * 检测Im应用中心状态 及查询im配置
     *
     * @return
     */
    @RequestMapping("/detectingImStatus")
    public ResponseResult detectingImStatus(@RequestParam("configKey") String config) {
        return webIndexService.detectingImStatus(config);
    }

    /**
     * 检测Im应用中心状态 及查询imAppKey
     *
     * @return
     */
    @RequestMapping("/detectingImAppKey")
    public ResponseResult detectingImAppKey() {
        return webIndexService.detectingImAppKey();
    }

    /**
     * 检测Im应用中心状态 及查询用户是否有未读消息
     *
     * @param token
     * @return
     */
    @RequestMapping("/detectingImChatHistoryRead")
    public ResponseResult detectingImChatHistoryRead(@RequestParam("token") String token) {
        return webIndexService.detectingImChatHistoryRead(token);
    }
}
