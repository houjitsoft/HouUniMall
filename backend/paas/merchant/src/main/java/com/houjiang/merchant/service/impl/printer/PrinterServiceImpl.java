package com.houjiang.merchant.service.impl.printer;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.printer.Printer;
import com.houjiang.merchant.common.entity.printer.PrinterTemplate;
import com.houjiang.merchant.common.enums.PrinterEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.printer.PrinterMapper;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.printer.IPrinterService;
import com.houjiang.merchant.service.printer.IPrinterTemplateService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.*;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.printer.PrinterDetailVo;
import com.houjiang.merchant.vo.printer.PrinterListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 打印机实现类
 * @author Amos
 */
@Service
public class PrinterServiceImpl implements IPrinterService {
        
    @Resource
    PrinterMapper printerMapper;
    @Resource
    IPrinterTemplateService iPrinterTemplateService;

    /**
     * 打印机列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<PrinterListedVo>
     */
    @Override
    public PageResult<PrinterListedVo> list(PageValidate pageValidate, PrinterSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<Printer> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id").eq("delete_time", 0);

        printerMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "like:name:str",
            "=:status:int",
            "=:brand:int",
            "=:orderPrint@order_print:str",
            "=:content:str",
        });

        IPage<Printer> iPage = printerMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<PrinterListedVo> list = new LinkedList<>();
        for(Printer item : iPage.getRecords()) {
            PrinterListedVo vo = new PrinterListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setContent(JSONObject.parseObject(item.getContent()));
            vo.setOrderPrint(JSONObject.parseObject(item.getOrderPrint()));
            vo.setBrandText(PrinterEnum.getBrandTextArr(item.getBrand()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 打印机详情
     *
     * @author Amos
     * @param id 主键参数
     * @return Printer
     */
    @Override
    public PrinterDetailVo detail(Integer id) {
        Printer model = printerMapper.selectOne(
                new QueryWrapper<Printer>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的打印机信息");
        }

        PrinterDetailVo vo = new PrinterDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setOrderPrint(JSONObject.parseObject(model.getOrderPrint()));
        vo.setContent(JSONObject.parseObject(model.getContent()));
        return vo;
    }

    /**
     * 打印机新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    @Transactional
    public void add(PrinterCreateValidate createValidate) {
        Printer model = new Printer();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setBrand(createValidate.getBrand());
        model.setOrderPrint(JSONObject.toJSONString(createValidate.getOrderPrint()));
        model.setContent(JSONObject.toJSONString(createValidate.getContent()));
        printerMapper.insert(model);

        iPrinterTemplateService.updateTemplatePrinterUse(model.getShopId());
    }

    /**
     * 打印机编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    @Transactional
    public void edit(PrinterUpdateValidate updateValidate) {
        Printer model = printerMapper.selectOne(
                new QueryWrapper<Printer>()
                    .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的打印机信息");
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setBrand(updateValidate.getBrand());
        model.setOrderPrint(JSONObject.toJSONString(updateValidate.getOrderPrint()));
        model.setContent(JSONObject.toJSONString(updateValidate.getContent()));
        printerMapper.updateById(model);

        iPrinterTemplateService.updateTemplatePrinterUse(model.getShopId());
    }

    /**
     * 打印机删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    @Transactional
    public void del(Integer id) {
        Printer model = printerMapper.selectOne(
                new QueryWrapper<Printer>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的打印机信息");
        }

        printerMapper.delete(new QueryWrapper<Printer>().eq("id", id));
        iPrinterTemplateService.updateTemplatePrinterUse(MerchantThreadLocal.getShopId());
    }

    @Override
    public void setStatus(PrinterSetStatusValidate validate) {
       PrinterDetailVo vo = this.detail(validate.getId());
       Printer printer = new Printer();
       printer.setId(vo.getId());
       printer.setStatus(validate.getStatus());
       printerMapper.updateById(printer);
    }

    @Override
    public List<Printer> getListByShopId(Integer shopId) {
        return printerMapper.selectList(new QueryWrapper<Printer>().eq("shop_id", shopId).eq("delete_time", 0));
    }

    @Override
    public void printTest(PrinterTestValidate testValidate) {
        Boolean sceneCheck = false;
        List<String> scenes = new ArrayList<String>();
        scenes.add("order_create");
        scenes.add("order_pay");
        scenes.add("self_print");
        JSONObject order = new JSONObject();
        order.put("pay_is", 1);
        order.put("buyer_remark", "测试打印备注");
        order.put("sn", ToolUtils.randomInt(12));
        order.put("from_text", "微信小程序");
        order.put("pay_way_text", "微信支付");
        order.put("status_text", "待发货");
        order.put("delivery_type_text", "快递发货");
        order.put("create_time", TimeUtils.timestampToDate(System.currentTimeMillis() / 1000));
        order.put("pay_time", TimeUtils.timestampToDate(System.currentTimeMillis() / 1000));
        order.put("express_confirm_time", TimeUtils.timestampToDate(System.currentTimeMillis() / 1000));
        order.put("total_goods_num", 1);
        order.put("goods_money", 0.01);
        order.put("express_money", 0);
        order.put("coupon_money", 0);
        order.put("discount_money", 0);
        order.put("need_pay_money", 0.01);
        order.put("address_contact", "测试联系人");
        order.put("address_mobile", "13800002222");
        order.put("address_content", "测试联系地址");
        JSONArray goods = new JSONArray();
        goods.add(new JSONObject() {{
            put("goods_original_price", 0.01);
            put("goods_price", 0.01);
            put("goods_sku_value", new JSONArray());
            put("goods_name", 0.01);
            put("goods_original_price", "测试打印商品");
            put("goods_num", 1);
            put("goods_code", ToolUtils.randomString(6));
        }});
        order.put("goods", goods);
        order.put("shop", new JSONObject() {{
            put("mobile", new JSONArray() {{
                add("13900002222");
            }});
        }});

        JSONObject moth = testValidate.getContent().getJSONObject("moth");
        testValidate.getOrderPrint().forEach((key, val) -> {
            JSONObject sceneInfo = testValidate.getOrderPrint().getJSONObject(key);
            //参与打印
            if (sceneInfo.getInteger("status").equals(YesNoEnum.YES.getCode())) {
                this.orderMoth(order, moth, sceneInfo);
            }
        });
    }

    @Override
    public void printOrder(OrderDetailVo order, String scene) {
        List<Printer> printers = printerMapper.selectList(new QueryWrapper<Printer>().eq("shop_id", order.getShopId()).eq("status", YesNoEnum.YES.getCode()).eq("delete_time", 0));
        if (printers.size() > 0) {
            for (Printer printer : printers) {
                JSONObject sceneInfo = JSONObject.parseObject(printer.getOrderPrint()).getJSONObject(scene);
                JSONObject moth = JSONObject.parseObject(printer.getContent()).getJSONObject("moth");
                //参与打印
                if (sceneInfo.getInteger("status").equals(YesNoEnum.YES.getCode())) {
                    this.orderMoth(ToolUtils.convertKeysToSnakeCase(JSONObject.parseObject(JSONObject.toJSONString(order))), moth, sceneInfo);
                }
            }
        } else {
            throw new OperateException("未找到可用打印机");
        }
    }

    /**
     * @notes 飞蛾打印
     * @param $order
     * @param $config
     * @param $template_id
     * @return void
     * @author Amos
     */
    private void orderMoth(JSONObject order, JSONObject config, JSONObject sceneInfo) {
        PrinterTemplate template = iPrinterTemplateService.getById(sceneInfo.getInteger("template_id"));
        if (StringUtils.isNull(template)) {
            throw new OperateException("找不到打印的模板");
        }
        JSONObject c = JSONObject.parseObject(template.getContent());
        AtomicReference<String> content = new AtomicReference<>("");
        if (StringUtils.isNotNull(c.getJSONObject("ticket_head"))) {
            JSONObject ticketHead = c.getJSONObject("ticket_head");
            if (StringUtils.isNotNull(ticketHead.getInteger("name_show")) && ticketHead.getInteger("name_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "--------------------------------<BR>");
                String name = "<C>" + ticketHead.getString("name") + "</C>";
                name = this.getMothSizeLabel(name, ticketHead.getString("name_size"));
                name = this.getMothFontLabel(name, ticketHead.getString("name_font"));
                content.set(content.get() + name + "<BR>");
            }

            if (StringUtils.isNotNull(ticketHead.getInteger("mall_name_show")) && ticketHead.getInteger("mall_name_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "--------------------------------<BR>");
                String name = "<C>" + ConfigUtils.get("website", "name", "商城名称") + "</C>";
                name = this.getMothSizeLabel(name, ticketHead.getString("mall_name_size"));
                name = this.getMothFontLabel(name, ticketHead.getString("mall_name_font"));
                content.set(content.get() + name + "<BR>");
            }

            if (StringUtils.isNotNull(ticketHead.getInteger("buyer_remark_show")) && ticketHead.getInteger("buyer_remark_show").equals(YesNoEnum.YES.getCode())) {
                //content += "'--------------------------------<BR>";
                content.set(content.get() + "--------------------------------<BR>");
                String name = "买家备注：" + order.getString("buyer_remark");
                name = this.getMothSizeLabel(name, ticketHead.getString("buyer_remark_size"));
                name = this.getMothFontLabel(name, ticketHead.getString("buyer_remark_font"));
                content.set(content.get() + name + "<BR>");
            }
        }
        if (StringUtils.isNotNull(c.getJSONObject("order"))) {
            JSONObject cOrder = c.getJSONObject("order");
            content.set(content.get() + "--------------------------------<BR>");
            if (StringUtils.isNotNull(cOrder.getInteger("show_sn")) && cOrder.getInteger("show_sn").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "订单编号：" + order.getString("sn") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_form")) && cOrder.getInteger("show_form").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "订单来源：" + order.getString("from_text") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_goods_type")) && cOrder.getInteger("show_goods_type").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "商品类型：实体商品" + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_pay_way")) && cOrder.getInteger("show_pay_way").equals(YesNoEnum.YES.getCode())) {
                String payWayText = order.getInteger("pay_is").equals(YesNoEnum.YES.getCode()) ?  order.getString("pay_way_text") : "--";
                content.set(content.get() + "支付方式：" + payWayText + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_status")) && cOrder.getInteger("show_status").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "订单状态：" + order.getString("status_text") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_send_type")) && cOrder.getInteger("show_send_type").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "配送方式：" + order.getString("delivery_type_text") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_create_time")) && cOrder.getInteger("show_create_time").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "下单时间：" + order.getString("create_time") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_pay_time")) && cOrder.getInteger("show_pay_time").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "付款时间：" + order.getString("pay_time") + "<BR>");
            }
            if (StringUtils.isNotNull(cOrder.getInteger("show_express_confirm_time")) && cOrder.getInteger("show_express_confirm_time").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "完成时间：" + order.getString("express_confirm_time") + "<BR>");
            }
        }
        if (StringUtils.isNotNull(c.getJSONObject("goods"))) {
            JSONObject cGoods = c.getJSONObject("goods");
            content.set(content.get() +  "--------------------------------<BR>");
            content.set(content.get() +  "商品名称          数量   金额<BR>");
            order.getJSONArray("goods").forEach((val) -> {
                JSONObject orderGoods = JSONObject.parseObject(JSONObject.toJSONString(val));
                //价格
                String orderGoodsPrice = "--";
                if (StringUtils.isNotNull(cGoods.getInteger("money_show")) && cGoods.getInteger("money_show").equals(YesNoEnum.YES.getCode())) {
                    orderGoodsPrice = StringUtils.isNotNull(cGoods.getString("money_type")) && cGoods.getString("money_type").equals("buy") ? orderGoods.getString("goods_price") : orderGoods.getString("goods_original_price");
                }
                // 商品名称
                String skuValue = StringUtils.isNotNull(orderGoods.getList("goods_sku_value", String.class)) ? "【" + StringUtils.join(orderGoods.getList("goods_sku_value", String.class), "，") + "】" : "";
                List<String> goodsNameArr = ToolUtils.mbStrSplit(orderGoods.getString("goods_name") + skuValue, cGoods.getString("name_size").equals("big") ? 4 : 5);
                Integer ko = 0;
                for (String goodsNameInfo : goodsNameArr) {
                    goodsNameInfo = this.getGoodsNameInfo(goodsNameInfo, orderGoods.getInteger("goods_num"), orderGoodsPrice, cGoods, ko);
                    ko++;
                    content.set(content.get() +  goodsNameInfo + "<BR>");
                }

                if (StringUtils.isNotNull(cGoods.getInteger("code_show")) && cGoods.getInteger("code_show").equals(YesNoEnum.YES.getCode())) {
                    content.set(content.get() +  "商品编码：" + orderGoods.getString("goods_code") + "<BR>");
                }
            });
            if ( content.get().endsWith("<BR>") == false) {
                content.set(content.get() + "<BR>");
            }
        }
        if (StringUtils.isNotNull(c.getJSONObject("order_money"))) {
            content.set(content.get() + "--------------------------------<BR>");
            JSONObject cOrderMoney = c.getJSONObject("order_money");
            if (StringUtils.isNotNull(cOrderMoney.getInteger("goods_num_show")) && cOrderMoney.getInteger("goods_num_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "商品件数             " + order.getInteger("total_goods_num") + "<BR>");
            }

            if (StringUtils.isNotNull(cOrderMoney.getInteger("money_info_show")) && cOrderMoney.getInteger("money_info_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "商品总价             " + order.getBigDecimal("goods_money") + "<BR>");
                content.set(content.get() + "运费                 " + order.getBigDecimal("express_money") + "<BR>");
            }

            if (StringUtils.isNotNull(cOrderMoney.getInteger("discount_show")) && cOrderMoney.getInteger("discount_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "店铺优惠             " + order.getBigDecimal("coupon_money") + "<BR>");
                content.set(content.get() + "会员折扣             " + order.getBigDecimal("discount_money") + "<BR>");
            }

            if (StringUtils.isNotNull(cOrderMoney.getInteger("real_pay_show")) && cOrderMoney.getInteger("real_pay_show").equals(YesNoEnum.YES.getCode())) {
                String payMoney = "实付金额             " + order.getBigDecimal("need_pay_money");
                payMoney = this.getMothSizeLabel(payMoney, cOrderMoney.getString("real_pay_size"));
                payMoney = this.getMothFontLabel(payMoney, cOrderMoney.getString("real_pay_font"));
                content.set(content.get() + payMoney + "<BR>");
            }
        }
        if (StringUtils.isNotNull(c.getJSONObject("receive"))) {
            content.set(content.get() + "--------------------------------<BR>");
            JSONObject cReceive = c.getJSONObject("receive");
            if (StringUtils.isNotNull(cReceive.getInteger("contact_show")) && cReceive.getInteger("contact_show").equals(YesNoEnum.YES.getCode())) {
                String contact = "收货人：" + order.getString("address_contact");
                contact = this.getMothSizeLabel(contact, cReceive.getString("contact_size"));
                contact = this.getMothFontLabel(contact, cReceive.getString("contact_font"));
                content.set(content.get() + contact + "<BR>");
            }

            if (StringUtils.isNotNull(cReceive.getInteger("mobile_show")) && cReceive.getInteger("mobile_show").equals(YesNoEnum.YES.getCode())) {
                String mobileStr = cReceive.getInteger("mobile_protect").equals(YesNoEnum.YES.getCode()) ? StringUtils.substrReplace(order.getString("address_mobile"), "****", 3, 4) : order.getString("address_mobile");
                String mobile = "联系方式：" + mobileStr;
                mobile = this.getMothSizeLabel(mobile, cReceive.getString("mobile_size"));
                mobile = this.getMothFontLabel(mobile, cReceive.getString("mobile_font"));
                content.set(content.get() + mobile + "<BR>");
            }

            if (StringUtils.isNotNull(cReceive.getInteger("address_show")) && cReceive.getInteger("address_show").equals(YesNoEnum.YES.getCode())) {
                String address = "收货地址：" + order.getString("address_content");
                address = this.getMothSizeLabel(address, cReceive.getString("address_size"));
                address = this.getMothFontLabel(address, cReceive.getString("address_font"));
                content.set(content.get() + address + "<BR>");
            }
        }
        if (StringUtils.isNotNull(c.getJSONObject("ticket_bottom"))) {
            content.set(content.get() + "--------------------------------<BR>");
            JSONObject cTicketBottom = c.getJSONObject("ticket_bottom");
            if (StringUtils.isNotNull(cTicketBottom.getInteger("mall_name_show")) && cTicketBottom.getInteger("mall_name_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "<C>" + ConfigUtils.get("website", "name", "商城名称") + "</C>");
            }
            if (StringUtils.isNotNull(cTicketBottom.getInteger("mall_mobile_show")) && cTicketBottom.getInteger("mall_mobile_show").equals(YesNoEnum.YES.getCode())) {
                String mobile = "";
                if (StringUtils.isNotNull(order.getJSONObject("shop"))) {
                    mobile = order.getJSONObject("shop").getList("mobile", String.class).get(0);
                }
                if (StringUtils.isNotNull(order.getJSONObject("shop_info"))) {
                    mobile = order.getJSONObject("shop_info").getList("mobile", String.class).get(0);
                }
                content.set(content.get() + "<C>" + mobile + "</C>");
            }
            if (StringUtils.isNotNull(cTicketBottom.getInteger("qr_code_show")) && cTicketBottom.getInteger("qr_code_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "<C>" + cTicketBottom.getString("qr_code_link") + "</C>");
            }
            if (StringUtils.isNotNull(cTicketBottom.getInteger("content_show")) && cTicketBottom.getInteger("content_show").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "<C>" + cTicketBottom.getString("content") + "</C>");
            }
            if (StringUtils.isNotNull(cTicketBottom.getInteger("print_time")) && cTicketBottom.getInteger("print_time").equals(YesNoEnum.YES.getCode())) {
                content.set(content.get() + "<C>" + TimeUtils.timestampToDate(System.currentTimeMillis() / 1000) + "</C>");
            }
        }

        PrinterUtils.printMsg(config, content, sceneInfo.getInteger("nums"));
    }

    private String getGoodsNameInfo(String name, Integer num, String price, JSONObject goods, Integer key) {
        String nameNew = this.getMothSizeLabel(name, goods.getString("name_size"));
        nameNew = this.getMothFontLabel(nameNew, goods.getString("name_font"));
        if (key.equals(0) == false) {
            return nameNew;
        } else {
            if (goods.getString("name_size").equals("big")) {
                return StringUtils.formatString(nameNew, num.toString(), price, 10);
            } else {
                return StringUtils.formatString(nameNew, num.toString(), price, 17);
            }
        }
    }

    private String getMothFontLabel(String name, String nameFont) {
        if (nameFont.equals("normal")) {
            name = "<BOLD>" + name + "</BOLD>";
        }
        return name;
    }

    private String getMothSizeLabel(String name, String nameSize) {
        if (nameSize.equals("big")) {
            name = "<B>" + name + "</B>";
        }
        return name;
    }
}
