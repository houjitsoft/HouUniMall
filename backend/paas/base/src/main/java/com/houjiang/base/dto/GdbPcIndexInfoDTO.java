package com.houjiang.base.dto;

import com.houjiang.base.vo.index.GdbPcIndexVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName GdbPcIndexInfoDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/5 9:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbPcIndexInfoDTO {

    /**
     * token
     */
    private String token;

    private List<GdbPcIndexVO> gdbPcIndexDTOList;

}
