<template>
    <div class="product-comment-content">
        <!-- 加载状态 -->
        <Loading v-if="loadingComments" compact text="{{ $t('加载中...') }}" />
        <!-- 错误状态 -->
        <div v-else-if="commentError" class="error-comments">
            <p>{{ commentError }}</p>
        </div>
        <!-- 评论内容 -->
        <div v-else-if="commentStats || (productComments && productComments.length > 0)">
            <!-- 评论统计信息 -->
            <div class="comment-stats" v-if="commentStats">
                <div class="stats-item">
                    <span class="stats-label">{{ $t('好评率') }}:</span>
                    <span class="stats-value">{{ commentStats.rate || '0%' }}</span>
                </div>
                <div class="stats-item">
                    <span class="stats-label">{{ $t('好评') }}:</span>
                    <span class="stats-value">{{ commentStats.goodTotalCount || 0 }}</span>
                </div>
                <div class="stats-item">
                    <span class="stats-label">{{ $t('中评') }}:</span>
                    <span class="stats-value">{{ commentStats.neutralTotalCount || 0 }}</span>
                </div>
                <div class="stats-item">
                    <span class="stats-label">{{ $t('差评') }}:</span>
                    <span class="stats-value">{{ commentStats.badTotalCount || 0 }}</span>
                </div>
                <div class="stats-item">
                    <span class="stats-label">{{ $t('晒单') }}:</span>
                    <span class="stats-value">{{ commentStats.imageTotalCount || 0 }}</span>
                </div>
            </div>
            <!-- 评论列表 -->
            <EmptyState v-if="!productComments || productComments.length === 0" :text="$t('暂无评论')" compact />
            <div v-else-if="productComments && productComments.length > 0" class="comments-list">
                <div class="comment-item" v-for="(comment, index) in productComments" :key="index">
                    <div class="comment-header">
                        <div class="comment-avatar">
                            <img v-if="comment.userAvatar" :src="comment.userAvatar" :alt="comment.userName" />
                            <span v-else>{{ (comment.isanonymous === 1 ? '匿' : comment.userName?.charAt(0)) || '匿'
                                }}</span>
                        </div>
                        <div class="comment-info">
                            <div class="comment-user">
                                {{ comment.isanonymous === 1 ? $t('匿名用户') : (comment.userName || $t('匿名用户')) }}
                                <span v-if="comment.scores" class="comment-score">
                                    <span v-for="i in 5" :key="i" class="star"
                                        :class="{ active: i <= (comment.scores || 0) }">★</span>
                                </span>
                            </div>
                            <div class="comment-time">{{ comment.createTime || comment.updateTime }}</div>
                            <div v-if="comment.goodsSpec" class="comment-spec">{{ $t('规格') }}:{{ comment.goodsSpec }}
                            </div>
                        </div>
                    </div>
                    <div class="comment-content">{{ $t(comment.content || '') || $t(comment.explainContent || '') }}
                    </div>
                    <!-- 商家回复 -->
                    <div v-if="comment.explainContent && comment.content" class="comment-reply">
                        <div class="reply-label">{{ $t('商家回复') }}:</div>
                        <div class="reply-content">{{ $t(comment.explainContent || '') }}</div>
                    </div>
                    <!-- 评论图片 -->
                    <div v-if="(comment.imagesList && comment.imagesList.length > 0) || comment.image"
                        class="comment-images">
                       <span v-for="(image, imgIndex) in comment.imagesList" :key="imgIndex">
                       	<img v-if="comment.imagesList&&image!=''" :src="image" :alt="image" class="comment-image" @click="previewImage(image)">
                       	<img v-else-if="comment.image&&comment.image!=''" :src="comment.image" :alt="comment.image" class="comment-image"
                       	    @click="previewImage(comment.image)">
                       </span>
                    </div>
                    <!-- 追评内容 -->
                    <div v-if="comment.additionalContent" class="comment-additional">
                        <div class="additional-header">
                            <span class="additional-label">{{ $t('追评') }}</span>
                            <span v-if="comment.additionalDay" class="additional-day">{{ comment.additionalDay
                                }}{{ $t('天后追评') }}</span>
                            <span v-if="comment.additionalContentTime" class="additional-time">{{
                                $t(comment.additionalContentTime || '') }}</span>
                        </div>
                        <div class="additional-content">{{ $t(comment.additionalContent || '') }}</div>
                        <!-- 追评图片 -->
                        <div v-if="comment.additionalImagesList && comment.additionalImagesList.length > 0"
                            class="comment-images">
							<span v-for="(image, imgIndex) in comment.additionalImagesList" :key="imgIndex">
								<img v-if="image!=''" :src="image" :alt="image" class="comment-image" @click="previewImage(image)">
							</span>
                            
                        </div>
                        <!-- 商家回复追评 -->
                        <div v-if="comment.additionalExplainContent" class="comment-reply">
                            <div class="reply-label">{{ $t('商家回复') }}:</div>
                            <div class="reply-content">{{ $t(comment.additionalExplainContent || '') }}</div>
                            <div v-if="comment.additionalReplyTime" class="reply-time">{{ $t(comment.additionalReplyTime
                                || '') }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 分页 -->
            <div v-if="commentStats && commentStats.records?.totalCount && commentStats.records?.totalCount > 0"
                class="pagination-wrapper">
                <div class="pagination-info">
                    <UPagination v-model:page="currentPage" :total="commentStats.records?.totalCount || 0"
                        :items-per-page="pageSize" class="pagination-right" @update:page="handlePageChange" />
                </div>
            </div>
        </div>
        <EmptyState v-else :text="$t('暂无评论')" compact />
        <PopupModal v-model="previewVisible" title="图片预览" width="min(920px, 92vw)"
            content-class="comment-image-preview-modal" @close="onPreviewModalClose">
            <div class="comment-image-preview-inner">
                <div class="comment-image-preview-toolbar" @click.stop>
                    <button type="button" class="preview-tool-btn" :disabled="previewScale <= PREVIEW_SCALE_MIN"
                        :aria-label="$t('缩小')" :title="$t('缩小')" @click="previewZoomOut">
                        <UIcon name="i-heroicons-magnifying-glass-minus" class="preview-tool-icon" />
                    </button>
                    <button type="button" class="preview-tool-btn" :disabled="previewScale >= PREVIEW_SCALE_MAX"
                        :aria-label="$t('放大')" :title="$t('放大')" @click="previewZoomIn">
                        <UIcon name="i-heroicons-magnifying-glass-plus" class="preview-tool-icon" />
                    </button>
                    <button type="button" class="preview-tool-btn" :aria-label="$t('旋转')" :title="$t('旋转')"
                        @click="previewRotate">
                        <UIcon name="i-heroicons-arrow-path" class="preview-tool-icon" />
                    </button>
                </div>
                <div class="comment-image-preview-wrap">
                    <img v-if="previewUrl" :src="previewUrl" alt="" class="comment-image-preview-img"
                        :style="previewImageTransformStyle" />
                </div>
            </div>
        </PopupModal>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import PopupModal from '~/components/Modal/PopupModal.vue'
import type { PagedByComments, CommentsEntity } from '~/types/order'

// 定义props
const props = withDefaults(defineProps<{
    commentStats?: PagedByComments | null
    productComments?: CommentsEntity[]
    loadingComments?: boolean
    commentError?: string | null
    currentPage?: number
    pageSize?: number
}>(), {
    productComments: () => [],
    loadingComments: false,
    commentError: null,
    commentStats: null,
    currentPage: 1,
    pageSize: 10
})

// 定义事件
const emit = defineEmits<{
    'page-change': [page: number]
}>()

// 当前页（使用 computed 从 props 获取）
const currentPage = computed({
    get: () => props.currentPage || 1,
    set: (value: number) => {
        emit('page-change', value)
    }
})

// 每页数量（从 props 获取）
const pageSize = computed(() => props.pageSize || 10)

// 处理分页变化
const handlePageChange = (page: number) => {
    emit('page-change', page)
}

const PREVIEW_SCALE_MIN = 0.25
const PREVIEW_SCALE_MAX = 4
const PREVIEW_SCALE_STEP = 0.25

const previewVisible = ref(false)
const previewUrl = ref('')
const previewRotateDeg = ref(0)
const previewScale = ref(1)

const previewImageTransformStyle = computed(() => ({
    transform: `rotate(${previewRotateDeg.value}deg) scale(${previewScale.value})`,
    transformOrigin: 'center center',
    transition: 'transform 0.2s ease'
}))

const resetPreviewView = () => {
    previewRotateDeg.value = 0
    previewScale.value = 1
}

const previewZoomIn = () => {
    const next = Math.min(
        PREVIEW_SCALE_MAX,
        Math.round((previewScale.value + PREVIEW_SCALE_STEP) * 100) / 100
    )
    previewScale.value = next
}

const previewZoomOut = () => {
    const next = Math.max(
        PREVIEW_SCALE_MIN,
        Math.round((previewScale.value - PREVIEW_SCALE_STEP) * 100) / 100
    )
    previewScale.value = next
}

const previewRotate = () => {
    previewRotateDeg.value = (previewRotateDeg.value + 90) % 360
}

const onPreviewModalClose = () => {
    previewUrl.value = ''
    resetPreviewView()
}

const previewImage = (imageUrl: string) => {
    if (!imageUrl) return
    resetPreviewView()
    previewUrl.value = imageUrl
    previewVisible.value = true
}
</script>

<style lang="scss" scoped>
.product-comment-content {

    .error-comments {
        padding: 40px 0;
        text-align: center;
        font-size: 14px;
        color: #f5222d;
    }

    .comment-stats {
        display: flex;
        gap: 20px;
        padding: 20px;
        margin-bottom: 20px;
        background-color: #f9f9f9;
        border-radius: 4px;
        flex-wrap: wrap;

        .stats-item {
            display: flex;
            align-items: center;
            gap: 5px;

            .stats-label {
                font-size: 14px;
                color: #666;
            }

            .stats-value {
                font-size: 14px;
                color: #333;
                font-weight: 500;
            }
        }
    }

    .comments-list {
        .comment-item {
            padding: 20px 0;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
                border-bottom: none;
            }

            .comment-header {
                display: flex;
                align-items: flex-start;
                margin-bottom: 15px;

                .comment-avatar {
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    background-color: #f0f0f0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #666;
                    font-weight: 500;
                    font-size: 14px;
                    margin-right: 15px;
                    flex-shrink: 0;
                    overflow: hidden;

                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }

                .comment-info {
                    flex: 1;

                    .comment-user {
                        font-size: 14px;
                        color: #333;
                        font-weight: 500;
                        margin-bottom: 5px;
                        display: flex;
                        align-items: center;
                        gap: 10px;

                        .comment-score {
                            display: flex;
                            gap: 2px;

                            .star {
                                color: #ddd;
                                font-size: 14px;

                                &.active {
                                    color: #ffa500;
                                }
                            }
                        }
                    }

                    .comment-time {
                        font-size: 12px;
                        color: #999;
                        margin-bottom: 5px;
                    }

                    .comment-spec {
                        font-size: 12px;
                        color: #999;
                    }
                }
            }

            .comment-content {
                font-size: 14px;
                color: #666;
                line-height: 1.6;
                margin-bottom: 15px;
            }

            .comment-reply {
                background-color: #f9f9f9;
                padding: 12px;
                border-radius: 4px;
                margin-bottom: 15px;

                .reply-label {
                    font-size: 12px;
                    color: #999;
                    margin-bottom: 5px;
                }

                .reply-content {
                    font-size: 14px;
                    color: #666;
                    line-height: 1.6;
                }

                .reply-time {
                    font-size: 12px;
                    color: #999;
                    margin-top: 5px;
                }
            }

            .comment-additional {
                margin-top: 15px;
                padding-top: 15px;
                border-top: 1px dashed #e8e8e8;

                .additional-header {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    margin-bottom: 10px;

                    .additional-label {
                        font-size: 12px;
                        color: #fff;
                        background-color: var(--color);
                        padding: 2px 8px;
                        border-radius: 3px;
                    }

                    .additional-day {
                        font-size: 12px;
                        color: #999;
                    }

                    .additional-time {
                        font-size: 12px;
                        color: #999;
                    }
                }

                .additional-content {
                    font-size: 14px;
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 10px;
                }
            }

            .comment-images {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
                margin-top: 10px;

                .comment-image {
                    width: 80px;
                    height: 80px;
                    object-fit: cover;
                    border-radius: 4px;
                    cursor: pointer;
                    transition: transform 0.3s;

                    &:hover {
                        transform: scale(1.05);
                    }
                }
            }
        }
    }

    .pagination-wrapper {
        margin-top: 30px;
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;
        display: flex;
        justify-content: flex-end;

        .pagination-info {
            display: flex;
            align-items: center;
            justify-content: flex-end;
        }
    }
}

.comment-image-preview-inner {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.comment-image-preview-toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding-bottom: 4px;
    border-bottom: 1px solid #eee;
}

.preview-tool-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    padding: 0;
    color: #333;
    background: #f5f5f5;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.15s, border-color 0.15s;

    &:hover:not(:disabled) {
        background: #eee;
        border-color: #ccc;
    }

    &:disabled {
        opacity: 0.45;
        cursor: not-allowed;
    }
}

.preview-tool-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
}

.comment-image-preview-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 120px;
    overflow: auto;
}

.comment-image-preview-img {
    display: block;
    width: 384px;
    max-width: 100%;
    height: auto;
}

:deep(.comment-image-preview-modal .modal-body) {
    padding: 16px;
    overflow: auto;
    max-height: calc(72vh + 48px);
}
</style>
