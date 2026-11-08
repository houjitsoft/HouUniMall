package com.houjiang.base.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AddLiveGoodsParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/28 14:01
 * @Version 1.0
 * @author Amos
 */
@Data
public class AddLiveGoodsParamDTO {

    private List<Integer> ids;
    private Integer roomId;
}
