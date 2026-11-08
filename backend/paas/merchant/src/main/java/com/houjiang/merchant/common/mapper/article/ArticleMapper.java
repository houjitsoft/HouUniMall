package com.houjiang.merchant.common.mapper.article;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.article.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章
 * @author Amos
 */
@Mapper
public interface ArticleMapper extends IBaseMapper<Article> {
}
