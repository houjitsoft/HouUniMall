package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.dto.member.AuthPerInfoDTO;
import com.houjiang.payment.dto.member.BankInfoDTO;
import com.houjiang.payment.dto.member.BindPhoneAppleRequest;
import com.houjiang.payment.dto.member.BindPhoneAppleResponse;
import com.houjiang.payment.dto.member.BindPhoneConfirmRequest;
import com.houjiang.payment.dto.member.BindPhoneConfirmResponse;
import com.houjiang.payment.dto.member.BindPhoneNotifyDTO;
import com.houjiang.payment.dto.member.EnterpriseInfoDTO;
import com.houjiang.payment.dto.member.EnterpriseOcrInfoDTO;
import com.houjiang.payment.dto.member.LegalPersonCardFaceInfoDTO;
import com.houjiang.payment.dto.member.LegalPersonCardNationalEmblemInfoDTO;
import com.houjiang.payment.dto.member.PersonInfoDTO;
import com.houjiang.payment.dto.member.PersonBankInfoDTO;
import com.houjiang.payment.dto.member.RegEnterpriseNotifyDTO;
import com.houjiang.payment.dto.member.RegEnterpriseRequest;
import com.houjiang.payment.dto.member.RegEnterpriseResponse;
import com.houjiang.payment.dto.member.RegPersonRequest;
import com.houjiang.payment.dto.member.RegPersonResponse;
import com.houjiang.payment.dto.member.RegSupplementNotifyDTO;
import com.houjiang.payment.dto.member.RegSupplementRequest;
import com.houjiang.payment.dto.member.RegSupplementResponse;
import com.houjiang.payment.dto.member.ApplyMemProtocolRequest;
import com.houjiang.payment.dto.member.ApplyMemProtocolResponse;
import com.houjiang.payment.dto.member.DivisionMemInfoDTO;
import com.houjiang.payment.dto.member.CouponInfoDTO;
import com.houjiang.payment.dto.member.ApplyMemProtocolNotifyDTO;
import com.houjiang.payment.dto.member.GenerateRegInviteLinkRequest;
import com.houjiang.payment.dto.member.GenerateRegInviteLinkResponse;
import com.houjiang.payment.dto.member.MemberRegSuccessNotifyDTO;
import com.houjiang.payment.dto.member.RegBindSybCusIdRequest;
import com.houjiang.payment.dto.member.RegBindSybCusIdResponse;
import com.houjiang.payment.entity.PayMemberEntity;
import com.houjiang.payment.entity.PayMemberNotifyEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayMemberMapper;
import com.houjiang.payment.mapper.PayMemberNotifyMapper;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.MemberService;
import com.houjiang.payment.util.TimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    private final PayMemberMapper payMemberMapper;
    private final PayMemberNotifyMapper payMemberNotifyMapper;

    /** 开户状态：处理中（已提交渠道，等审核） */
    private static final int MEMBER_STATUS_PROCESSING = 0;

    /** 开户状态：成功（渠道审核通过） */
    private static final int MEMBER_STATUS_SUCCESS = 1;

    /** 开户状态：失败（渠道审核驳回） */
    private static final int MEMBER_STATUS_FAILED = 2;

    /** 绑手机状态：未绑定 */
    private static final int BIND_PHONE_NONE = 0;

    /** 绑手机状态：已提交申请 */
    private static final int BIND_PHONE_APPLYING = 1;

    /** 绑手机状态：已绑定 */
    private static final int BIND_PHONE_BOUND = 2;

    /** 绑手机状态：绑定失败 */
    private static final int BIND_PHONE_FAILED = 3;

    /** 会员类型：企业 */
    private static final String MEMBER_TYPE_ENTERPRISE = "ENTERPRISE";

    /** 会员类型：个人 */
    private static final String MEMBER_TYPE_PERSON = "PERSON";

    /** 通知类型：企业开户结果 */
    private static final String NOTIFY_REG_ENTERPRISE = "REG_ENTERPRISE";

    @Override
    public RegEnterpriseResponse regEnterprise(RegEnterpriseRequest request) {
        log.info("开始企业会员实名开户, bizUserId:{}, memCategory:{}, chanNo:{}", 
                request.getBizUserId(), request.getMemCategory(), request.getChanNo());
        
        try {
            // 1. 参数校验
            validateRegEnterprise(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 解析并校验企业信息和银行卡信息
            EnterpriseInfoDTO enterpriseInfo = parseEnterpriseInfo(request.getEnterpriseInfo());
            BankInfoDTO bankInfo = parseBankInfo(request.getBankInfo());
            validateEnterpriseInfo(enterpriseInfo);
            validateBankInfo(bankInfo, enterpriseInfo);
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/regEnterprise";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("企业会员实名开户请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            RegEnterpriseResponse response = JSONUtil.toBean(jsonResponse, RegEnterpriseResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("企业会员开户响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("企业会员开户失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("企业会员开户失败: " + response.getMsg());
            }
            
            // 8. 保存会员记录到数据库：渠道侧已建档，本地必须留痕，
            //    否则审核回调回来时找不到档案，状态永远推不动
            upsertMember(request.getBizUserId(), MEMBER_TYPE_ENTERPRISE,
                    request.getMemCategory(), request.getChanNo());

            log.info("企业会员实名开户成功, bizUserId:{}", response.getBizUserId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("企业会员实名开户异常", e);
            throw PaymentException.systemError("企业会员实名开户失败: " + e.getMessage());
        }
    }
    
    @Override
    public RegPersonResponse regPerson(RegPersonRequest request) {
        log.info("开始个人会员实名及绑卡, bizUserId:{}, chanNo:{}", 
                request.getBizUserId(), request.getChanNo());

        try {
            // 1. 参数校验
            validateRegPerson(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 解析并校验个人信息和银行卡信息
            PersonInfoDTO personInfo = parsePersonInfo(request.getMemInfo());
            PersonBankInfoDTO bankInfo = parsePersonBankInfo(request.getBankInfo());
            validatePersonInfo(personInfo);
            validatePersonBankInfo(bankInfo);

            // 4. 生成签名
            signService.signRequest(request);

            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/regPerson";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("个人会员实名及绑卡请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            RegPersonResponse response = JSONUtil.toBean(jsonResponse, RegPersonResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("个人会员开户响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("个人会员开户失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("个人会员开户失败: " + response.getMsg());
            }

            // 7. 保存个人会员到数据库（个人开户同时提交绑卡，绑卡结果由确认流程推进）
            upsertMember(request.getBizUserId(), MEMBER_TYPE_PERSON, null, request.getChanNo());

            log.info("个人会员实名及绑卡处理完成, bizUserId:{}, code:{}, msg:{}", 
                    response.getBizUserId(), response.getCode(), response.getMsg());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("个人会员实名及绑卡异常", e);
            throw PaymentException.systemError("个人会员实名及绑卡失败: " + e.getMessage());
        }
    }
    
    @Override
    public String handleRegEnterpriseNotify(HttpServletRequest httpRequest) {
        log.info("收到企业会员开户结果通知");
        
        try {
            // 1. 解析通知参数
            Map<String, String> params = parseNotifyParams(httpRequest);
            RegEnterpriseNotifyDTO notifyDTO = parseRegEnterpriseNotifyParams(params);
            
            log.info("企业会员开户结果通知: bizUserId={}, auditResult={}, auditTime={}", 
                    notifyDTO.getBizUserId(), notifyDTO.getAuditResult(), notifyDTO.getAuditTime());
            
            // 2. 验签
            if (!signService.verifyNotify(params)) {
                log.error("企业会员开户结果通知验签失败");
                return "fail";
            }
            
            // 3. 幂等性检查
            if (isDuplicateRegEnterpriseNotify(notifyDTO.getBizUserId(), notifyDTO.getAuditTime())) {
                log.info("企业会员开户结果通知重复, bizUserId={}, auditTime={}", 
                        notifyDTO.getBizUserId(), notifyDTO.getAuditTime());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }
            
            // 4. 记录通知流水抢位：唯一键插不进去说明这条通知已经处理过（含并发重复），
            //    这一道就是幂等闸门，抢到位才继续更新状态
            if (!recordNotify(notifyDTO.getBizUserId(), NOTIFY_REG_ENTERPRISE, notifyDTO.getAuditTime(),
                    notifyDTO.getAuditResult(), String.valueOf(params))) {
                log.info("企业会员开户结果通知重复(并发), 跳过状态更新 bizUserId={}, auditTime={}",
                        notifyDTO.getBizUserId(), notifyDTO.getAuditTime());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }

            // 5. 更新会员状态到数据库：按渠道审核结果推进，档案不存在时补建一条
            try {
                updateMemberStatus(notifyDTO.getBizUserId(), notifyDTO.getAuditResult());
            } catch (Exception e) {
                // 状态没推进成功就撤掉幂等占位：否则渠道重发时被判成重复通知，状态永远修不回来
                payMemberNotifyMapper.delete(new LambdaQueryWrapper<PayMemberNotifyEntity>()
                        .eq(PayMemberNotifyEntity::getBizUserId, notifyDTO.getBizUserId())
                        .eq(PayMemberNotifyEntity::getNotifyType, NOTIFY_REG_ENTERPRISE)
                        .eq(PayMemberNotifyEntity::getNotifyKey, notifyDTO.getAuditTime()));
                throw e;
            }

            // 6. 业务事件留痕：审核结果落日志，供上游订阅或对账；不在此处外发，避免通知放大
            if ("2".equals(notifyDTO.getAuditResult())) {
                log.info("[会员开户] 审核通过 bizUserId={}, auditTime={}",
                        notifyDTO.getBizUserId(), notifyDTO.getAuditTime());
            } else if ("3".equals(notifyDTO.getAuditResult())) {
                log.warn("[会员开户] 审核驳回 bizUserId={}, remark={}",
                        notifyDTO.getBizUserId(), notifyDTO.getAuditRemark());
            }

            log.info("企业会员开户结果通知处理成功, bizUserId={}", notifyDTO.getBizUserId());
            
            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";
            
        } catch (Exception e) {
            log.error("企业会员开户结果通知处理异常", e);
            return "fail";
        }
    }
    
    @Override
    public RegSupplementResponse regSupplement(RegSupplementRequest request) {
        log.info("开始会员资料补录, bizUserId:{}, chanNo:{}", 
                request.getBizUserId(), request.getChanNo());
        
        try {
            // 1. 参数校验
            validateRegSupplement(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/regSupplement";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员资料补录请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            RegSupplementResponse response = JSONUtil.toBean(jsonResponse, RegSupplementResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("资料补录响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("资料补录失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("资料补录失败: " + response.getMsg());
            }
            
            // 7. 更新会员资料到数据库：补录的文件 ID 留档，便于后续审计追溯
            Map<String, String> ext = new HashMap<>();
            ext.put("uniCreditFileId", request.getUniCreditFileId());
            ext.put("legalIdZFileId", request.getLegalIdZFileId());
            ext.put("legalIdFFileId", request.getLegalIdFFileId());
            updateMemberExt(request.getBizUserId(), ext);

            log.info("会员资料补录成功, bizUserId:{}", response.getBizUserId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员资料补录异常", e);
            throw PaymentException.systemError("会员资料补录失败: " + e.getMessage());
        }
    }
    
    @Override
    public BindPhoneAppleResponse bindPhoneApple(BindPhoneAppleRequest request) {
        log.info("开始会员绑定手机号申请, bizUserId:{}, phone:{}, phoneType:{}", 
                request.getBizUserId(), request.getPhone(), request.getPhoneType());
        
        try {
            // 1. 参数校验
            validateBindPhoneApple(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 如果是企业授权手机号,解析并校验被授权人信息
            AuthPerInfoDTO authPerInfo = null;
            if ("2".equals(request.getPhoneType())) {
                authPerInfo = parseAuthPerInfo(request.getAuthPerInfo());
                validateAuthPerInfo(authPerInfo);
            }
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/bindPhoneApple";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员绑定手机号申请请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            BindPhoneAppleResponse response = JSONUtil.toBean(jsonResponse, BindPhoneAppleResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("绑定手机号申请响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("绑定手机号申请失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("绑定手机号申请失败: " + response.getMsg());
            }
            
            // 8. 记录绑手机申请：状态置为「申请中」，由确认接口推进到「已绑定」
            markBindPhone(request.getBizUserId(), request.getPhone(), BIND_PHONE_APPLYING);

            log.info("会员绑定手机号申请成功, bizUserId:{}, applyRespTraceNum:{}", 
                    response.getBizUserId(), response.getApplyRespTraceNum());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员绑定手机号申请异常", e);
            throw PaymentException.systemError("会员绑定手机号申请失败: " + e.getMessage());
        }
    }
    
    @Override
    public BindPhoneConfirmResponse bindPhoneConfirm(BindPhoneConfirmRequest request) {
        log.info("开始会员绑定手机号确认, bizUserId:{}, phone:{}, applyRespTraceNum:{}",
                request.getBizUserId(), request.getPhone(), request.getApplyRespTraceNum());

        try {
            // 1. 参数校验
            validateBindPhoneConfirm(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 测试环境验证码模拟规则：11111
            if (!"11111".equals(request.getVerifyCode())) {
                throw PaymentException.bizError("短信验证码错误(测试环境固定为11111)");
            }

            // 4. 生成签名
            signService.signRequest(request);

            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/bindPhoneConfirm";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员绑定手机号确认请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            BindPhoneConfirmResponse response = JSONUtil.toBean(jsonResponse, BindPhoneConfirmResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("绑定手机号确认响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("绑定手机号确认失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("绑定手机号确认失败: " + response.getMsg());
            }

            // 7. 更新绑定手机号结果到数据库
            markBindPhone(request.getBizUserId(), request.getPhone(), BIND_PHONE_BOUND);

            log.info("会员绑定手机号确认成功, bizUserId:{}, phone:{}",
                    response.getBizUserId(), response.getPhone());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员绑定手机号确认异常", e);
            throw PaymentException.systemError("会员绑定手机号确认失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApplyMemProtocolResponse applyMemProtocol(ApplyMemProtocolRequest request) {
        log.info("开始会员协议签约申请, bizUserId:{}, protocolType:{}, chanNo:{}", 
                request.getBizUserId(), request.getProtocolType(), request.getChanNo());

        try {
            // 1. 参数校验
            validateApplyMemProtocol(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 根据协议类型解析并校验协议信息
            DivisionMemInfoDTO divisionMemInfo = null;
            CouponInfoDTO couponInfo = null;
            if ("1".equals(request.getProtocolType())) {
                divisionMemInfo = parseDivisionMemInfo(request.getDivisionMemInfo());
                validateDivisionMemInfo(divisionMemInfo);
            } else if ("2".equals(request.getProtocolType())) {
                couponInfo = parseCouponInfo(request.getCouponInfo());
                validateCouponInfo(couponInfo);
            }

            // 4. 生成签名
            signService.signRequest(request);

            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/applyMemProtocol";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员协议签约申请请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            ApplyMemProtocolResponse response = JSONUtil.toBean(jsonResponse, ApplyMemProtocolResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("协议签约申请响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("协议签约申请失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("协议签约申请失败: " + response.getMsg());
            }

            // TODO: 8. 保存协议签约申请记录到数据库

            log.info("会员协议签约申请处理完成, bizUserId:{}, protocolType:{}, code:{}, msg:{}", 
                    request.getBizUserId(), request.getProtocolType(), response.getCode(), response.getMsg());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员协议签约申请异常", e);
            throw PaymentException.systemError("会员协议签约申请失败: " + e.getMessage());
        }
    }
    
    @Override
    public String handleRegSupplementNotify(HttpServletRequest httpRequest) {
        log.info("收到会员资料补录结果通知");
        
        try {
            // 1. 解析通知参数
            Map<String, String> params = parseNotifyParams(httpRequest);
            RegSupplementNotifyDTO notifyDTO = parseRegSupplementNotifyParams(params);
            
            log.info("会员资料补录结果通知: bizUserId={}, enterpriseCompareResult={}, legalPersonCompareResult={}", 
                    notifyDTO.getBizUserId(), notifyDTO.getEnterpriseCompareResult(), notifyDTO.getLegalPersonCompareResult());
            
            // 2. 验签
            if (!signService.verifyNotify(params)) {
                log.error("会员资料补录结果通知验签失败");
                return "fail";
            }
            
            // 3. 幂等性检查
            if (isDuplicateRegSupplementNotify(notifyDTO.getBizUserId())) {
                log.info("会员资料补录结果通知重复, bizUserId={}", notifyDTO.getBizUserId());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }
            
            // 4. 解析OCR识别结果
            EnterpriseOcrInfoDTO enterpriseOcrInfo = null;
            LegalPersonCardFaceInfoDTO cardFaceInfo = null;
            LegalPersonCardNationalEmblemInfoDTO cardEmblemInfo = null;
            
            if (StrUtil.isNotBlank(notifyDTO.getEnterpriseInfo())) {
                enterpriseOcrInfo = parseEnterpriseOcrInfo(notifyDTO.getEnterpriseInfo());
                log.info("企业影印件识别: creditCode={}, corpName={}", 
                        enterpriseOcrInfo.getCreditCode(), enterpriseOcrInfo.getCorpName());
            }
            
            if (StrUtil.isNotBlank(notifyDTO.getLegalPersonCardFaceInfo())) {
                cardFaceInfo = parseLegalPersonCardFaceInfo(notifyDTO.getLegalPersonCardFaceInfo());
                log.info("法人证件肖像面识别: name={}, idCard={}", 
                        cardFaceInfo.getName(), cardFaceInfo.getIdCard());
            }
            
            if (StrUtil.isNotBlank(notifyDTO.getLegalPersonCardNationalEmblemInfo())) {
                cardEmblemInfo = parseLegalPersonCardNationalEmblemInfo(notifyDTO.getLegalPersonCardNationalEmblemInfo());
                log.info("法人证件国徽面识别: validDate={}, isValid={}", 
                        cardEmblemInfo.getValidDate(), cardEmblemInfo.getIsValid());
            }
            
            // TODO: 5. 更新OCR识别结果到数据库
            // EnterpriseMember member = enterpriseMemberMapper.selectByBizUserId(notifyDTO.getBizUserId());
            // if (member != null) {
            //     member.setEnterpriseCompareResult(notifyDTO.getEnterpriseCompareResult());
            //     member.setLegalPersonCompareResult(notifyDTO.getLegalPersonCompareResult());
            //     member.setCompareResults(notifyDTO.getCompareResults());
            //     
            //     if (enterpriseOcrInfo != null && "000000".equals(enterpriseOcrInfo.getRespCode())) {
            //         member.setOcrCreditCode(enterpriseOcrInfo.getCreditCode());
            //         member.setOcrCorpName(enterpriseOcrInfo.getCorpName());
            //     }
            //     
            //     if (cardFaceInfo != null && "000000".equals(cardFaceInfo.getRespCode())) {
            //         member.setOcrLegalName(cardFaceInfo.getName());
            //         member.setOcrLegalIdCard(cardFaceInfo.getIdCard());
            //     }
            //     
            //     enterpriseMemberMapper.updateById(member);
            // }
            
            // TODO: 6. 记录通知到Redis,用于幂等性检查
            // String notifyKey = "member:supplement:notify:" + notifyDTO.getBizUserId();
            // redisTemplate.opsForValue().set(notifyKey, "1", 7, TimeUnit.DAYS);
            
            // TODO: 7. 根据比对结果发送业务通知
            // if ("1".equals(notifyDTO.getEnterpriseCompareResult()) 
            //         && "1".equals(notifyDTO.getLegalPersonCompareResult())) {
            //     // 比对成功,发送通知
            // } else {
            //     // 比对失败,发送警告
            // }
            
            log.info("会员资料补录结果通知处理成功, bizUserId={}", notifyDTO.getBizUserId());
            
            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";
            
        } catch (Exception e) {
            log.error("会员资料补录结果通知处理异常", e);
            return "fail";
        }
    }
    
    @Override
    public String handleBindPhoneNotify(HttpServletRequest httpRequest) {
        log.info("收到会员绑定手机号结果通知");

        try {
            // 1. 解析通知参数(获取全量参数,确保字段兼容)
            Map<String, String> params = parseNotifyParams(httpRequest);
            BindPhoneNotifyDTO notifyDTO = parseBindPhoneNotifyParams(params);

            log.info("会员绑定手机号结果通知: bizUserId={}, bindResult={}, phone={}, phoneType={}",
                    notifyDTO.getBizUserId(), notifyDTO.getBindResult(), notifyDTO.getPhone(), notifyDTO.getPhoneType());

            // 2. 验签
            if (!signService.verifyNotify(params)) {
                log.error("会员绑定手机号结果通知验签失败");
                return "fail";
            }

            // 3. 幂等性检查
            if (isDuplicateBindPhoneNotify(notifyDTO.getBizUserId(), notifyDTO.getBindTime())) {
                log.info("会员绑定手机号结果通知重复, bizUserId={}, bindTime={}",
                        notifyDTO.getBizUserId(), notifyDTO.getBindTime());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }

            // TODO: 4. 更新绑定手机号结果到数据库
            // 根据bindResult=1/2更新会员绑定手机号状态,记录失败原因bindErrorMsg等

            // TODO: 5. 记录通知到Redis,用于幂等性检查
            // String notifyKey = "member:bindphone:notify:" + notifyDTO.getBizUserId() + ":" + notifyDTO.getBindTime();
            // redisTemplate.opsForValue().set(notifyKey, "1", 7, TimeUnit.DAYS);

            // TODO: 6. 发送业务通知(如推送给BFF或消息队列)

            log.info("会员绑定手机号结果通知处理成功, bizUserId={}", notifyDTO.getBizUserId());

            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";

        } catch (Exception e) {
            log.error("会员绑定手机号结果通知处理异常", e);
            return "fail";
        }
    }
    
    @Override
    public String handleApplyMemProtocolNotify(HttpServletRequest httpRequest) {
        log.info("收到会员协议签约结果通知");

        try {
            // 1. 解析通知参数
            Map<String, String> params = parseNotifyParams(httpRequest);
            ApplyMemProtocolNotifyDTO notifyDTO = parseApplyMemProtocolNotifyParams(params);

            log.info("会员协议签约结果通知: bizUserId={}, protocolType={}, noticeType={}, divisionProtocolState={}, couponProtocolState={}",
                    notifyDTO.getBizUserId(), notifyDTO.getProtocolType(), notifyDTO.getNoticeType(),
                    notifyDTO.getDivisionProtocolState(), notifyDTO.getCouponProtocolState());

            // 2. 验签
            if (!signService.verifyNotify(params)) {
                log.error("会员协议签约结果通知验签失败");
                return "fail";
            }

            // 3. 幂等性检查
            if (isDuplicateApplyMemProtocolNotify(notifyDTO.getBizUserId(), notifyDTO.getDivisionProtocolNo(), notifyDTO.getNoticeType())) {
                log.info("会员协议签约结果通知重复, bizUserId={}, divisionProtocolNo={}, noticeType={}",
                        notifyDTO.getBizUserId(), notifyDTO.getDivisionProtocolNo(), notifyDTO.getNoticeType());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }

            // 4. 解析分账/抽佣协议信息
            if (StrUtil.isNotBlank(notifyDTO.getDivisionMemInfo())) {
                DivisionMemInfoDTO divisionMemInfo = parseDivisionMemInfo(notifyDTO.getDivisionMemInfo());
                log.info("分账协议信息: divisionBizUserId={}, divisionType={}, divisionRate={}",
                        divisionMemInfo.getDivisionBizUserId(), divisionMemInfo.getDivisionType(), divisionMemInfo.getDivisionRate());
            }

            if (StrUtil.isNotBlank(notifyDTO.getCouponInfo())) {
                CouponInfoDTO couponInfo = parseCouponInfo(notifyDTO.getCouponInfo());
                log.info("抽佣协议信息: couponType={}, couponRate={}",
                        couponInfo.getCouponType(), couponInfo.getCouponRate());
            }

            // TODO: 5. 更新协议签约/审核结果到数据库
            // 根据noticeType和protocolType更新对应协议状态,记录协议编号、审批结果等

            // TODO: 6. 记录通知到Redis,用于幂等性检查
            // String notifyKey = "member:protocol:notify:" + notifyDTO.getBizUserId() + ":" + notifyDTO.getDivisionProtocolNo() + ":" + notifyDTO.getNoticeType();
            // redisTemplate.opsForValue().set(notifyKey, "1", 7, TimeUnit.DAYS);

            // TODO: 7. 发送业务通知(如推送给BFF或消息队列)

            log.info("会员协议签约结果通知处理成功, bizUserId={}, divisionProtocolNo={}",
                    notifyDTO.getBizUserId(), notifyDTO.getDivisionProtocolNo());

            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";

        } catch (Exception e) {
            log.error("会员协议签约结果通知处理异常", e);
            return "fail";
        }
    }
    
    @Override
    public GenerateRegInviteLinkResponse generateRegInviteLink(GenerateRegInviteLinkRequest request) {
        log.info("开始会员注册邀请码生成, bizUserId:{}, memCategory:{}, chanNo:{}",
                request.getBizUserId(), request.getMemCategory(), request.getChanNo());

        try {
            // 1. 参数校验
            validateGenerateRegInviteLink(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 生成签名
            signService.signRequest(request);

            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/generateRegInviteLink";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员注册邀请码生成请求: {}, 参数: {}", apiUrl, jsonRequest);

            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );

            // 5. 解析响应并验签
            GenerateRegInviteLinkResponse response = JSONUtil.toBean(jsonResponse, GenerateRegInviteLinkResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("邀请码生成响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("邀请码生成失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("邀请码生成失败: " + response.getMsg());
            }

            // TODO: 7. 保存邀请码生成记录到数据库

            log.info("会员注册邀请码生成处理完成, bizUserId:{}, code:{}, msg:{}",
                    request.getBizUserId(), response.getCode(), response.getMsg());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员注册邀请码生成异常", e);
            throw PaymentException.systemError("会员注册邀请码生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public String handleMemberRegSuccessNotify(HttpServletRequest httpRequest) {
        log.info("收到会员注册成功通知");

        try {
            // 1. 解析通知参数
            Map<String, String> params = parseNotifyParams(httpRequest);
            MemberRegSuccessNotifyDTO notifyDTO = parseMemberRegSuccessNotifyParams(params);

            log.info("会员注册成功通知: bizUserId={}, memCategory={}, memSubType={}, memNo={}",
                    notifyDTO.getBizUserId(), notifyDTO.getMemCategory(), notifyDTO.getMemSubType(), notifyDTO.getMemNo());

            // 2. 验签
            if (!signService.verifyNotify(params)) {
                log.error("会员注册成功通知验签失败");
                return "fail";
            }

            // 3. 幂等性检查
            if (isDuplicateMemberRegSuccessNotify(notifyDTO.getBizUserId(), notifyDTO.getMemNo())) {
                log.info("会员注册成功通知重复, bizUserId={}, memNo={}",
                        notifyDTO.getBizUserId(), notifyDTO.getMemNo());
                // 注意:必须返回小写的success,前后不能有空格和换行符
                return "success";
            }

            // TODO: 4. 解析个人/企业/绑卡/渠道信息并更新会员及账户信息到数据库
            // memInfo/enterpriseInfo/bankList/chanList 字段为JSON,可在此处解析后落表

            // TODO: 5. 记录通知到Redis,用于幂等性检查
            // String notifyKey = "member:regsuccess:notify:" + notifyDTO.getBizUserId() + ":" + notifyDTO.getMemNo();
            // redisTemplate.opsForValue().set(notifyKey, "1", 7, TimeUnit.DAYS);

            // TODO: 6. 发送业务通知(如推送给BFF或消息队列)

            log.info("会员注册成功通知处理成功, bizUserId={}, memNo={}",
                    notifyDTO.getBizUserId(), notifyDTO.getMemNo());

            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";

        } catch (Exception e) {
            log.error("会员注册成功通知处理异常", e);
            return "fail";
        }
    }
    
    @Override
    public RegBindSybCusIdResponse regBindSybCusId(RegBindSybCusIdRequest request) {
        log.info("开始会员绑定收银宝商户号开户, bizUserId:{}, sybCusId:{}, chanNo:{}",
                request.getBizUserId(), request.getSybCusId(), request.getChanNo());

        try {
            // 1. 参数校验
            validateRegBindSybCusId(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 生成签名
            signService.signRequest(request);

            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "payMember/regBindSybCusId";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("会员绑定收银宝商户号请求: {}, 参数: {}", apiUrl, jsonRequest);

            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );

            // 5. 解析响应并验签
            RegBindSybCusIdResponse response = JSONUtil.toBean(jsonResponse, RegBindSybCusIdResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("绑定收银宝商户号响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("绑定收银宝商户号失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("绑定收银宝商户号失败: " + response.getMsg());
            }

            // TODO: 7. 保存绑定收银宝商户号信息到数据库

            log.info("会员绑定收银宝商户号开户处理完成, bizUserId:{}, code:{}, msg:{}",
                    request.getBizUserId(), response.getCode(), response.getMsg());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("会员绑定收银宝商户号开户异常", e);
            throw PaymentException.systemError("会员绑定收银宝商户号开户失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析会员绑定手机号结果通知参数
     */
    private BindPhoneNotifyDTO parseBindPhoneNotifyParams(Map<String, String> params) {
        BindPhoneNotifyDTO notifyDTO = new BindPhoneNotifyDTO();
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setBizUserId(params.get("bizUserId"));
        notifyDTO.setChanNo(params.get("chanNo"));
        notifyDTO.setBindResult(params.get("bindResult"));
        notifyDTO.setBindErrorMsg(params.get("bindErrorMsg"));
        notifyDTO.setBindTime(params.get("bindTime"));
        notifyDTO.setPhone(params.get("phone"));
        notifyDTO.setPhoneType(params.get("phoneType"));
        return notifyDTO;
    }
    
    /**
     * 检查是否为重复通知 - 绑定手机号结果
     * TODO: 实现Redis幂等性检查
     */
    private boolean isDuplicateBindPhoneNotify(String bizUserId, String bindTime) {
        // TODO: 从 Redis 中检查是否已经处理过该通知
        // String notifyKey = "member:bindphone:notify:" + bizUserId + ":" + bindTime;
        // return Boolean.TRUE.equals(redisTemplate.hasKey(notifyKey));
        return false;
    }
    
    /**
     * 检查是否为重复通知 - 会员协议签约结果
     * TODO: 实现Redis幂等性检查
     */
    private boolean isDuplicateApplyMemProtocolNotify(String bizUserId, String divisionProtocolNo, String noticeType) {
        // TODO: 从 Redis 中检查是否已经处理过该通知
        // String notifyKey = "member:protocol:notify:" + bizUserId + ":" + divisionProtocolNo + ":" + noticeType;
        // return Boolean.TRUE.equals(redisTemplate.hasKey(notifyKey));
        return false;
    }
    
    /**
     * 检查是否为重复通知 - 会员注册成功
     * TODO: 实现Redis幂等性检查
     */
    private boolean isDuplicateMemberRegSuccessNotify(String bizUserId, String memNo) {
        // TODO: 从 Redis 中检查是否已经处理过该通知
        // String notifyKey = "member:regsuccess:notify:" + bizUserId + ":" + memNo;
        // return Boolean.TRUE.equals(redisTemplate.hasKey(notifyKey));
        return false;
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(RegEnterpriseRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
        
        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }
    
    /**
     * 填充请求基础参数 - 资料补录
     */
    private void fillBaseRequest(RegSupplementRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
        
        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }
    
    /**
     * 填充请求基础参数 - 绑定手机号申请
     */
    private void fillBaseRequest(RegPersonRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
        
        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }
    
    /**
     * 填充请求基础参数 - 绑定手机号申请
     */
    private void fillBaseRequest(BindPhoneAppleRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
        
        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }
    
    /**
     * 验证个人会员实名及绑卡必填参数
     */
    private void validateRegPerson(RegPersonRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getMemAlias())) {
            throw PaymentException.bizError("会员简称不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getMemInfo())) {
            throw PaymentException.bizError("个人信息不能为空");
        }
        if (StrUtil.isBlank(request.getBankInfo())) {
            throw PaymentException.bizError("银行卡信息不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",目前仅支持yunst2isv");
        }

        // yunst2isv渠道必填参数校验
        if ("yunst2isv".equals(request.getChanNo())) {
            if (StrUtil.isBlank(request.getMemRole())) {
                throw PaymentException.bizError("yunst2isv渠道时会员角色不能为空");
            }
            if (StrUtil.isBlank(request.getMemSubType())) {
                throw PaymentException.bizError("yunst2isv渠道时会员子类型不能为空");
            }
            if (!"2".equals(request.getMemSubType())) {
                throw PaymentException.bizError("个人会员仅支持会员子类型2-分账方");
            }
        }
    }

    /**
     * 解析个人信息
     */
    private PersonInfoDTO parsePersonInfo(String memInfoJson) {
        try {
            return JSONUtil.toBean(memInfoJson, PersonInfoDTO.class);
        } catch (Exception e) {
            log.error("解析个人信息失败", e);
            throw PaymentException.bizError("个人信息格式错误: " + e.getMessage());
        }
    }

    /**
     * 解析个人银行卡信息
     */
    private PersonBankInfoDTO parsePersonBankInfo(String bankInfoJson) {
        try {
            return JSONUtil.toBean(bankInfoJson, PersonBankInfoDTO.class);
        } catch (Exception e) {
            log.error("解析个人银行卡信息失败", e);
            throw PaymentException.bizError("银行卡信息格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证个人信息
     */
    private void validatePersonInfo(PersonInfoDTO info) {
        if (info == null) {
            throw PaymentException.bizError("个人信息不能为空");
        }
        if (StrUtil.isBlank(info.getIdenName())) {
            throw PaymentException.bizError("姓名不能为空");
        }
        if (StrUtil.isBlank(info.getIdenNo())) {
            throw PaymentException.bizError("证件号码不能为空");
        }
        if (StrUtil.isBlank(info.getIdentityType())) {
            throw PaymentException.bizError("证件类型不能为空");
        }

        if (!"1".equals(info.getIdentityType())) {
            throw PaymentException.bizError("不支持的证件类型: " + info.getIdentityType()
                    + ",目前仅支持1-身份证");
        }
    }

    /**
     * 验证个人银行卡信息
     */
    private void validatePersonBankInfo(PersonBankInfoDTO info) {
        if (info == null) {
            throw PaymentException.bizError("银行卡信息不能为空");
        }
        if (StrUtil.isBlank(info.getAcctNo())) {
            throw PaymentException.bizError("银行卡号不能为空");
        }
        if (StrUtil.isBlank(info.getBankPhoneNo())) {
            throw PaymentException.bizError("银行卡绑定手机号不能为空");
        }
    }

    /**
     * 构建模拟响应 - 个人会员实名及绑卡
     * 同时实现对私卡绑定模拟规则
     */
    private RegPersonResponse buildMockRegPersonResponse(RegPersonRequest request, PersonBankInfoDTO bankInfo) {
        RegPersonResponse response = new RegPersonResponse();
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());

        String acctNo = bankInfo != null ? bankInfo.getAcctNo() : null;
        String code = "0";
        String msg = "交易成功";
        if (StrUtil.isNotBlank(acctNo) && acctNo.length() >= 4) {
            char c = acctNo.charAt(3);
            switch (c) {
                case '0':
                    code = "0";
                    msg = "交易成功";
                    break;
                case '1':
                    code = "9999";
                    msg = "查开户方原因";
                    break;
                case '2':
                    code = "9999";
                    msg = "无效卡号";
                    break;
                case '3':
                    code = "9999";
                    msg = "已挂失卡";
                    break;
                case '4':
                    code = "9999";
                    msg = "余额不足";
                    break;
                case '5':
                    code = "9999";
                    msg = "无此账户";
                    break;
                case '6':
                    code = "9999";
                    msg = "原交易失败，不收费";
                    break;
                case '7':
                    code = "9999";
                    msg = "户名错";
                    break;
                case '8':
                    code = "9999";
                    msg = "交易超时";
                    break;
                default:
                    code = "0";
                    msg = "交易成功";
                    break;
            }
        }
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }

    /**
     * 验证企业会员实名开户必填参数
     */
    private void validateRegEnterprise(RegEnterpriseRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getMemCategory())) {
            throw PaymentException.bizError("会员种类不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getNotifyUrl())) {
            throw PaymentException.bizError("通知地址不能为空");
        }
        if (StrUtil.isBlank(request.getMemAlias())) {
            throw PaymentException.bizError("会员简称不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getEnterpriseInfo())) {
            throw PaymentException.bizError("企业信息不能为空");
        }
        if (StrUtil.isBlank(request.getBankInfo())) {
            throw PaymentException.bizError("银行卡信息不能为空");
        }
            
        // 校验会员种类
        if (!"02".equals(request.getMemCategory()) && !"03".equals(request.getMemCategory())) {
            throw PaymentException.bizError("不支持的会员种类: " + request.getMemCategory() 
                    + ",仅支持02-企业挆03-个体工商户");
        }
            
        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo() 
                    + ",目前仅支持yunst2isv");
        }
            
        // yunst2isv渠道必填参数校验
        if ("yunst2isv".equals(request.getChanNo())) {
            if (StrUtil.isBlank(request.getMemRole())) {
                throw PaymentException.bizError("yunst2isv渠道时会员角色不能为空");
            }
            if (StrUtil.isBlank(request.getMemSubType())) {
                throw PaymentException.bizError("yunst2isv渠道时会员子类型不能为空");
            }
        }
    }
        
    /**
     * 验证会员资料补录必填参数
     */
    private void validateRegSupplement(RegSupplementRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getNotifyUrl())) {
            throw PaymentException.bizError("通知地址不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
            
        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo() 
                    + ",目前仅支持yunst2isv");
        }
            
        // 至少需要一个fileId
        if (StrUtil.isBlank(request.getUniCreditFileId()) 
                && StrUtil.isBlank(request.getLegalIdZFileId()) 
                && StrUtil.isBlank(request.getLegalIdFFileId())) {
            throw PaymentException.bizError("至少需要一个证件影印件fileId");
        }
    }
    
    /**
     * 解析企业信息
     */
    private EnterpriseInfoDTO parseEnterpriseInfo(String enterpriseInfoJson) {
        try {
            return JSONUtil.toBean(enterpriseInfoJson, EnterpriseInfoDTO.class);
        } catch (Exception e) {
            log.error("解析企业信息失败", e);
            throw PaymentException.bizError("企业信息格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 解析银行卡信息
     */
    private BankInfoDTO parseBankInfo(String bankInfoJson) {
        try {
            return JSONUtil.toBean(bankInfoJson, BankInfoDTO.class);
        } catch (Exception e) {
            log.error("解析银行卡信息失败", e);
            throw PaymentException.bizError("银行卡信息格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 验证企业信息
     */
    private void validateEnterpriseInfo(EnterpriseInfoDTO info) {
        if (StrUtil.isBlank(info.getCompanyName())) {
            throw PaymentException.bizError("企业名称不能为空");
        }
        if (StrUtil.isBlank(info.getCompanyAddress())) {
            throw PaymentException.bizError("企业地址不能为空");
        }
        if (StrUtil.isBlank(info.getCompanyAddrCode())) {
            throw PaymentException.bizError("地区码不能为空");
        }
        if (StrUtil.isBlank(info.getAuthType())) {
            throw PaymentException.bizError("认证类型不能为空");
        }
        if (StrUtil.isBlank(info.getUniCredit())) {
            throw PaymentException.bizError("统一社会信用代码不能为空");
        }
        if (StrUtil.isBlank(info.getLegalName())) {
            throw PaymentException.bizError("法人姓名不能为空");
        }
        if (StrUtil.isBlank(info.getIdentityType())) {
            throw PaymentException.bizError("证件类型不能为空");
        }
        if (StrUtil.isBlank(info.getLegalIds())) {
            throw PaymentException.bizError("法人身份证不能为空");
        }
        if (StrUtil.isBlank(info.getLegalPhone())) {
            throw PaymentException.bizError("法人手机号不能为空");
        }
        
        // 校验认证类型
        if (!"2".equals(info.getAuthType())) {
            throw PaymentException.bizError("不支持的认证类型: " + info.getAuthType() 
                    + ",目前仅支持2-一证");
        }
        
        // 校验证件类型
        if (!"1".equals(info.getIdentityType())) {
            throw PaymentException.bizError("不支持的证件类型: " + info.getIdentityType() 
                    + ",目前仅支持1-身份证");
        }
    }
    
    /**
     * 验证银行卡信息
     */
    private void validateBankInfo(BankInfoDTO info, EnterpriseInfoDTO enterpriseInfo) {
        if (StrUtil.isBlank(info.getAcctType())) {
            throw PaymentException.bizError("账户类型不能为空");
        }
        if (StrUtil.isBlank(info.getAcctNo())) {
            throw PaymentException.bizError("银行卡号不能为空");
        }
        if (StrUtil.isBlank(info.getOpenBankProvince())) {
            throw PaymentException.bizError("开户行所在省不能为空");
        }
        if (StrUtil.isBlank(info.getOpenBankCity())) {
            throw PaymentException.bizError("开户行所在市不能为空");
        }
        
        // 校验账户类型
        if (!"0".equals(info.getAcctType()) && !"1".equals(info.getAcctType())) {
            throw PaymentException.bizError("不支持的账户类型: " + info.getAcctType() 
                    + ",仅支持0-对私或1-对公");
        }
        
        // 对私账户必填参数
        if ("0".equals(info.getAcctType())) {
            if (StrUtil.isBlank(info.getBankPhoneNo())) {
                throw PaymentException.bizError("对私账户时银行卡绑定手机号不能为空");
            }
        }
        
        // 对公账户必填参数
        if ("1".equals(info.getAcctType())) {
            if (StrUtil.isBlank(info.getOpenBankNo())) {
                throw PaymentException.bizError("对公账户时银行代码不能为空");
            }
            if (StrUtil.isBlank(info.getBankName())) {
                throw PaymentException.bizError("对公账户时开户行支行名称不能为空");
            }
            if (StrUtil.isBlank(info.getUnionBank())) {
                throw PaymentException.bizError("对公账户时支付行号不能为空");
            }
        }
    }
    
    /**
     * 构建模拟响应
     */
    private RegEnterpriseResponse buildMockResponse(RegEnterpriseRequest request) {
        RegEnterpriseResponse response = new RegEnterpriseResponse();
        response.setCode("0");
        response.setMsg("开户成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());
        
        return response;
    }
    
    /**
     * 构建模拟响应 - 资料补录
     */
    private RegSupplementResponse buildMockSupplementResponse(RegSupplementRequest request) {
        RegSupplementResponse response = new RegSupplementResponse();
        response.setCode("0");
        response.setMsg("资料补录成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());
        
        return response;
    }
    
    /**
     * 解析通知参数
     */
    private Map<String, String> parseNotifyParams(HttpServletRequest httpRequest) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = httpRequest.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        return params;
    }
    
    /**
     * 解析企业会员开户结果通知参数
     */
    private RegEnterpriseNotifyDTO parseRegEnterpriseNotifyParams(Map<String, String> params) {
        RegEnterpriseNotifyDTO notifyDTO = new RegEnterpriseNotifyDTO();
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setBizUserId(params.get("bizUserId"));
        notifyDTO.setChanNo(params.get("chanNo"));
        notifyDTO.setAuditResult(params.get("auditResult"));
        notifyDTO.setAuditTime(params.get("auditTime"));
        notifyDTO.setAuditRemark(params.get("auditRemark"));
        notifyDTO.setAuditResultMsg(params.get("auditResultMsg"));
        return notifyDTO;
    }
    
    /**
     * 检查是否为重复通知
     * TODO: 实现Redis幂等性检查
     */
    private boolean isDuplicateRegEnterpriseNotify(String bizUserId, String auditTime) {
        // 早期这里恒返回 false，等于没有幂等：渠道重发一次回调状态就被覆盖一次。
        // 改为查通知流水表，真正的去重在 recordNotify 的唯一键上。
        Long count = payMemberNotifyMapper.selectCount(new LambdaQueryWrapper<PayMemberNotifyEntity>()
                .eq(PayMemberNotifyEntity::getBizUserId, bizUserId)
                .eq(PayMemberNotifyEntity::getNotifyType, NOTIFY_REG_ENTERPRISE)
                .eq(PayMemberNotifyEntity::getNotifyKey, auditTime));
        return count != null && count > 0;
    }

    /**
     * 会员档案 upsert。
     *
     * <p>渠道侧开户是外部副作用：调用成功，会员在渠道就存在了，本地不留痕就会出现
     * 「渠道有、本地无」的账实不符，审核回调回来也没有可更新的档案。
     * 因此这里落库失败直接抛错（宁可让上游感知失败，也不静默丢数据）。
     */
    private void upsertMember(String bizUserId, String memberType, String memCategory, String chanNo) {
        if (StrUtil.isBlank(bizUserId)) {
            log.warn("[会员档案] bizUserId 为空，跳过落库");
            return;
        }
        try {
            PayMemberEntity exist = selectMember(bizUserId);
            if (exist == null) {
                PayMemberEntity entity = new PayMemberEntity();
                entity.setBizUserId(bizUserId);
                entity.setMemberType(memberType);
                entity.setMemCategory(memCategory);
                entity.setChanNo(chanNo);
                entity.setStatus(MEMBER_STATUS_PROCESSING);
                entity.setBindPhoneStatus(BIND_PHONE_NONE);
                payMemberMapper.insert(entity);
                log.info("[会员档案] 新建 bizUserId={}, type={}", bizUserId, memberType);
                return;
            }
            PayMemberEntity update = new PayMemberEntity();
            update.setId(exist.getId());
            update.setMemberType(memberType);
            update.setMemCategory(memCategory);
            update.setChanNo(chanNo);
            payMemberMapper.updateById(update);
            log.info("[会员档案] 已存在，更新类型/渠道 bizUserId={}, type={}", bizUserId, memberType);
        } catch (DuplicateKeyException e) {
            log.info("[会员档案] 并发新建同一会员，忽略 bizUserId={}", bizUserId);
        } catch (Exception e) {
            log.error("[会员档案] 落库失败 bizUserId={}", bizUserId, e);
            throw PaymentException.systemError("会员档案保存失败: " + e.getMessage());
        }
    }

    /** 按渠道会员编号查档案。 */
    private PayMemberEntity selectMember(String bizUserId) {
        return payMemberMapper.selectOne(new LambdaQueryWrapper<PayMemberEntity>()
                .eq(PayMemberEntity::getBizUserId, bizUserId)
                .last("LIMIT 1"));
    }

    /**
     * 按渠道审核结果推进会员状态。
     *
     * <p>档案不存在时先补建（回调可能先于本地落库到达），避免状态永远停在「处理中」。
     */
    private void updateMemberStatus(String bizUserId, String auditResult) {
        if (StrUtil.isBlank(bizUserId)) {
            return;
        }
        Integer status;
        if ("2".equals(auditResult)) {
            status = MEMBER_STATUS_SUCCESS;
        } else if ("3".equals(auditResult)) {
            status = MEMBER_STATUS_FAILED;
        } else {
            log.warn("[会员档案] 未知审核结果，不更新状态 bizUserId={}, auditResult={}", bizUserId, auditResult);
            return;
        }
        PayMemberEntity exist = selectMember(bizUserId);
        if (exist == null) {
            upsertMember(bizUserId, MEMBER_TYPE_ENTERPRISE, null, null);
            exist = selectMember(bizUserId);
            if (exist == null) {
                throw PaymentException.systemError("会员档案缺失，无法更新开户状态: " + bizUserId);
            }
        }
        PayMemberEntity update = new PayMemberEntity();
        update.setId(exist.getId());
        update.setStatus(status);
        update.setAuditResult(auditResult);
        payMemberMapper.updateById(update);
        log.info("[会员档案] 状态更新 bizUserId={}, auditResult={}, status={}", bizUserId, auditResult, status);
    }

    /** 更新绑手机状态（申请中 / 已绑定 / 失败）。 */
    private void markBindPhone(String bizUserId, String phone, Integer bindStatus) {
        if (StrUtil.isBlank(bizUserId)) {
            return;
        }
        try {
            PayMemberEntity exist = selectMember(bizUserId);
            if (exist == null) {
                // 绑手机可能发生在开户档案建立之前，补一条再更新
                upsertMember(bizUserId, MEMBER_TYPE_PERSON, null, null);
                exist = selectMember(bizUserId);
                if (exist == null) {
                    log.warn("[会员档案] 档案缺失，绑手机状态未落库 bizUserId={}", bizUserId);
                    return;
                }
            }
            PayMemberEntity update = new PayMemberEntity();
            update.setId(exist.getId());
            update.setBindPhoneStatus(bindStatus);
            if (StrUtil.isNotBlank(phone)) {
                update.setPhone(phone);
            }
            payMemberMapper.updateById(update);
            log.info("[会员档案] 绑手机状态更新 bizUserId={}, status={}", bizUserId, bindStatus);
        } catch (Exception e) {
            log.error("[会员档案] 绑手机状态落库失败 bizUserId={}", bizUserId, e);
        }
    }

    /** 把补录资料（文件 ID 等）写入 ext。 */
    private void updateMemberExt(String bizUserId, Map<String, String> ext) {
        if (StrUtil.isBlank(bizUserId) || ext == null || ext.isEmpty()) {
            return;
        }
        try {
            PayMemberEntity exist = selectMember(bizUserId);
            if (exist == null) {
                log.warn("[会员档案] 档案不存在，补录资料未落库 bizUserId={}", bizUserId);
                return;
            }
            PayMemberEntity update = new PayMemberEntity();
            update.setId(exist.getId());
            update.setExt(JSONUtil.toJsonStr(ext));
            payMemberMapper.updateById(update);
        } catch (Exception e) {
            log.error("[会员档案] 补录资料落库失败 bizUserId={}", bizUserId, e);
        }
    }

    /**
     * 记录通知流水并抢占幂等位。
     *
     * @return true=首次到达（调用方继续处理）；false=重复通知（调用方应忽略）
     */
    private boolean recordNotify(String bizUserId, String notifyType, String notifyKey,
                                 String auditResult, String payload) {
        if (StrUtil.isBlank(bizUserId)) {
            return false;
        }
        String key = notifyKey == null ? "" : notifyKey;
        PayMemberNotifyEntity entity = new PayMemberNotifyEntity();
        entity.setBizUserId(bizUserId);
        entity.setNotifyType(notifyType);
        entity.setNotifyKey(key);
        entity.setAuditResult(auditResult);
        entity.setPayload(payload == null ? null : payload.length() > 2000 ? payload.substring(0, 2000) : payload);
        try {
            payMemberNotifyMapper.insert(entity);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
    
    /**
     * 解析会员资料补录结果通知参数
     */
    private RegSupplementNotifyDTO parseRegSupplementNotifyParams(Map<String, String> params) {
        RegSupplementNotifyDTO notifyDTO = new RegSupplementNotifyDTO();
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setBizUserId(params.get("bizUserId"));
        notifyDTO.setChanNo(params.get("chanNo"));
        notifyDTO.setEnterpriseCompareResult(params.get("enterpriseCompareResult"));
        notifyDTO.setLegalPersonCompareResult(params.get("legalPersonCompareResult"));
        notifyDTO.setCompareResults(params.get("compareResults"));
        notifyDTO.setEnterpriseInfo(params.get("enterpriseInfo"));
        notifyDTO.setLegalPersonCardFaceInfo(params.get("legalPersonCardFaceInfo"));
        notifyDTO.setLegalPersonCardNationalEmblemInfo(params.get("legalPersonCardNationalEmblemInfo"));
        return notifyDTO;
    }
    
    /**
     * 解析会员协议签约结果通知参数
     */
    private ApplyMemProtocolNotifyDTO parseApplyMemProtocolNotifyParams(Map<String, String> params) {
        ApplyMemProtocolNotifyDTO notifyDTO = new ApplyMemProtocolNotifyDTO();
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setBizUserId(params.get("bizUserId"));
        notifyDTO.setChanNo(params.get("chanNo"));
        notifyDTO.setNoticeType(params.get("noticeType"));
        notifyDTO.setProtocolType(params.get("protocolType"));
        notifyDTO.setDivisionProtocolState(params.get("divisionProtocolState"));
        notifyDTO.setCouponProtocolState(params.get("couponProtocolState"));
        notifyDTO.setAuditResult(params.get("auditResult"));
        notifyDTO.setAuditTime(params.get("auditTime"));
        notifyDTO.setAuditResultMsg(params.get("auditResultMsg"));
        notifyDTO.setDivisionProtocolNo(params.get("divisionProtocolNo"));
        notifyDTO.setDivisionMemInfo(params.get("divisionMemInfo"));
        notifyDTO.setCouponProtocolNo(params.get("couponProtocolNo"));
        notifyDTO.setCouponInfo(params.get("couponInfo"));
        return notifyDTO;
    }
    
    /**
     * 解析会员注册成功通知参数
     */
    private MemberRegSuccessNotifyDTO parseMemberRegSuccessNotifyParams(Map<String, String> params) {
        MemberRegSuccessNotifyDTO notifyDTO = new MemberRegSuccessNotifyDTO();
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setMemAlias(params.get("memAlias"));
        notifyDTO.setMemCategory(params.get("memCategory"));
        notifyDTO.setBizUserId(params.get("bizUserId"));
        notifyDTO.setMemSubType(params.get("memSubType"));
        notifyDTO.setMemNo(params.get("memNo"));
        notifyDTO.setPhoneNo(params.get("phoneNo"));
        notifyDTO.setMemInfo(params.get("memInfo"));
        notifyDTO.setEnterpriseInfo(params.get("enterpriseInfo"));
        notifyDTO.setBankList(params.get("bankList"));
        notifyDTO.setChanList(params.get("chanList"));
        return notifyDTO;
    }
    
    /**
     * 检查是否为重复通知 - 资料补录
     * TODO: 实现Redis幂等性检查
     */
    private boolean isDuplicateRegSupplementNotify(String bizUserId) {
        // TODO: 从 Redis 中检查是否已经处理过该通知
        // String notifyKey = "member:supplement:notify:" + bizUserId;
        // return Boolean.TRUE.equals(redisTemplate.hasKey(notifyKey));
        return false;
    }
    
    /**
     * 验证会员绑定手机号申请必填参数
     */
    private void validateBindPhoneApple(BindPhoneAppleRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getPhone())) {
            throw PaymentException.bizError("绑定手机号不能为空");
        }
        if (StrUtil.isBlank(request.getPhoneType())) {
            throw PaymentException.bizError("绑定类型不能为空");
        }
        if (StrUtil.isBlank(request.getNotifyUrl())) {
            throw PaymentException.bizError("绑定手机号通知地址不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",目前仅支持yunst2isv");
        }

        // 校验绑定类型
        if (!"1".equals(request.getPhoneType()) && !"2".equals(request.getPhoneType())) {
            throw PaymentException.bizError("不支持的绑定类型: " + request.getPhoneType()
                    + ",仅支持1-法人手机号或2-企业授权手机号");
        }

        // 企业授权手机号时,被授权人信息必传
        if ("2".equals(request.getPhoneType()) && StrUtil.isBlank(request.getAuthPerInfo())) {
            throw PaymentException.bizError("绑定类型为2时被授权人信息不能为空");
        }
    }

    /**
     * 解析被授权人信息
     */
    private AuthPerInfoDTO parseAuthPerInfo(String authPerInfoJson) {
        try {
            return JSONUtil.toBean(authPerInfoJson, AuthPerInfoDTO.class);
        } catch (Exception e) {
            log.error("解析被授权人信息失败", e);
            throw PaymentException.bizError("被授权人信息格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证被授权人信息
     */
    private void validateAuthPerInfo(AuthPerInfoDTO authPerInfo) {
        if (authPerInfo == null) {
            throw PaymentException.bizError("被授权人信息不能为空");
        }
        if (StrUtil.isBlank(authPerInfo.getAuthPerType())) {
            throw PaymentException.bizError("授权类型不能为空");
        }
        if (StrUtil.isBlank(authPerInfo.getAuthPerName())) {
            throw PaymentException.bizError("授权人名称不能为空");
        }
        if (StrUtil.isBlank(authPerInfo.getAuthPerIdNo())) {
            throw PaymentException.bizError("授权人证件号不能为空");
        }
        if (StrUtil.isBlank(authPerInfo.getAuthPerIdType())) {
            throw PaymentException.bizError("授权人证件类型不能为空");
        }

        // 校验授权类型
        if (!"1".equals(authPerInfo.getAuthPerType()) && !"2".equals(authPerInfo.getAuthPerType())) {
            throw PaymentException.bizError("不支持的授权类型: " + authPerInfo.getAuthPerType()
                    + ",仅支持1-线上或2-线下");
        }

        // 证件类型仅支持身份证
        if (!"1".equals(authPerInfo.getAuthPerIdType())) {
            throw PaymentException.bizError("不支持的证件类型: " + authPerInfo.getAuthPerIdType()
                    + ",目前仅支持1-身份证");
        }

        // 授权类型为线下时,授权协议文件必传
        if ("2".equals(authPerInfo.getAuthPerType())
                && StrUtil.isBlank(authPerInfo.getAuthProcotolFileId())) {
            throw PaymentException.bizError("线下授权时授权协议文件fileId不能为空");
        }
    }

    /**
     * 验证会员绑定手机号确认必填参数
     */
    private void validateBindPhoneConfirm(BindPhoneConfirmRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getApplyRespTraceNum())) {
            throw PaymentException.bizError("申请响应业务关联流水号不能为空");
        }
        if (StrUtil.isBlank(request.getPhone())) {
            throw PaymentException.bizError("绑定手机号不能为空");
        }
        if (StrUtil.isBlank(request.getVerifyCode())) {
            throw PaymentException.bizError("短信验证码不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",目前仅支持yunst2isv");
        }
    }

    /**
     * 填充请求基础参数 - 绑定手机号确认
     */
    private void fillBaseRequest(BindPhoneConfirmRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }

        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }

    /**
     * 构建模拟响应 - 绑定手机号申请
     */
    private BindPhoneAppleResponse buildMockBindPhoneAppleResponse(BindPhoneAppleRequest request, AuthPerInfoDTO authPerInfo) {
        BindPhoneAppleResponse response = new BindPhoneAppleResponse();
        // 授权类型为线下时,返回3000-已受理待审核; 其他情况返回0-处理成功
        if ("2".equals(request.getPhoneType())
                && authPerInfo != null
                && "2".equals(authPerInfo.getAuthPerType())) {
            response.setCode("3000");
            response.setMsg("已受理待审核");
        } else {
            response.setCode("0");
            response.setMsg("处理成功");
        }
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());
        response.setApplyRespTraceNum("AR" + System.currentTimeMillis());

        // 只有绑定类型为2且授权类型是1返回授权手机号签约链接
        if ("2".equals(request.getPhoneType())
                && authPerInfo != null
                && "1".equals(authPerInfo.getAuthPerType())) {
            response.setAuthSignLink("https://mock.tqfpay.com/authSign/" + request.getBizUserId());
        }

        return response;
    }

    /**
     * 构建模拟响应 - 绑定手机号确认
     */
    private BindPhoneConfirmResponse buildMockBindPhoneConfirmResponse(BindPhoneConfirmRequest request) {
        BindPhoneConfirmResponse response = new BindPhoneConfirmResponse();
        response.setCode("0");
        response.setMsg("绑定成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());
        response.setPhone(request.getPhone());
        return response;
    }
    
    /**
     * 验证会员协议签约申请必填参数
     */
    private void validateApplyMemProtocol(ApplyMemProtocolRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getProtocolType())) {
            throw PaymentException.bizError("协议类型不能为空");
        }
        if (StrUtil.isBlank(request.getNotifyUrl())) {
            throw PaymentException.bizError("签约结果回调地址不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",目前仅支持yunst2isv");
        }

        // 校验协议类型
        if (!"1".equals(request.getProtocolType()) && !"2".equals(request.getProtocolType())) {
            throw PaymentException.bizError("不支持的协议类型: " + request.getProtocolType()
                    + ",仅支持1-分账协议或2-抽佣协议");
        }

        // 协议类型对应的协议信息必传
        if ("1".equals(request.getProtocolType()) && StrUtil.isBlank(request.getDivisionMemInfo())) {
            throw PaymentException.bizError("协议类型为1时分账协议信息不能为空");
        }
        if ("2".equals(request.getProtocolType()) && StrUtil.isBlank(request.getCouponInfo())) {
            throw PaymentException.bizError("协议类型为2时抽佣协议信息不能为空");
        }
    }

    /**
     * 验证会员注册邀请码生成必填参数
     */
    private void validateGenerateRegInviteLink(GenerateRegInviteLinkRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getMemCategory())) {
            throw PaymentException.bizError("会员种类不能为空");
        }
        if (StrUtil.isBlank(request.getMemAlias())) {
            throw PaymentException.bizError("会员简称不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo()) && !"yunst2yhzh".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",仅支持yunst2isv或yunst2yhzh");
        }

        // yunst2yhzh渠道不支持个人
        if ("yunst2yhzh".equals(request.getChanNo()) && "01".equals(request.getMemCategory())) {
            throw PaymentException.bizError("yunst2yhzh渠道不支持个人会员(01)");
        }

        // chanNo为yunst2isv和yunst2yhzh时, memSubType/memRole必填
        if ("yunst2isv".equals(request.getChanNo()) || "yunst2yhzh".equals(request.getChanNo())) {
            if (StrUtil.isBlank(request.getMemSubType())) {
                throw PaymentException.bizError("渠道为yunst2isv或yunst2yhzh时会员类型(memSubType)不能为空");
            }
            if (StrUtil.isBlank(request.getMemRole())) {
                throw PaymentException.bizError("渠道为yunst2isv或yunst2yhzh时会员角色(memRole)不能为空");
            }
        }
    }
    
    /**
     * 验证会员绑定收银宝商户号开户必填参数
     */
    private void validateRegBindSybCusId(RegBindSybCusIdRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getMemAlias())) {
            throw PaymentException.bizError("会员简称不能为空");
        }
        if (StrUtil.isBlank(request.getSerialNo())) {
            throw PaymentException.bizError("请求流水号不能为空");
        }
        if (StrUtil.isBlank(request.getSybCusId())) {
            throw PaymentException.bizError("收银宝商户号不能为空");
        }

        // 校验渠道
        if (!"yunst2isv".equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo()
                    + ",目前仅支持yunst2isv");
        }

        // chanNo为yunst2isv时, memSubType/memRole/isCoupon必填
        if ("yunst2isv".equals(request.getChanNo())) {
            if (StrUtil.isBlank(request.getMemSubType())) {
                throw PaymentException.bizError("渠道为yunst2isv时会员类型(memSubType)不能为空");
            }
            if (StrUtil.isBlank(request.getMemRole())) {
                throw PaymentException.bizError("渠道为yunst2isv时会员角色(memRole)不能为空");
            }
            if (StrUtil.isBlank(request.getIsCoupon())) {
                throw PaymentException.bizError("渠道为yunst2isv时是否抽佣(isCoupon)不能为空");
            }

            // memSubType目前仅支持1-收款方
            if (!"1".equals(request.getMemSubType())) {
                throw PaymentException.bizError("不支持的会员类型(memSubType): " + request.getMemSubType()
                        + ",目前仅支持1-收款方");
            }

            // isCoupon 只能是0或1
            if (!"0".equals(request.getIsCoupon()) && !"1".equals(request.getIsCoupon())) {
                throw PaymentException.bizError("不支持的是否抽佣(isCoupon)取值: " + request.getIsCoupon()
                        + ",仅支持0或1");
            }
        }
    }

    /**
     * 填充请求基础参数 - 会员协议签约申请
     */
    private void fillBaseRequest(ApplyMemProtocolRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }

        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }

    /**
     * 填充请求基础参数 - 会员注册邀请码生成
     */
    private void fillBaseRequest(GenerateRegInviteLinkRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }

        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }
    
    /**
     * 填充请求基础参数 - 会员绑定收银宝商户号开户
     */
    private void fillBaseRequest(RegBindSybCusIdRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }

        // 如果没有serialNo,使用reqSeqNo
        if (StrUtil.isBlank(request.getSerialNo())) {
            request.setSerialNo(request.getReqSeqNo());
        }
    }

    /**
     * 解析分账协议信息
     */
    private DivisionMemInfoDTO parseDivisionMemInfo(String divisionMemInfoJson) {
        try {
            return JSONUtil.toBean(divisionMemInfoJson, DivisionMemInfoDTO.class);
        } catch (Exception e) {
            log.error("解析分账协议信息失败", e);
            throw PaymentException.bizError("分账协议信息格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证分账协议信息
     */
    private void validateDivisionMemInfo(DivisionMemInfoDTO info) {
        if (info == null) {
            throw PaymentException.bizError("分账协议信息不能为空");
        }
        if (StrUtil.isBlank(info.getDivisionBizUserId())) {
            throw PaymentException.bizError("分账方商户会员编号不能为空");
        }
        if (StrUtil.isBlank(info.getDivisionType())) {
            throw PaymentException.bizError("分账方式不能为空");
        }
        if (StrUtil.isBlank(info.getDivisionRate())) {
            throw PaymentException.bizError("最高分账比例不能为空");
        }
        if (StrUtil.isBlank(info.getDivisionProcotolFileId())) {
            throw PaymentException.bizError("分账业务佐证材料fileId不能为空");
        }

        // 分账方式仅支持分账比例
        if (!"1".equals(info.getDivisionType())) {
            throw PaymentException.bizError("不支持的分账方式: " + info.getDivisionType()
                    + ",目前仅支持1-分账比例");
        }

        // 校验最高分账比例
        try {
            BigDecimal rate = new BigDecimal(info.getDivisionRate());
            if (rate.scale() > 4) {
                throw PaymentException.bizError("最高分账比例最多支持4位小数");
            }
            if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw PaymentException.bizError("最高分账比例必须大于0且不超过1");
            }
        } catch (NumberFormatException e) {
            throw PaymentException.bizError("最高分账比例格式错误");
        }
    }

    /**
     * 解析抽佣协议信息
     */
    private CouponInfoDTO parseCouponInfo(String couponInfoJson) {
        try {
            return JSONUtil.toBean(couponInfoJson, CouponInfoDTO.class);
        } catch (Exception e) {
            log.error("解析抽佣协议信息失败", e);
            throw PaymentException.bizError("抽佣协议信息格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证抽佣协议信息
     */
    private void validateCouponInfo(CouponInfoDTO info) {
        if (info == null) {
            throw PaymentException.bizError("抽佣协议信息不能为空");
        }
        if (StrUtil.isBlank(info.getCouponType())) {
            throw PaymentException.bizError("抽佣方式不能为空");
        }
        if (StrUtil.isBlank(info.getCouponRate())) {
            throw PaymentException.bizError("抽佣比例不能为空");
        }

        // 抽佣方式仅支持分账比例
        if (!"1".equals(info.getCouponType())) {
            throw PaymentException.bizError("不支持的抽佣方式: " + info.getCouponType()
                    + ",目前仅支持1-分账比例");
        }

        // 校验抽佣比例
        try {
            BigDecimal rate = new BigDecimal(info.getCouponRate());
            if (rate.scale() > 4) {
                throw PaymentException.bizError("抽佣比例最多支持4位小数");
            }
            if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw PaymentException.bizError("抽佣比例必须大于0且不超过1");
            }
        } catch (NumberFormatException e) {
            throw PaymentException.bizError("抽佣比例格式错误");
        }
    }

    /**
     * 构建模拟响应 - 会员协议签约申请
     */
    private ApplyMemProtocolResponse buildMockApplyMemProtocolResponse(ApplyMemProtocolRequest request,
                                                                       DivisionMemInfoDTO divisionMemInfo,
                                                                       CouponInfoDTO couponInfo) {
        ApplyMemProtocolResponse response = new ApplyMemProtocolResponse();
        response.setCode("0");
        response.setMsg("签约链接生成成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());

        // 收款方签约链接
        response.setRecSignLink("https://mock.tqfpay.com/memProtocol/rec/" + request.getBizUserId());

        // 分账方签约链接(仅分账协议返回)
        if ("1".equals(request.getProtocolType()) && divisionMemInfo != null) {
            response.setDivSignLink("https://mock.tqfpay.com/memProtocol/div/" + divisionMemInfo.getDivisionBizUserId());
        }

        // 签约链接失效时间(模拟当前时间)
        response.setProtocolLinkEndTime(TimeUtil.getCurrentDateTimeStr());

        return response;
    }

    /**
     * 构建模拟响应 - 会员注册邀请码生成
     */
    private GenerateRegInviteLinkResponse buildMockGenerateRegInviteLinkResponse(GenerateRegInviteLinkRequest request) {
        GenerateRegInviteLinkResponse response = new GenerateRegInviteLinkResponse();
        response.setCode("0");
        response.setMsg("邀请码生成成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());

        // 邀请注册链接
        response.setRegInviteLink("https://mock.tqfpay.com/regInvite/" + request.getBizUserId());
        // 邀请链接失效时间(模拟当前时间)
        response.setRegInviteLinkEndTime(TimeUtil.getCurrentDateTimeStr());

        return response;
    }
    
    /**
     * 构建模拟响应 - 会员绑定收银宝商户号开户
     */
    private RegBindSybCusIdResponse buildMockRegBindSybCusIdResponse(RegBindSybCusIdRequest request) {
        RegBindSybCusIdResponse response = new RegBindSybCusIdResponse();
        response.setCode("0");
        response.setMsg("绑定收银宝商户号成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBizUserId(request.getBizUserId());
        return response;
    }

    /**
     * 解析企业OCR识别信息
     */
    private EnterpriseOcrInfoDTO parseEnterpriseOcrInfo(String enterpriseInfoJson) {
        try {
            return JSONUtil.toBean(enterpriseInfoJson, EnterpriseOcrInfoDTO.class);
        } catch (Exception e) {
            log.error("解析企业OCR识别信息失败", e);
            return new EnterpriseOcrInfoDTO();
        }
    }

    /**
     * 解析法人证件肖像面识别信息
     */
    private LegalPersonCardFaceInfoDTO parseLegalPersonCardFaceInfo(String cardFaceInfoJson) {
        try {
            return JSONUtil.toBean(cardFaceInfoJson, LegalPersonCardFaceInfoDTO.class);
        } catch (Exception e) {
            log.error("解析法人证件肖像面识别信息失败", e);
            return new LegalPersonCardFaceInfoDTO();
        }
    }

    /**
     * 解析法人证件国徽面识别信息
     */
    private LegalPersonCardNationalEmblemInfoDTO parseLegalPersonCardNationalEmblemInfo(String cardEmblemInfoJson) {
        try {
            return JSONUtil.toBean(cardEmblemInfoJson, LegalPersonCardNationalEmblemInfoDTO.class);
        } catch (Exception e) {
            log.error("解析法人证件国徽面识别信息失败", e);
            return new LegalPersonCardNationalEmblemInfoDTO();
        }
    }
}
