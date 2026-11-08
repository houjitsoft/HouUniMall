package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.base.entity.BaseShareUrlEntity;
import com.houjiang.base.mapper.BaseShareUrlMapper;
import com.houjiang.base.service.BaseShareUrlService;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.secverifyapiutil.QRCodeUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseShareUrlServiceImpl extends ServiceImpl<BaseShareUrlMapper, BaseShareUrlEntity>
    implements BaseShareUrlService{

    @Value("${domainName}")
    private String domainName;

    private static final Logger LOG = LoggerFactory.getLogger(BaseShareUrlServiceImpl.class);
    /**
     * 通用二维码生成方法
     * 通过传入的参数生成带有分享链接的二维码。
     *
     * @param param 包含token、shareType等信息的参数映射。
     * @return 返回生成二维码的相关信息，包括二维码链接和base64编码的字符串。
     * @throws NotFoundException 如果分享链接不存在，则抛出NotFoundException。
     * @throws IOException 如果处理图片文件时发生错误，则抛出IOException。
     * @throws WriterException 如果生成二维码时发生错误，则抛出WriterException。
     */
    @Override
    public ResponseResult commonCreatQrCode(Map<String, Object> param) throws NotFoundException, IOException, WriterException {
        // 通过token获取用户信息
        Object tokenObj = param.get("token");
        if (tokenObj == null) {
            return ResponseResult.error("token参数不能为空");
        }
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(tokenObj.toString());

        // 根据分享类型查询唯一的分享链接
        //1、查询分享地址
        Object shareTypeObj = param.get("shareType");
        if (shareTypeObj == null) {
            return ResponseResult.error("shareType参数不能为空");
        }
        String shareUrl = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseShareUrlEntity>()
                              .eq(BaseShareUrlEntity::getType, shareTypeObj.toString())).getShareUrl();

        // 如果分享链接不存在，则返回失败结果
        if (StringUtils.isEmpty(shareUrl)) {
            return ResponseResult.fail(0, "分享地址未配置~");
        }

        // 构造带参数的完整分享链接
        shareUrl = domainName + shareUrl + "?shareUserId=" + userInfo.getUserId() + "&platForm=0" + "&shareType=" + shareTypeObj.toString() + "&relevanceId=" + userInfo.getShopId();

        // 将分享链接转换为base64编码的字符串
        String baseString = QRCodeUtil.text2Base64Str(shareUrl);

        // 准备返回的数据映射，包含分享链接和base64编码字符串
        Map<String, String> returnMap = new HashMap<>(8);
        returnMap.put("url", shareUrl);
        returnMap.put("baseString", baseString);

        // 记录日志
        LOG.info("分享路径：" + shareUrl);

        // 返回成功结果，包含分享链接和base64编码字符串
        return ResponseResult.success(returnMap);
    }
}




