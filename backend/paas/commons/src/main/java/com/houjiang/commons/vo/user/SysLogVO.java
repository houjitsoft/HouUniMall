package com.houjiang.commons.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName SysLogVO
 * @Description 操作日志
 * @Author Amos
 * @Date 2023/6/14 16:54
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLogVO {

    private Long id;

    private String userid;

    /** 用户名 */
    private String username;

    /** 操作 */
    private String operation;

    /** 方法名 */
    private String method;

    /** 参数 */
    private String logParam;

    /** ip地址 */
    private String logIp;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
