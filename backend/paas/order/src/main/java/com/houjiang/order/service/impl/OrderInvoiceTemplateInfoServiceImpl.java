package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.TokenUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.entity.OrderInvoiceTemplateInfoEntity;
import com.houjiang.order.mapper.OrderInvoiceTemplateInfoMapper;
import com.houjiang.order.service.OrderInvoiceTemplateInfoService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ClassName OrderInvoiceTemplateInfoServiceImpl
 * @Description  发票模版管理表
 * @Author Amos
 * @Date 2023-07-04 13:32:35
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderInvoiceTemplateInfoServiceImpl extends ServiceImpl<OrderInvoiceTemplateInfoMapper, OrderInvoiceTemplateInfoEntity>
        implements OrderInvoiceTemplateInfoService {

    /**
     * 根据传入的参数查询发票信息列表。
     *
     * @param param 包含查询参数和用户令牌的映射对象。
     * @return 返回查询结果的响应对象。
     *
     * 此方法首先通过令牌获取用户信息，如果令牌无效，则返回相应的错误响应。
     * 接着，它将用户ID添加到查询参数中，并执行分页查询。如果查询过程中发生异常，
     * 则返回系统错误的响应。正常情况下，它会返回成功的响应，其中包含查询结果的分页信息。
     */
    @Override
    public ResponseResult queryInvoiceInfoList(Map<String,Object> param) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 检查用户信息是否有效
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        try {
            // 将用户ID添加到查询参数中
            param.put("userId",userInfo.getUserId());
            // 执行分页查询
            // 分页查询数据
            IPage<OrderInvoiceTemplateInfoEntity> page = this.baseMapper.queryInvoiceInfoList(new Query<OrderInvoiceTemplateInfoEntity>().getPage(param),param);
            // 返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(page));

        } catch (Exception e) {
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
        // 返回系统错误的失败响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    @Override
    public ResponseResult insertInvoiceTmp(Map<String, Object> param) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        try {
            param.put("userId",userInfo.getUserId());
            if (param.get("invoiceTmpId") == null) {
                Integer count = this.baseMapper.queryInvoiceInfoListCount(param);
                //发票模版大于10个无法添加
                if(null !=count && count >= 10){
                    return ResponseResult.fail(BizCodeEnume.INVOICE_TEMPLATES_HAS_SUPERIOR_LIMIT);
                }
                int isDefault = (Integer) param.get("isDefault");
                //如果新插入的为默认配置，则将其他的配置isDefault置为0
                if (1 == isDefault) {
                    this.baseMapper.updateDefault(param);
                }
                this.baseMapper.insertInvoiceTmp(param);
            } else {
                this.baseMapper.updateInvoiceTmp(param);
            }
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR);

    }

    @Override
    public ResponseResult deleteInvoiceTmp(Map<String, Object> param) {
            // 从参数中获取token
            String token = (String) param.get("token");
            // 校验token的有效性
            ResponseResult tokenResult = TokenUtils.checkToken(token);
            // 如果token校验失败，则返回错误结果
            if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
                return tokenResult;
            } else {
                // 如果token校验成功，则将用户ID存入参数中
                param.put("userId", tokenResult.getData().toString());
            }
            try {
                // 根据用户ID和发票模板ID删除相应的发票模板信息
                this.baseMapper.delete(new LambdaQueryWrapper<OrderInvoiceTemplateInfoEntity>()
                        .eq(OrderInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                        .eq(OrderInvoiceTemplateInfoEntity::getInvoiceTmpId, param.get("invoiceTmpId").toString()));
                // 删除成功，则返回成功结果
                return ResponseResult.success();
            } catch (Exception e) {
                // 删除过程中发生异常，则记录日志并返回失败结果
                log.error(e.getMessage());
                return ResponseResult.fail();
            }

    }

    @Override
    public ResponseResult queryInvoiceTmp(Map<String, Object> param) {
        // 从参数中获取token，用于后续的身份验证
        String token = (String) param.get("token");

        // 验证token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);

        // 如果token验证失败，则直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token验证成功，将用户ID存入参数中，以便后续操作
            param.put("userId", tokenResult.getData().toString());
        }

        try {
            // 根据发票模板ID从数据库中查询发票模板信息
            OrderInvoiceTemplateInfoEntity invoiceTmpEntity = this.baseMapper.selectById(param.get("invoiceTmpId").toString());

            // 返回查询成功的发票模板信息
            return ResponseResult.success(invoiceTmpEntity);
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());

            // 返回查询失败的错误信息
            return ResponseResult.fail(e.getMessage());
        }


    }

    @Override
    public ResponseResult updateInvoiceTmp(Map<String, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        // 校验token的有效性
        ResponseResult tokenResult = TokenUtils.checkToken(token);
        // 如果token校验失败，则直接返回错误结果
        if (tokenResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return tokenResult;
        } else {
            // 如果token校验成功，将用户ID存入参数中
            param.put("userId", tokenResult.getData().toString());
        }
        try {
            // 根据invoiceType的值，确定invoiceHeadType的值
            if (param.get("invoiceType").equals(1)) {
                param.put("invoiceHeadType", 2);
            }
            // 将isDefault参数转换为整型
            int isDefault = Integer.valueOf(param.get("isDefault").toString());
            // 如果当前设置为默认，则将其他默认设置改为非默认
            // 如果新插入的为默认配置，则将其他的配置isDefault置为0
            if (1 == isDefault) {
                this.baseMapper.update(null, new LambdaUpdateWrapper<OrderInvoiceTemplateInfoEntity>()
                        .eq(OrderInvoiceTemplateInfoEntity::getUserId, param.get("userId").toString())
                        .set(OrderInvoiceTemplateInfoEntity::getIsDefault, 0));
            }
            // 更新发票模板信息
            this.baseMapper.updateInvoiceTmp(param);
            // 返回成功结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 记录异常信息
            log.error(e.getMessage());
            // 返回失败结果，并附带异常信息
            return ResponseResult.fail(e.getMessage());
        }


    }

    @Override
    public void insertInvoiceTmpFeign(Map<String, Object> param) {
        this.baseMapper.insertInvoiceTmpFeign(param);
    }

    @Override
    public void updateInvoiceTmpFeign(Map<String, Object> param) {
        this.baseMapper.updateInvoiceTmpFeign(param);
    }

}
