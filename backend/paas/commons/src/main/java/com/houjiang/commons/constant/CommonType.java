package com.houjiang.commons.constant;

import com.houjiang.commons.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Amos
 */
public interface CommonType {

    /**
     * 通用状态
     */
    enum commonStatus {
        NO(0, "否"),
        YES(1, "是"),
        OTHER(2, "其他");


        /**
         * @param code 编码
         * @param desc desc
         */
        commonStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<commonStatus> op = Arrays.stream(commonStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<commonStatus> op = Arrays.stream(commonStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 通用状态
     */
    enum commonState {
        NO(1, "否"),
        YES(2, "是");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        commonState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<commonStatus> op = Arrays.stream(commonStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<commonStatus> op = Arrays.stream(commonStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 状态值
     */
    enum configEnable {
        NOT_AVAILABLE(0, "不可用"),
        AVAILABLE(1, "可用");


        /**
         * @param code 编码
         * @param desc desc
         */
        configEnable(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<configEnable> op = Arrays.stream(configEnable.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<configEnable> op = Arrays.stream(configEnable.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    enum pointsLogType {
        GAIN(1, "获取积分"),
        CONSUME(2, "积分消费");


        /**
         * @param code 编码
         * @param desc desc
         */
        pointsLogType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<pointsLogType> op = Arrays.stream(pointsLogType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<pointsLogType> op = Arrays.stream(pointsLogType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum pointType {
        REGISTRATION(1, "会员注册"),
        LOGIN(2, "会员登录"),
        APPRAISE(3, "评价"),
        PURCHASE(4, "购买商品"),
        ADMIN(5, "管理员操作"),
        POINTS_EXCHANGE_GOODS(6, "积分换购商品"),
        POINTS_EXCHANGE_VOUCHER(7, "积分兑换代金券"),
        RETURN_OF_GOODS(8, "退货"),
        PlUS_REMOVE_OBSTACLES(9, "开通plus会员");

        /**
         * @param code 编码
         * @param desc desc
         */
        pointType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<pointType> op = Arrays.stream(pointType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<pointType> op = Arrays.stream(pointType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum storeStatus {
        SUCCESSFULLY(3, "开店成功"),
        REVIEWED_PAYMENT(2, "待审核付款"),
        REVIEWED_DATA(1, "待审核资料"),
        CLOSE(0, "关闭");

        /**
         * @param code 编码
         * @param desc desc
         */
        storeStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<storeStatus> op = Arrays.stream(storeStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<storeStatus> op = Arrays.stream(storeStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum storeType {
        SELLER_STORE(1, "卖家店铺"),
        SUPPLIER_STORE(2, "供应商店铺"),
        BRAND_STORE(3, "品牌店铺");


        /**
         * @param code 编码
         * @param desc desc
         */
        storeType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<storeType> op = Arrays.stream(storeType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<storeType> op = Arrays.stream(storeType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum isApproved {
        TO_BE_REVIEWED(0, "待审核"),
        APPROVED(1, "审核通过"),
        FAILED_TO_REVIEW(2, "审核不通过");


        /**
         * @param code 编码
         * @param desc desc
         */
        isApproved(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<isApproved> op = Arrays.stream(isApproved.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<isApproved> op = Arrays.stream(isApproved.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /** 启用和禁用状态 */
    enum smsEnableDisableStatus {
        FORBIDDEN(0, "禁用"),
        STARTUSING(1, "启用");


        /**
         * @param code 编码
         * @param desc desc
         */
        smsEnableDisableStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<smsEnableDisableStatus> op = Arrays.stream(smsEnableDisableStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<smsEnableDisableStatus> op = Arrays.stream(smsEnableDisableStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /** 伪删除标识 */
    enum pseudoDeleteIdentification {
        DELETE(0, "禁用"),
        NORMAL(1, "启用");


        /**
         * @param code 编码
         * @param desc desc
         */
        pseudoDeleteIdentification(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<pseudoDeleteIdentification> op = Arrays.stream(pseudoDeleteIdentification.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<pseudoDeleteIdentification> op = Arrays.stream(pseudoDeleteIdentification.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /** 初始化状态 */
    enum initializationStatus {
        SUCCESS(1, "初始化成功"),
        FAIL(2, "初始化失败");


        /**
         * @param code 编码
         * @param desc desc
         */
        initializationStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<initializationStatus> op = Arrays.stream(initializationStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<initializationStatus> op = Arrays.stream(initializationStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /** 初始化状态 */
    enum uuidType {
        USER("U", "用户UUID");


        /**
         * @param code 编码
         * @param desc desc
         */
        uuidType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(String code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<uuidType> op = Arrays.stream(uuidType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static String getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<uuidType> op = Arrays.stream(uuidType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public String getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /** 启绑定类型 */
    enum bindType {
        MOBILE(1, "mobile"),
        WEIXIN(2, "weixin"),
        /** QQ 互联（统一登录渠道 qq） */
        QQ(3, "qq"),
        /** 企业微信（统一登录渠道 wecom） */
        WECOM(4, "wecom"),
        /** 钉钉（统一登录渠道 dingtalk） */
        DINGTALK(5, "dingtalk"),
        /** 飞书（统一登录渠道 feishu） */
        FEISHU(6, "feishu");


        /**
         * @param code 编码
         * @param desc desc
         */
        bindType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<bindType> op = Arrays.stream(bindType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<bindType> op = Arrays.stream(bindType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 账号店铺状态
     */
    enum shopBaseExist {
        NO_OPEN_STORE(0, "未开店"),
        OPEN_STORE(1, "开店成功"),
        SUB_ACCOUNT(2, "商家的子账号"),
        /**
         * 店铺关闭状态
         */
        STORE_CLOSED(3, "店铺关闭");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        shopBaseExist(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<shopBaseExist> op = Arrays.stream(shopBaseExist.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<shopBaseExist> op = Arrays.stream(shopBaseExist.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商家入驻店铺支付凭证状态 0-待商家上传付款凭证;1-待审核付款凭证;2-审核不通过；3-审核通过
     */
    enum JoinPayStatus {
        WAIT_UPLOAD(0, "待商家上传付款凭证"),
        WAIT_AUDIT(1, "待审核付款凭证"),
        AUDIT_ERROR(2, "审核不通过"),
        AUDIT_SUCCESS(3, "审核通过");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        JoinPayStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<JoinPayStatus> op = Arrays.stream(JoinPayStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<JoinPayStatus> op = Arrays.stream(JoinPayStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 店铺类型
     */
    enum ShopType {
        COMMON_SHOP(1, "普通店铺"),
        SUPPLY_SHOP(2, "供应商"),
        BRAND_SHOP(3, "品牌店铺");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        ShopType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<ShopType> op = Arrays.stream(ShopType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<ShopType> op = Arrays.stream(ShopType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 店铺入驻类型
     */
    enum JoinType {
        PERSONAL_SHOP(0, "个人入驻"),
        BUSINESS_SHOP(1, "企业入驻"),
        SUPPLY_SHOP(2, "供应商入驻");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        JoinType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<JoinType> op = Arrays.stream(JoinType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<JoinType> op = Arrays.stream(JoinType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 审核状态 1-正常；2-待审核；3-审核不通过
     */
    enum auditVerify {
        AUDIT_SUCCESS(1, "正常"),
        AUDIT_WAIT(2, "待审核"),
        AUDIT_FAILED(3, "审核不通过");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        auditVerify(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<auditVerify> op = Arrays.stream(auditVerify.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<auditVerify> op = Arrays.stream(auditVerify.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 审核状态 0:个人入驻 1:企业入驻 2:门店厂商 3:供应商
     */
    enum ShopBusiness {
        INDIVIDUAL(0, "个人入驻"),
        BUSINESS(1, "企业入驻"),
        STORE(2, "门店厂商"),
        SUPPLIER(3, "供应商");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        ShopBusiness(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<ShopBusiness> op = Arrays.stream(ShopBusiness.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<ShopBusiness> op = Arrays.stream(ShopBusiness.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 审核 1待审核 2审核通过 3审核失败
     */
    enum VerifyStatus {
        NO_NEED(0, "不需要"),
        AUDIT_WAIT(1, "待审核"),
        AUDIT_SUCCESS(2, "审核通过"),
        AUDIT_FAILED(3, "审核失败");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        VerifyStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<VerifyStatus> op = Arrays.stream(VerifyStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<VerifyStatus> op = Arrays.stream(VerifyStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 自定义类型
     */
    enum customStatus {
        HOT_COMMODITIES(5, "热销商品");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        customStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<customStatus> op = Arrays.stream(customStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<customStatus> op = Arrays.stream(customStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /** 网站初始化内容设置状态 */
    enum articleStatus {
        STARTUSING(1, "启用"),
        CLOSED(2, "关闭");


        /**
         * @param code 编码
         * @param desc desc
         */
        articleStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<articleStatus> op = Arrays.stream(articleStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<articleStatus> op = Arrays.stream(articleStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 自定义类型
     */
    enum indexConfig {
        COMMODITY(1, "商品"),
        SHOP(2, "店铺"),
        CLASSIFY(3, "分类");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        indexConfig(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<indexConfig> op = Arrays.stream(indexConfig.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<indexConfig> op = Arrays.stream(indexConfig.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 员工权限
     */
    enum employeePermissions {
        PURCHASE_INQUIRY(1, "采购/询价"),
        ORGANIZATIONAL(2, "配置组织架构"),
        CONFIGURATION_PROCESS(3, "配置流程"),
        ACCOUNTING_PERIOD_MANAGEMENT(4, "账期管理"),
        POINTS_MALL(5, "积分商城");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        employeePermissions(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<employeePermissions> op = Arrays.stream(employeePermissions.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<employeePermissions> op = Arrays.stream(employeePermissions.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 配置类型
     */
    enum configType {
        SEARCH(0, "search"),
        INDEX_SLIDER(1, "index_slider"),
        PC_INDEX_AD(2, "pc_index_ad"),
        SUPPLY_PC_INDEX_AD(2, "supply_pc_index_ad"),
        SETTING(3, "setting"),
        LOGO_WRITING(4, "logo_copywriting"),
        INFORMATION_SLIDER(6, "information_slider"),
        // 平台管理规范
        PLATFORM_OPERATION_SPECIFICATIONS(100, "platform_operation_specifications");

        /**
         * @param code 编码
         * @param desc desc
         */
        configType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<configType> op = Arrays.stream(configType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<configType> op = Arrays.stream(configType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 配置键
     */
    enum configKey {
        SETTING_LOGO(0, "setting_logo"),
        SETTING_BUYER_LOGO(1, "setting_buyer_logo"),
        SETTING_SELLER_LOGO(2, "setting_seller_logo"),
        SHOP_DEFAULT_CONFIG(3, "shop_default_config"),
        SHOP_LOGO_WRITING(4, "logo_copywriting_ico_shop"),
        GOODS_LOGO_WRITING(4, "logo_copywriting_ico_goods"),
        PLATFROM_SETTING(5, "setting_phone"),
        PLATFROM_SEARCH_SETTING(6, "search"),
        PLATFROM_PHOTO_SETTING(7, "photo"),
        WEB_ME_PAGE_ADVERTISING(8, "web_me_page_advertising"),
        THEME_UNI_COLOR(9, "uni_color"),
        THEME_WEB_COLOR(10, "theme_web_color"),
        PC_INDEX_SLIDER(11, "index_slider"),
        FOOD_SLIDER_PC(12, "foodSliderPC"),
        JOIN_SETTING(13, "join_setting"),
        BUSIBESS_SWITCH(13, "business_switch"),
        PASSWORD_FORMAT(13, "password_format"),
        PASSWORD_LENGTH(13, "password_length"),
        ITEGRAL_IMAGE(13, "integral_image"),
        WEB_COLOR(15, "web_color"),
        DESENSITIZATION_SWITCH(24, "desensitization_switch"),
        SHOP_APP_SERVICE_AGREEMENT(25, "shop_app_service_agreement"),
        SHOP_APP_PRIVACY_AGREEMENT(26, "shop_app_privacy_agreement"),
        WEBPOS_SWITCH(26, "webPos_switch"),
        AUTOMATIC_DELIVERY_TIME(27, "automatic_delivery_time"),
        EXTEND_THE_DELIVERY_TIME(28, "extend_the_delivery_time"),
        AFTER_SALES_TIME(29, "after_sales_time"),
        // 平台接受短信手机号
        PLATFORM_SMS_MOBILE(30, "platform_sms_mobile"),
        // 平台接受短信手机号
        STORAGE_SWITCH(32, "storage_switch"),
        // 平台客服联系电话
        SETTING_PHONE(33, "setting_phone"),
        // 电子邮件
        SETTING_EMAIL(34, "setting_email"),
        //电子邮件授权码
        SETTING_EMAIL_CODE(35, "setting_email_code"),
        SETTING(36, "setting"),
        // 国家化开关
        INTERNATIONALIZATION_SWITCH(37, "internationalization_switch"),
        // stripe支付开关
        STRIPE_SWITCH(38, "stripe_switch"),
        // 多语言开关
        LANGUAGE_SWITCH(39, "language_switch"),
        // 商品自动翻译开关（开启后商家端不展示多语言 Tab，前台机翻兜底）
        AUTO_TRANSLATE_SWITCH(391, "auto_translate_switch"),
        // paypal开关
        PAYPAL_SWITCH(40, "paypal_switch"),
        // 积分与现金的兑换比例
        POINTS_CAHS_EXCHANGE_RATIO(41, "points_cash_exchange_ratio"),

        OTHER(99, "other"),
        RETENTION_MONEY_DEFAULT_LIMIT(100, "retention_money_default_limit");


        /**
         * @param code 编码
         * @param desc desc
         */
        configKey(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<configKey> op = Arrays.stream(configKey.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<configKey> op = Arrays.stream(configKey.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 配置键
     */
    enum loginPlatform {
        PC_LOGIN(1, "PC登录"),
        BOSS_LOGIN(2, "平台后台登录"),
        BUSINESS_LOGIN(3, "商家系统登录"),
        SUPPLIER_LOGIN(4, "供应商系统登录"),
        PROMOTION_LOGIN(5, "推广公司登录"),
        ORDERFOOD_LOGIN(6, "O2O点餐系统登录"),
        ERP_LOGIN(7, "ERP系统登录"); 


        /**
         * @param code 编码
         * @param desc desc
         */
        loginPlatform(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<loginPlatform> op = Arrays.stream(loginPlatform.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<loginPlatform> op = Arrays.stream(loginPlatform.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * Boolean状态值
     */
    enum booleanStatus {
        FALSE(0, "false"),
        TRUE(1, "true");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        booleanStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<booleanStatus> op = Arrays.stream(booleanStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<booleanStatus> op = Arrays.stream(booleanStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭
     */
    enum shopStatus {
        STORE_CLOSED(0, "店铺关闭"),
        STORE_WAIT_JOIN_BASE(1, "待审核资料"),
        STORE_WAIT_JOIN_PAY(2, "待审核付款"),
        STORE_OPEN_SUCCESS(3, "开店成功");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        shopStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<shopStatus> op = Arrays.stream(shopStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<shopStatus> op = Arrays.stream(shopStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 经销申请状态
     */
    enum dealerStatus {
        WAIT_AUTH(1, "待审核"),
        AUTH_PASS(2, "审核通过"),
        AUTH_REFUSE(3, "审核不通过"),
        DEALER_DEL(4, "已删除");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        dealerStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<dealerStatus> op = Arrays.stream(dealerStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<dealerStatus> op = Arrays.stream(dealerStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum dealerType {
        PUT_AWAY(1, "一键上架"),
        BUY(2, "立即购买");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        dealerType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<dealerType> op = Arrays.stream(dealerType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<dealerType> op = Arrays.stream(dealerType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品一键上架记录状态
     */
    enum putawayState {
        NORMAL(1, "正常"),
        CANCEL(2, "取消");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        putawayState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<putawayState> op = Arrays.stream(putawayState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<putawayState> op = Arrays.stream(putawayState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * spu商品状态 0下架，1正常，10违规（禁售）,2-店铺下架商品下架
     * 商品锁定 （）
     * 商品屏蔽 （1 不屏蔽 2 屏蔽）
     */
    enum commonSpuState {
        GOODS_OFF_SHELF(0, "商品下架"),
        GOODS_NORMAL(1, "正常商品"),
        STORE_OFF_SHELF(2, "店铺下架商品下架"),
        GOODS_ILLEGAL(10, "违规（禁售）"),
        GOODS_NOT_LOCK(1, "不锁定"),
        GOODS_LOCK(2, "锁定"),
        GOODS_NOT_SHIELD(1, "不屏蔽"),
        // 定时发布的商品状态，还未到发布时间
        SCHEDULED_RELEASE(4, "定时发布"),
        // 已放入仓库，待销售的商品状态
        PUT_INTO_WAREHOUSE(5, "放入仓库"),
        GOODS_SHIELD(2, "屏蔽");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        commonSpuState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<commonSpuState> op = Arrays.stream(commonSpuState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<commonSpuState> op = Arrays.stream(commonSpuState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品上下架状态 1-上架 2-下架 0-关闭店铺手动下架,3-店铺下架商品下架
     */
    enum goodsIsShelves {
        STORE_CLOSED(0, "关闭店铺手动下架"),
        GOODS_ON_THE_SHELF(1, "商品上架"),
        GOODS_OFF_THE_SHELF(0, "商品下架"),
        STORE_OFF_SHELF(2, "店铺下架商品下架");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        goodsIsShelves(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<goodsIsShelves> op = Arrays.stream(goodsIsShelves.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<goodsIsShelves> op = Arrays.stream(goodsIsShelves.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 代金券模版状态(1-有效,2-失效)
     */
    enum voucherTState {
        EFFECTIVE(1, "有效"),
        LOSE_EFFICACY(2, "失效");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        voucherTState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<voucherTState> op = Arrays.stream(voucherTState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<voucherTState> op = Arrays.stream(voucherTState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 代金券状态(1-未用,2-已用,3-过期,4-收回)
     */
    enum voucherState {
        UNUSED(1, "未用"),
        USED(2, "已用"),
        OVERDUE(3, "过期"),
        TACK_BACK(4, "收回");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        voucherState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<voucherState> op = Arrays.stream(voucherState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<voucherState> op = Arrays.stream(voucherState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 购物车状态
     */
    enum shoppingCartStatus {
        NORMAL(0, "正常"),
        DELETE(1, "删除"),
        UNDERCARRIAGE(2, "店铺关闭商品下架");


        /**
         * @param code 编码
         * @param desc desc
         */
        shoppingCartStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<shoppingCartStatus> op = Arrays.stream(shoppingCartStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<shoppingCartStatus> op = Arrays.stream(shoppingCartStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品状态
     */
    enum productStatus {
        UNDERCARRIAGE(0, "下架"),
        NORMAL(1, "正常"),
        VIOLATION(10, "违规(禁售)"),
        STORE_OFF_SHELF_GOODS_OFF_SHELF(2, "店铺下架商品下架"),
        DELETE(3, "删除");


        /**
         * @param code 编码
         * @param desc desc
         */
        productStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<productStatus> op = Arrays.stream(productStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<productStatus> op = Arrays.stream(productStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品审核
     */
    enum productReview {
        PASS(1, "通过"),
        FAILED(0, "不通过"),
        UNDER_REVIEW(10, "审核中");


        /**
         * @param code 编码
         * @param desc desc
         */
        productReview(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<productReview> op = Arrays.stream(productReview.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<productReview> op = Arrays.stream(productReview.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 秒杀商品状态
     */

    enum seckillProductStatus {
        NORMAL(1, "正常"),
        FINISH(2, "结束"),
        ADMIN_SHUTDOWN(10, "管理员关闭");


        /**
         * @param code 编码
         * @param desc desc
         */
        seckillProductStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<seckillProductStatus> op = Arrays.stream(seckillProductStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<seckillProductStatus> op = Arrays.stream(seckillProductStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */

    enum promotionStatus {
        NOT_PARTICIPATING(0, "不参加"),
        BARGAIN(1, "砍价"),
        SECKILL(2, "秒杀"),
        DISCOUNT(3, "限时折扣"),
        COLLAGE(4, "拼团"),
        FULL_REDUCTION_AND_FREE_DISCOUNT(5, "满减赠折");


        /**
         * @param code 编码
         * @param desc desc
         */
        promotionStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<promotionStatus> op = Arrays.stream(promotionStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<promotionStatus> op = Arrays.stream(promotionStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 根据不同时间段查询 类型:1、全部，2、当日，3、本周，4、本月，5、年度
     */

    enum timePeriod {
        TIME_ALL(1, "全部"),
        TIME_TODAY(2, "当天"),
        TIME_WEEK(3, "本周"),
        TIME_MONTH(4, "本月"),
        TIME_YEAR(5, "本年");

        /**
         * @param code 编码
         * @param desc desc
         */
        timePeriod(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<timePeriod> op = Arrays.stream(timePeriod.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<timePeriod> op = Arrays.stream(timePeriod.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 操作状态 0 正常 1 是添加  2是删除
     */
    enum operateStatus {
        NO_OPERATE(0, "正常"),
        ADD(1, "添加"),
        DEL(2, "删除");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        operateStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<operateStatus> op = Arrays.stream(operateStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<operateStatus> op = Arrays.stream(operateStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 1：待审核（用户申请开启后数据库状态为1）
     * 2：正常（平台审核通过）
     * 3：待审核（用户申请关闭后数据库状态为3）
     * 4：审核失败（平台审核不通过）
     */
    enum deliveryApplyStatus {
        NOT_APPLIED(0, "未申请"),
        OPEN_TO_BE_REVIEWED(1, "开启待审核"),
        APPROVED(2, "开启审核通过"),
        OFF_TO_BE_REVIEWED(3, "关闭待审核"),
        OPEN_FAILED_TO_REVIEW(4, "开启审核不通过"),
        OFF_FAILED_TO_REVIEW(5, "关闭审核不通过");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        deliveryApplyStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<deliveryApplyStatus> op = Arrays.stream(deliveryApplyStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<deliveryApplyStatus> op = Arrays.stream(deliveryApplyStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 订单删除状态
     */

    enum orderDel {
        DELETE(1, "删除"),
        NOT_DELETE(0, "不删除");


        /**
         * @param code 编码
         * @param desc desc
         */
        orderDel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<orderDel> op = Arrays.stream(orderDel.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<orderDel> op = Arrays.stream(orderDel.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 红包状态(1-未用,2-已用,3-过期)
     */
    enum redpacketState {
        UNUSED(1, "未用"),
        USED(2, "已用"),
        OVERDUE(3, "过期");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        redpacketState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<redpacketState> op = Arrays.stream(redpacketState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<redpacketState> op = Arrays.stream(redpacketState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-多品新人红包 6-专属红包 默认2
     */
    enum redpacketTType {
        NEW_PERSON_REGISTRATION(1, "新人注册红包"),
        ORDINARY(2, "普通红包"),
        MULTIPLE_PRODUCTS(3, "多品红包"),
        SHOP(4, "店铺红包"),
        MULTI_PRODUCT_NEWCOMERS(5, "多品新人红包"),
        EXCLUSIVE(6, "专属红包"),
        PLUS_MEMBERSHIP(7, "plus会员");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        redpacketTType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<redpacketTType> op = Arrays.stream(redpacketTType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<redpacketTType> op = Arrays.stream(redpacketTType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 订单状态
     */

    enum orderStatus {
        TO_BE_PAID(1, "待支付"),
        PAID(2, "已付款"),
        TO_BE_SHIPPED(3, "待发货"),
        SHIPPED(4, "已发货"),
        SIGNED_FOR(5, "已签收"),
        CONFIRM_RECEIPT(6, "确认收货"),
        CANCEL(7, "取消"),
        REFUNDING(8, "退款中"),
        REFUNDED(9, "已退款"),
        PICKED_UP_BY_ONESELF(11, "待自提"),
        VIRTUAL_ORDER_HAS_EXPIRED(12, "虚拟订单已过期"),
        PARTIAL_SHIPMENT(16, "部分发货");

        /**
         * @param code 编码
         * @param desc desc
         */
        orderStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<orderStatus> op = Arrays.stream(orderStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<orderStatus> op = Arrays.stream(orderStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 退货状态
     */

    enum orderReturnStatus {
        NO_RETURNS(0, "无退货"),
        RETURNING_GOODS(1, "退货中"),
        RETURN_COMPLETED(2, "退货完成"),
        RETURN_FAILED(3, "退货不通过");


        /**
         * @param code 编码
         * @param desc desc
         */
        orderReturnStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<orderReturnStatus> op = Arrays.stream(orderReturnStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<orderReturnStatus> op = Arrays.stream(orderReturnStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 退款状态
     */

    enum orderRefundStatus {
        NO_REFUND(0, "无退款"),
        REFUNDING(1, "退款中"),
        REFUND_COMPLETED(2, "退款完成"),
        REFUND_FAILED(3, "退款不通过");


        /**
         * @param code 编码
         * @param desc desc
         */
        orderRefundStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<orderRefundStatus> op = Arrays.stream(orderRefundStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<orderRefundStatus> op = Arrays.stream(orderRefundStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    enum invoiceStatus {
        NOT_INVOICE(0, "未开票"),
        WAIT_APPROVED(1, "审核中"),
        INVOICE(2, "已出票"),
        REFUSE_APPROVED(3, "未通过");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        invoiceStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<invoiceStatus> op = Arrays.stream(invoiceStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<invoiceStatus> op = Arrays.stream(invoiceStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 订单类型 1 普通商品订单 2 商家批发订单 3 爆品绑定订单 4 直播间订单
     * 虚拟订单 0 是 1 否
     * 积分商品订单 0是 1否
     * 是否是门店自提订单 1是 2否
     */
    enum orderType {
        GOODS_TYPE(1, "普通商品订单"),
        SUPPLY_TYPE(2, "商家批发订单"),
        HOT_GOODS_TYPE(3, "爆品绑定订单"),
        IS_VIRTUAL(0, "是虚拟订单"),
        NOT_VIRTUAL(1, "不是虚拟订单"),
        IS_INTEGRA(0, "是积分商品订单"),
        NOT_INTEGRA(1, "不是积分商品订单"),
        IS_SHOP_TAKE(1, "是门店自提订单"),
        NOT_SHOP_TAKE(2, "不是门店自提订单");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        orderType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<orderType> op = Arrays.stream(orderType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<orderType> op = Arrays.stream(orderType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 协议状态状态:ME_AGREEMENT -企业;US_AGREEMENT-个人;GEN_COR_AGREEMENT-入驻合同;GY_ME_AGREEMENT-供应商商家 GY_US_AGREEMENT-供应商用户
     */
    enum agreement {
        ME_AGREEMENT("ME_AGREEMENT", "企业"),
        US_AGREEMENT("US_AGREEMENT", "个人"),
        GEN_COR_AGREEMENT("GEN_COR_AGREEMENT", "入驻合同"),
        GY_ME_AGREEMENT("GY_ME_AGREEMENT", "供应商商家"),
        GY_US_AGREEMENT("GY_US_AGREEMENT", "供应商用户"),
        BR_AGREEMENT("BR_AGREEMENT", "通用");

        private String type;
        private String desc;

        /**
         * @param type 类型
         * @param desc desc
         */
        agreement(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<agreement> op = Arrays.stream(agreement.values())
                    .filter(statusEnum -> statusEnum.getType().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static String getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<agreement> op = Arrays.stream(agreement.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getType();
            } else {
                return null;
            }
        }

        /**
         * 查询类型
         * @return 处理结果
         */
        public String getType() {
            return type;
        }

        /**
         * 设置类型
         * @param type 类型
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 支付配置
     */
    enum payStart {
        /**
         * @param h5 h5
         */
        PAY_CONF_00("00", "微信 PC h5"),
        PAY_CONF_01("01", "app"),
        PAY_CONF_02("02", "微信小程序"),
        PAY_CONF_03("03", "支付宝"),
        PAY_CONF_04("04", "微信公众号");

        private String type;
        private String desc;

        /**
         * @param type 类型
         * @param desc desc
         */
        payStart(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<payStart> op = Arrays.stream(payStart.values())
                    .filter(statusEnum -> statusEnum.getType().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static String getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<payStart> op = Arrays.stream(payStart.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getType();
            } else {
                return null;
            }
        }

        /**
         * 查询类型
         * @return 处理结果
         */
        public String getType() {
            return type;
        }

        /**
         * 设置类型
         * @param type 类型
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 直播商品审核状态1 通过 2 审核中 3 不通过  4待平台审核  5待微信审核
     */
    enum liveGoodsVerify {
        APPROVED(1, "通过"),
        AUDITING(2, "审核中"),
        AUDIT_ERROR(3, "不通过"),
        WAIT_AUDIT(4, "待平台审核"),
        WAIT_WX_AUDIT(5, "待微信审核");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        liveGoodsVerify(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<liveGoodsVerify> op = Arrays.stream(liveGoodsVerify.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<liveGoodsVerify> op = Arrays.stream(liveGoodsVerify.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 微信相关接口
     */
    enum wxApiUrl {
        LIVE_GOODS_ADD(1, "https://api.weixin.qq.com/wxaapi/broadcast/goods/add?access_token="),
        LIVE_GOODS_DELETE(2, "https://api.weixin.qq.com/wxaapi/broadcast/goods/delete?access_token="),
        WX_UPLOAD_IMAGES(3, "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE"),
        WX_GET_ROLE_LIST(4, "https://api.weixin.qq.com/wxaapi/broadcast/role/getrolelist?access_token=ACCESSTOKEN&keyword=KEYWORD&role=ROLE"),
        WX_LIVE_DEL_ROLE(5, "/wxaapi/broadcast/role/deleterole?access_token="),
        WX_LIVE_CREATE_ROOM(6, "/wxaapi/broadcast/room/create?access_token="),
        WX_LIVE_INFO(7, "/wxa/business/getliveinfo?access_token="),
        // 提交物流信息接口
        WX_SHIPPING_INFO(8, "/wxa/sec/order/upload_shipping_info?access_token="),
        API_WECHAT_QQ_URL(99, "https://api.weixin.qq.com");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        wxApiUrl(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<wxApiUrl> op = Arrays.stream(wxApiUrl.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<wxApiUrl> op = Arrays.stream(wxApiUrl.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 佣金订单状态
     */
    enum CommissionOrderStatus {
        UN_PAY(1, "待支付"),
        UN_SETTLE(2, "未入账"),
        SETTLED(3, "已入账"),
        RETURN(4, "已退款"),
        CANCEL(5, "已取消"),
        FROZEN(6, "已冻结");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        CommissionOrderStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<CommissionOrderStatus> op = Arrays.stream(CommissionOrderStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<CommissionOrderStatus> op = Arrays.stream(CommissionOrderStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 付款状态
     */

    enum recordStatus {
        TO_BE_PAID(1, "待支付"),
        PAID(2, "已付款"),
        TO_BE_SHIPPED(3, "待发货"),
        SHIPPED(4, "已发货"),
        SIGNED_FOR(5, "已签收"),
        CONFIRM_RECEIPT(6, "确认收货"),
        CANCEL(7, "取消"),
        REFUNDING(8, "退款中"),
        REFUNDED(9, "已退款"),
        PICKED_UP_BY_ONESELF(11, "待自提"),
        VIRTUAL_ORDER_HAS_EXPIRED(12, "虚拟订单已过期");


        /**
         * @param code 编码
         * @param desc desc
         */
        recordStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<recordStatus> op = Arrays.stream(recordStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<recordStatus> op = Arrays.stream(recordStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 拼团交易记录状态
     */
    enum recordStatus2 {
        WAITING_FOR_DISPOSE(1, "待处理"),
        DEAL_FINISHED(2, "完成"),
        DEAL_CANCEL(3, "取消"),
        WITHDRAW_CANCEL(4, "提现失败");


        /**
         * @param code 编码
         * @param desc desc
         */
        recordStatus2(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<recordStatus2> op = Arrays.stream(recordStatus2.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<recordStatus2> op = Arrays.stream(recordStatus2.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 审批状态
     */
    enum approvalStatus {
        UNDER_APPROVAL(0, "审批中"),
        APPROVAL_SUCCEEDED(1, "审批成功"),
        REJECTED(2, "已驳回"),
        TIMED_OUT(3, "已超时"),
        CANCELED(4, "已取消");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        approvalStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<approvalStatus> op = Arrays.stream(approvalStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<approvalStatus> op = Arrays.stream(approvalStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 退货状态
     */
    enum returnState {
        MERCHANT_REJECTION(0, "商家拒绝"),
        NEW_LAUNCH_PENDING_SELLER_REVIEW(1, "新发起等待卖家审核"),
        APPROVED_BY_THE_SELLER(2, "卖家审核通过"),
        SELLER_FAILED_THE_REVIEW(3, "卖家审核不通过"),
        SELLER_RECEIVES_GOODS(4, "卖家审核不通过"),
        PLATFORM_APPROVED(5, "平台审核通过"),
        BUYER_HAS_FILLED_IN_LOGISTICS(6, "买家已填物流"),
        PLATFORM_REJECTION(7, "平台拒绝"),
        REFUND_CLOSED(8, "退款关闭"),
        PLATFORM_INTERVENTION(9, "平台介入"),
        ACCEPTANCE_NOT_PASSED(10, "验收不通过"),
        TO_BE_RETURNED_BY_THE_BUYER(11, "待买家退货"),
        TO_BE_ACCEPTED(12, "待验收"),
        RETURN_CLOSED(13, "退货关闭"),
        // 用户撤销了退款申请，关闭退款流程
        USER_CANCELS_REFUND_CLOSED(15, "用户撤销退款关闭"),
        // 用户撤销了退货申请，关闭退货流程
        USER_CANCELS_RETURN_CLOSED(16, "用户撤销退货关闭"),
        // 线下订单待平台退款
        WAITING_FOR_PLATFORM_REFUND(17, "线下订单待平台退款"),
        // 卖家发出换货物流
        SEND_BARTER_LOGISTICS(18, "卖家发出换货物流"),
        // 买家确认收货
        BUYER_BARTER_CONFIRM_RECEIPT(19, "买家确认收货"),
        // 用户撤销换货关闭
        USER_CANCELS_BARTER_CLOSED(20, "用户撤销换货关闭"),
        // 卖家部分发出换货物流
        SEND_PART_BARTER_LOGISTICS(21, "卖家发出换货物流"),
        // 买家部分确认收货
        BUYER_PART_BARTER_CONFIRM_RECEIPT(22, "买家部分确认收货"),
        // 换货关闭
        BARTER_CLOSED(23, "换货关闭");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        returnState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<returnState> op = Arrays.stream(returnState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<returnState> op = Arrays.stream(returnState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 平台介入状态
     */
    enum interventionState {
        WAITING_FOR_SELLER(0, "待卖家上传凭证"),
        PENDING_PLATFORM_REVIEW(1, "待平台审核"),
        PROCESSED(2, "已处理"),
        BUYER_WITHDRAWAL(3, "买家撤销");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        interventionState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<interventionState> op = Arrays.stream(interventionState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<interventionState> op = Arrays.stream(interventionState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 平台处理结果
     */
    enum processingResults {
        BUYER_VICTORY(0, "买家胜利"),
        SELLER_VICTORY(1, "卖家胜利"),
        USER_REVOCATION(2, "用户撤销");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        processingResults(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<processingResults> op = Arrays.stream(processingResults.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<processingResults> op = Arrays.stream(processingResults.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 常用审核状态 0-正常；1-待审核；2-审核不通过
     */
    enum commonUseAuditStatus {
        AUDIT_SUCCESS(0, "正常"),
        AUDIT_WAIT(1, "待审核"),
        AUDIT_FAILED(2, "审核不通过");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        commonUseAuditStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<commonUseAuditStatus> op = Arrays.stream(commonUseAuditStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<commonUseAuditStatus> op = Arrays.stream(commonUseAuditStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 账单状态（0 待还款 1部分还款 2已还清 3已逾期）
     */
    enum accountStatus {
        WAIT_REPAYMENT(0, "待还款"),
        PART_REPAYMENT(1, "部分还款"),
        PAID_OFF(2, "已还清"),
        OVERDUE(3, "已逾期");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        accountStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<accountStatus> op = Arrays.stream(accountStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<accountStatus> op = Arrays.stream(accountStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 账单消息
     */
    enum AccountMessage {
        COLLECTION_SUCCESS(0, "还款审核成功，结果：通过！"),
        COLLECTION_FAIL(1, "还款审核成功，结果：不通过！"),
        PAID_OFF(2, "已还清"),
        OVERDUE(3, "已逾期");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        AccountMessage(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<AccountMessage> op = Arrays.stream(AccountMessage.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<AccountMessage> op = Arrays.stream(AccountMessage.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 是否授权账期 0，未授权 1，已授权 2，已冻结
     */
    enum IsAccount {
        UNAUTHORIZED(0, "未授权"),
        AUTHORIZED(1, "已授权"),
        FROZEN(2, "已冻结");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        IsAccount(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<IsAccount> op = Arrays.stream(IsAccount.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<IsAccount> op = Arrays.stream(IsAccount.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 消息类型买家 1订单信息 3账户信息 4其他
     */
    enum messageType {
        ORDER_MESSAGE(1, "订单信息"),
        ACCOUNT_MESSAGE(3, "账户信息"),
        OTHER(4, "其他");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        messageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<messageType> op = Arrays.stream(messageType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<messageType> op = Arrays.stream(messageType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 提现申请状态
     */
    enum isSucceed {
        AUDIT_WAIT(0, "待审核"),
        HAVE_IN_HAND(1, "进行中"),
        PAYMENT_COMPLETED(2, "打款完成"),
        PASS_THROUGH(3, "通过"),
        NO_PASS(4, "不通过");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        isSucceed(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<isSucceed> op = Arrays.stream(isSucceed.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<isSucceed> op = Arrays.stream(isSucceed.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 移动端 首页装修
     * 页面状态(1  首页 2 专题页)
     * 页面数据活动状态（0 活动结束 1 活动正常）
     * 页面数据商品状体（1 上架 2 下架）
     * 页面发布状态 （0 未发布 1 以发布）
     * 页面使用状态 （0 未使用 1已使用）
     * 文件状态（1 图片  2 视频）
     */
    enum modularizationdState {

        INDEX(1, "首页"),
        NOT_INDEX(2, "专题页"),
        ACTIVITY_START(0, "活动结束"),
        ACTIVITY_END(1, "活动正常"),
        GOODS_SHELVES(1, "商品上架"),
        GOODS_TAKEDOWN(2, "商品下架"),
        PAGE_USE(1, "页面已使用"),
        PAGE_NOT_USE(0, "页面未使用"),
        PAGE_PUBLISH(1, "页面已发布"),
        PAGE_NOT_PUBLISH(0, "页面未发布"),
        FILE_IMAGE(1, "图片"),
        FILE_VIDEO(2, "视频");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        modularizationdState(Integer code, String desc) {

            this.code = code;

            this.desc = desc;

        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {

            /**
             * @param null null
             */
            if (code == null) {

                return null;

            }

            Optional<modularizationdState> op = Arrays.stream(modularizationdState.values())

                    .filter(statusEnum -> statusEnum.getCode().equals(code))

                    .findFirst();

            if (op.isPresent()) {

                return op.get().getDesc();

            } else {

                return null;

            }

        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {

            if (StringUtils.isEmpty(name)) {

                return null;

            }

            Optional<modularizationdState> op = Arrays.stream(modularizationdState.values())

                    .filter(statusEnum -> statusEnum.getDesc().equals(name))

                    .findFirst();

            if (op.isPresent()) {

                return op.get().getCode();

            } else {

                return null;

            }

        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {

            return code;

        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {

            this.code = code;

        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {

            return desc;

        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {

            this.desc = desc;

        }

    }

    /**
     * 余额明细类型
     */
    enum detailType {
        DEFAULT(0, "默认"),
        BALANCE_RECHARGE(1, "余额充值"),
        ORDER_REFUND(2, "订单退款"),
        WITHDRAWAL_FAILED(3, "提现失败"),
        WITHDRAWAL_OF_BALANCE(4, "余额提现"),
        BALANCE_TRANSFER(5, "余额转账"),
        SHOPPING_CONSUMPTION(6, "购物消费"),
        SHOPPING_CARD_RECHARGE(7, "购物卡充值"),
        BALANCE_DEDUCTION(8, "余额扣款");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        detailType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<detailType> op = Arrays.stream(detailType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<detailType> op = Arrays.stream(detailType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 资产类型:1-收入；2-支出
     */
    enum assetType {
        INCOME(1, "收入"),
        EXPENDITURE(2, "支出");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        assetType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<assetType> op = Arrays.stream(assetType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<assetType> op = Arrays.stream(assetType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 转帐状态
     */
    enum transferStatus {
        PROCESSING(0, "处理中"),
        RECEIVED(1, "已收到"),
        EXCEED_THE_TIME(2, "过期");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        transferStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<transferStatus> op = Arrays.stream(transferStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<transferStatus> op = Arrays.stream(transferStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 活动商品状态， 1-正常/2-结束/3-管理员关闭
     */
    enum activityGoodsState {
        NORMAL(1, "正常"),
        FINISH(2, "结束"),
        ADMIN_SHUT_DOWN(3, "管理员关闭");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        activityGoodsState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<activityGoodsState> op = Arrays.stream(activityGoodsState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<activityGoodsState> op = Arrays.stream(activityGoodsState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 用户状态
     */
    enum userState {
        NOT_ACTIVE(0, "未激活"),
        NOT_CERTIFIED(1, "未认证"),
        CERTIFIED(2, "已认证"),
        LOCK(3, "锁定"),
        MULTIPLE_ACCOUNTS(8, "多账号");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        userState(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<userState> op = Arrays.stream(userState.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<userState> op = Arrays.stream(userState.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 账单最后状态 1未还款，2待审核，3审核失败，4已逾期，5已还清，6部分还款
     */
    enum finalStatus {
        UNPAID(1, "未还款"),
        REVIEWED(2, "待审核"),
        AUDIT_FAILED(3, "审核失败"),
        OVERDUE(4, "已逾期"),
        PAID_OFF(5, "已还清"),
        PARTIAL_REPAYMENT(6, "部分还款");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        finalStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<finalStatus> op = Arrays.stream(finalStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<finalStatus> op = Arrays.stream(finalStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 回款审核状态 0 无状态/审核成功 1待审核 2审核失败
     */
    enum collectionStatus {
        STATELESS(0, "无状态/审核成功"),
        AUDIT_SUCCEEDED(1, "待审核"),
        REVIEWED(2, "审核失败");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        collectionStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<collectionStatus> op = Arrays.stream(collectionStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<collectionStatus> op = Arrays.stream(collectionStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 级别
     */
    enum level {
        ONE(1, "one"),
        TWO(2, "two"),
        THREE(3, "three");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        level(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<level> op = Arrays.stream(level.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<level> op = Arrays.stream(level.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 其他
     */
    enum other {
        /**
         * @param 0 0
         */
        MO_ID(0, "默认id 0"),
        MO_DISPLAY_ORDER(1, "默认排序"),
        RETURN_START_NO(0, "不存在退款退货"),
        RETURN_START_YES(1, "存在退款退货"),
        QUOTE_NUM(2, "报价最大次数"),
        NOT_USED(0, "未使用"),
        OTHER(99, "other");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        other(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<other> op = Arrays.stream(other.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<other> op = Arrays.stream(other.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 运费 包邮（0不包邮 1全国包邮 2自定义）
     * 条件( 1 重量 2 件数)
     * 默认规则（0 否 1 是）
     */
    enum shippingStart {
        NOT_SHIPPING(0, "不包邮"),
        SHIPPING(1, "全国包邮"),
        CUSTOMIZATION_SHIPPING(2, "自定义"),
        WEIGHT(1, "重量"),
        ITEM(1, "件数"),
        DEFAULT_RULES(1, "是"),
        NOT_DEFAULT_RULES(0, "否");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        shippingStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<shippingStart> op = Arrays.stream(shippingStart.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<shippingStart> op = Arrays.stream(shippingStart.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品分类状态 （0 无等级 1 一级分类 2 二级分类  3 三级分类）
     * 当前分类下是否允许分布虚拟商品 （0 是 不允许  1 是 允许）
     */
    enum catStart {
        /**
         * @param  
         */
        GRADE_ZERO(0, "无等级 或是顶级分类"),
        GRADE_ONE(1, "一级分类"),
        GRADE_TWO(2, "二级分类"),
        GRADE_THREE(3, "三级分类"),
        IS_VIRTUAL(1, "允许"),
        NOT_IS_VIRTUAL(0, "不允许");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        catStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<catStart> op = Arrays.stream(catStart.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<catStart> op = Arrays.stream(catStart.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 询价单状态     0：待报价/暂未接到报价，1：部分报价，2：已报价, 5: 已有商品价格得询价
     */
    enum inquiryStuats {
        WAIT_QUOTED(0, "待报价/暂未接到报价"),
        PART_QUOTED(1, "部分报价"),
        QUOTED(2, "已报价"),
        HAVE_PRICE(5, "已有商品价格得询价");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        inquiryStuats(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<inquiryStuats> op = Arrays.stream(inquiryStuats.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<inquiryStuats> op = Arrays.stream(inquiryStuats.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商品类型     0 积分商品
     * 1 普通商品
     */
    enum goodsType {
        GOODS_TYPE_INTEGRAL(0, "积分商品"),
        GOODS_TYPE_ORDINARY(1, "普通商品");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        goodsType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<goodsType> op = Arrays.stream(goodsType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<goodsType> op = Arrays.stream(goodsType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 支付方式  1-余额支付，2-支付宝支付，3-微信支付， 6-购物卡支付，
     * 16-paypal支付, 17-0元兑换券
     * <p>
     * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
     */
    enum payType {
        PAY_TYPE_BALANCE(1, "余额支付"),
        PAY_TYPE_ZFB(2, "支付宝支付"),
        PAY_TYPE_WX(3, "微信支付"),
        PAY_TYPE_SHOPPING_CARDS(6, "购物卡支付"),
        PAY_TYPE_ONLINE_PAY(12, "线下支付"),
        PAY_TYPE_PAYPAL(16, "paypal支付"),
        PAY_TYPE_PRODUCT_VOUCHER(17, "0元兑换券"),
        PAY_TYPE_STRIPE(18, "Stripe支付"),

        ONLINE_PAY(1, "线上支付"),
        OFFLINE_PAY(2, "线下支付"),
        GOODS_ARRIVED_PAY(3, "货到付款"),
        BILLING_PERIO_PAY(4, "账期支付"),
        CARD_AND_ALIPAY_PAYMENT(7, "充值卡+支付宝"),
        CARD_AND_WECHAT_PAYMENT(8, "充值卡+微信"),
        BALANCE(3, "BALANCE"),
        ALI(1, "ALI"),
        WX(2, "WX");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        payType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<payType> op = Arrays.stream(payType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<payType> op = Arrays.stream(payType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 支付类型
     */
    enum payCode {
        ALI(1, "支付宝"),
        WX(2, "微信"),
        BALANCE(3, "余额");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        payCode(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<payCode> op = Arrays.stream(payCode.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<payCode> op = Arrays.stream(payCode.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 企业用户状态 0企业注册 1升级企业 2变更主体
     * 1 企业审核通过 0  企业审核不通过
     * 0-待审核,1-审核通过,2-审核不通过
     * 部门级别 1为默认 2为次级
     */
    enum businessType {
        OFFLINE_PAY(0, "企业注册"),
        GOODS_ARRIVED_PAY(1, "升级企业"),
        BILLING_PERIO_PAY(2, "变更主体"),
        IS_PASS(1, "企业审核通过"),
        not_Pass(0, "企业审核不通过"),
        TO_BE_REVIEWED(0, "待审核"),
        PASS_AUDIT(1, "审核通过"),
        FAIL_PASS_AUDIT(2, "审核不通过"),
        BUSINESS_REGISTRATION(1, "是企业注册"),
        NOT_BUSINESS_REGISTRATION(0, "不是企业注册"),
        DEPARTMENT_LEVEL_DEFAULT(1, "默认"),
        DEPARTMENT_LEVEL(2, "次级");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        businessType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<businessType> op = Arrays.stream(businessType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<businessType> op = Arrays.stream(businessType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 售后类型 1.退款 2.仅退款 3退货退款
     */
    enum returnMethodType {
        REFUND(1, "退款"),
        ONLY_REFUND(2, "仅退款"),
        RETURN(3, "退货退款"),
        // 换货类型，编码为4，描述为换货
        BARTER(4, "换货");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        returnMethodType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<returnMethodType> op = Arrays.stream(returnMethodType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<returnMethodType> op = Arrays.stream(returnMethodType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 全部型号添加状态 1.只添加购物车 2.只添加询价篮 3.询价篮跟购物车
     */
    enum allModelStatus {
        ONLY_CART(1, "只添加购物车"),
        ONLY_BASKET(2, "只添加询价篮"),
        CART_AND_BASKET(3, "询价篮跟购物车");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        allModelStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<allModelStatus> op = Arrays.stream(allModelStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<allModelStatus> op = Arrays.stream(allModelStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 供应商状态
     */
    enum supplierGoodsStatus {
        //商品操作类型
        PUTAWAY(1, "一键上架"),
        BUY(2, "立即购买"),
        APPLY(3, "申请经销商"),
        HAVA_GOODS(4, "已经销"),
        // 经销申请状态
        WAIT_AUTH(1, "待审核"),
        AUTH_PASS(2, "审核通过"),
        AUTH_REFUSE(3, "审核不通过"),
        DEALER_DEL(4, "已删除");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        supplierGoodsStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<supplierGoodsStatus> op = Arrays.stream(supplierGoodsStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<supplierGoodsStatus> op = Arrays.stream(supplierGoodsStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 佣金订单状态
     */
    enum goodsShareType {
        VK_INVITE(1, "微客申请购买指定分销商品"),
        VK_COMMISSION(2, "微客分享购买获佣");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        goodsShareType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<goodsShareType> op = Arrays.stream(goodsShareType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<goodsShareType> op = Arrays.stream(goodsShareType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 用户邀请类型
     */
    enum inviteRecordType {
        VK_INVITE(1, "申请微客邀请"),
        SHARE_GOODS(2, "首次分享购买邀请");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        inviteRecordType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<inviteRecordType> op = Arrays.stream(inviteRecordType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<inviteRecordType> op = Arrays.stream(inviteRecordType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 店铺会员用户来源
     */
    enum vipUserSource {
        MALLS_INVITE(1, "商家邀请注册"),
        VK_INVITE(2, "微客邀请会员注册"),
        YG_INVITE(3, "员工邀请会员注册"),
        BUY_GOODS(4, "微客分享购买店铺商品"),
        SELF_BUY_GOODS(5, "自主购买店铺商品"),
        VK_APPLY_INVITE(6, "申请微客-邀请会员"),
        VK_APPLY_BUY(7, "申请微客-购买商品"),
        COMMON_INVITE(8, "普通会员邀请");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        vipUserSource(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<vipUserSource> op = Arrays.stream(vipUserSource.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<vipUserSource> op = Arrays.stream(vipUserSource.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 微客申请条件
     */
    enum vKAddCondition {
        //类型
        RANDOM(1, "满足任一勾选条件"),
        ALL(2, "同时满足所有条件"),
        //条件
        BUY_GOODS(1, "购买指定商品"),
        INVITE_USER(2, "邀请会员"),
        /**
         * @param  
         */
        BUY_INVITE(3, "购买+邀请(如果条件是满足任一条件 则两者满足一样即可)");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        vKAddCondition(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<vKAddCondition> op = Arrays.stream(vKAddCondition.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<vKAddCondition> op = Arrays.stream(vKAddCondition.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 微客用户状态
     */
    enum vKStatus {
        PASS(1, "通过"),
        REJECT(2, "拒绝"),
        AUTH(3, "待审核"),
        DELETE(4, "已删除"),
        ;


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        vKStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<vKStatus> op = Arrays.stream(vKStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<vKStatus> op = Arrays.stream(vKStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 微客用户状态
     */
    enum vKAddType {
        MALL_INVITE(1, "商家邀请"),
        MALL_ADD(2, "商家自己添加"),
        VK_INVITE(3, "微客邀请");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        vKAddType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 是否经销商品  0 普通商品   1 经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    enum dealerGoodsFlag {
        MALL_INVITE(0, "普通商品"),
        MALL_ADD(1, "经销商品(商家一键上架供应商商品"),
        VK_INVITE(2, "购买经销商品（商家通过立即购买供应商上架的商品）");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        dealerGoodsFlag(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<dealerGoodsFlag> op = Arrays.stream(dealerGoodsFlag.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<dealerGoodsFlag> op = Arrays.stream(dealerGoodsFlag.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 是否存在退货退款 1存在  0 不存在
     */
    enum isReteun {
        RETURN_NO(0, "不存在"),
        RETURN_YES(1, "存在");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        isReteun(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<isReteun> op = Arrays.stream(isReteun.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<isReteun> op = Arrays.stream(isReteun.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * plus 会员 年卡 季卡 月卡
     */
    enum plusThaliType {
        PLUS_THALI_MOON(1, "月卡"),
        PLUS_THALI_SEASON(2, "季卡"),
        PLUS_THALI_YEAR(3, "年卡"),
        PLUS_CONTENT1(4, "尊敬的用户，您的PLUS"),
        PLUS_CONTENT2(4, "权益-红包券已到账，请查收！"),
        PLUS_CONTENT3(5, "红包到账通知");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        plusThaliType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<plusThaliType> op = Arrays.stream(plusThaliType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<plusThaliType> op = Arrays.stream(plusThaliType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * plus 支付方式 1-余额支付，2-购物卡，3-微信支付，4 支付宝 5 平台赠送
     */
    enum plusPayType {
        PLUS_BALANCE_PAY(1, "余额支付"),
        PLUS_CARD_PAY(2, "购物卡"),
        PLUS_WX_PAY(3, "微信支付"),
        PLUS_ALI_PAY(4, "支付宝"),
        PLUS_PLATFORM_PAY(5, "平台赠送");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        plusPayType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<plusPayType> op = Arrays.stream(plusPayType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<plusPayType> op = Arrays.stream(plusPayType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * plus 会员 1 PLUS专属折扣, 2 无门槛红包,3 红包券,4 送积分,5 积分倍率
     */
    enum plusEquityType {
        PLUS_EXCLUSIVE(1, "PLUS专属折扣"),
        PLUS_NO_THRESHOLD_REDPAPER(2, "无门槛红包"),
        PLUS_RED_PAPER(3, "红包券"),
        PLUS_INTEGRAL(4, "送积分"),
        PLUS_INTEGRAL_MAGNIFICATION(5, "积分倍率");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        plusEquityType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<plusEquityType> op = Arrays.stream(plusEquityType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<plusEquityType> op = Arrays.stream(plusEquityType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 通用状态
     */
    enum afterSalesMethods {
        NO_AFTER_SALES(1, "未参加过售后"),
        ONLY_REFUND(2, "参加过仅退款售后"),
        REFUND(3, "参加过退货售后"),
        RETURN(4, "参加过退款售后");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        afterSalesMethods(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<afterSalesMethods> op = Arrays.stream(afterSalesMethods.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<afterSalesMethods> op = Arrays.stream(afterSalesMethods.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 通用状态
     */
    enum goodsOperateType {
        PUTAWAY(1, "一键上架"),
        BUY(2, "立即购买"),
        APPLY(3, "申请经销商"),
        HAVA_GOODS(4, "已经销"),
        ;


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        goodsOperateType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<goodsOperateType> op = Arrays.stream(goodsOperateType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<goodsOperateType> op = Arrays.stream(goodsOperateType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 通用状态
     */
    enum PromotionReceiveOrNot {
        NO_RECEIVE(0, "未领取"),
        IMMEDIATELY(1, "立即使用"),
        NEW_VIP(2, "新人专享"),
        ALREADY_USE(3, "已使用"),
        EXPIRE(4, "已过期"),
        RECOVERY(5, "回收"),
        BOOKING(6, "还未开始，倒计时"),
        NO_NUM_GET(7, "已抢光");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        PromotionReceiveOrNot(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<PromotionReceiveOrNot> op = Arrays.stream(PromotionReceiveOrNot.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<PromotionReceiveOrNot> op = Arrays.stream(PromotionReceiveOrNot.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * oss类型 1.阿里云 2.minio
     */
    enum ossType {
        ALIYUN(1, "aliyun"),
        MINIO(2, "minio"),
        // 腾讯云 COS
        TENCENT_CLOUD(3, "tencentCloud"),
        // 西部数码 FSS
        WEST_FSS(4, "westFss");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        ossType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<ossType> op = Arrays.stream(ossType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<ossType> op = Arrays.stream(ossType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum TreasureStatus {

        NO_PAY(1, "尚未缴纳"),
        ALREADY_PAY(2, "已缴纳"),
        VERIFING(3, "审核中"),
        VERIFY_FAIL(4, "审核失败");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        TreasureStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<TreasureStatus> op = Arrays.stream(TreasureStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<TreasureStatus> op = Arrays.stream(TreasureStatus.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 核销状态
     */
    enum isCancel {
        NO(0, "订单已核销"),
        YES(1, "订单未核销"),
        EXPIRE(2, "订单过期");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        isCancel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<isCancel> op = Arrays.stream(isCancel.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<isCancel> op = Arrays.stream(isCancel.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 微客申请条件
     */
    enum VKSetType {
        BUY_GOODS(1, "购买指定商品"),
        INVITE_USER(2, "邀请会员"),
        /**
         * @param  
         */
        BUY_INVITE(3, "购买+邀请(如果条件是满足任一条件 则两者满足一样即可)");

        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        VKSetType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<VKSetType> op = Arrays.stream(VKSetType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<VKSetType> op = Arrays.stream(VKSetType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 支付单来源
     */
    enum payPlatFrom {
        PC(1, "pc"),
        WAP(2, "wap微信浏览器"),
        ANDROID(3, "安卓"),
        IOS(5, "IOS"),
        MINI_PROGRAM(6, "微信小程序"),
        WAP_BROWSER(7, "wap浏览器");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        payPlatFrom(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<payPlatFrom> op = Arrays.stream(payPlatFrom.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<payPlatFrom> op = Arrays.stream(payPlatFrom.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 支付请求来源
     */
    enum PayRequestFrom {
        PC(1, "PC"),
        APP(2, "APP"),
        HFIVE(3, "h5"),
        MINI_PROGRAM(4, "微信小程序"),
        WX_BROWSER(5, "微信浏览器"),
        WAP(6, "wap(h5,小程序，微信浏览器统称)");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        PayRequestFrom(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<PayRequestFrom> op = Arrays.stream(PayRequestFrom.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<PayRequestFrom> op = Arrays.stream(PayRequestFrom.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 用户类型 平台用户  普通用户
     */
    enum UserType {
        REGULAR_USERS(1, "普通用户"),
        /**
         * @param  
         */
        PLATFOM_USERS(2, "平台用户 ");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        UserType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<UserType> op = Arrays.stream(UserType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<UserType> op = Arrays.stream(UserType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 配送订单状态
     */
    enum DeliveryOrderStart {
        IS_DELIVERY(1, "是同城配送"),
        not_is_delivery(2, "不是同城配送"),
        NOT_PICKUP(0, "待取货"),
        PICKUP(1, "已取货，待配送");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        DeliveryOrderStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<DeliveryOrderStart> op = Arrays.stream(DeliveryOrderStart.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<DeliveryOrderStart> op = Arrays.stream(DeliveryOrderStart.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 骑手实名认证
     */
    enum RiderAuthentication {
        UNDER_REVIEW(1, "审核中"),
        REVIEW_SUCCESSFUL(2, "审核成功"),
        REVIEW_FAILED(3, "审核失败");


        private Integer code;

        /**
         * @param code 编码
         * @param desc desc
         */
        RiderAuthentication(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<RiderAuthentication> op = Arrays.stream(RiderAuthentication.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<RiderAuthentication> op = Arrays.stream(RiderAuthentication.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 组合活动类型的枚举类，定义了两种组合类型：固定组合和搭配组合。
     * 枚举值包含一个代码和描述，用于区分和解释不同的组合类型。
     */
    enum combinationType {
        // 定义了两种具体的组合类型
        // 固定组合的代码为1，描述为“固定组合”
        FIXED(1, "固定组合"),
        // 搭配组合的代码为2，描述为“搭配组合”
        COLLOCATION(2, "搭配组合");

        // 枚举构造函数，初始化每个枚举值的代码和描述
        /**
         * @param code 编码
         * @param desc desc
         */
        combinationType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /** 枚举类型的代码 */
        private Integer code;
        /** 枚举类型的描述 */
        private String desc;

        /**
         * 获取组合类型的代码。
         *
         * @return 组合类型的代码。
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置组合类型的代码。
         *
         * @param code 要设置的组合类型的代码。
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取组合类型的描述。
         *
         * @return 组合类型的描述。
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置组合类型的描述。
         *
         * @param desc 要设置的组合类型的描述。
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    /**
     * 组合活动类型的枚举类，定义了活动的四种状态。
     */
    enum combinationStatus {
        /**
         * 活动未开始的状态。
         */
        NOT_STARTED(0, "未开始"),
        /**
         * 活动已开始的状态。
         */
        STARTED(1, "已开始"),
        /**
         * 活动已结束的状态。
         */
        ENDED(2, "已结束"),
        /**
         * 活动已失效的状态。
         */
        INVALID(3, "已失效");

        /**
         * 构造方法，初始化每个状态的代码和描述。
         *
         * @param code 状态代码
         * @param desc 状态描述
         */
        combinationStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /** 状态代码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 获取状态代码。
         *
         * @return 状态代码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态代码。
         *
         * @param code 状态代码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 1 物流配送 2 自主配送
     */
    enum shipmentType {
        LOGISTICS_DELIVERY(1, "物流配送"),
        SELF_DELIVERY(2, "自主配送");

        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        shipmentType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 虚拟商品 0 是 1 否 3 称重商品
     */
    enum virtualGoods {
        IS_VIRTUAL(0, "是虚拟商品"),
        NOT_VIRTUAL(1, "不是虚拟商品"),
        IS_WEIGHING(3, "称重商品");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        virtualGoods(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<virtualGoods> op = Arrays.stream(virtualGoods.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<virtualGoods> op = Arrays.stream(virtualGoods.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 1 是店铺优惠券 2是商品优惠券 3 商品兑换券
     */
    enum isShopVoucher {
        SHOP_VOUCHER(1, "店铺优惠券"),
        GOODS_VOUCHER(2, "商品优惠券"),
        PRODUCT_WEIGHING(3, "商品兑换券");

        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        isShopVoucher(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<isShopVoucher> op = Arrays.stream(isShopVoucher.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<isShopVoucher> op = Arrays.stream(isShopVoucher.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 配置键枚举类，用于定义系统配置项的键。
     * 每个配置键都包含一个唯一编码（code）和对该键的描述（desc）。
     */
    enum messageTemplate {
        // 企业认证审核成功通知
        CODE_A(1, "企业认证审核成功通知"),
        // 企业认证审核驳回通知
        CODE_B(2, "企业认证审核驳回通知"),
        // 订单审批通知
        CODE_C(3, "订单审批通知"),
        // 退款成功通知
        CODE_D(4, "退款成功通知"),
        // 退货通知
        CODE_E(5, "退货通知"),
        // 店铺入驻结果
        CODE_F(6, "店铺入驻结果"),
        // 订单付款成功通知
        CODE_G(7, "订单付款成功通知"),
        // 用户退款申请通知
        CODE_H(8, "用户退款申请通知"),
        // 用户退货申请通知
        CODE_I(9, "用户退货申请通知"),
        // 新店铺入驻待审核通知
        CODE_J(10, "新店铺入驻待审核通知"),
        // 企业注册待审核提醒
        CODE_K(11, "企业注册待审核提醒"),
        // 付款凭证待审核通知
        CODE_L(12, "付款凭证待审核通知"),
        // 售后单介入待处理通知（电脑端）
        CODE_M(13, "售后单介入待处理通知"),
        // 退款失败通知
        CODE_N(14, "退款失败通知"),
        // 发送商家发货通知
        CODE_O(15, "商家发货通知"),
        // 兑换券活动通知
        CODE_P(16, "兑换券活动通知"),
        // 通用模板
        CODE_Q(17, "通用模板"),
        // 商品库存不足模板
        CODE_R(18, "商品库存不足模板"),
        // 商家发货成功模板
        CODE_S(19, "商家发货成功模板"),
        // 提醒商家发货模板
        CODE_T(20, "提醒商家发货模板"),
        // 提醒用户付款模板
        CODE_U(21, "提醒用户付款模板"),
        // PLUS权益到账提醒
        CODE_V(22, "PLUS权益到账提醒"),
        // 质保金缴纳提醒
        CODE_W(23, "质保金缴纳提醒"),
        // 店铺入驻资质审核
        CODE_X(24, "店铺入驻资质审核"),
        // 店铺缴纳租金
        CODE_Y(25, "店铺缴纳租金"),
        // 企业资质审核
        CODE_Z(26, "企业资质审核"),
        // 经销商审核
        CODE_A_1(27, "经销商审核"),
        // 秒杀开抢前预约提醒（站内信 + 短信）
        CODE_SECKILL_REMIND(28, "秒杀开抢提醒"),
        // 其他设置项
        OTHER(99, "other");

        /** 配置键编码 */
        private Integer code;
        /** 配置键描述 */
        private String desc;

        // 构造方法，初始化每个配置键的编码和描述
        /**
         * @param code 编码
         * @param desc desc
         */
        messageTemplate(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        // 获取配置键编码
        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        // 设置配置键编码
        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        // 获取配置键描述
        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        // 设置配置键描述
        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 配置键枚举类，用于定义系统配置项的键。
     * 每个配置键都包含一个唯一编码（code）和对该键的描述（desc）。
     */
    enum messageTemplateType {
        // 企业认证审核成功通知
        MEMBER_MESSAGE(1, "会员消息"),
        // 企业认证审核驳回通知
        MERCHANT_MESSAGE(2, "商家消息"),
        // 订单审批通知
        PLATFORM_MESSAGE(3, "平台消息"),
        // 其他设置项
        EMAIL_MESSAGE(4, "邮件消息");

        /** 配置键编码 */
        private Integer code;
        /** 配置键描述 */
        private String desc;

        // 构造方法，初始化每个配置键的编码和描述
        /**
         * @param code 编码
         * @param desc desc
         */
        messageTemplateType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        // 获取配置键编码
        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        // 设置配置键编码
        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        // 获取配置键描述
        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        // 设置配置键描述
        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 活动配置
     */
    enum languageEnum {
        /**
         * @param EN en
         */
        en(1, "en", "English - EN"),
        /**
         * @param YUE yue
         */
        yue(2, "yue", "粤语 - YUE"),
        /**
         * @param WYW wyw
         */
        wyw(3, "wyw", "文言文- WYW"),
        /**
         * @param JP jp
         */
        jp(4, "jp", "日本語 - JP"),
        /**
         * @param KOR kor
         */
        kor(5, "kor", "한국어 - KOR"),
        /**
         * @param FRA fra
         */
        fra(6, "fra", "Français - FRA"),
        /**
         * @param SPA spa
         */
        spa(7, "spa", "Español - SPA"),
        /**
         * @param TH th
         */
        th(8, "th", "ภาษาไทย - TH"),
        /**
         * @param TH th
         */
        ara(9, "ara", "ภาษาไทย - TH"),
        /**
         * @param RU ru
         */
        ru(10, "ru", "Русский язык - RU"),
        /**
         * @param PT pt
         */
        pt(11, "pt", "Português - PT"),
        /**
         * @param DE de
         */
        de(12, "de", "Deutsch - DE"),
        /**
         * @param IT it
         */
        it(13, "it", "Italiano - IT"),
        /**
         * @param EL el
         */
        el(14, "el", "Ελληνικά - EL"),
        /**
         * @param NL nl
         */
        nl(15, "nl", "Nederlands - NL"),
        /**
         * @param PL pl
         */
        pl(16, "pl", "Język polski - PL"),
        /**
         * @param BUL bul
         */
        bul(17, "bul", "Български език - BUL"),
        /**
         * @param EST est
         */
        est(18, "est", "Eesti keel - EST"),
        /**
         * @param DAN dan
         */
        dan(19, "dan", "Dansk - DAN"),
        /**
         * @param FIN fin
         */
        fin(20, "fin", "Suomi - FIN"),
        /**
         * @param CS cs
         */
        cs(21, "cs", "Čeština - CS"),
        /**
         * @param ROM rom
         */
        rom(22, "rom", "Română - ROM"),
        /**
         * @param SLO slo
         */
        slo(23, "slo", "Slovenščina - SLO"),
        /**
         * @param SWE swe
         */
        swe(24, "swe", "Svenska - SWE"),
        /**
         * @param HU hu
         */
        hu(25, "hu", "Magyar - HU"),
        /**
         * @param CHT cht
         */
        cht(26, "cht", "繁體中文 - CHT"),
        /**
         * @param VIE vie
         */
        vie(27, "vie", "Tiếng Việt - VIE"),
        /**
         * @param AUTO auto
         */
        auto(99, "auto", "自动 - AUTO");

        /** 状态码 */
        private Integer code;
        /** 状态键 */
        private String key;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        languageEnum(Integer code, String key, String desc) {
            this.code = code;
            this.key = key;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public String getKey() {
            return key;
        }

        /**
         * 设置状态码。
         *
         * @param key 要设置的状态码
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }

        /**
         * 查询descby键
         * @param key 键
         * @return 处理结果
         */
        public static String getDescByKey(String key) {
            /**
             * @param null null
             */
            if (key == null) {
                return null;
            }
            // 流式处理枚举值，过滤出编码匹配的用户类型，返回其描述
            Optional<languageEnum> op = Arrays.stream(languageEnum.values())
                    .filter(statusEnum -> statusEnum.getKey().equals(key))
                    .findFirst();
            return op.map(languageEnum::getDesc).orElse(null);
        }
    }

    /**
     * 定义sms类型的枚举，包括阿里云,unimall，腾讯云。
     */
    enum smsType {
        // 阿里云短信服务
        ALIYUN(1, "aliyun"),
        // unimall短信服务
        UNIMALL(2, "unimall"),
        // 腾讯云短信服务
        TENCENT_CLOUD(3, "tencentCloud");

        /** OSS类型的编码 */
        private Integer code;
        /** OSS类型的描述 */
        private String desc;

        /**
         * 构造函数用于初始化OSS类型。
         *
         * @param code OSS类型的编码。
         * @param desc OSS类型的描述。
         */
        smsType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取OSS类型的编码。
         *
         * @return OSS类型的编码。
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置OSS类型的编码。
         *
         * @param code 要设置的OSS类型的编码。
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取OSS类型的描述。
         *
         * @return OSS类型的描述。
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置OSS类型的描述。
         *
         * @param desc 要设置的OSS类型的描述。
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 余额充值订单 状态。 支付类型
     * 1支付宝 2 微信 3 线下支付
     * 1 待支付 2 待审核 3 审核失败 4 已取消 5 已完成
     */
    enum balanceRechargeOrderStart {
        // 支付宝支付
        ALI_PAY(1, "支付宝"),
        // 微信支付
        WX_PAY(2, "微信"),
        // 线下支付
        OFFLINE_PAY(3, "线下支付"),

        // 待支付状态
        WAITING_PAY(1, "待支付"),
        // 待审核状态
        WAITING_AUDIT(2, "未入账，待平台审核"),
        // 审核失败状态
        AUDIT_FAIL(3, "充值失败，平台审核驳回"),
        // 已取消状态
        CANCEL(4, "已取消"),
        // 已完成状态
        FINISH(5, "已完成");

        /** 定义枚举实例的代码值 */
        private Integer code;
        /** 定义枚举实例的描述信息 */
        private String desc;

        // 构造函数，初始化枚举实例的代码值和描述信息
        /**
         * @param code 编码
         * @param desc desc
         */
        balanceRechargeOrderStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        // 获取代码值
        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        // 设置代码值
        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        // 获取描述信息
        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        // 设置描述信息
        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 库存流水类型 1为入库单，2为出库单，3为调拨单
     */
    enum inventoryFlowType {
        PUT_STORAGE(1, "入库单"),
        OUT_STORAGE(2, "出库单"),
        TRANSFER_ORDER(3, "调拨单");
        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        inventoryFlowType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }

        /**
         * 查询descby编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getDescByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            // 流式处理枚举值，过滤出编码匹配的用户类型，返回其描述
            Optional<inventoryFlowType> op = Arrays.stream(inventoryFlowType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(inventoryFlowType::getDesc).orElse(null);
        }
    }

    /**
     * 库存流水类型详情 1采购入库 2 供应商采购入库 3销售退库入库 4调拨入库 5销售出库 6调拨出库 7 库存预警 8满仓调拨
     */
    enum inventoryFlowTypeDetail {
        PURCHASE_INVENTORY(1, "采购入库"),
        SUPPLIER_PROCUREMENT_AND_WAREHOUSING(2, "供应商采购入库"),
        RETURN_TO_INVENTORY_AND_STORAGE(3, "销售退库入库"),
        TRANSFER_INVENTORY(4, "调拨入库"),
        SALES_OUTBOUND(5, "销售出库"),
        TRANSFER_OUTBOUND(6, "调拨出库"),
        INVENTORY_ALERT(7, "库存预警"),
        FULL_WAREHOUSE_TRANSFER(8, "满仓调拨");
        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        inventoryFlowTypeDetail(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }

        /**
         * 查询descby编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getDescByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            // 流式处理枚举值，过滤出编码匹配的用户类型，返回其描述
            Optional<inventoryFlowTypeDetail> op = Arrays.stream(inventoryFlowTypeDetail.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(inventoryFlowTypeDetail::getDesc).orElse(null);
        }
    }
    /**
     * 客服配置键
     */
    enum imConfigKey {
        // 客服类型
        CUDSTOMER_SERVICE_TYPE(0, "customer_service_type"),
        // 客服未接待设置
        CUSTOMER_RESPONSE(1, "customer_response"),
        // 客户未响应设置
        USER_RESPONSE(2, "user_response"),
        // 会话分配模式
        SESSION_ALLOCATION(1, "session_allocation"),
        // 网易云信AppKey
        WY_APP_KEY(4, "wy_app_key"),
        // AppSecret
        WY_APP_SECRET(5, "wy_app_secret"),
        ENABLE(1, "启用"),
        DISABLE(0, "禁用");


        /**
         * @param code 编码
         * @param desc desc
         */
        imConfigKey(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<imConfigKey> op = Arrays.stream(imConfigKey.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 客服
     */
    enum imStart {
        /**
         * @param  
         */
        IM_SERVER(1,"客服 服务"),
        /**
         * @param  
         */
        MOBILE_SERVIMER(2,"手机号 服务"),
        /**
         * @param  
         */
        THIRD_PARTY_SERVER(3,"第三方 服务"),

        // 快捷用语 是店铺
        /**
         * @param  
         */
        QUICK_IS_SHOP(1, "快捷用语 是店铺"),
        // 快捷用语 是平台
        /**
         * @param  
         */
        QUICK_IS_PLATFORM(2, "快捷用语 是平台"),

        // 自动回复用语 是店铺
        /**
         * @param  
         */
        REPLY_IS_SHOP(1, "自动回复用语 是店铺"),
        // 自动回复用语 是平台
        /**
         * @param  
         */
        REPLY_IS_PLATFORM(2, "自动回复用语 是平台"),

        // 店铺客服
        KF_IS_SHOP(1, "店铺客服"),
        // 平台客服
        KF_IS_PLATFORM(2, "平台客服"),

        // 是普通用户
        IS_USER(1, "普通用户"),
        // 是 客服
        IS_KF(2, "客服"),

        IS_FORWARD(1, "聊天记录是转发"),
        NOT_FORWARD(2, "聊天记录不是是转发"),

        IS_TXT(0, "文本消息"),
        IS_IMAGE(1, "图片消息"),
        IS_VIDEO(3, "视频地址"),
        IS_FILE(6, "文件消息"),
        IS_GOODS(11, "商品消息"),
        IS_ORDER(12, "订单消息"),
        IS_AUTO_REPLY(13, "自动回复消息"),
        CONTACT_LIVE_SUPPORt(14, "联系人工客服"),
        IS_TIPS(15,"提示消息"),
        IS_TRANSFER_TIPS(16,"会话转接成功"),
        /**
         * @param  
         */
        IS_JSON(100, "100：自定义消息 （json）"),


        NORMAL(1, "正常"),
        END(2, "结束"),

        is_read(1, "消息已读"),
        not_is_read(2, "消息未读"),

        REPLY_CONTENT_TXT(1, "自动回复语句是文本"),
        REPLY_CONTENT_IMAGE(2, "自动回复语句是图片"),

        IS_MALL(1, "是从商城跳转进入"),
        NOT_MALL(2, "是从商城跳转进入"),

        ENABLE(1, "启用"),
        DISABLE(2, "禁用");


        /**
         * @param code 编码
         * @param desc desc
         */
        imStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<imStart> op = Arrays.stream(imStart.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }



        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }/**
     * 心得
     */
    enum articleStart {

        PENDING_REVIEW(0, "待审核"),

        AUDIT_SUCCESS(1, "审核通过"),

        AUDIT_FAILED(2, "审核失败");


        /**
         * @param code 编码
         * @param desc desc
         */
        articleStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<articleStart> op = Arrays.stream(articleStart.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }



        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 商家 代发 购买 铺货 供应商商品 状态
     */
    enum supplierDealerPutawayStart {
        DROPSHIPPING(1, "代发"),
        PURCHASE(2, "购买"),
        STOCKING(3, "铺货");

        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        supplierDealerPutawayStart(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 平台供应链配置的枚举类型，包含不同的平台供应链配置选项及其对应的编码和描述。
     */
    enum supplierProcConfig {
        /**
         * 进货模式开关配置，编码为1，描述为"purchasing_switch"
         */
        PURCHASING_SWITCH(1, "purchasing_switch"),
        /**
         * 代发模式开关配置，编码为2，描述为"delivery_switch"
         */
        DELIVERY_SWITCH(2, "delivery_switch"),
        /**
         * 铺货模式开关配置，编码为3，描述为"distribution_switch"
         */
        DISTRIBUTION_SWITCH(3, "distribution_switch");

        /**
         * 配置编码
         */
        private Integer code;
        /**
         * 配置描述
         */
        private String desc;

        /**
         * 枚举构造函数，初始化平台供应链配置项的编码和描述。
         *
         * @param code 配置项的编码。
         * @param desc 配置项的描述。
         */
        supplierProcConfig(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取配置项的编码。
         *
         * @return 配置项的编码。
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置配置项的编码。
         *
         * @param code 要设置的配置项编码。
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取配置项的描述。
         *
         * @return 配置项的描述。
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置配置项的描述。
         *
         * @param desc 要设置的配置项描述。
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 是否代发
     */
    enum dropShipping {
        DROPSHIPPING_YES(1, "是"),
        DROPSHIPPING_NOT(2, "否");

        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 枚举的构造函数，用于初始化状态的编码和描述。
         *
         * @param code 状态编码
         * @param desc 状态描述
         */
        dropShipping(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取状态码。
         *
         * @return 状态码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态码。
         *
         * @param code 要设置的状态码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述。
         *
         * @param desc 要设置的状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 订单类型枚举，定义了订单的三种类型及其对应的编码和描述。
     * 枚举值包括普通商品订单、询价商品订单和代客商品订单。
     */
    enum typesOfOrder {
        // 普通商品订单的类型
        GENERAL_ORDER(0, "普通商品订单"),
        // 询价商品订单的类型
        INQUIRY_ORDER(1, "询价商品订单"),
        // 代客商品订单的类型
        VALET_ORDER(2, "代客商品订单");

        /** 枚举类型的成员变量：订单编码和订单描述 */
        private Integer code;
        private String desc;

        /**
         * typesOfOrder 枚举的构造函数，用于初始化每个订单类型的编码和描述。
         *
         * @param code 订单的编码。
         * @param desc 订单的描述。
         */
        typesOfOrder(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取订单的编码。
         *
         * @return 订单的编码。
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置订单的编码。
         *
         * @param code 要设置的订单编码。
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取订单的描述。
         *
         * @return 订单的描述。
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置订单的描述。
         *
         * @param desc 要设置的订单描述。
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 定义ai技能类型 1 功能咨询 2 数据检索 3 文案创作 4 智能画图 5 问答百科 6 ai数字人视频
     */
    enum aiSkillType {
        // 功能咨询
        FUNCTION_CONSULTATION(1, "功能咨询"),
        // 数据检索
        DATA_RETRIEVAL(2, "数据检索"),
        // 文案创作
        COPYWRITING_CREATION(3, "文案创作"),
        // 智能画图
        INTELLIGENT_DRAWING(4, "智能画图"),
        // 问答百科
        QA_ENCYCLOPEDIA(5, "问答百科"),
        // 数字人视频
        DIGITAL_HUMAN_VIDEO(6, "数字人视频"),
        // 商品搜索
        GOODS_SEARCH(10, "商品搜索"),
        // 生成商品详情
        CREATE_PRODUCT_DETAILS(8, "生成商品详情"),
        // 图生图
        IMAGE_TO_IMAGE(9, "图生图");

        /** 技能类型的编码 */
        private Integer code;
        /** 技能类型的描述 */
        private String desc;

        /**
         * 构造函数用于初始化OSS类型。
         *
         * @param code OSS类型的编码。
         * @param desc OSS类型的描述。
         */
        aiSkillType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * from编码
         * @param skillType skill类型
         * @return 处理结果
         */
        public static aiSkillType fromCode(Integer skillType) {
            return Arrays.stream(aiSkillType.values())
                    .filter(type -> type.getCode().equals(skillType))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * 获取OSS类型的编码。
         *
         * @return OSS类型的编码。
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置OSS类型的编码。
         *
         * @param code 要设置的OSS类型的编码。
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取OSS类型的描述。
         *
         * @return OSS类型的描述。
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置OSS类型的描述。
         *
         * @param desc 要设置的OSS类型的描述。
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 图片风格
     */
    enum imageStyle {
        // 日漫动画
        riman(1, "riman"),
        // 水墨画
        shuimo(2, "shuimo"),
        // 莫奈
        monai(3, "monai"),
        // 扁平插画
        bianping(4, "bianping"),
        // 像素插画
        xiangsu(5, "xiangsu"),
        // 儿童绘本
        ertonghuiben(6, "ertonghuiben"),
        // 2d渲染
        threedxuanran(7, "3dxuanran"),
        // 漫画
        manhua(8, "manhua"),
        // 黑白漫画
        heibaimanhua(9, "heibaimanhua"),
        // 写实
        xieshi(10, "xieshi"),
        // 动漫
        dongman(11, "dongman"),
        // 毕加索
        bijiasuo(12, "bijiasuo"),
        // 赛博朋克
        saibopengke(13, "saibopengke"),
        // 油画
        youhua(14, "youhua"),
        // 马赛克
        masaike(15, "masaike"),
        // 青花瓷
        qinghuaci(16, "qinghuaci"),
        // 新年剪纸画
        xinnianjianzhi(17, "xinnianjianzhi"),
        // 新年花艺
        xinnianhuayi(18, "xinnianhuayi");


        /**
         * @param code 编码
         * @param desc desc
         */
        imageStyle(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<imageStyle> op = Arrays.stream(imageStyle.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(imageStyle::getDesc).orElse(null);
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 图片风格
     */
    enum imageAspectRatio {
        // 1:1
        one(1, "1024:1024"),
        // 3:4
        two(2, "768:1024"),
        // 4:3
        three(3, "1024:768"),
        // 9:16
        four(4, "720:1280"),
        // 16:9
        five(5, "1280:720"),
        // 3:5
        six(6, "768:1280"),
        // 5:3
        seven(7, "1280:768");


        /**
         * @param code 编码
         * @param desc desc
         */
        imageAspectRatio(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<imageAspectRatio> op = Arrays.stream(imageAspectRatio.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(imageAspectRatio::getDesc).orElse(null);
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 物流模板
     */
    enum transportTemplateType {
        /**
         * @param  
         */
        DEFAULT_TEMPLATE(1, "是默认模板 "),
        DOMESTIC_TEMPLATE(2, "国内物流模板"),
        INTERNATIONAL_TEMPLATE(1, "国际物流模板");


        /**
         * @param code 编码
         * @param desc desc
         */
        transportTemplateType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<transportTemplateType> op = Arrays.stream(transportTemplateType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(transportTemplateType::getDesc).orElse(null);
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 物流模板
     */
    enum transportAreaType {
        DOMESTIC_AREA(2, "国内售卖区域"),
        INTERNATIONAL_AREA(1, "国际售卖区域");


        /**
         * @param code 编码
         * @param desc desc
         */
        transportAreaType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<transportTemplateType> op = Arrays.stream(transportTemplateType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(transportTemplateType::getDesc).orElse(null);
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通, 3 京东VOP
     */
    enum SupplierSource {
        BUSINESS(0, "商家自营商品"),
        PLATFORM_SUPPLIER(1, "代发平台供应链商品"),
        YI_YA_TONG(2, "怡亚通"),
        JING_DONG(3, "京东VOP");


        /**
         * @param code 编码
         * @param desc desc
         */
        SupplierSource(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<SupplierSource> op = Arrays.stream(SupplierSource.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(SupplierSource::getDesc).orElse(null);
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 快递状态枚举类
     * 用于定义快递包裹在不同阶段的状态
     */
    enum deliveryStatus {
        // 暂无轨迹信息
        ZERO(0, "暂无轨迹信息"),
        // 已揽收
        ONE(1, "已揽收"),
        // 在途中
        TWO(2, "在途中"),
        // 已签收
        THREE(3, "已签收"),
        // 问题件
        FOUR(4, "问题件"),
        // 转寄
        FIVE(5, "转寄"),
        // 清关
        SIX(6, "清关");

        /**
         * 构造函数
         * 初始化快递状态的代码和描述
         *
         * @param code 状态代码
         * @param desc 状态描述
         */
        deliveryStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /** 状态代码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        /**
         * 获取状态代码
         *
         * @return 状态代码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置状态代码
         *
         * @param code 状态代码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取状态描述
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置状态描述
         *
         * @param desc 状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }

        /**
         * 根据状态代码获取状态描述
         * 如果代码为空或在枚举中找不到对应的代码，则返回null
         *
         * @param code 状态代码
         * @return 对应的状态描述或null
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<deliveryStatus> op = Arrays.stream(deliveryStatus.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            return op.map(deliveryStatus::getDesc).orElse(null);
        }
    }

    /**
     * 规格版本状态 1 正常  2 删除 3 下架
     */
    enum SpecVersionStatus {
        NORMAL(1, "正常"),
        DELETED(2, "已删除"),
        OFF_SHELF(1, "已下架");


        /**
         * @param code 编码
         * @param desc desc
         */
        SpecVersionStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private Integer code;
        private String desc;

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 换货状态枚举类，定义了订单的四种换货状态。
     */
    enum orderBarterStatus {
        // 无换货状态
        NO_BARTER(0, "无换货"),
        // 换货进行中状态
        BARTERING(1, "换货中"),
        // 换货完成状态
        BARTER_COMPLETED(2, "换货完成"),
        // 换货失败状态
        BARTER_FAILED(3, "换货不通过");
        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        // 枚举构造函数，初始化每个状态的代码和描述
        /**
         * @param code 编码
         * @param desc desc
         */
        orderBarterStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取换货状态的代码。
         *
         * @return 状态代码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置换货状态的代码。
         *
         * @param code 状态代码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取换货状态的描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置换货状态的描述。
         *
         * @param desc 状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 换货状态 0 发出换货物流 1 用户确认收货
     */
    enum barterStatus {
        // 卖家发出换货物流
        SEND_BARTER_LOGISTICS(0, "卖家发出换货物流"),
        // 买家确认收货
        BUYER_BARTER_CONFIRM_RECEIPT(1, "买家确认收货");
        /** 状态码 */
        private Integer code;
        /** 状态描述 */
        private String desc;

        // 枚举构造函数，初始化每个状态的代码和描述
        /**
         * @param code 编码
         * @param desc desc
         */
        barterStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取换货状态的代码。
         *
         * @return 状态代码
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置换货状态的代码。
         *
         * @param code 状态代码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 获取换货状态的描述。
         *
         * @return 状态描述
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置换货状态的描述。
         *
         * @param desc 状态描述
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
    /**
     * 消息类型买家 1订单信息 3账户信息 4其他
     */
    enum aiMessageType {
        TEXT(1, "文本"),
        IMAGES(2, "图片");


        private Integer code;
        private String desc;

        /**
         * @param code 编码
         * @param desc desc
         */
        aiMessageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 查询by编码
         * @param code 编码
         * @return 处理结果
         */
        public static String getByCode(Integer code) {
            /**
             * @param null null
             */
            if (code == null) {
                return null;
            }
            Optional<aiMessageType> op = Arrays.stream(aiMessageType.values())
                    .filter(statusEnum -> statusEnum.getCode().equals(code))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getDesc();
            } else {
                return null;
            }
        }

        /**
         * 查询by名称
         * @param name 名称
         * @return 处理结果
         */
        public static Integer getByName(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            Optional<aiMessageType> op = Arrays.stream(aiMessageType.values())
                    .filter(statusEnum -> statusEnum.getDesc().equals(name))
                    .findFirst();
            if (op.isPresent()) {
                return op.get().getCode();
            } else {
                return null;
            }
        }

        /**
         * 查询编码
         * @return 处理结果
         */
        public Integer getCode() {
            return code;
        }

        /**
         * 设置编码
         * @param code 编码
         */
        public void setCode(Integer code) {
            this.code = code;
        }

        /**
         * 查询desc
         * @return 处理结果
         */
        public String getDesc() {
            return desc;
        }

        /**
         * 设置desc
         * @param desc desc
         */
        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
