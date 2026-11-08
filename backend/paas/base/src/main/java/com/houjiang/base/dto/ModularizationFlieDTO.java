package com.houjiang.base.dto;

import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ModularizationFlieDTO {

    private String fileName;
    private Integer fileType;
    private List<OssUploadUpdateParamDTO> paramList;
    private Integer page;

    private Integer limit;

    private String token;

    private String userId;

    private String shopId;

}
