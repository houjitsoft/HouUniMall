package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jfinal.weixin.sdk.encrypt.WXBizMsgCrypt;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.RedisConstants;
import com.houjiang.commons.dto.OpenComponentDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.PayAuthorizationAppletInformationEntity;
import com.houjiang.payment.mapper.PayAuthorizationAppletInformationMapper;
import com.houjiang.payment.service.PayAuthorizationAppletInformationService;
import com.houjiang.payment.vo.templateListEntityVO;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName PayAuthorizationAppletInformationServiceImpl
 * @Description  授权小程序信息表
 * @Author Amos
 * @Date 2023-07-20 09:39:51
 * @Version 1.0
 * @author Amos
 */
@Service
@RefreshScope
public class PayAuthorizationAppletInformationServiceImpl extends ServiceImpl<PayAuthorizationAppletInformationMapper, PayAuthorizationAppletInformationEntity>
        implements PayAuthorizationAppletInformationService {

    @Value("${firstLevel}")
    private String firstLevel;

    @Override
    public ResponseResult queryShopAppletConfigFeign(Map<String, Object> map) {
        Map<String,String> resultMap = this.baseMapper.queryShopAppletConfigFeign(map.get("shopId").toString());
        return ResponseResult.success(resultMap);
    }

    @Override
    public ResponseResult querysecret(Map<String, Object> map) {
        // 从map中获取token字符串，并转换为String类型
        String token = String.valueOf(map.get("token"));

        // 使用token尝试获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if(userInfo!=null){
            // 将用户信息中的店铺ID转换为Integer类型
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            // 初始化结果映射，用于存储查询到的支付授权小程序信息
            HashMap<String, Object> result = new HashMap<>();
            // 根据店铺ID查询支付授权小程序信息
            PayAuthorizationAppletInformationEntity querysecret = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getShopId,shopId));
            // 检查是否查询到支付授权小程序信息
            if (querysecret != null){
                // 将查询到的授权小程序ID和密钥添加到结果映射中
                result.put("authorizerAppid",querysecret.getAuthorizerAppid());
                result.put("secret",querysecret.getSecret());
                // 返回成功结果，包含授权小程序ID和密钥
                return ResponseResult.success(result);
            }
            // 如果未查询到信息，返回失败结果，表示未获取到信息
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        }
        // 如果用户信息无效，返回失败结果，表示token无效
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);


    }

    /**
     * 根据传入的参数释放授权小程序。
     * @param map 包含释放操作所需参数的映射，其中必须包含token信息。
     * @return 根据操作结果返回对应的响应结果。
     */
    @Override
    public ResponseResult release(Map<String, Object> map) {
        // 从参数映射中提取token
        String token = String.valueOf(map.get("token"));
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if(userInfo != null) {
            // 提取用户关联的店铺ID
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 根据店铺ID查询授权小程序信息
            // 根据shopId查询小程序信息
            PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
            // 提取授权小程序的appid
            // 授权小程序appid
            String authorizerAppid = entity.getAuthorizerAppid();
            // 尝试从Redis中获取授权小程序的access_token
            String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 如果access_token不存在，则更新access_token
            // 过期重新获取
            if (authorizerAccessToken == null) {
                updateauthorizeraccesstoken();
            }
            // 重新从Redis中获取授权小程序的access_token
            // 从Redis中获取授权方的access_token，用于后续的API调用
            authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 构建释放授权的API请求URL
            // 构造调用开放平台接口的URL，access_token作为查询参数
            String url = OpenComponentDTO.releaseUrl + "?access_token=" + authorizerAccessToken;
            // 创建空的JSONObject作为API请求的数据体
            // 创建一个空的JSONObject，用于发送POST请求的数据体
            JSONObject jsonObject = new JSONObject();
            // 发送POST请求释放授权，并获取响应数据
            // 发送POST请求，并获取响应数据
            String data = HttpsUtil.sendPost(url, jsonObject.toString());
            // 解析API的响应数据
            // 解析响应数据为JSONObject
            JSONObject getData = JSONObject.parseObject(data);
            // 根据API响应结果返回对应的处理结果
            // 判断接口调用是否成功，成功则返回成功结果，失败则返回失败结果
            if (getData.get("errcode").equals(0)) {
                return ResponseResult.success(getData);
            } else {
                return ResponseResult.fail((Integer) getData.get("errcode"), (String) getData.get("errmsg"));
            }
        }
        // 如果用户信息无效，返回token无效的错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    @Override
    public ResponseResult setPrivacySetting(Map<String, Object> map) {
        String token = String.valueOf(map.get("token"));
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if(userInfo!=null) {
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            //根据shopId查询小程序信息
            PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getShopId,shopId));
            //授权小程序appid
            String authorizerAppid = entity.getAuthorizerAppid();
            String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);

            //过期重新获取
            if (StringUtils.isEmpty(authorizerAccessToken) || "null".equals(authorizerAccessToken)) {
                updateauthorizeraccesstoken();
            }
            authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);

            System.out.println(authorizerAccessToken);

            String url = OpenComponentDTO.setPrivacySettingUrl + "?access_token=" + authorizerAccessToken;

            //要收集的用户信息配置
            ArrayList<Object> setting_list = new ArrayList<>();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("privacy_key", "Location");
            jsonObject1.put("privacy_text", "查询当前用户附近的门店信息");
            setting_list.add(jsonObject1);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("privacy_key", "PhoneNumber");
            jsonObject2.put("privacy_text", "登录小程序");
            setting_list.add(jsonObject2);

            //通知方式
            JSONObject owner_setting = new JSONObject();
            owner_setting.put("notice_method", "短信");
            owner_setting.put("contact_phone", "15236038307");

            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("sdk_name", "wxParser");
            jsonObject3.put("sdk_biz_name", "广州爱范儿科技股份有限公司");


            ArrayList<Object> sdk_list = new ArrayList<>();
            JSONObject jsonObject4 = new JSONObject();
            jsonObject4.put("privacy_key", "");
            jsonObject4.put("privacy_text", "");
            sdk_list.add(jsonObject4);

            jsonObject3.put("sdk_list", sdk_list);

            //参数封装
            JSONObject jsonObject = new JSONObject();
            //1 表示现网版  2  表示开发版
            jsonObject.put("privacy_ver", 2);
            jsonObject.put("setting_list", setting_list);
            jsonObject.put("owner_setting", owner_setting);

            String data = HttpsUtil.sendPost(url, jsonObject.toString());
            JSONObject getData = JSONObject.parseObject(data);
            if (getData.get("errcode").equals(0)){
                return ResponseResult.success(getData);
            }else{
                return ResponseResult.fail((Integer) getData.get("errcode"), (String) getData.get("errmsg"));
            }

        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    @Override
    public ResponseResult updatesecret(Map<String, Object> map) {
        // 从传入的map中获取token字符串
        String token = String.valueOf(map.get("token"));
        // 使用token尝试获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if(userInfo != null) {
            // 将用户信息中的店铺ID转换为整型
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 将店铺ID存入map中，准备后续更新操作
            map.put("shopId", shopId);
            // 更新用户信息中的密码（或其他需要更新的字段）
            Integer updatesecret = this.baseMapper.updatesecret(map);
            // 检查更新是否成功
            if (updatesecret > 0) {
                // 更新成功，返回相应的成功响应
                return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
            }
            // 更新失败，返回相应的失败响应
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
        // 用户信息无效，返回相应的失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    /**
     * 通过令牌获取最新的小程序审核状态。
     *
     * @param map 包含令牌等信息的参数映射。
     * @return 返回审核状态的结果。
     */
    @Override
    public ResponseResult getLatestAuditStatus(Map<String, Object> map) {
        // 从参数映射中提取令牌
        String token = String.valueOf(map.get("token"));
        // 根据令牌解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息存在
        if(userInfo != null) {
            // 提取店铺ID
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 根据店铺ID查询小程序授权信息
            // 根据shopId查询小程序信息
            PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                                                            .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
            // 获取小程序的授权APPID
            // 授权小程序appid
            String authorizerAppid = entity.getAuthorizerAppid();
            // 从Redis中获取小程序的授权访问令牌
            String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 如果令牌不存在，则更新令牌
            // 过期重新获取
            if (authorizerAccessToken == null) {
                updateauthorizeraccesstoken();
            }
            // 重新获取更新后的令牌
            authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 构建查询最新审核状态的URL
            String url = OpenComponentDTO.getLatestAuditStatusUrl + "?access_token=" + authorizerAccessToken;
            // 发起HTTP GET请求获取最新审核状态
            String data = HttpsUtil.sendGet(url, "");
            // 解析返回的数据
            JSONObject getData = JSONObject.parseObject(data);
            // 如果返回码为0，表示请求成功，返回审核状态数据
            if (getData.get("errcode").equals(0)) {
                return ResponseResult.success(getData);
            }
            // 如果请求失败，返回失败结果
            return ResponseResult.fail(getData);
        }
        // 如果用户信息不存在，返回令牌无效的错误结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 提交小程序审核链接
     * @param map 包含token等信息的Map对象
     * @return 返回操作结果，成功或失败的详细信息
     */
    @Override
    public ResponseResult
    submitAuditUrl(Map<String, Object> map) {
        // 从map中获取token
        String token = String.valueOf(map.get("token"));
        // 根据token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if(userInfo != null) {
            // 获取商家ID
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 根据商家ID查询小程序授权信息
            // 根据shopId查询小程序信息
            PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
            // 获取授权小程序的appid
            // 授权小程序appid
            String authorizerAppid = entity.getAuthorizerAppid();
            // 从Redis中获取授权小程序的access_token
            String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 如果access_token过期或不存在，则更新access_token
            // 过期重新获取
            if (authorizerAccessToken == null) {
                updateauthorizeraccesstoken();
            }
            authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 检查小程序接口隐藏信息是否正常
            // 获取接口隐藏信息检测结果
            String infoUrl = OpenComponentDTO.privacyUrl + "?access_token=" + authorizerAccessToken;
            String info = HttpsUtil.sendGet(infoUrl, "");
            JSONObject jsonObject2 = JSONObject.parseObject(info);
            // 如果接口调用出错，则返回错误信息
            if (!jsonObject2.get("errcode").equals(0)) {
                return ResponseResult.fail(jsonObject2);
            }
            // 准备提交审核的参数
            JSONObject jsonObject = new JSONObject();
            // 获取所有类目名称
            String getAllCategoryNameUrl = OpenComponentDTO.getAllCategoryNameUrl + "?access_token=" + authorizerAccessToken;
            String data = HttpsUtil.sendGet(getAllCategoryNameUrl, "");
            JSONObject getData = JSONObject.parseObject(data);
            // 如果获取类目名称成功，则添加到提交审核的参数中
            if (getData.get("errcode").equals(0)) {
                List<Map<String,Object>> list = (List<Map<String,Object>>) getData.get("category_list");
                System.out.println(getData.get("category_list"));
                if(null != list){
                    if( list.size() <= 5){
                        jsonObject.put("item_list",list);
                    }else{
                        List<Map<String, Object>> firstFive = list.stream()
                                .limit(5)
                                .collect(Collectors.toList());
                        jsonObject.put("item_list",firstFive);
                    }
                }
            } else {
                // 如果获取类目名称失败，则返回错误信息
                return ResponseResult.fail(0, "获取类目名称异常");
            }
            //购物交易类小程序需填写订单中心path
            jsonObject.put("order_path","/member/orderList/orderList");
            // 提交小程序审核
            String url = OpenComponentDTO.submitAuditUrl + "?access_token=" + authorizerAccessToken;
            String data1 = HttpsUtil.sendPost(url, jsonObject.toString());
            JSONObject fromObject = JSONObject.parseObject(data1);
            // 如果提交审核成功，则更新小程序的审核ID，并返回成功信息
            if (fromObject.get("errcode").equals(0)) {
                // 将审核编号入库
                this.baseMapper.update(null, new LambdaUpdateWrapper<PayAuthorizationAppletInformationEntity>()
                        .eq(PayAuthorizationAppletInformationEntity::getAuthorizerAppid, authorizerAppid)
                        .set(PayAuthorizationAppletInformationEntity::getAuditId, String.valueOf(fromObject.get("auditid"))));
                return ResponseResult.success(fromObject);
            }
            // 如果提交审核失败，则返回失败信息
            return ResponseResult.fail(fromObject);
        }
        // 如果token无效，则返回错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 上传媒体文件到代码审核平台。
     *
     * @param mediaFile 媒体文件，待上传的文件。
     * @param token 用户授权令牌，用于获取用户信息和授权访问令牌。
     * @return 返回上传结果，包含媒体文件ID。
     */
    @Override
    public ResponseResult uploadMediaToCodeAudit(MultipartFile mediaFile, String token) {
        // 根据token获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if(userInfo != null) {
            // 将用户信息中的店铺ID转换为整型。
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 根据店铺ID查询授权小程序信息。
            // 根据shopId查询小程序信息
             PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                     .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
            // 获取授权小程序的AppID。
            // 授权小程序appid
            String authorizerAppid = entity.getAuthorizerAppid();
            // 从Redis中获取授权小程序的访问令牌。
            String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
            // 如果访问令牌为空，则更新访问令牌。
            // 过期重新获取
            if (authorizerAccessToken == null){
                updateauthorizeraccesstoken();
            }
            authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);

            try {
                // 构建上传URL，并设置请求方法为POST。
                URL urlObj = new URL(OpenComponentDTO.uploadmediaUrl+"?access_token=" + authorizerAccessToken);
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("POST");
                // 允许输入输出流操作。
                conn.setDoInput(true);
                conn.setDoOutput(true);
                // 禁用缓存。
                conn.setUseCaches(false);
                // 设置连接属性。
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                // 设置请求体的Content-Type。
                String BOUNDARY = "----------" + System.currentTimeMillis();
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                // 构建请求体。
                StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition:form-data;name=\"media\";filename=\"" + mediaFile.getOriginalFilename() + "\";filelength=\"" + mediaFile.getSize() + "\"\r\n");
                sb.append("Content-Type:application/octet-stream\r\n\r\n");
                byte[] head = sb.toString().getBytes("utf-8");
                OutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(head);
                out.write(mediaFile.getBytes());
                byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");
                out.write(foot);
                out.flush();
                out.close();
                // 读取响应。
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = null;
                String result = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    result = buffer.toString();
                } catch (IOException var17) {
                    var17.printStackTrace();
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
                // 解析响应结果。
                JSONObject getData = JSONObject.parseObject(result);
                // 如果上传成功，根据媒体文件类型更新小程序信息，并返回媒体文件ID。
                if (getData.get("errcode").equals(0)){
                    System.out.println(getData.get("type"));
                    if (getData.get("type").equals("image")){
                        this.baseMapper.update(null,new LambdaUpdateWrapper<PayAuthorizationAppletInformationEntity>()
                                .eq(PayAuthorizationAppletInformationEntity::getAuthorizerAppid,authorizerAppid)
                                .set(PayAuthorizationAppletInformationEntity::getPicIdList,String.valueOf(getData.get("mediaid"))));
                    }else{
                        this.baseMapper.update(null,new LambdaUpdateWrapper<PayAuthorizationAppletInformationEntity>()
                                .eq(PayAuthorizationAppletInformationEntity::getAuthorizerAppid,authorizerAppid)
                                .set(PayAuthorizationAppletInformationEntity::getVideoIdList,String.valueOf(getData.get("mediaid"))));
                    }
                    return ResponseResult.success(getData.get("mediaid"));
                }
            } catch (IOException var19) {
                // 如果上传过程中发生IO异常，返回操作失败结果。
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
            // 如果未能成功获取用户信息，返回令牌无效结果。
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

/**
 * 根据用户token获取对应小程序的二维码。
 *
 * @param map 包含token等信息的参数映射。
 * @return 返回包含二维码图片数据的响应结果。
 */
@Override
public ResponseResult getqrcode(Map<String, Object> map) {
    // 从参数映射中获取token
    String token = String.valueOf(map.get("token"));
    // 初始化二维码的encoded字符串
    String encoded = null;
    // 通过token获取用户信息
    UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
    // 检查用户信息是否有效
    if(userInfo != null) {
        // 将用户信息中的店铺ID转换为整型
        Integer shopId = Integer.valueOf(userInfo.getShopId());
        // 根据店铺ID查询授权小程序的信息
        // 根据shopId查询小程序信息
        PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
        // 获取授权小程序的appid
        // 授权小程序appid
        String authorizerAppid = entity.getAuthorizerAppid();
        // 从Redis中获取授权小程序的访问令牌
        String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
        // 如果访问令牌不存在，则更新访问令牌
        // 过期重新获取
        if (authorizerAccessToken == null) {
            updateauthorizeraccesstoken();
        }
        // 重新获取更新后的访问令牌
        authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
        // 构造获取二维码的URL
        String url = OpenComponentDTO.getqrcodeUrl + "?access_token=" + authorizerAccessToken;
        // 构造二维码的路径参数
        String path = "store%2FstoreIndex%2FstoreIndex%3FshopId%3D" + shopId;
        // 组合完整的二维码URL
        URI u = URI.create(url + "&path=" + path);
        try (InputStream inputStream = u.toURL().openStream()) {
            // 读取二维码图片数据
            // copyInputStreamToFile(inputStream);
            byte[] bytes = new byte[0];
            bytes = IOUtils.toByteArray(inputStream);
            // 对图片数据进行Base64编码
            encoded = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回包含二维码图片数据的响应结果
        return ResponseResult.success("data:image/jpeg;base64," + encoded);
    }
    // 如果用户信息无效，返回令牌无效的响应结果
    return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
}


    /**
     * 提交支付授权信息
     * @param map 包含令牌和模板ID的映射
     * @return 根据操作结果返回的成功或失败的响应
     */
    @Override
    public ResponseResult commit(Map<String, Object> map) {
        // 从映射中提取令牌
        String token = String.valueOf(map.get("token"));
        // 尝试根据令牌解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息存在
        if(userInfo != null) {
            // 提取商店ID
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 根据商店ID查询支付授权小程序信息
            // 根据shopId查询小程序信息
            PayAuthorizationAppletInformationEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId));
            // 如果找到授权信息
            if(entity != null) {
                // 获取授权小程序的APPID
                // 授权小程序appid
                String authorizerAppid = entity.getAuthorizerAppid();
                // 尝试从Redis获取授权访问令牌
                String authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
                // 如果令牌不存在，则更新令牌
                // 过期重新获取
                if(authorizerAccessToken == null) {
                    updateauthorizeraccesstoken();
                    // 再次尝试获取令牌
                    authorizerAccessToken = RedisUtil.getNotExpire(authorizerAppid);
                }
                // 构建请求URL
                String url = OpenComponentDTO.commitUrl + "?access_token=" + authorizerAccessToken;
                // 准备请求参数
                Map<String, Object> jsonObject1 = new HashMap<>();
                jsonObject1.put("shopId", shopId);
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("ext", jsonObject1);
                jsonObject2.put("extAppid", authorizerAppid);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("template_id", map.get("templateId"));
                jsonObject.put("ext_json", jsonObject2.toString());
                // 发起POST请求
                String data = HttpsUtil.sendPost(url, jsonObject.toString());
                // 解析响应
                JSONObject getData = JSONObject.parseObject(data);
                // 如果操作成功
                if(getData.getInteger("errcode").equals(0)) {
                    // 返回成功的响应结果
                    return ResponseResult.success(getData);
                } else {
                    // 否则返回失败的响应结果
                    return ResponseResult.fail((Integer) getData.get("errcode"), (String) getData.get("errmsg"));
                }
            }
        }
        // 如果用户信息不存在或未找到授权信息，返回令牌无效的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 将信息添加到模板中。
     *
     * @param map 包含token等信息的映射表。
     * @return 添加操作的结果。
     */
    @Override
    public ResponseResult addToTemplate(Map<String, Object> map) {
        // 从映射表中获取token值
        String token = String.valueOf(map.get("token"));
        // 从Redis中获取用户信息，基于token
        String userInfoStr = RedisUtil.getNotExpire(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果用户信息不存在或为空，则返回失败结果
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 检查是否存在component_access_token
        boolean isExist = RedisUtil.exists("component_access_token");
        // 从Redis中获取component_access_token
        //第三方平台接口调用凭证
        String component_access_token = RedisUtil.getNotExpire("component_access_token");
        // 如果component_access_token不存在，则通过调用接口获取，并设置到Redis中
        if(!isExist){
            component_access_token = getComponentAccessToken(false);
            RedisUtil.set("component_access_token",component_access_token,7000);
        }
        // 构建请求URL，包含component_access_token
        String url = OpenComponentDTO.gettemplatelistUrl+"?access_token="+component_access_token;
        // 准备请求参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("template_type", 0);
        // 发起GET请求，获取模板列表数据
        String data = HttpsUtil.sendGet(url, jsonObject.toString());
        // 解析返回的数据
        JSONObject getData = JSONObject.parseObject(data);
        // 将返回的模板列表数据转换为指定类型的列表
        List<templateListEntityVO> templateList = JSON.parseObject( getData.get("template_list").toString(), new TypeReference<List<templateListEntityVO>>() {
        });
        // 找到模板列表中模板ID最大的模板
        Optional<templateListEntityVO> max = templateList.stream().
                max(Comparator.comparingLong(templateListEntityVO::getTemplateId));
        // 如果接口返回码为0，表示成功，返回包含最大模板ID的响应结果
        if (getData.get("errcode").equals(0)){
            return ResponseResult.success(max.get());
        }
        // 如果接口返回码不为0，表示失败，返回失败的响应结果
        return ResponseResult.fail();
    }

    @Override
    public ResponseResult getPreauthCode(Map<String, Object> map) {
        String token = String.valueOf(map.get("token"));
        String userInfoStr = RedisUtil.getNotExpire(HouConfig.HOU_USER_TOKEN_USER + token);
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        //判断缓存中是否存在
        boolean isExist = RedisUtil.exists("component_access_token");
        String componentAccessToken = RedisUtil.getNotExpire("component_access_token");
        /**token若不存在，返回重新获取*/
        if(!isExist){
            componentAccessToken = getComponentAccessToken(false);
        }
        /**拼装待发送的Json*/
        String url = OpenComponentDTO.pre_auth_code_url+"?component_access_token="+componentAccessToken;
        JSONObject json = new JSONObject();
        json.put("component_appid",OpenComponentDTO.appId);

        /**发送Https请求到微信*/
        String retStr = HttpsUtil.sendPost(url,json.toString());
        JSONObject resultJson = JSONObject.parseObject(retStr);
        if(resultJson == null ||
                (resultJson.getString("errcode") != null && resultJson.getString("errcode").equals("42001"))){
            componentAccessToken = getComponentAccessToken(true);
            /**拼装待发送的Json*/
            url = OpenComponentDTO.pre_auth_code_url+"?component_access_token="+componentAccessToken;

            /**发送Https请求到微信*/
            retStr = HttpsUtil.sendPost(url,json.toString());
            resultJson = JSONObject.parseObject(retStr);
        }
        String preauthcode = resultJson.getString("pre_auth_code");
        if (preauthcode == null){
            return ResponseResult.fail();
        }

        RedisUtil.set("pre_auth_code", preauthcode, 1800);

        //生成授权码   回调路径上线后改成页面路径
        String returnUrl = OpenComponentDTO.componentloginpageUrl + "?component_appid="+OpenComponentDTO.appId+
                "&pre_auth_code="+preauthcode+"&redirect_uri="+ firstLevel + "/payment/pay/authorizationAppletInformation/authcode?token="+token;

        return ResponseResult.success(returnUrl);
    }

    @Override
    public ResponseResult checkAuth(Map<String, Object> map) {
        // 从map中获取token字符串
        String token = String.valueOf(map.get("token"));
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if(userInfo != null) {
            // 将用户信息中的店铺ID转换为整型
            Integer shopId = Integer.valueOf(userInfo.getShopId());
            // 查询已授权的小程序数量
            // 根据shopId查询小程序
            int count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getShopId, shopId)));
            // 如果查询结果大于0，表示商家已授权小程序
            if (count > 0) {
                // 返回商家已授权小程序的结果
                return ResponseResult.success(BizCodeEnume.MERCHANT_HAS_AUTHORIZED_THE_MINI_PROGRAM);
            }
            // 返回商家未授权小程序的结果
            return ResponseResult.fail(BizCodeEnume.MERCHANT_IS_NOT_AUTHORIZED_FOR_THE_MINI_PROGRAM);
        }
        // 返回token无效的结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    @Override
    public String ticket(String timestamp, String nonce, String msgSignature, String postData) {

        System.out.println("++++++++++++++++++"+timestamp+"----"+nonce+"----"+msgSignature+"----"+postData);

        try {
            //这个类是微信官网提供的解密类,需要用到消息校验Token 消息加密Key和服务平台appid
            WXBizMsgCrypt pc = new WXBizMsgCrypt(OpenComponentDTO.COMPONENT_TOKEN,
                    OpenComponentDTO.aesKey, OpenComponentDTO.appId);
            String xml = pc.decryptMsg(msgSignature, timestamp, nonce, postData);
            // 将xml转为map
            Map<String, Object> result = null;
            try {
                result = Xml2MapUtil.xml2map(xml);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Map<String, Object> xml1 = (Map<String, Object>) result.get("xml");
            String componentVerifyTicket = String.valueOf(xml1.get("ComponentVerifyTicket"));

            // 存储平台授权票据,保存ticket
            OpenComponentDTO.component_verify_ticket = componentVerifyTicket;
            RedisUtil.set("componentVerifyTicket", componentVerifyTicket, 7000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @Override
    public void authcode(HttpServletResponse response, String authCode, String expiresIn, String token){
        String userInfoStr = RedisUtil.get(RedisConstants.USER_TOKEN_USER + token);
        if (null == userInfoStr || userInfoStr.equals("")) {
            //return "token无效";
        }
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        Integer shopId = Integer.valueOf(userInfo.getShopId());
        //清除缓存中的预授权码
        RedisUtil.remove("pre_auth_code");
        boolean isExist = RedisUtil.exists("component_access_token");
        String component_access_token = RedisUtil.getNotExpire("component_access_token");
        if(!isExist){
            component_access_token = getComponentAccessToken(false);
        }

        try {

            //设置请求参数
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("component_appid", OpenComponentDTO.appId);
            jsonObject.put("authorization_code", authCode);
            String api_query_auth = OpenComponentDTO.OPEN_URL_AUTHORIZERACCESSTOKEN+"?component_access_token="+component_access_token;
            String responseData = HttpsUtil.sendPost(api_query_auth,jsonObject.toString());
            JSONObject postData = JSONObject.parseObject(responseData);

            //得到授权信息
            JSONObject authorizationInfo = (JSONObject) postData.get("authorization_info");
            //得到授权方 APPID    （存入数据库）
            String authorizerAppid = (String) authorizationInfo.get("authorizer_appid");
            //得到接口调用令牌
            String authorizerAccessToken = (String) authorizationInfo.get("authorizer_access_token");
            //得到接口刷新令牌   （存入数据库）
            String authorizerRefreshToken = (String) authorizationInfo.get("authorizer_refresh_token");
            //接口调用令牌的有效期
            Integer accessTokenexpiresIn = (Integer) authorizationInfo.get("expires_in");

            //先数据库插入授权信息
            PayAuthorizationAppletInformationEntity infoEntity = new PayAuthorizationAppletInformationEntity();
            infoEntity.setAuthorizerAppid(authorizerAppid);
            infoEntity.setAuthorizerRefreshToken(authorizerRefreshToken);
            infoEntity.setShopId(shopId);

            //根据authorizerAppid查询小程序信息
            PayAuthorizationAppletInformationEntity authorizationInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>()
                    .eq(PayAuthorizationAppletInformationEntity::getAuthorizerAppid,authorizerAppid));
            Integer integer = null;
            if (authorizationInfoEntity == null){
                integer = this.baseMapper.insert(infoEntity);
            }else{
                integer = this.baseMapper.update(infoEntity,new LambdaUpdateWrapper<PayAuthorizationAppletInformationEntity>()
                        .eq(PayAuthorizationAppletInformationEntity::getAuthorizerAppid,authorizerAppid));
            }

            if (integer <= 0){
                //return "fail";
            }

            //将token存入redis
            RedisUtil.set(authorizerAppid,authorizerAccessToken,7000);

        } catch (Exception e) {

        }
        try {
            //扫码成功后跳转到 success.html页面 或者给予提示信息
            response.sendRedirect(firstLevel + "/bcenter/#/shop/appletmanagement");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateauthorizeraccesstoken(){
        List<PayAuthorizationAppletInformationEntity> updaterefreshtoken = new ArrayList<>();
        boolean isExist = RedisUtil.exists("component_access_token");
        //第三方平台接口调用凭证
        String component_access_token = RedisUtil.getNotExpire("component_access_token");
        if(!isExist){
            component_access_token = getComponentAccessToken(false);
        }
        //获取授权帐号调用令牌
        String url = OpenComponentDTO.authorizer_access_token_url+"?component_access_token=" + component_access_token;
        List<PayAuthorizationAppletInformationEntity> list = this.baseMapper.selectList(new LambdaQueryWrapper<PayAuthorizationAppletInformationEntity>());
        if (list.size() > 0 && list != null) {
            for (PayAuthorizationAppletInformationEntity authorizerInfo : list) {

                String data;
                try {
                    //设置请求参数
                    JSONObject jsonObject = new JSONObject();
                    if (authorizerInfo != null) {
                        //第三方平台appid
                        jsonObject.put("component_appid", OpenComponentDTO.appId);
                        //授权方appid
                        jsonObject.put("authorizer_appid", authorizerInfo.getAuthorizerAppid());
                        //授权方刷新token
                        jsonObject.put("authorizer_refresh_token", authorizerInfo.getAuthorizerRefreshToken());

                        data = HttpsUtil.sendPost(url, jsonObject.toString());
                        JSONObject postData = JSONObject.parseObject(data);



                        if (postData.get("authorizer_access_token") != null && postData.get("authorizer_refresh_token") != null) {
                            String authorizer_access_token = String.valueOf((postData.get("authorizer_access_token")));
                            String authorizer_refresh_token = String.valueOf((postData.get("authorizer_refresh_token")));

                            //将token存入redis
                            RedisUtil.set(authorizerInfo.getAuthorizerAppid(),authorizer_access_token,600);

                            authorizerInfo.setAuthorizerRefreshToken(authorizer_refresh_token);
                            updaterefreshtoken.add(authorizerInfo);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        for (PayAuthorizationAppletInformationEntity entity : updaterefreshtoken)
        {
            //批量更新刷新token
            this.baseMapper.updateById(entity);
        }

    }

    /**
     * 获取component_access_token
     * @return
     */
    public String getComponentAccessToken(boolean flag) {
        //判断缓存中是否存在
        boolean isExist = RedisUtil.exists("componentVerifyTicket");
        String componentAccessToken = null;
        /**ticket若不存在，返回错误信息*/
        if(!isExist || flag){
            /**错误信息*/
            String url = "https://api.weixin.qq.com/cgi-bin/stable_token";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("grant_type", "client_credential");
            jsonObject.put("appid", OpenComponentDTO.appId);
            jsonObject.put("secret", OpenComponentDTO.secret);
            String data = HttpsUtil.sendPost(url, jsonObject.toString());

            JSONObject resultJson = JSONObject.parseObject(data);
            componentAccessToken = resultJson.getString("access_token");
//            return "如果出现这条消息，说明ticket以及token的过期处理存在BUG";
            /**保存token，并设置有效时间*/
            RedisUtil.set("component_access_token", componentAccessToken, 7000);
            return componentAccessToken;
        }
        String componentVerifyTicket = RedisUtil.getNotExpire("componentVerifyTicket");
        /**拼装待发送的Json*/
        JSONObject json = new JSONObject();
        json.put("component_appid", OpenComponentDTO.appId);
        json.put("component_appsecret",OpenComponentDTO.secret);
        json.put("component_verify_ticket",componentVerifyTicket);

        /**发送Https请求到微信*/
        String retStr = HttpsUtil.sendPost(OpenComponentDTO.component_access_token_url,json.toString());
        System.out.println(retStr);

        JSONObject resultJson = JSONObject.parseObject(retStr);
        /**在返回结果中获取token*/
        componentAccessToken = resultJson.getString("component_access_token");
        /**保存token，并设置有效时间*/
        RedisUtil.set("component_access_token", componentAccessToken, 7000);
        return componentAccessToken;
    }

}
