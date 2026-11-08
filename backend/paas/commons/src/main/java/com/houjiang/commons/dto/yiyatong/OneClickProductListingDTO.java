package com.houjiang.commons.dto.yiyatong;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName OneClickProductListingDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/11 9:06
 * @Version 1.0
 * @author Amos
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class OneClickProductListingDTO extends UserTokenBeanDTO {

    /**
     * spuIdList
     */
    private List<String> spuIdList;
    /**
     * skuList
     */
    private List<String> skuIdList;

    private UserInfoVO userInfoVO;
}
