package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ShipmentsListDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/10 19:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShipmentsListDTO {

    private List<ShipmentsYiYaTongDTO> shipmentsGoodsDTOList;

    private String token;

}
