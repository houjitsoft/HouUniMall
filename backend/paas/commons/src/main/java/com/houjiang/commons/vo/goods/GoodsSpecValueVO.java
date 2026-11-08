package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName GoodsSpecValueVO
 * @Description
 * @Author Amos
 * @Date 2023/6/9 15:57
 * @Version 1.0
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSpecValueVO implements Serializable {

    private String paramName;
    private String paramValue;
    private Integer sort;
}
