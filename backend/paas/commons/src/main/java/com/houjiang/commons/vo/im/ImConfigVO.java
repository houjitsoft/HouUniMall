package com.houjiang.commons.vo.im;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;



/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2024-10-18 11:01:14
 * @Version 1.0
 * @author Amos
 */
@Data
public class ImConfigVO  {
    /**
     * 配置键
     */
    private String configKey;
    /**
     * 配置值
     */
    private String configValue;
    /**
     * 配置值
     */
    private String configValue2;
    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
    /**
     * 配置描述
     */
    private String configComment;

}
