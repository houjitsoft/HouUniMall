package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ReturnListDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/11 9:35
 * @Version 1.0
 * @author Amos
 */
@Data
public class ReturnListDTO {

    private List<ReturnYiYaTongInfoDTO> returnList;

    private String token;
}
