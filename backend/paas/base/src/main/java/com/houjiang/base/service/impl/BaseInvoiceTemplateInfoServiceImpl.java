package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseInvoiceTemplateInfoEntity;
import com.houjiang.base.feign.OrderClient;
import com.houjiang.base.mapper.BaseInvoiceTemplateInfoMapper;
import com.houjiang.base.service.BaseInvoiceTemplateInfoService;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.TokenUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseInvoiceTemplateInfoServiceImpl extends ServiceImpl<BaseInvoiceTemplateInfoMapper, BaseInvoiceTemplateInfoEntity>
    implements BaseInvoiceTemplateInfoService{

    @Autowired
    private OrderClient orderClient;
    /**
     * 查询发票信息列表
     * 通过传入的参数查询用户的发票模板信息列表，用于展示用户已有的发票模板。
     *
     * @param param 查询参数，包含token等必要信息
     * @return 返回查询结果，包含发票模板信息的分页数据
     */
    @Override
    public ResponseResult queryInvoiceInfoList(Map<String, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID加入参数中，用于后续的查询
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据参数查询发票模板信息，使用LambdaQueryWrapper进行条件构造
            IPage<BaseInvoiceTemplateInfoEntity> page = this.baseMapper.selectPage(
                new Query<BaseInvoiceTemplateInfoEntity>().getPage(param),
                new LambdaQueryWrapper<BaseInvoiceTemplateInfoEntity>()
                    .eq(BaseInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                    .eq(param.containsKey("invoiceType"), BaseInvoiceTemplateInfoEntity::getInvoiceType, param.containsKey("invoiceType") ? param.get("invoiceType").toString() : null)
            );
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 记录异常信息，并返回失败结果
            log.error(e.getMessage());
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 插入或更新发票模板信息
     * 根据传入的参数判断是插入新的发票模板还是更新已有的发票模板。插入时会对模板数量进行检查，确保不超过限制。
     *
     * @param param 插入或更新参数，包含token、发票信息等必要信息
     * @return 返回操作结果，成功或失败
     */
    @Override
    public ResponseResult insertInvoiceTmp(Map<String, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID加入参数中，用于后续的操作
            param.put("userId", tokenResult.getData().toString());
        }
        // 检查发票抬头是否为空
        if (!param.containsKey("invoiceHead")) {
            return ResponseResult.success(BizCodeEnume.INVOICE_ADDRESS_TITLE_IS_NULL.getMsg());
        }
        // 如果发票类型参数不存在，默认设置为0
        if (!param.containsKey("invoiceType")) {
            param.put("invoiceType", 0);
        }
        // 如果发票抬头类型参数不存在，默认设置为1
        if (!param.containsKey("invoiceHeadType")) {
            param.put("invoiceHeadType", 1);
        }
        try {
            // 根据发票类型判断是否需要修改发票抬头类型
            if (param.get("invoiceType").equals(1)) {
                param.put("invoiceHeadType", 2);
            }
            // 检查发票模板数量是否达到上限
            if (param.get("invoiceTmpId") == null) {
                Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<BaseInvoiceTemplateInfoEntity>()
                        .eq(BaseInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                        .eq(param.containsKey("invoiceType"), BaseInvoiceTemplateInfoEntity::getInvoiceType, param.containsKey("invoiceType") ? param.get("invoiceType").toString() : null)));
                if (null != count && count >= 10) {
                    return ResponseResult.fail(BizCodeEnume.INVOICE_TEMPLATES_HAS_SUPERIOR_LIMIT);
                }
            }
            // 如果插入的是默认模板，将其他模板的默认标志设置为0
            int isDefault = Integer.parseInt(param.get("isDefault").toString());
            if (1 == isDefault) {
                this.baseMapper.update(null, new LambdaUpdateWrapper<BaseInvoiceTemplateInfoEntity>()
                    .eq(BaseInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                    .set(BaseInvoiceTemplateInfoEntity::getIsDefault, 0));
            }
            // 根据invoiceTmpId是否存在，判断是插入新模板还是更新已有模板
            if (param.get("invoiceTmpId") == null) {
                orderClient.insertInvoiceTmpFeign(param);
            } else {
                orderClient.updateInvoiceTmpFeign(param);
            }
            // 返回操作成功结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 记录异常信息，并返回失败结果
            log.error(e.getMessage());
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据发票模板ID查询发票模板信息。
     * 该方法用于通过提供的发票模板ID从数据库中检索相应的发票模板详情。
     * @param param 包含查询所需参数的Map，其中必须包含token和invoiceTmpId。
     * @return 返回查询结果的ResponseResult对象，包含发票模板的详细信息。
     */
    @Override
    public ResponseResult queryInvoiceTmp(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID添加到参数中
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据发票模板ID查询发票模板实体
            BaseInvoiceTemplateInfoEntity invoiceTmpEntity = this.baseMapper.selectById(param.get("invoiceTmpId").toString());
            // 返回查询成功的响应结果，包含发票模板实体
            return ResponseResult.success(invoiceTmpEntity);
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回查询失败的响应结果，包含异常信息
            return ResponseResult.error(e.getMessage());
        }

    }

    /**
     * 更新发票模板信息。
     * 该方法用于更新已存在的发票模板的详细信息，包括发票类型、发票头类型和默认设置等。
     * @param param 包含更新所需参数的Map，其中必须包含token、invoiceTmpId和其他要更新的字段。
     * @return 返回操作结果的ResponseResult对象，如果操作成功，则不包含具体数据。
     */
    @Override
    public ResponseResult updateInvoiceTmp(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID添加到参数中
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据发票类型设置发票头类型
            if (param.get("invoiceType").equals(1)) {
                param.put("invoiceHeadType", 2);
            }
            // 判断是否设置为默认发票模板
            int isDefault = Integer.valueOf(param.get("isDefault").toString());
            // 如果是默认模板，则将其他用户的默认设置改为非默认
            // 如果新插入的为默认配置，则将其他的配置isDefault置为0
            if (1 == isDefault) {
                this.baseMapper.update(null, new LambdaUpdateWrapper<BaseInvoiceTemplateInfoEntity>()
                        .eq(BaseInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                        .set(BaseInvoiceTemplateInfoEntity::getIsDefault, 0));
            }
            // 更新发票模板信息
            orderClient.updateInvoiceTmpFeign(param);
            // 返回操作成功的响应结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回操作失败的响应结果，包含异常信息
            return ResponseResult.error(e.getMessage());
        }

    }

    /**
     * 删除指定的发票模板。
     * 该方法用于根据提供的发票模板ID删除相应的发票模板记录。
     * @param param 包含删除所需参数的Map，其中必须包含token和invoiceTmpId。
     * @return 返回操作结果的ResponseResult对象，如果操作成功，则不包含具体数据。
     */
    @Override
    public ResponseResult deleteInvoiceTmp(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID添加到参数中
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据用户ID和发票模板ID删除发票模板记录
            this.baseMapper.delete(new LambdaQueryWrapper<BaseInvoiceTemplateInfoEntity>()
                    .eq(BaseInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                    .eq(BaseInvoiceTemplateInfoEntity::getInvoiceTmpId, param.get("invoiceTmpId").toString()));
            // 返回操作成功的响应结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回操作失败的响应结果
            return ResponseResult.fail();
        }
    }
}




