<template>
    <el-card class="!border-none" shadow="never">
        <el-page-header
            :content="route.query.id ? '编辑模板' : '新增模板'"
            @back="$router.back()"
        />
    </el-card>
    <el-card class="!border-none mt-2" shadow="never">
        <div class="text-xl">模版信息</div>
        <el-form
            :model="formData"
            label-width="120px"
            class="w-[540px] mt-4"
            :rules="formRules"
            ref="formRef"
        >
            <el-form-item label="模板类型" prop="template_name">
                <el-radio-group v-model="formData.type">
                    <el-radio :label="0">商品订单</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="模板名称" prop="name">
                <el-input placeholder="请输入模板名称" v-model="formData.name"></el-input>
            </el-form-item>
        </el-form>
    </el-card>
    <div class="flex w-full">
        <el-card class="!border-none mt-2 flex-1" shadow="never">
            <div class="text-xl">模版编辑</div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>票头</div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_head.name_show"
                        >小票名称</el-checkbox
                    >
                    <el-input
                        placeholder="请输入小票名称"
                        v-model="formData.content.ticket_head.name"
                        class="ml-3 w-[300px]"
                    ></el-input>

                    <el-select
                        v-model="formData.content.ticket_head.name_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.ticket_head.name_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_head.mall_name_show"
                        >商城名称</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.ticket_head.mall_name_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.ticket_head.mall_name_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_head.buyer_remark_show"
                        >买家备注</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.ticket_head.buyer_remark_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.ticket_head.buyer_remark_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
            </div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>订单信息</div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_sn"
                        >订单编号</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_form"
                        >订单来源
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_goods_type"
                        >商品类型
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_pay_way"
                        >支付方式
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_status"
                        >订单状态
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_send_type"
                        >配送方式
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_create_time"
                        >下单时间
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_pay_time"
                        >付款时间
                    </el-checkbox>
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order.show_express_confirm_time"
                        >完成时间
                    </el-checkbox>
                </div>
            </div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>商品信息</div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.goods.name_show"
                        :disabled="true"
                        >商品名称</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.goods.name_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.goods.name_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.goods.num_show"
                        :disabled="true"
                        >商品数量</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.goods.code_show"
                        >编码</el-checkbox
                    >
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.goods.money_show"
                        >商品金额</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.goods.money_type"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 金额 </span>
                        </template>
                        <el-option label="售价" value="original">售价 </el-option>
                        <el-option label="实付" value="buy">实付 </el-option>
                    </el-select>
                </div>
            </div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>订单金额</div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order_money.goods_num_show"
                        >商品总件数</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order_money.money_info_show"
                        >金额明细</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order_money.discount_show"
                        >优惠明细</el-checkbox
                    >
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.order_money.real_pay_show"
                        >实付金额</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.order_money.real_pay_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.order_money.real_pay_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
            </div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>收货信息</div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.receive.contact_show"
                        >收货人姓名</el-checkbox
                    >

                    <el-select
                        v-model="formData.content.receive.contact_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.receive.contact_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.receive.mobile_show"
                        >收货人号码</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.receive.mobile_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.receive.mobile_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.receive.mobile_protect"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 脱敏 </span>
                        </template>
                        <el-option label="是" value="1"> 是</el-option>
                        <el-option label="否" value="0">否 </el-option>
                    </el-select>
                </div>
                <div class="flex mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.receive.address_show"
                        >收货人地址</el-checkbox
                    >
                    <el-select
                        v-model="formData.content.receive.address_size"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option label="大" value="big">大 </el-option>
                        <el-option label="小" value="small">小 </el-option>
                    </el-select>
                    <el-select
                        v-model="formData.content.receive.address_font"
                        placeholder="请选择"
                        class="ml-3 w-[200px]"
                    >
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option label="正常" value="normal"> 正常</el-option>
                        <el-option label="加粗" value="bold">加粗 </el-option>
                    </el-select>
                </div>
            </div>
            <!-- //TODO自提功能未完成 -->
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2" v-if="false">
                <div>自提信息</div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >门店名称</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >门店地址</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >门店联系人</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >门店联系电话</el-checkbox
                    >
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >提货人</el-checkbox
                    >
                    <el-select v-model="formData.name" placeholder="请选择" class="ml-3 w-[200px]">
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option :label="formData.name" :value="formData.name"> </el-option>
                    </el-select>
                    <el-select v-model="formData.name" placeholder="请选择" class="ml-3 w-[200px]">
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option :label="formData.name" :value="formData.name"> </el-option>
                    </el-select>
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.consignee_info.mobile"
                        >联系号码</el-checkbox
                    >
                    <el-select v-model="formData.name" placeholder="请选择" class="ml-3 w-[200px]">
                        <template #prefix>
                            <span> 字号 </span>
                        </template>
                        <el-option :label="formData.name" :value="formData.name"> </el-option>
                    </el-select>
                    <el-select v-model="formData.name" placeholder="请选择" class="ml-3 w-[200px]">
                        <template #prefix>
                            <span> 字体 </span>
                        </template>
                        <el-option :label="formData.name" :value="formData.name"> </el-option>
                    </el-select>
                    <el-select v-model="formData.name" placeholder="请选择" class="ml-3 w-[200px]">
                        <template #prefix>
                            <span> 脱敏 </span>
                        </template>
                        <el-option :label="formData.name" :value="formData.name"> </el-option>
                    </el-select>
                </div>
            </div>
            <div class="p-8 bg-[#F7F7F7] rounded-md mt-2">
                <div>底部信息</div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_bottom.mall_name_show"
                        >商城名称</el-checkbox
                    >
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_bottom.mall_mobile_show"
                        >商城联系电话</el-checkbox
                    >
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_bottom.qr_code_show"
                        >二维码</el-checkbox
                    >
                    <el-input
                        placeholder="请输入二维码链接"
                        v-model="formData.content.ticket_bottom.qr_code_link"
                        class="ml-3 w-[300px]"
                    ></el-input>
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_bottom.content_show"
                        >自定义内容</el-checkbox
                    >
                    <el-input
                        placeholder="请输入自定义内容"
                        v-model="formData.content.ticket_bottom.content"
                        class="ml-3 w-[300px]"
                    ></el-input>
                </div>
                <div class="mt-3">
                    <el-checkbox
                        true-label="1"
                        false-label="0"
                        v-model="formData.content.ticket_bottom.print_time"
                        >小票打印时间</el-checkbox
                    >
                </div>
            </div>
        </el-card>
        <el-card class="!border-none mt-2 flex-1 ml-2" shadow="never">
            <div>
                <div class="m-4 text-2xl">预览</div>
                <div class="template">
                    <div
                        v-show="formData.content.ticket_head.name_show == 1"
                        class="line"
                        :class="{
                            'font-bold':
                                formData.content.ticket_head.name_font == 'bold' ? true : false,
                            'text-2xl':
                                formData.content.ticket_head.name_size == 'big' ? true : false
                        }"
                    >
                        {{ formData.content.ticket_head.name || '小票名称' }}
                    </div>
                    <div
                        class="line"
                        v-show="formData.content.ticket_head.mall_name_show == 1"
                        :class="{
                            'font-bold':
                                formData.content.ticket_head.mall_name_font == 'bold'
                                    ? true
                                    : false,
                            'text-2xl':
                                formData.content.ticket_head.mall_name_size == 'big' ? true : false
                        }"
                    >
                        商城名称
                    </div>
                    <div
                        class="line"
                        v-show="formData.content.ticket_head.buyer_remark_show == 1"
                        :class="{
                            'font-bold':
                                formData.content.ticket_head.buyer_remark_font == 'bold'
                                    ? true
                                    : false,
                            'text-2xl':
                                formData.content.ticket_head.buyer_remark_size == 'big'
                                    ? true
                                    : false
                        }"
                    >
                        买家备注：我的礼物要包装好一点，尽量早一点发货哦，谢谢啦～
                    </div>
                    <div class="flex flex-col line template_order text_size">
                        <div v-if="formData.content.order.show_sn == 1">
                            订单编号：MWE20012345738754657834
                        </div>
                        <div v-if="formData.content.order.show_form == 1">订单来源：微信小程序</div>
                        <div v-if="formData.content.order.show_goods_type == 1">
                            商品类型：实体商品
                        </div>
                        <div v-if="formData.content.order.show_pay_way == 1">
                            支付方式：微信支付
                        </div>
                        <div v-if="formData.content.order.show_status == 1">订单状态：已完成</div>
                        <div v-if="formData.content.order.show_send_type == 1">
                            配送方式：快递发货
                        </div>
                        <div v-if="formData.content.order.show_create_time == 1">
                            下单时间：2024-12-12 12:16
                        </div>
                        <div v-if="formData.content.order.show_pay_time == 1">
                            付款时间：2024-12-12 12:16
                        </div>
                        <div v-if="formData.content.order.show_express_confirm_time == 1">
                            完成时间：2024-12-12 12:16
                        </div>
                    </div>
                    <div class="line text_size">
                        <div class="flex justify-between">
                            <div v-if="formData.content.goods.name_show == 1">商品名称</div>
                            <div>数量</div>
                            <div v-if="formData.content.goods.money_show == 1">金额</div>
                        </div>
                        <div class="flex justify-between">
                            <div>
                                <div
                                    v-if="formData.content.goods.name_show"
                                    style="text-align: left"
                                    :class="{
                                        'font-bold':
                                            formData.content.goods.name_font == 'bold'
                                                ? true
                                                : false,
                                        'text-2xl':
                                            formData.content.goods.name_size == 'big' ? true : false
                                    }"
                                >
                                    生椰拿铁
                                </div>
                                <div
                                    style="text-align: left"
                                    v-if="formData.content.goods.code_show == 1"
                                >
                                    编码：20241212
                                </div>
                            </div>
                            <div>1</div>
                            <div v-if="formData.content.goods.money_show == 1">￥16.00</div>
                        </div>
                        <div class="flex justify-between">
                            <div>
                                <div
                                    v-if="formData.content.goods.name_show == 1"
                                    :class="{
                                        'font-bold':
                                            formData.content.goods.name_font == 'bold'
                                                ? true
                                                : false,
                                        'text-2xl':
                                            formData.content.goods.name_size == 'big' ? true : false
                                    }"
                                    style="text-align: left"
                                >
                                    生椰拿铁
                                </div>
                                <div
                                    style="text-align: left"
                                    v-if="formData.content.goods.code_show == 1"
                                >
                                    编码：20241212
                                </div>
                            </div>
                            <div>1</div>
                            <div v-if="formData.content.goods.money_show == 1">￥16.00</div>
                        </div>
                    </div>
                    <div class="line text_size">
                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.goods_num_show == 1"
                        >
                            <div>商品件数</div>
                            <div>7</div>
                        </div>
                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.money_info_show == 1"
                        >
                            <div>商品总价</div>
                            <div>￥700.00</div>
                        </div>
                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.money_info_show == 1"
                        >
                            <div>运费</div>
                            <div>￥6.00</div>
                        </div>
                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.discount_show == 1"
                        >
                            <div>店铺优化</div>
                            <div>-￥10.00</div>
                        </div>

                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.discount_show == 1"
                        >
                            <div>会员折扣</div>
                            <div>-￥20.00</div>
                        </div>
                        <div
                            class="flex justify-between"
                            v-if="formData.content.order_money.real_pay_show == 1"
                            :class="{
                                'font-bold':
                                    formData.content.order_money.real_pay_font == 'bold'
                                        ? true
                                        : false,
                                'text-2xl':
                                    formData.content.order_money.real_pay_size == 'big'
                                        ? true
                                        : false
                            }"
                        >
                            <div>实付金额</div>
                            <div>￥38.00</div>
                        </div>
                    </div>
                    <div class="flex flex-col line template_order text_size">
                        <div
                            v-if="formData.content.receive.contact_show == 1"
                            :class="{
                                'font-bold':
                                    formData.content.receive.contact_font == 'bold' ? true : false,
                                'text-2xl':
                                    formData.content.receive.contact_size == 'big' ? true : false
                            }"
                        >
                            收货人：李富贵
                        </div>
                        <div
                            :class="{
                                'font-bold':
                                    formData.content.receive.mobile_font == 'bold' ? true : false,
                                'text-2xl':
                                    formData.content.receive.mobile_size == 'big' ? true : false
                            }"
                            v-if="
                                formData.content.receive.mobile_show == 1 &&
                                formData.content.receive.mobile_protect
                            "
                        >
                            联系方式：130****1300
                        </div>
                        <div
                            :class="{
                                'font-bold':
                                    formData.content.receive.mobile_font == 'bold' ? true : false,
                                'text-2xl':
                                    formData.content.receive.mobile_size == 'big' ? true : false
                            }"
                            v-if="
                                formData.content.receive.mobile_show == 1 &&
                                !formData.content.receive.mobile_protect
                            "
                        >
                            联系方式：13013001300
                        </div>
                        <div
                            v-if="formData.content.receive.address_show == 1"
                            :class="{
                                'font-bold':
                                    formData.content.receive.address_font == 'bold' ? true : false,
                                'text-2xl':
                                    formData.content.receive.address_size == 'big' ? true : false
                            }"
                        >
                            收货地址：广东省广州市天河区珠江新城临江大道39号B座20607
                        </div>
                    </div>
                    <!-- TODO自提功能未完成 -->
                    <div class="flex flex-col line template_order text_size" v-if="false">
                        <div>门店信息</div>
                        <div v-show="formData.selffetch_shop.shop_name == 1">美宜佳</div>
                        <div v-show="formData.selffetch_shop.contacts == 1">联系人：李先生</div>
                        <div v-show="formData.selffetch_shop.shop_address == 1">
                            门店地址：广东省 广州市 天河区 花城大道888号
                        </div>
                    </div>
                    <div class="flex flex-col line template_order text_size" v-if="false">
                        <div>买家信息</div>
                        <div v-show="formData.consignee_info.name">收货人：陈小姐</div>
                        <div v-show="formData.consignee_info.mobile">联系方式：130****1619</div>
                        <div v-show="formData.consignee_info.address">
                            收货地址：广东省 广州市 天河区 花城大道01号
                        </div>
                    </div>

                    <div class="line flex justify-center flex-col items-center">
                        <div class="" v-if="formData.content.ticket_bottom.mall_name_show == 1">
                            商城名称
                        </div>
                        <div
                            class="mt-2"
                            v-if="formData.content.ticket_bottom.mall_mobile_show == 1"
                        >
                            010-1234567
                        </div>
                        <img
                            src="@/assets/images/qr_code.png"
                            alt=""
                            class="template_img mt-2"
                            v-if="formData.content.ticket_bottom.qr_code_show == 1"
                        />
                        <div class="mt-2" v-if="formData.content.ticket_bottom.content_show == 1">
                            {{ formData.content.ticket_bottom.content }}
                        </div>
                        <div class="mt-2" v-if="formData.content.ticket_bottom.print_time">
                            打印时间：2024-12-12 16:30
                        </div>
                    </div>
                </div>
            </div>
        </el-card>
    </div>
    <footer-btns>
        <el-button type="primary" @click="handlesave">保存</el-button>
    </footer-btns>
</template>
<script lang="ts" setup>
// import { addtemplate, templatedetail, templateedit } from '@/api/application'
import { apiTemAdd, apiTemEdit, getTemDetial } from '@/api/marketing/ticket'
import feedback from '@/utils/feedback'
import { reactive } from 'vue'
const route = useRoute()
const router = useRouter()

const formData = reactive<any>({
    id: '',
    type: 0,
    name: '',
    content: {
        ticket_head: {
            name_show: '1',
            name: '小票名称',
            name_size: 'small',
            name_font: 'normal',
            mall_name_show: '1',
            mall_name: '这是商城名称',
            mall_name_size: 'big',
            mall_name_font: 'bold',
            buyer_remark_show: '1',
            buyer_remark_size: 'small',
            buyer_remark_font: 'normal'
        },
        order: {
            show_sn: '1',
            show_form: '1',
            show_goods_type: '1',
            show_pay_way: '1',
            show_status: '1',
            show_send_type: '1',
            show_create_time: '1',
            show_pay_time: '0',
            show_express_confirm_time: '0'
        },
        goods: {
            name_show: '1',
            name_size: 'small',
            name_font: 'normal',
            num_show: '1',
            code_show: '1',
            money_show: '1',
            money_type: 'original'
        },
        order_money: {
            goods_num_show: '1',
            money_info_show: '1',
            discount_show: '1',
            real_pay_show: '1',
            real_pay_size: 'big',
            real_pay_font: 'bold'
        },
        receive: {
            contact_show: '1',
            contact_size: 'big',
            contact_font: 'bold',
            mobile_show: '1',
            mobile_size: 'big',
            mobile_font: 'bold',
            mobile_protect: '1',
            address_show: '1',
            address_size: 'big',
            address_font: 'bold'
        },
        ticket_bottom: {
            mall_name_show: '1',
            mall_mobile_show: '1',
            qr_code_show: '0',
            qr_code_link: '',
            content_show: '1',
            content: '谢谢惠顾，欢迎下次光临',
            print_time: '1'
        }
    }
})
const formRules = {
    name: [
        {
            required: true,
            message: '请输入模板名称',
            trigger: ['blur']
        }
    ]
}
const handlesave = async () => {
    if (!route.query.id) await apiTemAdd(formData)
    else await apiTemEdit(formData)
    router.back()
}

const getData = async () => {
    if (!route.query.id) return
    const data = await getTemDetial({ id: route.query.id })
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })
}
getData()
</script>
<style scoped lang="scss">
.template {
    width: 358px;
    // height: 872px;
    border: 1px solid rgba(187, 187, 187, 1);
    text-align: center;
    margin-left: 250px;
    padding: 20px 0;
    .line {
        padding: 12px;
        border-bottom: 1px dashed rgba(224, 224, 224, 1);
    }
    &_order {
        text-align: left;
    }
    .text_size {
        font-size: 12px;
    }
    &_img {
        width: 108px;
        height: 108px;
    }
}
</style>
