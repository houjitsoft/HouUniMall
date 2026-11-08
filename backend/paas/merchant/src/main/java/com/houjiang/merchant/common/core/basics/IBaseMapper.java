package com.houjiang.merchant.common.core.basics;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.query.MPJQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.houjiang.merchant.common.util.TimeUtils;
import org.apache.ibatis.annotations.Mapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 基类Mapper
 * @param <T>
 */
@Mapper
public interface IBaseMapper<T> extends MPJBaseMapper<T> {

    /**
     * 求和聚合
     *
     * @param field 字段名
     * @param queryWrapper 条件构造器
     * @return Long
     */
    default BigDecimal sum(String field, QueryWrapper<T> queryWrapper) {
        /**
         * TODO 补充方法说明
         */
        queryWrapper.select("IFNULL(sum("+field+"), 0) as totalValue");
        /**
         * TODO 补充方法说明
         */
        List<Object> objects = this.selectObjs(queryWrapper);
        /**
         * TODO 补充方法说明
         */
        if (objects.size() > 0) {
            /**
             * TODO 补充方法说明
             */
            return (BigDecimal) objects.get(0);
        }
        /**
         * TODO 补充方法说明
         */
        return new BigDecimal(0);
    }

    /**
     * 设置搜索条件
     *
     * @author Amos
     * @param queryWrapper 条件构造器
     * @param param 参数[条件:键@数据库字段:类型]
     * @param conditions 条件[ new String[]{"like:username:str", "=:role:int"} ]
     */
    default void setSearch(MPJQueryWrapper<T> queryWrapper, Object param, String[] conditions) {
        /**
         * TODO 补充方法说明
         */
        Type types = new TypeToken<Map<String, String>>() {}.getType();
        /**
         * TODO 补充方法说明
         */
        Map<String, String> params = JSON.parseObject(JSONObject.toJSONString(param), types);

        /**
         * TODO 补充方法说明
         * @param conditions TODO
         */
        for (String condition : conditions) {
            /**
             * TODO 补充方法说明
             */
            String[] array   = condition.split(":");
            /**
             * TODO 补充方法说明
             */
            String type      = array.length > 2 ? array[2].trim() : "";
            /**
             * TODO 补充方法说明
             */
            String[] keyArr  = array[1].trim().split("@");
            /**
             * TODO 补充方法说明
             */
            String where     = array[0].trim();
            /**
             * TODO 补充方法说明
             */
            String key       = keyArr[0].trim();
            /**
             * TODO 补充方法说明
             */
            String value     = params.getOrDefault(key, "");
            /**
             * TODO 补充方法说明
             */
            String field     = keyArr.length > 1 ? keyArr[1].trim() : keyArr[0].trim();

            /**
             * TODO 补充方法说明
             */
            if (!where.equals("datetime") && (value == null || value.equals(""))) {
                continue;
            }

            /**
             * TODO 补充方法说明
             */
            if ((!type.equals("") && !Arrays.asList("long", "str", "int").contains(type))) {
                /**
                 * TODO 补充方法说明
                 */
                System.out.println("搜索参数类型不在固定值内[int,long,str]");
                continue;
            }

            Object val = value;
            /**
             * TODO 补充方法说明
             */
            switch (where) {
                case "=":
                case "<>":
                case ">=":
                case ">":
                case "<=":
                case "<":
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        val = Integer.parseInt(value);
                    } else if (type.equals("long")) {
                        /**
                         * TODO 补充方法说明
                         */
                        val = Long.parseLong(value);
                    }
                    break;
                case "notIn":
                case "in":
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        List<Long> longData = new ArrayList<>();
                        /**
                         * TODO 补充方法说明
                         * @param value.split(" TODO
                         */
                        for (String v : value.split(",")) {
                            /**
                             * TODO 补充方法说明
                             */
                            longData.add(Long.parseLong(v.trim()));
                        }
                        val = longData;
                    } else if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        List<Integer> intData = new ArrayList<>();
                        /**
                         * TODO 补充方法说明
                         * @param value.split(" TODO
                         */
                        for (String v : value.split(",")) {
                            /**
                             * TODO 补充方法说明
                             */
                            intData.add(Integer.parseInt(v.trim()));
                        }
                        val = intData;
                    }
                    break;
            }

            /**
             * TODO 补充方法说明
             */
            switch (where) {
                case "<>":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.ne(field, val);
                    break;
                case "=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.eq(field, val);
                    break;
                case ">":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.gt(field, val);
                    break;
                case ">=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.ge(field, val);
                    break;
                case "<":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.lt(field, val);
                    break;
                case "<=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.le(field, val);
                    break;
                case "between":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] betArr = value.split(",");
                    /**
                     * TODO 补充方法说明
                     */
                    if (!type.equals("") && type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, Integer.parseInt(betArr[0]), Integer.parseInt(betArr[1]));
                    } else if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, Long.parseLong(betArr[0]), Long.parseLong(betArr[1]));
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, betArr[0], betArr[1]);
                    }
                    break;
                case "notBetween":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] notBetArr = value.split(",");
                    /**
                     * TODO 补充方法说明
                     */
                    if (!type.equals("") && type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, Integer.parseInt(notBetArr[0]), Integer.parseInt(notBetArr[1]));
                    } else if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, Long.parseLong(notBetArr[0]), Long.parseLong(notBetArr[1]));
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, notBetArr[0], notBetArr[1]);
                    }
                    break;
                case "like":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.like(field, val);
                    break;
                case "notLike":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.notLike(field, val);
                    break;
                case "likeLeft":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.likeLeft(field, val);
                    break;
                case "likeRight":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.likeRight(field, val);
                    break;
                case "in":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.in(field, val);
                    break;
                case "notIn":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.notIn(field, val);
                    break;
                case "datetime":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] dateKeys = key.split("-");
                    /**
                     * TODO 补充方法说明
                     */
                    String dateStart  = params.getOrDefault(dateKeys[0].trim(), "");
                    /**
                     * TODO 补充方法说明
                     */
                    String dateEnd    = dateKeys.length > 1 ? params.getOrDefault(dateKeys[1].trim(), "") : "";

                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("long")) {
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateEnd.equals("")) { queryWrapper.le(field, Long.parseLong(dateEnd)); }
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateStart.equals("")) { queryWrapper.ge(field, Long.parseLong(dateStart)); }
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateStart.equals("")) { queryWrapper.ge(field, TimeUtils.dateToTimestamp(dateStart)); }
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateEnd.equals("")) { queryWrapper.le(field, TimeUtils.dateToTimestamp(dateEnd)); }
                    }
                    break;
            }
        }
    }


    /**
     * 设置搜索条件
     *
     * @author Amos
     * @param queryWrapper 条件构造器
     * @param param 参数[条件:键@数据库字段:类型]
     * @param conditions 条件[ new String[]{"like:username:str", "=:role:int"} ]
     */
    default void setSearch(QueryWrapper<T> queryWrapper, Object param, String[] conditions) {
        /**
         * TODO 补充方法说明
         */
        Type types = new TypeToken<Map<String, String>>() {}.getType();
        /**
         * TODO 补充方法说明
         */
        Map<String, String> params = JSON.parseObject(JSONObject.toJSONString(param), types);

        /**
         * TODO 补充方法说明
         * @param conditions TODO
         */
        for (String condition : conditions) {
            /**
             * TODO 补充方法说明
             */
            String[] array   = condition.split(":");
            /**
             * TODO 补充方法说明
             */
            String type      = array.length > 2 ? array[2].trim() : "";
            /**
             * TODO 补充方法说明
             */
            String where     = array[0].trim();
            /**
             * TODO 补充方法说明
             */
            String[] keyArr  = array[1].trim().split("@");
            /**
             * TODO 补充方法说明
             */
            String key       = keyArr[0].trim();
            /**
             * TODO 补充方法说明
             */
            String field     = keyArr.length > 1 ? keyArr[1].trim() : keyArr[0].trim();
            /**
             * TODO 补充方法说明
             */
            String value     = params.getOrDefault(key, "");

            /**
             * TODO 补充方法说明
             */
            if (value.equals("") && !where.equals("datetime")) {
                continue;
            }

            /**
             * TODO 补充方法说明
             */
            if ((!type.equals("") && !Arrays.asList("int", "long", "str").contains(type))) {
                /**
                 * TODO 补充方法说明
                 */
                System.out.println("搜索参数类型不在固定值内[int,long,str]");
                continue;
            }

            Object val = value;
            /**
             * TODO 补充方法说明
             */
            switch (where) {
                case "=":
                case "<>":
                case ">":
                case ">=":
                case "<":
                case "<=":
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        val = Integer.parseInt(value);
                    } else if (type.equals("long")) {
                        /**
                         * TODO 补充方法说明
                         */
                        val = Long.parseLong(value);
                    }
                    break;
                case "in":
                case "notIn":
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        List<Integer> intData = new ArrayList<>();
                        /**
                         * TODO 补充方法说明
                         * @param value.split(" TODO
                         */
                        for (String v : value.split(",")) {
                            /**
                             * TODO 补充方法说明
                             */
                            intData.add(Integer.parseInt(v.trim()));
                        }
                        val = intData;
                    } else if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        List<Long> longData = new ArrayList<>();
                        /**
                         * TODO 补充方法说明
                         * @param value.split(" TODO
                         */
                        for (String v : value.split(",")) {
                            /**
                             * TODO 补充方法说明
                             */
                            longData.add(Long.parseLong(v.trim()));
                        }
                        val = longData;
                    }
                    break;
            }

            /**
             * TODO 补充方法说明
             */
            switch (where) {
                case "=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.eq(field, val);
                    break;
                case "<>":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.ne(field, val);
                    break;
                case ">":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.gt(field, val);
                    break;
                case ">=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.ge(field, val);
                    break;
                case "<":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.lt(field, val);
                    break;
                case "<=":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.le(field, val);
                    break;
                case "between":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] betArr = value.split(",");
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, Integer.parseInt(betArr[0]), Integer.parseInt(betArr[1]));
                    } else if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, Long.parseLong(betArr[0]), Long.parseLong(betArr[1]));
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.between(field, betArr[0], betArr[1]);
                    }
                    break;
                case "notBetween":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] notBetArr = value.split(",");
                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("int")) {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, Integer.parseInt(notBetArr[0]), Integer.parseInt(notBetArr[1]));
                    } else if (type.equals("long")){
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, Long.parseLong(notBetArr[0]), Long.parseLong(notBetArr[1]));
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        queryWrapper.notBetween(field, notBetArr[0], notBetArr[1]);
                    }
                    break;
                case "like":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.like(field, val);
                    break;
                case "notLike":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.notLike(field, val);
                    break;
                case "likeLeft":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.likeLeft(field, val);
                    break;
                case "likeRight":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.likeRight(field, val);
                    break;
                case "in":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.in(field, val);
                    break;
                case "notIn":
                    /**
                     * TODO 补充方法说明
                     */
                    queryWrapper.notIn(field, val);
                    break;
                case "datetime":
                    /**
                     * TODO 补充方法说明
                     */
                    String[] dateKeys = key.split("-");
                    /**
                     * TODO 补充方法说明
                     */
                    String dateStart  = params.getOrDefault(dateKeys[0].trim(), "");
                    /**
                     * TODO 补充方法说明
                     */
                    String dateEnd    = dateKeys.length > 1 ? params.getOrDefault(dateKeys[1].trim(), "") : "";

                    /**
                     * TODO 补充方法说明
                     */
                    if (type.equals("long")) {
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateStart.equals("")) { queryWrapper.ge(field, Long.parseLong(dateStart)); }
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateEnd.equals("")) { queryWrapper.le(field, Long.parseLong(dateEnd)); }
                    } else {
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateStart.equals("")) { queryWrapper.ge(field, TimeUtils.dateToTimestamp(dateStart)); }
                        /**
                         * TODO 补充方法说明
                         */
                        if (!dateEnd.equals("")) { queryWrapper.le(field, TimeUtils.dateToTimestamp(dateEnd)); }
                    }
                     break;
            }
        }
    }


}
