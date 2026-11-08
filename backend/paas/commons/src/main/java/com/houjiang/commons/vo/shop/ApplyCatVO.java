package com.houjiang.commons.vo.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class ApplyCatVO {
    private Integer shopId;
    private Integer catId;
    private String batchId;
    private Integer start;
    private Float catCommission;
    private String catName;
    private List catNames;
    List<ApplyCatVO> catEntities;
    private String shopName;
    private String userMobile;
    private String reason;
    private Integer catTotal;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatTime;



}
