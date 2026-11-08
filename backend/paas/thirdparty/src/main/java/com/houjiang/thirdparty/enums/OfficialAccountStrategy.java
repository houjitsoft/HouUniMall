package com.houjiang.thirdparty.enums;

import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.thirdparty.dto.TemplateDataDTO;
import com.houjiang.thirdparty.utils.WeChatUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */

public enum OfficialAccountStrategy {

    /**
     * 公众号模板消息策略枚举类，用于定义不同场景下的公众号模板消息发送策略
     */
    CODE_A {
        /**
         * 执行发送订阅消息的任务
         * @param map 包含发送消息所需参数的映射，包括官方账号OpenID和模板ID等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取访问令牌，用于后续的微信接口调用
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数映射中获取官方账号OpenID和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();

            // 如果访问令牌获取成功，则继续构造和发送消息
            if (accessToken != null) {
                // 初始化小程序信息，包括appid和页面路径
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                //miniprogram.put("pagepath", "/salesperson/index/index"); // 页面路径暂未设置
                // 构造消息数据，包括业务名称和当前时间
                Map<String, TemplateDataDTO> data = new HashMap<>(3);
                data.put("thing1", new TemplateDataDTO(map.get("businessName").toString()));
                data.put("time5", new TemplateDataDTO(DateUtils.getCNTime()));
                // 调用工具类方法，发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印日志，表明通知发送成功
            System.out.println("执行具体策略，发送企业认证审核通过通知");
        }

    },
    CODE_B {
        /**
         * 执行发送模板消息的任务
         * @param map 包含执行任务所需参数的映射，包括officialAccountOpenId和templateId以及businessName
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数中提取公众号OpenID和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌有效，则准备发送模板消息
            if (accessToken != null) {
                // 初始化小程序信息，用于模板消息中的跳转设置
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                //miniprogram.put("pagepath", "/salesperson/index/index"); // 页面路径，注释掉可能是未最终确定
                // 准备模板消息中的动态数据部分
                Map<String, TemplateDataDTO> data = new HashMap<>(3);
                data.put("thing2", new TemplateDataDTO(map.get("businessName").toString()));
                data.put("time2", new TemplateDataDTO(DateUtils.getCNTime()));
                // 调用工具类方法发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印任务执行的说明，这里可能是为了调试或日志记录目的
            System.out.println("执行具体策略，发送企业认证审核驳回通知");
        }

    },
    CODE_C {
        /**
         * 执行发送模板消息的方法
         * 根据传入的参数，向指定的微信用户发送模板消息通知
         *
         * @param map 包含发送消息所需参数的映射，如官方账号OpenId、模板ID、订单ID等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数映射中获取官方账号OpenId、模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌不为空
            if (accessToken != null) {
                // 初始化小程序信息，用于模板消息中的小程序跳转
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                // miniprogram.put("pagepath", "/salesperson/index/index"); // 设置小程序页面路径，此处代码被注释表示当前未使用该功能
                // 初始化模板消息数据内容
                Map<String, TemplateDataDTO> data = new HashMap<>(3);
                data.put("character_string2", new TemplateDataDTO(map.get("orderId").toString()));
                data.put("thing8", new TemplateDataDTO(map.get("status").toString()));
                data.put("time11", new TemplateDataDTO(DateUtils.getCNTime()));
                // 调用工具类方法，发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印日志，表示消息发送操作已完成
            System.out.println("执行具体策略，发送订单审批通知");
        }

    },
    CODE_D {
        /**
         * 执行发送微信订阅消息的任务
         * 根据提供的参数map，构造并发送订单退款成功通知的微信订阅消息
         * @param map 包含必要参数的Map，包括officialAccountOpenId, templateId, orderId, goodsName
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数中提取公众号OpenID和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌有效，则准备并发送订阅消息
            if (accessToken != null) {
                // 初始化小程序信息，用于订阅消息中的跳转设置
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                // 准备订阅消息的数据内容
                Map<String, TemplateDataDTO> data = new HashMap<>(3);
                data.put("character_string5", new TemplateDataDTO(map.get("orderId").toString()));
                data.put("thing2", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 调用工具方法发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印任务执行完成的日志信息
            System.out.println("执行具体策略，发送订单退款成功通知");
        }

    },
    CODE_E {
        /**
         * 执行发送退货通知的逻辑
         * 该方法通过微信模板消息服务，向用户发送包含小程序链接的订阅消息通知
         *
         * @param map 包含必要参数的映射，包括officialAccountOpenId, templateId, businessName, goodsName, status等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数映射中提取公众号OpenID和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 确保获取到了有效的访问令牌
            if (accessToken != null) {
                // 初始化小程序信息，用于跳转至小程序指定页面
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                // 初始化模板消息的数据内容
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                // 设置模板中的商家名称
                data.put("character_string2", new TemplateDataDTO(map.get("orderId").toString()));
                // 设置模板中的商品名称
                data.put("thing11", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 设置模板中的时间，使用工具类获取当前的中文时间格式
                data.put("time5", new TemplateDataDTO(DateUtils.getCNTime()));
                // 设置模板中的订单状态
                data.put("const12", new TemplateDataDTO(map.get("status").toString()));
                // 调用工具类发送模板消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印日志，表明消息发送逻辑已执行
            System.out.println("执行具体策略，发送用户退货通知");
        }

    },
    CODE_F {
        /**
         * 执行发送模板消息的策略
         * @param map 包含所需参数的映射，如官方账号OpenId、模板ID、店铺名称等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从映射中获取官方账号的OpenId和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌不为空
            if (accessToken != null) {
                // 初始化小程序信息，包括appid和页面路径
                Map<String, String> miniprogram = new HashMap<>(2);
                if (!StringUtils.isEmpty(WXPayConfig.SHOP_APP_ID)) {
                    miniprogram.put("appid", WXPayConfig.SHOP_APP_ID);
                }
                // 初始化模板数据，包括店铺名称、当前时间和审核结果
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                data.put("thing6", new TemplateDataDTO(map.get("shopName").toString()));
                data.put("time7", new TemplateDataDTO(DateUtils.getCNTime()));
                data.put("const5", new TemplateDataDTO(map.get("result").toString()));
                // 调用工具类发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印日志，表示已执行发送商家入驻申请审核结果通知的操作
            System.out.println("执行具体策略，发送商家入驻申请审核结果通知");
        }

    },
    CODE_G {
        /**
         * 执行发送订阅消息的任务
         * @param map 包含发送订阅消息所需参数的映射，包括officialAccountOpenId、templateId和orderId
         * 1. 获取access_token，用于授权访问微信接口
         * 2. 从map中提取officialAccountOpenId和templateId，用于组装小程序链接和定制消息内容
         * 3. 若access_token非空，则构造小程序链接和消息数据，并调用接口发送订阅消息
         * 4. 打印通知发送成功的日志信息
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信access_token，用于后续调用微信接口
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从参数map中获取公众号openid和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 若access_token非空，则准备并发送订阅消息
            if (accessToken != null) {
                // 构造小程序链接所需的参数，包括appid
                Map<String, String> miniprogram = new HashMap<>(2);
                if (!StringUtils.isEmpty(WXPayConfig.SHOP_APP_ID)) {
                    miniprogram.put("appid", WXPayConfig.SHOP_APP_ID);
                }
                // 构造模板消息的数据内容，包括订单ID和当前时间
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                data.put("character_string2", new TemplateDataDTO(map.get("orderId").toString()));
                data.put("time8", new TemplateDataDTO(DateUtils.getCNTime()));
                // 调用接口发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印通知发送成功的日志信息
            System.out.println("执行具体策略，发送订单付款成功通知");
        }
    },
    CODE_H {
        /**
         * 执行发送用户退款申请通知的逻辑
         *
         * @param map 包含发送通知所需信息的映射，如officialAccountOpenId、templateId、orderId、goodsName等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从map中获取公众号的OpenID和模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌不为空
            if (accessToken != null) {
                // 初始化小程序信息，包括appid
                Map<String, String> miniprogram = new HashMap<>(2);
                if (!StringUtils.isEmpty(WXPayConfig.SHOP_APP_ID)) {
                    miniprogram.put("appid", WXPayConfig.SHOP_APP_ID);
                }
                // 准备模板数据，包括订单ID和商品名称
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                data.put("character_string3", new TemplateDataDTO(map.get("orderId").toString()));
                data.put("thing4", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 调用工具类发送订阅消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 打印日志，表示已执行发送通知的操作
            System.out.println("执行具体策略，发送用户退款申请通知");
        }
    },
    CODE_I {
        /**
         * 执行发送用户退货申请通知的逻辑
         *
         * @param map 包含发送通知所需信息的映射，包括官方账号的OpenID、模板ID、订单ID和商品名称等
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从映射中获取官方账号的OpenID、模板ID和小程序的appid
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 如果访问令牌不为空，准备发送模板消息
            if (accessToken != null) {
                // 准备小程序跳转信息
                Map<String, String> miniprogram = new HashMap<>(2);
                if (!StringUtils.isEmpty(WXPayConfig.SHOP_APP_ID)) {
                    miniprogram.put("appid", WXPayConfig.SHOP_APP_ID);
                }
                // 准备模板消息的数据内容
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                // 订单ID
                data.put("character_string1", new TemplateDataDTO(map.get("orderId").toString()));
                // 商品名称
                data.put("thing3", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 当前时间
                data.put("time10", new TemplateDataDTO(DateUtils.getCNTime()));
                // 调用工具类发送模板消息
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 输出执行策略完成的日志信息
            System.out.println("执行具体策略，发送用户退货申请通知");
        }
    },
    CODE_N {
        /**
         * 执行发送退款拒绝通知的策略
         * 本方法通过微信模板消息推送通知给用户，关于退款拒绝的相关信息
         *
         * @param map 包含订单信息及模板ID的键值对，具体包括：
         *            - "officialAccountOpenId": 公众号的关注用户OpenID
         *            - "templateId": 模板消息的模板ID
         *            - "orderId": 订单号
         *            - "goodsName": 商品名称
         */
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从map中提取公众号关注用户的OpenID和模板消息的模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 确保访问令牌有效
            if (accessToken != null) {
                // 初始化小程序信息，用于模板消息跳转至小程序页面
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                // 初始化模板消息的数据部分，用于填充模板中的变量
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                // 设置订单号信息到模板数据中
                data.put("character_string4", new TemplateDataDTO(map.get("orderId").toString()));
                // 设置商品名称信息到模板数据中
                data.put("thing3", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 调用微信接口，发送模板消息给用户
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 标记操作完成
            System.out.println("执行具体策略，发送退款拒绝通知");
        }
    },
    CODE_O {
        @Override
        public void execute(Map<String, Object> map) {
            // 获取微信访问令牌
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            // 从map中提取公众号关注用户的OpenID和模板消息的模板ID
            String officialAccountOpenId = map.get("officialAccountOpenId").toString();
            String templateId = map.get("wxTemplateId").toString();
            // 确保访问令牌有效
            if (accessToken != null) {
                // 初始化小程序信息，用于模板消息跳转至小程序页面
                Map<String, String> miniprogram = new HashMap<>(2);
                miniprogram.put("appid", WXPayConfig.Applet_APP_ID);
                // 初始化模板消息的数据部分，用于填充模板中的变量
                Map<String, TemplateDataDTO> data = new HashMap<>(4);
                // 设置订单号信息到模板数据中
                data.put("character_string1", new TemplateDataDTO(map.get("orderId").toString()));
                // 设置快递单号信息到模板数据中
                data.put("character_string2", new TemplateDataDTO(map.get("trackingNumber").toString()));
                // 设置快递公司信息到模板数据中
                data.put("thing11", new TemplateDataDTO(map.get("expressCompany").toString()));
                // 设置商品名称信息到模板数据中
                data.put("thing12", new TemplateDataDTO(StringUtils.truncateString(map.get("goodsName").toString(), 20)));
                // 设置商品数量信息到模板数据中
                data.put("character_string10", new TemplateDataDTO(map.get("goodsNum").toString()));
                // 调用微信接口，发送模板消息给用户
                WeChatUtil.sendSubscribeMessage(accessToken, officialAccountOpenId, templateId, data, miniprogram);
            }
            // 标记操作完成
            System.out.println("执行具体策略，发送商家发货通知");
        }
    };

    /**
     * TODO 补充方法说明
     * @param map TODO
     */
    public abstract void execute(Map<String, Object> map);

}
