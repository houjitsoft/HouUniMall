package com.houjiang.merchant.vo.mall;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class MallShopAdminDetailVo implements Serializable {
    private Integer id;
    private Integer shopId;
    private Long userId;
    private String loginAccount;
    private String userMobile;
    private String userName;
    private Integer isSuper;
    private String name;
    private String face;
    private Integer status;
    private Integer loginAll;
    private Integer loginLastTime;
    private String loginLastIp;
    private List<String> deptArr;
    private List<Integer> deptId;
    private List<String> jobsArr;
    private List<Integer> jobsId;
    private List<String> roleArr;
    private List<Integer> roleId;
}
