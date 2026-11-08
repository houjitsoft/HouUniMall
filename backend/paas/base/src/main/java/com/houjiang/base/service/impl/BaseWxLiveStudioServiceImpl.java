package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.AdLiveRoomParamDTO;
import com.houjiang.base.dto.AddLiveGoodsParamDTO;
import com.houjiang.base.dto.WxEstablishLiveRoomDTO;
import com.houjiang.base.entity.BaseWxLiveStudioEntity;
import com.houjiang.base.mapper.BaseWxLiveStudioMapper;
import com.houjiang.base.service.BaseWxLiveStudioService;
import com.houjiang.base.utils.AccessTokenUtil;
import com.houjiang.base.vo.AdLiveRoomVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.wx.HttpUtlis;
import com.houjiang.commons.util.wx.ImageUtils;
import com.houjiang.commons.util.wx.NewWxMaterialResult;
import com.houjiang.commons.util.wx.UploadWxUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseWxLiveStudioServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:56
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseWxLiveStudioServiceImpl extends ServiceImpl<BaseWxLiveStudioMapper, BaseWxLiveStudioEntity>
        implements BaseWxLiveStudioService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fontUrlPath}")
    private String urlPath;

    /**
     * 添加直播房间
     * @param param 直播房间参数
     * @return 添加结果
     */
    @Override
    public ResponseResult addLiveRoom(AdLiveRoomParamDTO param) {
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 若用户信息为空，则返回token无效的结果
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 添加直播房间到数据库
        Integer integer = this.baseMapper.addLiveRoom(param);
        // 若添加成功（大于0），则创建微信直播房间
        if (integer > 0) {
            // 创建直播键
            Map<String, Object> map = new HashMap<>();
            map.put("id", param.getId());
            map.put("shopId", param.getShopId());
            // 调用微信创建直播房间接口
            ResponseResult responseResult = wxFoundLiveRoom(map);
            return ResponseResult.success(responseResult);
        }
        // 若添加失败，则返回操作失败的结果
        return ResponseResult.fail(2, "操作失败");
    }

    /**
     * 在微信中创建直播房间
     * @param map 相关参数映射表
     * @return 创建结果
     */
    public ResponseResult wxFoundLiveRoom(Map<String, Object> map) {
        try {
            // 初始化HttpUtlis工具类
            HttpUtlis httpUtlis = new HttpUtlis();
            HttpUtlis httpUtlis1 = new HttpUtlis();
            HttpUtlis httpUtlis2 = new HttpUtlis();
            // 微信API主机地址
            String host = "https://api.weixin.qq.com";
            // 获取微信访问令牌
            String accessToken = AccessTokenUtil.queryAccessToken();
            // 创建直播房间的API路径
            String addpath = "/wxaapi/broadcast/room/create?access_token=" + accessToken;
            // 根据房间ID查询直播信息
            WxEstablishLiveRoomDTO wxEstablishLiveRoom = this.baseMapper.queryLiveRoom(Integer.valueOf(map.get("id").toString()));
            // 下载并处理封面图片
            Map<String, String> stringMap = httpUtlis.doGetImages(wxEstablishLiveRoom.getCoverImg(), 2, urlPath);
            String picName = stringMap.get("pathName");
            // 判断图片是否支持缩略图，不支持则返回失败结果
            Integer integer = ImageUtils.judgeIsThumbnail(picName);
            if (integer == 2) {
                return ResponseResult.fail(2, "该图片不支持缩略图");
            }
            // 上传封面图片到微信并更新封面图片ID
            NewWxMaterialResult materialResult = UploadWxUtil.uploadFile(picName, accessToken);
            wxEstablishLiveRoom.setCoverImg(materialResult.getMediaId());
            // 下载并处理分享图片
            Map<String, String> stringMap1 = httpUtlis1.doGetImages(wxEstablishLiveRoom.getShareImg(), 3, urlPath);
            String picName1 = stringMap1.get("pathName");
            Integer integer1 = ImageUtils.judgeIsThumbnail(picName1);
            if (integer1 == 2) {
                return ResponseResult.fail(2, "该图片不支持缩略图");
            }
            // 上传分享图片到微信并更新分享图片ID
            NewWxMaterialResult materialResult1 = UploadWxUtil.uploadFile(picName1, accessToken);
            wxEstablishLiveRoom.setShareImg(materialResult1.getMediaId());
            // 下载并处理feeds图片
            Map<String, String> stringMap2 = httpUtlis2.doGetImages(wxEstablishLiveRoom.getFeedsImg(), 4, urlPath);
            String picName2 = stringMap2.get("pathName");
            Integer integer2 = ImageUtils.judgeIsThumbnail(picName2);
            if (integer2 == 2) {
                return ResponseResult.fail(2, "该图片不支持缩略图");
            }
            // 上传feeds图片到微信并更新feeds图片ID
            NewWxMaterialResult materialResult2 = UploadWxUtil.uploadFile(picName2, accessToken);
            wxEstablishLiveRoom.setFeedsImg(materialResult2.getMediaId());
            // 调用微信接口创建直播房间
            AdLiveRoomVO result = restTemplate.postForObject(host + addpath, wxEstablishLiveRoom, AdLiveRoomVO.class);
            // 若创建成功，则添加商品到直播房间
            if (result.getErrcode() == 0) {
                this.addLiveGoods(Integer.valueOf(map.get("shopId").toString()), result.getRoomId());
                return ResponseResult.success("创建直播间成功", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(2, "创建直播间失败");
    }

    /**
     * 向直播房间添加商品
     * @param shopId 商店ID
     * @param roomId 直播房间ID
     * @return 添加结果
     */
    public Boolean addLiveGoods(Integer shopId,Integer roomId){
        Boolean b = false;
        // 初始化添加商品参数
        AddLiveGoodsParamDTO addLiveGoodsParam = new AddLiveGoodsParamDTO();
        // 查询已存在的直播房间ID
        List<Integer> integers = this.baseMapper.queryLiveRoomId(shopId);
        // 若存在直播房间，则设置商品ID和直播房间ID
        if (integers.size() != 0) {
            addLiveGoodsParam.setIds(integers);
            addLiveGoodsParam.setRoomId(roomId);
            // 获取微信访问令牌
            String accessToken = AccessTokenUtil.queryAccessToken();
            // 添加商品到直播房间的API路径
            String url = "https://api.weixin.qq.com/wxaapi/broadcast/room/addgoods?access_token=";
            // 调用微信接口添加商品到直播房间
            String result = restTemplate.postForObject(url + accessToken, addLiveGoodsParam, String.class);
            // 若添加成功，则返回true
            if (null != result) {
                b = true;
            }
        }
        return b;
    }
}
