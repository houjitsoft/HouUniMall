package com.houjiang.merchant.service.printer;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.printer.Printer;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.*;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.printer.PrinterDetailVo;
import com.houjiang.merchant.vo.printer.PrinterListedVo;

import java.util.List;

/**
 * 打印机服务接口类
 * @author Amos
 */
public interface IPrinterService {

    /**
     * 打印机列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<PrinterListedVo>
     */
    PageResult<PrinterListedVo> list(PageValidate pageValidate, PrinterSearchValidate searchValidate);

    /**
     * 打印机详情
     *
     * @author Amos
     * @param id 主键ID
     * @return PrinterDetailVo
     */
    PrinterDetailVo detail(Integer id);

    /**
     * 打印机新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(PrinterCreateValidate createValidate);

    /**
     * 打印机编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(PrinterUpdateValidate updateValidate);

    /**
     * 打印机删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 编辑状态
     * @param validate
     */
    void setStatus(PrinterSetStatusValidate validate);

    /**
     * 根据店铺DI返回列表
     * @param shopId
     * @return
     */
    List<Printer> getListByShopId(Integer shopId);

    /**
     * @notes 测试打印
     * @return bool
     * @author Amos
     */

    /**
     * TODO 补充方法说明
     * @param testValidate TODO
     */
    void printTest(PrinterTestValidate testValidate);

    /**
     * 订单打印
     * @param detail
     * @param selfPrint
     */
    void printOrder(OrderDetailVo order, String scene);
}
