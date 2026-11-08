package com.houjiang.thirdparty.enums;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.CheckPhoneUtil;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.thirdparty.dto.TencentSmsDTO;
import com.houjiang.thirdparty.utils.AliyunSmsUtils;
import com.houjiang.thirdparty.utils.MailUtil;
import com.houjiang.thirdparty.utils.TencentSmsUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 */

public enum SmsStrategy {

    /**
     * 短信策略枚举类，用于定义不同场景下的短信发送策略
     */
    CODE_A {
        /**
         * 执行发送通知的策略
         * 当企业认证审核通过时，触发此方法以通知相关人员
         *
         * @param map 包含发送通知所需信息的键值对映射
         *            例如，收件人的联系方式和要发送的消息内容
         *            此处的具体内容可能包含企业认证审核通过的相关信息
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 调用sendSms方法发送短信通知，同时传递一个空的ArrayList作为接收者的列表
            // 这里可能涉及到群发通知的逻辑，列表中可以包含多个接收者的联系方式
            sendSms(map, new ArrayList<>());

            // 打印日志，记录通知发送的执行动作
            // 此日志有助于监控通知发送的状态，以及后续的问题排查
            System.out.println("执行具体策略，发送企业认证审核通过通知");
        }
    },
    CODE_B {
        /**
         * 执行发送企业认证审核驳回通知的策略
         *
         * @param map 包含企业认证审核信息的映射，其中包含驳回原因等关键信息
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表来存储驳回原因
            List<String> list = new ArrayList<>();
            // 将驳回原因添加到列表中
            list.add(map.get("reason").toString());
            // 调用发送短信的方法，传递原始映射和包含驳回原因的列表
            sendSms(map, list);
            // 打印执行策略的信息，通知发送
            System.out.println("执行具体策略，发送企业认证审核驳回通知");
        }
    },
    CODE_C {
        /**
         * 执行发送订单审批通知的策略
         *
         * @param map 包含通知所需信息的键值对映射，如用户昵称、订单审批ID和备注
         *            使用Map集合可以灵活地处理不同来源的通知参数
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 初始化一个字符串列表，用于存储短信内容的部分
            List<String> list = (List<String>) map.get("list");
            sendSms(map, list);
            // 打印日志信息，表示已执行具体策略并发送订单审批通知
            System.out.println("执行具体策略，发送订单审批通知");
        }

    },
    CODE_D {
        /**
         * 执行具体策略，根据订单ID发送订单退款成功通知
         *
         * @param map 包含订单信息的键值对，如订单ID
         *            - "orderId": 订单ID，用于唯一标识一个订单
         * &#064;override  覆盖父类的execute方法，实现具体的操作逻辑
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表，用于存储需要处理的订单ID
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("orderId").toString());
            // 调用发送短信的方法，传递订单信息和包含订单ID的列表
            sendSms(map, list);
            // 输出执行具体策略的操作信息，表示已经发送订单退款成功通知
            System.out.println("执行具体策略，发送订单退款成功通知");
        }
    },
    CODE_E {
        /**
         * 执行发送用户申请退货通知的策略
         *
         * @param map 包含订单ID和结果的键值对映射
         *            - 键"orderId" 对应 用户的订单ID
         *            - 键"result" 对应 退货申请的结果
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表，用于存储短信内容的部分信息
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("orderId").toString());
            // 将退货申请的结果添加到列表中
            list.add(map.get("result").toString());
            // 调用发送短信的方法，传递原始映射和构造的列表作为参数
            sendSms(map, list);
            // 打印执行策略的信息，表明已发送用户申请退货的通知
            System.out.println("执行具体策略，发送用户申请退货通知");
        }

    },
    CODE_F {
        /**
         * 执行具体的策略，用于发送商家入驻申请审核结果的通知
         *
         * @param map 包含商家名称和审核结果的键值对，键为字符串，值为对象
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个字符串列表，用于存储短信内容
            List<String> list = new ArrayList<>();
            // 将商家名称添加到列表中
            list.add(map.get("shopName").toString());
            // 将审核结果添加到列表中
            list.add(map.get("result").toString());
            // 调用发送短信的方法，传递原始map和构建的列表
            sendSms(map, list);
            // 打印执行策略的信息，表明已发送商家入驻申请审核结果通知
            System.out.println("执行具体策略，发送商家入驻申请审核结果通知");
        }

    },
    CODE_G {
        /**
         * 执行具体策略，根据订单信息发送通知
         *
         * @param map 包含订单信息的键值对，如订单ID
         * &#064;override  覆盖父类的execute方法
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表，用于存储需要处理的订单ID
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("orderId").toString());
            // 调用发送短信方法，传递订单信息和包含订单ID的列表
            sendSms(map, list);
            // 输出执行策略的信息，表示已发送订单付款成功通知
            System.out.println("执行具体策略，发送订单付款成功通知");
        }
    },
    CODE_H {
        /**
         * 执行发送用户退款申请通知的具体策略
         *
         * @param map 包含订单信息的键值对地图，期望从中提取orderId
         *            - {@code map.get("orderId")} 应该返回一个可以转换为字符串的订单ID
         * @throws IllegalArgumentException 如果{@code map}不包含"orderId"或其值无法转换为字符串，可能会抛出异常
         * @throws NullPointerException 如果{@code map}为{@code null}，可能会抛出空指针异常
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个字符串列表，用于存储订单ID
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("orderId").toString());

            // 调用发送短信的方法，传递订单信息和包含订单ID的列表
            sendSms(map, list);

            // 输出执行策略的信息，表明已发送用户退款申请通知
            System.out.println("执行具体策略，发送用户退款申请通知");
        }
    },
    CODE_I {
        /**
         * 执行发送用户退货申请通知的具体策略
         *
         * @param map 包含订单信息的键值对集合，其中订单信息用于后续处理流程
         *            - "orderId" 是用于标识订单的唯一ID
         * @throws RuntimeException 如果地图中不包含必要的订单ID信息，可能导致运行时异常
         * <p>
         * 此方法首先从map中提取订单ID信息，并将其转换为字符串形式，然后将该信息添加到一个字符串列表中
         * 这是因为sendSms方法可能需要一个详细的信息列表来构造短信内容
         * 随后调用sendSms方法，将map和包含订单ID的列表作为参数传递，以便发送短信通知
         * 最后，打印一条消息，指示已执行具体策略并发送用户退货申请通知
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表，用于存储从map中提取的订单ID信息
            List<String> list = new ArrayList<>();
            // 将订单ID信息转换为字符串并添加到列表中
            list.add(map.get("orderId").toString());
            // 调用sendSms方法发送短信通知，传递原始map和包含订单ID的列表
            sendSms(map, list);
            // 打印消息，指示已执行具体策略并发送用户退货申请通知
            System.out.println("执行具体策略，发送用户退货申请通知");
        }
    },
    CODE_J {
        /**
         * 执行发送新店铺入驻待审核通知的具体策略
         *
         * @param map 包含店铺信息的键值对集合，期望从中提取店铺名称
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个字符串列表，用于存储待发送短信通知的信息
            List<String> list = new ArrayList<>();
            // 从map中提取店铺名称并添加到列表中
            list.add(map.get("shopName").toString());
            // 调用sendSms方法，发送短信通知
            sendSms(map, list);
            // 打印日志，说明当前操作的目的
            System.out.println("执行具体策略，发送新店铺入驻待审核通知");
        }
    },
    CODE_K {
        /**
         * 执行发送企业注册待审核提醒通知的策略
         *
         * @param map 包含业务名称和其他信息的键值对映射
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个字符串列表，用于存储业务名称
            List<String> list = new ArrayList<>();
            // 将业务名称从map中提取并添加到列表中
            list.add(map.get("businessName").toString());
            // 调用sendSms方法，传递原始map和包含业务名称的列表
            sendSms(map, list);
            // 输出执行策略和发送通知的操作提示
            System.out.println("执行具体策略，发送企业注册待审核提醒通知");
        }
    },
    CODE_L {
        /**
         * 执行发送通知的策略
         * 该方法根据提供的参数map发送付款凭证待审核的通知
         * 使用泛型Map作为参数是为了灵活处理不同的通知参数
         * @param map 包含通知所需信息的键值对集合
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 初始化一个字符串列表，用于存储发送通知过程中的必要信息或错误信息
            List<String> list = new ArrayList<>();
            // 调用sendSms方法发送短信通知，传递参数map和用于收集信息的列表list
            sendSms(map, list);
            // 打印通知执行的信息，表明策略已执行且通知等待审核
            System.out.println("执行具体策略，发送付款凭证待审核通知");
        }
    },
    CODE_M {
        /**
         * 执行发送售后单介入待处理通知的具体策略
         *
         * @param map 包含订单信息的键值对映射，用于获取订单ID
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个字符串列表，用于存储需要的订单ID
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("orderId").toString());
            // 调用发送短信的方法，传递订单信息和包含订单ID的列表
            sendSms(map, list);
            // 输出执行策略的信息，表示已发送售后单介入待处理通知
            System.out.println("执行具体策略，发送售后单介入待处理通知");
        }
    },
    CODE_N {
        /**
         * 执行具体策略，根据提供的参数发送通知短信
         * 此方法首先会从参数map中提取orderId，然后构造一个短信发送所需的信息列表
         * 最后，调用sendSms方法发送短信，并打印出执行策略的说明
         *
         * @param map 包含订单信息的映射表，用于构造短信内容
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 初始化一个字符串列表，用于存储短信发送所需的参数
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中，订单ID作为重要参数用于后续的短信发送
            list.add(map.get("orderId").toString());
            // 将审核不通过原因添加到列表中，作为重要参数用于后续的短信发送
            list.add(map.get("returnShopMessage").toString());
            // 调用发送短信的方法，传递原始map和构造的列表作为参数
            sendSms(map, list);
            // 打印执行策略的操作说明，表明已执行发送退款拒绝通知的操作
            System.out.println("执行具体策略，发送退款拒绝通知");
        }
    },
    CODE_O {
        /**
         * 执行具体策略，根据提供的参数发送通知短信
         * 此方法首先会从参数map中提取orderId，然后构造一个短信发送所需的信息列表
         * 最后，调用sendSms方法发送短信，并打印出执行策略的说明
         *
         * @param map 包含订单信息的映射表，用于构造短信内容
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 初始化一个字符串列表，用于存储短信发送所需的参数
            List<String> list = new ArrayList<>();
            // 将商家名称添加到列表中，商家名称作为重要参数用于后续的短信发送
            list.add(map.get("shopName").toString());
            // 将快递单号添加到列表中，快递单号作为重要参数用于后续的短信发送
            list.add(map.get("trackingNumber").toString());
            // 将快递公司名称添加到列表中，快递公司名称作为重要参数用于后续的短信发送
            list.add(map.get("expressCompany").toString());
            // 调用发送短信的方法，传递原始map和构造的列表作为参数
            sendSms(map, list);
            // 打印执行策略的操作说明，表明已执行发送商家发货通知的操作
            System.out.println("执行具体策略，发送商家发货通知");
        }
    },
    CODE_P {
        /**
         * 执行具体策略，根据提供的参数发送通知短信
         * 此方法首先会从参数map中提取orderId，然后构造一个短信发送所需的信息列表
         * 最后，调用sendSms方法发送短信，并打印出执行策略的说明
         *
         * @param map 包含订单信息的映射表，用于构造短信内容
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 创建一个列表，用于存储需要处理的订单ID
            List<String> list = new ArrayList<>();
            // 将订单ID添加到列表中
            list.add(map.get("shopName").toString());
            // 调用发送短信的方法，传递订单信息和包含订单ID的列表
            sendSms(map, list);
            // 输出执行具体策略的操作信息，表示已经发送订单退款成功通知
            System.out.println("执行具体策略，发送兑换券活动通知");
        }
    };
    /**
     * 发送短信方法
     * 根据参数判断是否为顺丰用户，并根据不同的情况调用不同的方法发送短信
     *
     * @param map  包含用户手机号、短信模板内容、是否顺丰用户及短信模板ID等信息的Map对象
     * @param list 具体用途在代码其他部分定义的列表
     */
    private static void sendSms(Map<String, Object> map, List<String> list) {
        // 获取用户手机号
        String userMobile = (String) map.get("mobile");
        // 获取短信模板内容
        String result = (String) map.get("smsTemplateContent");
        // 判断是否为顺丰用户
        String type = (String) map.get("type");
        // 获取电话区号
        String interDialingCode = null;
        if(null != map.get("interDialingCode") && StringUtils.isNotBlank(map.get("interDialingCode").toString())){
            interDialingCode = map.get("interDialingCode").toString();
        }else{
            interDialingCode = "+86";
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            if("1".equals(map.get("emailStatus").toString())){
                // 设置短信内容，具体实现方法在代码其他部分定义
                MailUtil.sendMail(userMobile, Integer.parseInt(map.get("messageTemplateId").toString()), list);
            }
        }else{
            if("1".equals(map.get("smsStatus").toString())){
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    // 设置短信内容，具体实现方法在代码其他部分定义
                    result = setContent(result, list);
                    // 调用腾讯短信发送工具类发送短信
                    TencentSmsUtils.send(userMobile, result,interDialingCode);
                } else if(type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 调用腾讯短信测试方法发送短信，适用于非顺丰用户
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile),
                            map.get("smsTemplateId").toString(), list));
                } else {
                    // 阿里云发送
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile),
                            map.get("smsTemplateId").toString(), list));
                }
            }
        }
    }

    /**
     * 替换字符串中的占位符为列表中的实际内容
     *
     * @param content 原始字符串，其中可能包含占位符{}，用于替换
     * @param list    一个字符串列表，其中的元素将用来替换占位符
     * @return 替换占位符后的字符串
     * <p>
     * 说明：
     * 该方法遍历字符串列表，将占位符{"i"}替换为列表中对应索引的字符串，
     * 直到处理完列表中的所有元素或没有匹配的占位符为止
     */
    private static String setContent(String content, List<String> list) {
        // 遍历列表中的每个元素，寻找并替换占位符
        for (int i = 1; i <= list.size(); i++) {
            // 检查内容中是否包含当前索引的占位符
            if (content.contains("{" + i + "}")) {
                // 使用列表中对应索引的元素替换占位符
                content = content.replace("{" + i + "}", list.get(i - 1));
            }
        }
        // 返回替换后的最终内容
        return content;
    }

    /**
     * TODO 补充方法说明
     * @param map TODO
     */
    public abstract void execute(Map<String, Object> map);

}
