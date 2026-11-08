package com.houjiang.commons.dto.order;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlatFormMemberSelfQueryDTO extends PageHelper {
    private String token;
    private String orderId;
    private String buyerUserName;
    private String shopName;
    private String paymentNumberk;
    private String airTime;
    private String finishTime;
    private String orderStatus;
    private String parentShopId;
    private String districtCityId;
    private String inviteSubsiteId;
    private String isNotshopid;
    private String buyerUserId;


}
