package com.houjiang.commons.vo.order;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class LogisticsInfoVO {

    /**
     * 物流状态编码
     */
    private String StateEx;

    private List<?> PreCoordinates;

    /**
     * 快递公司编码
     */
    private String ShipperCode;

    /**
     * 物流轨迹
     */
    private List<TraceVO> Traces;

    /**
     * 途经城市
     */
    private List<CoordinateVO> Coordinates;

    /**
     * 用户ID
     */
    private String EBusinessID;

    private String RouteMapUrlCode;

    private boolean CityIsNull;

    /**
     * 派件快递员手机号
     */
    private String DeliveryManTel;

    /**
     * 成功与否(true/false)
     */
    private boolean Success;

    /**
     * 收货城市经纬度
     */
    private String ReceiverCityLatAndLng;

    /**
     * 轨迹地图URL
     */
    private String RouteMapUrl;

    /**
     * 快递单号
     */
    private String LogisticCode;

    /**
     * 普通物流状态
     * 0-暂无轨迹信息
     * 1-已揽收
     * 2-在途中
     * 3-签收
     * 4-问题件
     * 5-转寄
     * 6-清关
     */
    private String State;

    /**
     * 发货城市经纬度
     */
    private String SenderCityLatAndLng;

    /**
     * 当前城市
     */
    private String Location;

    /**
     * 失败原因
     */
    private String Reason;

    /**
     * 订单预计到达时间
     */
    private String EstimatedDeliveryTime;

    /**
     * 订单编号
     */
    private String OrderCode;

    /**
     * 用户自定义回传字段
     */
    private String Callback;

    /**
     * 派件网点的名称
     */
    private String Station;

    /**
     * 派件网点的电话
     */
    private String StationTel;

    /**
     * 派件快递员
     */
    private String DeliveryMan;

    /**
     * 下一站城市
     */
    private String NextCity;

}
