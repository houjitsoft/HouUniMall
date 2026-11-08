package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.QueryResultDTO;
import com.houjiang.base.dto.WxChatIdParamDTO;
import com.houjiang.base.entity.BaseWxLiveHostEntity;
import com.houjiang.base.mapper.BaseWxLiveHostMapper;
import com.houjiang.base.service.BaseWxLiveHostService;
import com.houjiang.base.utils.AccessTokenUtil;
import com.houjiang.base.vo.*;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.config.RedisCache;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName BaseWxLiveHostServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/7/24 13:38
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseWxLiveHostServiceImpl extends ServiceImpl<BaseWxLiveHostMapper, BaseWxLiveHostEntity>
        implements BaseWxLiveHostService {

    @Resource
    private RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 查询直播主播状态
     * @param param 用户令牌信息
     * @return 查询结果
     */
    @Override
    public ResponseResult queryLiveHostState(UserTokenBeanDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 若用户信息为空，则返回非法令牌错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 根据用户店铺ID查询主播状态
        QuLiveHostStateResultVO result = this.baseMapper.queryLiveHostState(Integer.valueOf(userInfo.getShopId()));
        // 若查询结果不为空，则返回成功结果，包含查询数据
        if (null != result) {
            return ResponseResult.success(1, "请求成功", result);
        }
        // 若查询结果为空，则返回成功结果，说明未添加主播
        return ResponseResult.success(1, "未添加主播", result);
    }

    /**
     * 申请成为直播主播
     * @param param 请求参数，包含token和微信ID
     * @return 申请结果
     */
    @Override
    public ResponseResult applyForHost(Map<String, Object> param) {
        // 从参数中获取token并解析用户信息
        String token = param.get("token").toString();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 若用户信息为空，则返回非法令牌错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 创建主播申请对象，并设置相关属性
        BaseWxLiveHostEntity applyMap = new BaseWxLiveHostEntity();
        applyMap.setUserId(userInfo.getUserId());
        applyMap.setShopId(Integer.valueOf(userInfo.getShopId()));
        applyMap.setOperation(1);
        applyMap.setWechatId(param.get("wxChatId").toString());
        applyMap.setState(4);
        applyMap.setHeadPortrait(userInfo.getUserAvatar());
        // 查询是否存在相同的微信ID申请，若存在，则返回重复申请错误信息
        QueryWrapper<BaseWxLiveHostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wechat_id",param.get("wxChatId"));
        Integer repetition = Math.toIntExact(this.baseMapper.selectCount(queryWrapper));
        if (repetition > 0) {
            return ResponseResult.fail(2, "此微信已经申请，请不要重复提交");
        }
        // 插入主播申请数据，若插入成功，则返回成功结果
        Integer integer = this.baseMapper.insert(applyMap);
        if (null != integer && integer == 1) {
            return ResponseResult.success(1, "申请成功", integer);
        }
        // 若插入失败，则返回失败结果
        return ResponseResult.fail(2, "申请失败");
    }

    /**
     * 更新微信ID对应的主播状态
     * @param param 请求参数，包含token、微信ID等信息
     * @return 更新结果
     */
    @Override
    public ResponseResult upWeChatId(Map<String, Object> param) {
        // 微信API基础URL
        String host = "https://api.weixin.qq.com";
        // 获取访问令牌
        String accessToken = AccessTokenUtil.queryAccessToken();
        // 删除主播角色接口路径
        String delpath = "/wxaapi/broadcast/role/deleterole?access_token=" + accessToken;
        // 查询主播角色接口路径
        String getpath = "https://api.weixin.qq.com/wxaapi/broadcast/role/getrolelist?access_token=ACCESSTOKEN&keyword=KEYWORD&role=ROLE";
        // 从参数中获取token和微信ID
        String token = param.get("token").toString();
        String wxChatId = param.get("wechatId").toString();
        // 解析token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 若用户信息为空，则返回非法令牌错误信息
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 查询当前店铺是否已设置主播
        QueryResultVO queryResult = null;
        QueryWrapper<BaseWxLiveHostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id",userInfo.getShopId());
        BaseWxLiveHostEntity wxLiveHosts = this.baseMapper.selectOne(queryWrapper);
        // 若未设置主播，则返回错误信息
        if (null == wxLiveHosts) {
            return ResponseResult.fail(2, "请先设置微信号");
        }
        // 更新主播微信ID和状态的对象
        BaseWxLiveHostEntity hostEntity = new BaseWxLiveHostEntity();
        hostEntity.setWechatId(wxChatId);
        hostEntity.setId(wxLiveHosts.getId());
        hostEntity.setState(4);
        hostEntity.setOperation(0);
        hostEntity.setHoststate(2);
        // 根据当前主播状态，进行不同的操作
        if (wxLiveHosts.getState() == 1 || wxLiveHosts.getState() == 6) {
            queryResult = new QueryResultVO();
            queryResult.setRole(2);
            queryResult.setUsername(wxChatId);
            // 替换查询接口中的占位符
            getpath = getpath.replace("ACCESSTOKEN", accessToken).replace("KEYWORD", wxChatId).replace("ROLE", "2");
            QuroleResultVO forObject = restTemplate.getForObject(getpath, QuroleResultVO.class);
            // 若查询结果不为空，则删除主播角色
            if (forObject.getList().size() != 0) {
                SetRoleReponseResultVO result = restTemplate.postForObject(host + delpath, queryResult, SetRoleReponseResultVO.class);
                // 若删除成功，则更新数据库中的主播状态
                if (result.getErrcode() == 0) {
                    Integer integer = this.baseMapper.updateById(hostEntity);
                    if (1 == integer) {
                        return ResponseResult.success(integer);
                    }
                }
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.OPERATION_FAILED.getMsg());
            }
            // 若查询结果为空，则直接更新数据库中的主播状态
            Integer integer = this.baseMapper.updateById(hostEntity);
            if (1 == integer) {
                return ResponseResult.success(integer);
            }
        }
        // 若当前主播状态不符合条件，则直接更新数据库中的主播状态
        Integer integer = this.baseMapper.updateById(hostEntity);
        if (1 == integer) {
            return ResponseResult.success(integer);
        }
        return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.OPERATION_FAILED.getMsg());
    }


    /**
     * 根据房间名查询直播房间信息。
     * @param param 查询参数，包含房间名。
     * @return 查询结果，包含符合条件的直播房间列表。
     */
    @Override
    public ResponseResult queryLiveRoomsByName(Map<String, String> param) {
        try {
            // 用于存储查询到的房间信息
            List<RoomInfoVO> room_info = new ArrayList<>();
            // 不断查询直到没有新的房间信息
            for (int i = 1; i > 0; i++) {
                // 获取微信访问令牌
                String accessToken = AccessTokenUtil.queryAccessToken();
                // 构建微信API的URL
                String host = "https://api.weixin.qq.com";
                String addpath = "/wxa/business/getliveinfo?access_token=" + accessToken;
                // 设置查询参数，指定查询起始位置和数量
                Map<String, Integer> map = new HashMap<>();
                map.put("start", i - 1);
                map.put("limit", 20);
                // 发起查询请求
                LiveStudioResultVO results = restTemplate.postForObject(host + addpath, map, LiveStudioResultVO.class);
                // 如果查询到房间信息，则添加到结果列表中
                if (results.getRoom_info().size() > 0) {
                    for (RoomInfoVO roomInfo : results.getRoom_info()) {
                        room_info.add(roomInfo);
                    }
                } else {
                    // 如果没有新的房间信息，则结束查询
                    break;
                }
            }
            // 如果参数中包含房间名，则过滤出房间名匹配的房间信息
            if (null != param && null != param.get("name")) {
                List<RoomInfoVO> boys = room_info.stream().filter(r -> r.getRoomid().equals(param.get("name"))).collect(Collectors.toList());
                return ResponseResult.success(boys);
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        // 如果查询过程中出现异常，则返回错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }


    /**
     * 分页查询直播房间信息。
     * @param param 查询参数，包含分页信息。
     * @return 查询结果，包含分页的直播房间列表。
     */
    @Override
    public ResponseResult queryLiveRooms(PageHelper param) {
        // 根据分页参数计算查询的起始位置
        param.setCurrentPage((param.getStart() - 1) * param.getLimit());
        // 获取微信访问令牌
        String accessToken = AccessTokenUtil.queryAccessToken();
        // 构建微信API的URL
        String host = "https://api.weixin.qq.com";
        String addpath = "/wxa/business/getliveinfo?access_token=" + accessToken;
        // 构建查询参数
        JSONObject json = new JSONObject();
        json.put("start", param.getCurrentPage());
        json.put("limit", param.getLimit());
        // 发起查询请求
        /**发送Https请求到微信*/
        String data = HttpsUtil.sendPost(host + addpath, json.toString());
        return ResponseResult.success(data);
    }

    /**
     * 停止直播。
     * @param param 操作参数，包含主播ID和操作状态等信息。
     * @return 操作结果，表示操作是否成功。
     */
    @Override
    public ResponseResult stopLive(Map<String, Object> param) {
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());
        if (null != userInfo) {
            // 更新主播状态为停止直播
            this.baseMapper.update(null, new LambdaUpdateWrapper<BaseWxLiveHostEntity>()
                    .eq(BaseWxLiveHostEntity::getId, param.get("id").toString())
                    .set(BaseWxLiveHostEntity::getOperation, CommonType.commonStatus.NO.getCode())
                    .set(BaseWxLiveHostEntity::getState, CommonType.liveGoodsVerify.APPROVED.getCode())
                    .set(BaseWxLiveHostEntity::getHoststate, param.get("hostState").toString()));
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // 如果用户信息获取失败，则返回错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 平台申请成为主播。
     * @param param 申请参数，包含用户ID、申请状态等信息。
     * @return 申请结果，表示申请是否成功。
     */
    @Override
    public ResponseResult platformApplyForHost(WxChatIdParamDTO param) {
        ResponseResult rr = new ResponseResult();
        String token = param.getToken();
        // 检查token是否存在
        boolean isExistToken = redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + token);
        if (isExistToken) {
            // 如果申请状态为3（表示审核状态），则更新数据库中的审核状态
            if (param.getState() == 3) {
                Integer integer = this.baseMapper.update(null, new LambdaUpdateWrapper<BaseWxLiveHostEntity>()
                        .eq(BaseWxLiveHostEntity::getId, param.getId())
                        .set(BaseWxLiveHostEntity::getOperation, CommonType.commonStatus.NO.getCode())
                        .set(BaseWxLiveHostEntity::getState, param.getState())
                        .set(BaseWxLiveHostEntity::getCause, param.getCause()));
                if (integer > 0) {
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                }
            }
            // 构建微信API的URL
            String host = "https://api.weixin.qq.com";
            String accessToken = AccessTokenUtil.queryAccessToken();
            String addpath = "/wxaapi/broadcast/role/addrole?access_token=" + accessToken;
            String delpath = "/wxaapi/broadcast/role/deleterole?access_token=" + accessToken;
            QueryResultDTO queryResult = null;
            // 根据操作类型，决定是添加还是删除主播角色
            if (param.getOperation() == 1) {
                queryResult = new QueryResultDTO();
                queryResult.setRole(2);
                queryResult.setUsername(param.getWxChatId());
                SetRoleReponseResultVO result = restTemplate.postForObject(host + addpath, queryResult, SetRoleReponseResultVO.class);
                // 根据微信API的返回结果，更新数据库中的状态和返回信息
                if (result.getErrcode() == 0) {
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setMessage("主播角色设置成功");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 1, "微信审核通过");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                        rr.setData(integer);
                    }
                } else if (result.getErrcode() == 40001) {
                    rr.setData(result.getMsg());
                    rr.setMessage("AccessToken过期或者失效请联系管理员");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                } else if (result.getErrcode() == 40002) {
                    rr.setData(result.getMsg());
                    rr.setMessage("此用户未实名，请先实名认证");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败，用户未实名");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                } else if (result.getErrcode() == 40003) {
                    rr.setData(result.getMsg());
                    rr.setMessage("添加角色达到上限");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败，添加角色上限联系管理员");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                } else if (result.getErrcode() == 40004) {
                    rr.setData(result.getMsg());
                    rr.setMessage("重复添加角色");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败，重复添加");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                } else if (result.getErrcode() == 20002) {
                    rr.setData(result.getMsg());
                    rr.setMessage("入参不正确");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                } else {
                    rr.setData(result.getMsg());
                    rr.setMessage("微信审核失败");
                    Integer integer = this.baseMapper.updateHostState(param.getId(), 3, "微信审核失败，未知原因，联系管理员");
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    }
                }
                // 删除主播角色的操作逻辑
            } else {
                queryResult = new QueryResultDTO();
                queryResult.setRole(2);
                queryResult.setUsername(param.getWxChatId());
                SetRoleReponseResultVO result = restTemplate.postForObject(host + delpath, queryResult, SetRoleReponseResultVO.class);
                // 根据删除结果，更新数据库中的状态和返回信息
                if (result.getErrcode() == 0) {
                    rr.setMessage("删除成功");
                    Integer integer = this.baseMapper.deleteById(param.getId());
                    if (1 == integer) {
                        rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                        rr.setData(integer);
                    }
                } else {
                    rr.setCode(CommonStatusEnum.SUCCESS.getCode());
                    rr.setMessage("删除失败");
                    rr.setData(result.getMsg());
                }
            }
        } else {
            rr.setCode(10086);
            rr.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return rr;
    }

    /**
     * 查询主播信息。
     * @param param 查询参数，包含用户ID等信息。
     * @return 查询结果，包含主播列表。
     */
    @Override
    public ResponseResult queryHosts(WxChatIdParamDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            try {
                Map data = ParseMapUtils.beanToMap(param);
                IPage<WxHostsVO> page = this.baseMapper.queryHosts(new Query<WxHostsVO>().getPage(data), param);
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
}
