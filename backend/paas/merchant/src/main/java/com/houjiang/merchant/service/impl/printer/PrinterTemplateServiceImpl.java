package com.houjiang.merchant.service.impl.printer;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.query.MPJQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.printer.Printer;
import com.houjiang.merchant.common.entity.printer.PrinterTemplate;
import com.houjiang.merchant.common.enums.PrinterEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.printer.PrinterTemplateMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.printer.IPrinterService;
import com.houjiang.merchant.service.printer.IPrinterTemplateService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateCreateValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateSearchValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateUpdateValidate;
import com.houjiang.merchant.vo.printer.PrinterTemplateDetailVo;
import com.houjiang.merchant.vo.printer.PrinterTemplateListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 打印机模版实现类
 * @author Amos
 */
@Service
public class PrinterTemplateServiceImpl implements IPrinterTemplateService {
        
    @Resource
    PrinterTemplateMapper printerTemplateMapper;
    @Resource
    IPrinterService iPrinterService;

    /**
     * 打印机模版列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<PrinterTemplateListedVo>
     */
    @Override
    public PageResult<PrinterTemplateListedVo> list(PageValidate pageValidate, PrinterTemplateSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<PrinterTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id").eq("delete_time", 0);

        printerTemplateMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "=:type:int",
            "like:name:str",
            "=:printerUse@printer_use:int",
            "=:content:str",
        });

        IPage<PrinterTemplate> iPage = printerTemplateMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<PrinterTemplateListedVo> list = new LinkedList<>();
        for(PrinterTemplate item : iPage.getRecords()) {
            PrinterTemplateListedVo vo = new PrinterTemplateListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setTypeName(this.getTypeNameAttr(item.getType()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    private String getTypeNameAttr(Integer type) {
        return PrinterEnum.getTemplateTypeTextArr(type);
    }

    /**
     * 打印机模版详情
     *
     * @author Amos
     * @param id 主键参数
     * @return PrinterTemplate
     */
    @Override
    public PrinterTemplateDetailVo detail(Integer id) {
        PrinterTemplate model = printerTemplateMapper.selectOne(
                new QueryWrapper<PrinterTemplate>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的模板信息");
        }

        PrinterTemplateDetailVo vo = new PrinterTemplateDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setContent(JSONObject.parseObject(model.getContent()));
        return vo;
    }

    /**
     * 打印机模版新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(PrinterTemplateCreateValidate createValidate) {
        PrinterTemplate model = new PrinterTemplate();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setType(createValidate.getType());
        model.setName(createValidate.getName());
        //model.setPrinterUse(createValidate.getPrinterUse());
        model.setContent(JSONObject.toJSONString(createValidate.getContent()));
        printerTemplateMapper.insert(model);
    }

    /**
     * 打印机模版编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(PrinterTemplateUpdateValidate updateValidate) {
        PrinterTemplate model = printerTemplateMapper.selectOne(
                new QueryWrapper<PrinterTemplate>()
                    .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的模板信息");
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setType(updateValidate.getType());
        model.setName(updateValidate.getName());
       // model.setPrinterUse(updateValidate.getPrinterUse());
        model.setContent(JSONObject.toJSONString(updateValidate.getContent()));
        printerTemplateMapper.updateById(model);
    }

    /**
     * 打印机模版删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        PrinterTemplate model = printerTemplateMapper.selectOne(
                new QueryWrapper<PrinterTemplate>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的模板信息");
        }

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        printerTemplateMapper.updateById(model);
        //printerTemplateMapper.delete(new QueryWrapper<PrinterTemplate>().eq("id", id));
    }

    @Override
    public void updateTemplatePrinterUse(Integer shopId) {
        List<Printer> printerList = iPrinterService.getListByShopId(shopId);
        List<Integer> templateIds = new ArrayList<Integer>();
        if (printerList.size() > 0) {
            for (Printer item : printerList) {
                JSONObject orderPrint = JSONObject.parseObject(item.getOrderPrint());
                JSONObject orderCreate = orderPrint.getJSONObject("order_create");
                JSONObject orderPay = orderPrint.getJSONObject("order_pay");
                JSONObject selfPrint = orderPrint.getJSONObject("self_print");
                templateIds.add(orderCreate.getInteger("template_id"));
                templateIds.add(orderPay.getInteger("template_id"));
                templateIds.add(selfPrint.getInteger("template_id"));
            }
            List<Integer> uniqueIds = templateIds.stream().distinct().collect(Collectors.toList());

            if (uniqueIds.size() > 0) {
                PrinterTemplate printerTemplate = new PrinterTemplate();
                printerTemplate.setPrinterUse(YesNoEnum.YES.getCode());
                printerTemplate.setUpdateTime(System.currentTimeMillis() / 1000);
                printerTemplateMapper.update(printerTemplate, new QueryWrapper<PrinterTemplate>().in("id", uniqueIds));

                PrinterTemplate printerTemplate2 = new PrinterTemplate();
                printerTemplate2.setPrinterUse(YesNoEnum.NO.getCode());
                printerTemplate2.setUpdateTime(System.currentTimeMillis() / 1000);
                printerTemplateMapper.update(printerTemplate2, new QueryWrapper<PrinterTemplate>().notIn("id", uniqueIds));
            }
        }
    }

    @Override
    public PrinterTemplate getById(Integer id) {
        return printerTemplateMapper.selectOne(new QueryWrapper<PrinterTemplate>().eq("delete_time", 0).eq("id", id));
    }
}
