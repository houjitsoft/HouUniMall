package com.houjiang.commons.dto.goods;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName CommunityArticleListDTO
 * @Description 查询社区关注用户心得
 * @Author Amos
 * @Date 2023/6/9 13:37
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommunityArticleListDTO extends PageUserTokenBeanDTO {

    /**
     * 标签内容
     */
    private String tagContent;

    /**
     * 标签Id
     */
    private Integer tagId;

    /**
     * 类别ID
     */
    private Integer catId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 审核状态
     */
    private Integer auditState;

    /**
     * 用户ID
     */
    private List<String> followUserIds;

    /**
     * 查询参数
     */
    private String queryStr;
}
