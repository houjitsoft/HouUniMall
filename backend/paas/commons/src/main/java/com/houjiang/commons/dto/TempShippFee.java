package com.houjiang.commons.dto;

import com.houjiang.commons.dto.shop.WapShippingQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class TempShippFee {

    private List<WapShippingQueryDTO> listTmp;

    private Integer transportTemplate;

}
