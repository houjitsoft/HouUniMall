package com.houjiang.commons.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-06-2116:00
 * @Description: 经销配置
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerSetVO {
    private String configKey;
    private String value1;
    private String value2;
    private String value3;
    private Integer configEnable;

    public DealerSetVO(String configKey) {
        this.configKey = configKey;
        this.value1 = "0";
        this.configEnable = 2;
    }
}
