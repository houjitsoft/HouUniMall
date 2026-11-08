import { defineStore } from 'pinia'
import type { ArticleState } from '~/types/store'

export const useArticleStore = defineStore('article', {
    // 状态
    state: (): ArticleState => ({
        articleGroupId: null,
        articleId: null
    }),

    // 计算属性
    getters: {
        // 获取文章分类ID
        getArticleGroupId: (state) => state.articleGroupId,
        // 获取文章ID
        getArticleId: (state) => state.articleId
    },

    // 操作方法
    actions: {
        // 设置文章分类ID
        setArticleGroupId(articleGroupId: number | null) {
            this.articleGroupId = articleGroupId
        },

        // 设置文章ID
        setArticleId(articleId: number | null) {
            this.articleId = articleId
        },

        // 清除文章分类ID
        clearArticleGroupId() {
            this.articleGroupId = null
        },

        // 清除文章ID
        clearArticleId() {
            this.articleId = null
        },

        // 清除所有文章相关数据
        clearAll() {
            this.articleGroupId = null
            this.articleId = null
        }
    },

    persist: {
        key: 'article-state',
        storage: typeof window !== 'undefined' ? window.sessionStorage : undefined,
        paths: ['articleGroupId', 'articleId']
    } as any
})
