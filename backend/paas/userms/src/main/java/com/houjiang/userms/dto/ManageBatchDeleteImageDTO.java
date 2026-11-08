package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageBatchDeleteImageDTO
 * @Author Amos
 * @Date 2023/6/29 9:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageBatchDeleteImageDTO extends UserTokenBeanDTO {
    /**
     * 附件id
     */
    private Integer uploadId;

    /**
     * 附件id集合
     */
    private List<Integer> uploadIds;
}
