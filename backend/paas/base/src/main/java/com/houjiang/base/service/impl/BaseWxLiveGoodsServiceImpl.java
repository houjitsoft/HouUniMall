package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.GoodsInfoDTO;
import com.houjiang.base.dto.QuCommonsParamDTO;
import com.houjiang.base.dto.SubmitAuditCommonsDTO;
import com.houjiang.base.dto.SubmitAuditGoodsDTO;
import com.houjiang.base.entity.BaseWxLiveGoodsEntity;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseWxLiveGoodsMapper;
import com.houjiang.base.service.BaseWxLiveGoodsService;
import com.houjiang.base.utils.AccessTokenUtil;
import com.houjiang.base.vo.DelWxSubmitGoodsVO;
import com.houjiang.base.vo.WxSubmitGoodsVO;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.util.wx.HttpUtlis;
import com.houjiang.commons.util.wx.NewWxMaterialResult;
import com.houjiang.commons.util.wx.UploadWxUtil;
import com.houjiang.commons.vo.base.BaseWxLiveGoodsVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseWxLiveGoodsServiceImpl extends ServiceImpl<BaseWxLiveGoodsMapper, BaseWxLiveGoodsEntity>
    implements BaseWxLiveGoodsService{

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fontUrlPath}")
    private String urlPath;

    @Autowired
    private GoodsClient goodsClient;


    /**
     * 提交商品审核
     * @param param 包含商品ID和token的参数映射
     * @return 删除商品后的响应结果
     */
    @Override
    public ResponseResult upAuditCommons(Map<String, Object> param) {
        // 从参数中获取token
        String token = param.get("token").toString();
        // 构建删除商品的API路径
        String path = "https://api.weixin.qq.com/wxaapi/broadcast/goods/delete?access_token=";

        // 根据商品ID查询商品信息
        BaseWxLiveGoodsEntity wxLiveGoods =this.baseMapper.selectById(param.get("id").toString());

        // 准备传递给API的商品ID映射
        Map<String, Integer> map = new HashMap<>();
        if (null != wxLiveGoods) {
            map.put("goodsId", wxLiveGoods.getWxGoodsId());
        }
        // 获取微信访问令牌
        String accessToken = AccessTokenUtil.queryAccessToken();
        // 完善API路径
        path = path + accessToken;
        // 调用微信API提交商品审核
        DelWxSubmitGoodsVO result = restTemplate.postForObject(path, map, DelWxSubmitGoodsVO.class);

        // 根据API返回结果进行处理
        if (result.getErrcode() == 0) {
            // 商品删除成功，从数据库中移除
            Integer integer = this.baseMapper.deleteById(param.get("id").toString());
            if (integer > 0) {
               return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
        } else if (result.getErrcode() == 300015) {
            // 商品不存在
            return ResponseResult.success(BizCodeEnume.GOODS_NOT_EXIST);
        } else {
            // 其他错误情况
            return ResponseResult.success(BizCodeEnume.OPERATION_FAILED);
        }
        // 默认错误处理
        return ResponseResult.success(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 查询直播商品
     * @param param 包含查询条件和token的参数映射
     * @return 直播商品列表的响应结果
     */
    @Override
    public ResponseResult queryLiveCommons(Map<String, Object> param) {
        // 从参数中获取token
        String token = param.get("token").toString();
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
        // 如果用户信息存在，添加店铺ID到查询参数
        if (null != userInfo) {
            param.put("shopId",userInfo.getShopId());
        }else{
            param.put("shopId",null);
        }

        // 根据查询参数查询直播商品
        IPage<BaseWxLiveGoodsEntity> page=this.baseMapper.queryLiveCommons(new Query<BaseWxLiveGoodsEntity>().getPage(param),
                param);
        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 删除直播商品
     * @param param 包含待删除商品ID列表和token的参数映射
     * @return 删除操作的响应结果
     */
    @Override
    public ResponseResult delLiveCommons(Map<String, Object> param) {
        // 从参数中获取token
        String token = param.get("token").toString();
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息不存在，返回错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 获取待删除的商品ID列表
        List<Integer> commonIds = (List<Integer>) param.get("commonIds");
        // 准备实际删除的ID列表
        List<Integer> ids = new ArrayList<>();
        // 准备需要提交审核的商品ID映射
        Map<String, Object> map = new HashMap<>();
        // 遍历待删除列表，处理每个商品
        for (Integer commonId : commonIds) {
            QueryWrapper<BaseWxLiveGoodsEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("common_id",commonId);
            BaseWxLiveGoodsEntity wxLiveGoods = this.baseMapper.selectOne(queryWrapper);
            // 如果商品存在且未通过审核，加入删除列表
            if(null != wxLiveGoods) {
                if (1 != wxLiveGoods.getIsverify()) {
                    ids.add(wxLiveGoods.getId());
                } else {
                    // 如果商品已通过审核，提交重新审核
                    map.put("id",wxLiveGoods.getId());
                    this.upAuditCommons(map);
                }
            }
        }
        // 批量删除商品
        Integer integer = this.baseMapper.deleteBatchIds(ids);
        // 如果删除成功，返回成功响应
        if (integer > 0) {
            return ResponseResult.success();
        }
        // 删除失败，返回错误响应
        return ResponseResult.fail(2, "操作失败");
    }

    /**
     * 添加通用商品信息
     * @param param 包含token和通用商品ID的参数DTO
     * @return 添加操作的结果，成功或失败
     */
    @Override
    public ResponseResult addLiveCommons(QuCommonsParamDTO param) {
        // 通过token获取用户信息
        String token = param.getToken();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 若token非法，返回错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }

        // 获取需要添加的通用商品ID数组
        Integer[] commonIds=param.getCommonIds();
        // 初始化待添加的商品列表
        List<BaseWxLiveGoodsEntity> wxLiveGoodsList = new ArrayList<>();
        // 遍历通用商品ID，查询详细信息并设置状态，加入到商品列表
        for (Integer commonId : commonIds) {
            BaseWxLiveGoodsVO vo = goodsClient.queryCommonDetail(commonId);
            BaseWxLiveGoodsEntity wxLiveGoods =new BaseWxLiveGoodsEntity();
            BeanUtils.copyProperties(vo,wxLiveGoods);
            wxLiveGoods.setOperation(1);
            wxLiveGoods.setIsverify(4);
            wxLiveGoodsList.add(wxLiveGoods);
        }
        // 批量插入商品信息
        Integer integer = this.baseMapper.insertLiveGoods(wxLiveGoodsList);
        // 若插入成功，返回成功结果
        if (integer > 0) {
            return ResponseResult.success();
        }
        // 插入失败，返回失败信息
        return ResponseResult.fail(2, "操作失败");
    }

    /**
     * 提交通用商品审核
     * @param param 包含token、商品ID、审核状态和原因的参数DTO
     * @return 审核提交的结果，成功或失败
     */
    @Override
    public ResponseResult submitAuditCommons(SubmitAuditCommonsDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 若token有效，处理审核操作
        if (null != userInfo) {
            String path = HouConfig.WX_LIVE_GOODS;
            // 若审核不通过，更新商品信息并返回成功结果
            if (param.getIsverify().equals(CommonType.liveGoodsVerify.AUDIT_ERROR.getCode())) {
                Integer integer = this.baseMapper.update(null,new LambdaUpdateWrapper<BaseWxLiveGoodsEntity>()
                                             .eq(BaseWxLiveGoodsEntity::getId,param.getId())
                                             .set(BaseWxLiveGoodsEntity::getCause,param.getCause())
                                             .set(BaseWxLiveGoodsEntity::getIsverify, CommonType.liveGoodsVerify.AUDIT_ERROR.getCode())
                                             .set(BaseWxLiveGoodsEntity::getOperation,CommonType.commonStatus.NO.getCode()));
                if (integer > 0) {
                    return ResponseResult.success();
                }
            }
            // 根据商品ID查询商品信息
            BaseWxLiveGoodsEntity wxLiveGoods = this.baseMapper.selectById(param.getId());
            // 若商品名称过长，截取前13个字符
            if (wxLiveGoods.getCommonName().length() > 14) {
                wxLiveGoods.setCommonName(wxLiveGoods.getCommonName().substring(0, 13));
            }
            // 准备商品审核提交的参数
            SubmitAuditGoodsDTO goodsParam = new SubmitAuditGoodsDTO();
            GoodsInfoDTO goodsInfo = new GoodsInfoDTO();
            if (null != wxLiveGoods) {
                String accessToken = AccessTokenUtil.queryAccessToken();
                path = path + accessToken;
                //获取商品图片
                String commonImage = wxLiveGoods.getCommonImage();
                HttpUtlis httpUtlis = new HttpUtlis();
                Map<String, String> stringStringMap = httpUtlis.doGetImages(commonImage, 1, urlPath);
                if (null != stringStringMap.get("integer") && "false".equals(stringStringMap.get("integer"))) {
                    return ResponseResult.fail(BizCodeEnume.WX_LIVE_HOST_12);
                }
                NewWxMaterialResult materialResult = null;
                try {
                    materialResult = UploadWxUtil.uploadFile(stringStringMap.get("pathName"), accessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                goodsInfo.setCoverImgUrl(materialResult.getMediaId());
                goodsInfo.setName(wxLiveGoods.getCommonName());
                goodsInfo.setPriceType(1);
                goodsInfo.setPrice(wxLiveGoods.getGoodsPrice());
                goodsInfo.setUrl("pages/description/descrption?goodsId=" + wxLiveGoods.getGoodsId() + "&commonId=" + wxLiveGoods.getCommonId());
                goodsParam.setGoodsInfo(goodsInfo);
                try {
                    WxSubmitGoodsVO result = restTemplate.postForObject(path, goodsParam, WxSubmitGoodsVO.class);
                    // 若提交成功，更新商品信息并返回成功结果
                    if (result.getErrcode() == 0) {
                        Integer integer =  this.baseMapper.update(null,new LambdaUpdateWrapper<BaseWxLiveGoodsEntity>()
                                .eq(BaseWxLiveGoodsEntity::getId,param.getId())
                                .set(BaseWxLiveGoodsEntity::getWxGoodsId,result.getGoodsId())
                                .set(BaseWxLiveGoodsEntity::getCause,CommonType.liveGoodsVerify.WAIT_WX_AUDIT.getDesc())
                                .set(BaseWxLiveGoodsEntity::getIsverify, CommonType.liveGoodsVerify.WAIT_WX_AUDIT.getCode())
                                .set(BaseWxLiveGoodsEntity::getOperation,CommonType.commonStatus.NO.getCode()));
                        if (integer > 0) {
                            return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
                }
            }
            // 审核提交失败，返回失败结果
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
        // token非法，返回失败结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
}




