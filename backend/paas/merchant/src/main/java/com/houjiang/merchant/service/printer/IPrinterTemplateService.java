package com.houjiang.merchant.service.printer;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.printer.PrinterTemplate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateCreateValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateSearchValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateUpdateValidate;
import com.houjiang.merchant.vo.printer.PrinterTemplateDetailVo;
import com.houjiang.merchant.vo.printer.PrinterTemplateListedVo;

/**
 * 打印机模版服务接口类
 * @author Amos
 */
public interface IPrinterTemplateService {

    /**
     * 打印机模版列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<PrinterTemplateListedVo>
     */
    PageResult<PrinterTemplateListedVo> list(PageValidate pageValidate, PrinterTemplateSearchValidate searchValidate);

    /**
     * 打印机模版详情
     *
     * @author Amos
     * @param id 主键ID
     * @return PrinterTemplateDetailVo
     */
    PrinterTemplateDetailVo detail(Integer id);

    /**
     * 打印机模版新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(PrinterTemplateCreateValidate createValidate);

    /**
     * 打印机模版编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(PrinterTemplateUpdateValidate updateValidate);

    /**
     * 打印机模版删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * @notes 更新模版 打印机使用
     * @return void
     * @author Amos
     */
    void updateTemplatePrinterUse(Integer shopId);

    /**
     * 根据id返回记录
     * @param id
     * @return
     */
    PrinterTemplate getById(Integer id);
}
