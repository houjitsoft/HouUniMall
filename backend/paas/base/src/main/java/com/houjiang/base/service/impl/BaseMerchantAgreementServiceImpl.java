package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.GdbMerchantAgreementDTO;
import com.houjiang.base.entity.BaseMerchantAgreementEntity;
import com.houjiang.base.feign.ThirdPartyServiceClient;
import com.houjiang.base.mapper.BaseMerchantAgreementMapper;
import com.houjiang.base.service.BaseMerchantAgreementService;
import com.houjiang.base.vo.GdbMerchantAgreementVO;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseMerchantAgreementServiceImpl
 * @Description 入驻协议
 * @Author Amos
 * @Date 2023/6/21 14:04
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseMerchantAgreementServiceImpl extends ServiceImpl<BaseMerchantAgreementMapper, BaseMerchantAgreementEntity> implements BaseMerchantAgreementService {

    @Autowired
    private BaseMerchantAgreementMapper baseMerchantAgreementMapper;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    /**
     * 根据条件查询所有协议
     * @param gdbMerchantAgreement 查询条件
     * @return 查询结果
     */
    @Override
    public ResponseResult selectAllAgreement(GdbMerchantAgreementDTO gdbMerchantAgreement) {
        GdbMerchantAgreementVO result = baseMerchantAgreementMapper.selectAgreement(gdbMerchantAgreement);
        return ResponseResult.success(result);
    }

    /**
     * 更新协议信息
     * @param file 需要上传的文件
     * @param sign 协议类型标识
     * @return 更新结果
     */
    @Override
    public ResponseResult updateAgreement(MultipartFile file, String sign) {
        //上传协议到oss
        ResponseResult responseResult = thirdPartyServiceClient.uploadToOssFile(file);
        // 验证 URL 是否为有效的本地文件路径
        String url = responseResult.getData().toString();
        if(null!=url){
            File pdfFile = new File(url);
            try {
                // 获取文件大小（字节）
                long fileSizeInBytes = pdfFile.length();
                // 将字节数转换为KB
                double fileSizeInKB = fileSizeInBytes / 1024.0;
                // 检查文件大小是否超过限制
                if(fileSizeInKB > 2048){
                    return ResponseResult.fail(BizCodeEnume.FILE_SIZE_GT_TWO);
                }
                // 查询现有协议
                //查询协议是否存在
                BaseMerchantAgreementEntity baseMerchantAgreementEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseMerchantAgreementEntity>()
                        .eq(BaseMerchantAgreementEntity::getSign,sign));

                // 如果协议不存在，则新增协议
                //新增
                if (null == baseMerchantAgreementEntity) {
                    baseMerchantAgreementEntity = new BaseMerchantAgreementEntity();
                    baseMerchantAgreementEntity.setUrl(url);
                    // 根据sign设置协议名称和类型
                    //企业
                    if (CommonType.agreement.ME_AGREEMENT.getType().equals(sign)) {
                        baseMerchantAgreementEntity.setName(CommonType.agreement.ME_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.ME_AGREEMENT.getType());
                    } else if (CommonType.agreement.US_AGREEMENT.getType().equals(sign)) {
                        //个人
                        baseMerchantAgreementEntity.setName(CommonType.agreement.US_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.US_AGREEMENT.getType());

                    } else if (CommonType.agreement.GY_ME_AGREEMENT.getType().equals(sign)) {
                        //供应商商家
                        baseMerchantAgreementEntity.setName(CommonType.agreement.GY_ME_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.GY_ME_AGREEMENT.getType());
                    } else if (CommonType.agreement.GY_US_AGREEMENT.getType().equals(sign)) {
                        //供应商用户
                        baseMerchantAgreementEntity.setName(CommonType.agreement.GY_US_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.GY_US_AGREEMENT.getType());
                    } else if (CommonType.agreement.GEN_COR_AGREEMENT.getType().equals(sign)) {
                        //通用
                        baseMerchantAgreementEntity.setName(CommonType.agreement.GEN_COR_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.GEN_COR_AGREEMENT.getType());
                    }else if(CommonType.agreement.BR_AGREEMENT.getType().equals(sign)) {
                        //通用
                        baseMerchantAgreementEntity.setName(CommonType.agreement.BR_AGREEMENT.getDesc());
                        baseMerchantAgreementEntity.setSign(CommonType.agreement.BR_AGREEMENT.getType());
                    }
                    this.baseMapper.addAgreement(baseMerchantAgreementEntity);
                } else {
                    // 如果协议已存在，则更新协议URL
                    //存入新url
                    baseMerchantAgreementEntity.setUrl(url);
                    //修改
                    this.baseMapper.addAgreement(baseMerchantAgreementEntity);
                }
                return ResponseResult.success(url);

            } catch (SecurityException e) {
                // 处理安全异常
                System.err.println("Failed to get file size due to security restrictions.");
            }
        }
        return ResponseResult.success(url);
 }

    /**
     * 根据条件查询协议
     * @param map 查询条件
     * @return 查询结果
     */
    @Override
    public ResponseResult queryAgreement(Map<String, Object> map) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            List<BaseMerchantAgreementEntity> baseMerchantAgreementEntities = this.baseMapper.selectList(
                    new LambdaQueryWrapper<BaseMerchantAgreementEntity>()
                            .eq(map.get("sign") != null, BaseMerchantAgreementEntity::getSign, map.get("sign")));
            return ResponseResult.success(baseMerchantAgreementEntities);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

}
