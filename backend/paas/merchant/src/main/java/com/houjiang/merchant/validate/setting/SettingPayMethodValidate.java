package com.houjiang.merchant.validate.setting;

import com.houjiang.merchant.vo.setting.SettingPaymentMethodVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SettingPayMethodValidate {

    List<List<SettingPaymentMethodVo>> data;

}
