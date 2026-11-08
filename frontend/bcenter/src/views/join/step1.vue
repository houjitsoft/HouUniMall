<template>
    <div>
        <div class="wrap">
            <div class="settle-cont-box">
                <h3>{{ shopType === 2 ? '供应商店铺入驻流程' : '企业店铺入驻流程' }}</h3>
                <el-steps :active="1" align-center style="margin-top: 20px">
                    <el-step title="入驻须知" :icon="ElIconDocument"></el-step>
                    <el-step title="商家基本信息填写" :icon="ElIconEdit"></el-step>
                    <el-step title="缴纳店铺租金" :icon="ElIconPostcard"></el-step>
                    <el-step title="入驻成功" :icon="ElIconDocumentChecked"></el-step>
                </el-steps>
                <div class="set-text">
                    <div ref="pdfContainer" class="pdf-container">
                        <div v-if="errorMessage" class="pdf-error-message">
                            <p>{{ errorMessage }}</p>
                            <button class="pdf-control-btn" @click="retryLoad">重试</button>
                        </div>

                        <div v-else-if="pdfSource">
                            <!-- 统一使用浏览器原生 PDF 预览（iframe），避免引入 vue-pdf-embed 依赖 -->
                            <div class="pdf-iframe-wrap">
                                <iframe :src="pdfSource" frameborder="0" class="pdf-iframe" @load="handleIframeLoaded"
                                    @error="handlePdfError"></iframe>
                            </div>
                        </div>

                        <div v-else-if="url" class="pdf-placeholder">
                            <p>协议已准备好，请点击下方按钮加载预览：</p>
                            <div
                                style="margin-top:16px; display:flex; gap:16px; align-items:center; justify-content:center; flex-wrap">
                                <button class="pdf-control-btn" @click="showPreview" :disabled="isLoading">
                                    {{ isLoading ? '加载中...' : '加载协议预览' }}
                                </button>
                                <a :href="url" target="_blank" class="pdf-link">在新标签页中打开协议</a>
                            </div>
                        </div>

                        <div v-else class="pdf-placeholder">
                            <p>正在获取协议信息...</p>
                            <div class="loading-spinner" v-if="isLoading"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="tc settle-bottom-operate">
            <div class="bottom-btn-box">
                <el-checkbox v-model="checked" color="#fd2d53" :disabled="!agreementReady">
                    我已阅读并同意以上协议
                </el-checkbox>
                <el-button @click="back">上一步</el-button>
                <el-button type="danger" @click="next" :disabled="!agreementReady">下一步</el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
    Document as ElIconDocument,
    Edit as ElIconEdit,
    Postcard as ElIconPostcard,
    DocumentChecked as ElIconDocumentChecked,
} from '@element-plus/icons-vue'
import { agreement } from '@/api/join'
import { useAppConfig } from '@/composables/useAppConfig'
// 路由相关
const router = useRouter()
const route = useRoute()

// 响应式数据
const checked = ref(false)
const shopType = ref(0)
const joinId = ref('')
const url = ref('')
const pdfSource = ref(null)
const numPages = ref(0)
const pdfContainer = ref(null)
const previewActivated = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const agreementReady = computed(() => Boolean(url.value) && !errorMessage.value)
let intersectionObserver = null
// 常量定义 - 协议标识
const AGREEMENT_TYPES = {
    INDIVIDUAL: 'US_AGREEMENT', // 个人
    ENTERPRISE: 'ME_AGREEMENT', // 企业
    SUPPLIER: 'GY_ME_AGREEMENT' // 供应商
}
// 初始化组件
onMounted(async () => {
    try {
        // 从路由获取参数
        shopType.value = parseInt(route.query.shopType) || 0
        joinId.value = route.query.joinId || ''

        await fetchData()

        // 注册IntersectionObserver用于自动触发预览
        setTimeout(() => {
            try {
                if (window.IntersectionObserver && pdfContainer.value && url.value) {
                    intersectionObserver = new IntersectionObserver((entries) => {
                        const entry = entries[0]
                        if (entry && entry.isIntersecting && !previewActivated.value) {
                            // 自动加载PDF预览
                            previewActivated.value = true
                            showPreview()

                            // 断开连接避免重复触发
                            intersectionObserver.disconnect()
                            intersectionObserver = null
                        }
                    }, {
                        threshold: 0.2,
                        rootMargin: '100px'
                    })

                    intersectionObserver.observe(pdfContainer.value)
                }
            } catch (e) {
                // 忽略observer错误，保留人工加载按钮
                console.warn('IntersectionObserver初始化失败', e)
            }
        }, 200)
    } catch (error) {
        ElMessage.error('页面初始化出错，请刷新重试')
    }
})

onUnmounted(() => {
    if (intersectionObserver) {
        intersectionObserver.disconnect()
        intersectionObserver = null
    }
})

// 获取协议数据
const fetchData = async () => {
    // 根据店铺类型选择对应的协议标识
    let currentSign = shopType.value === 0 ? AGREEMENT_TYPES.INDIVIDUAL :
        shopType.value === 1 ? AGREEMENT_TYPES.ENTERPRISE :
            AGREEMENT_TYPES.SUPPLIER
    try {
        isLoading.value = true
        errorMessage.value = ''
        url.value = ''
        pdfSource.value = null
        checked.value = false
        const res = await agreement(currentSign)

        if (res.code === 1 && res.data?.url) {
            // 只保存 URL，不自动加载 PDF，避免初始进入触发下载
            url.value = res.data.url
        } else {
            errorMessage.value = '获取到的协议数据不完整，请联系客服'
        }
    } catch (error) {
        errorMessage.value = error?.message || '获取协议失败，请稍后重试'
    } finally {
        // 确保加载状态被重置
        setTimeout(() => {
            isLoading.value = false
        }, 500)
    }
}

// 处理PDF加载完成事件
const handleLoaded = (num) => {
    // 尝试从参数获取页数
    if (typeof num === 'number' && num > 0) {
        numPages.value = num
    } else if (typeof num === 'object' && num) {
        numPages.value = num.numPages || num.pageCount || 1
    } else {
        numPages.value = 1
    }

    // 加载完成后重置加载状态
    isLoading.value = false
}

// 处理PDF加载错误
const handlePdfError = (error) => {
    console.error('PDF加载失败:', error)

    // 设置友好的错误信息
    errorMessage.value = error.message || 'PDF加载失败，请稍后重试'

    // 重置预览状态
    pdfSource.value = null
    numPages.value = 0
}

// 处理iframe加载完成
const handleIframeLoaded = () => {
    isLoading.value = false
    // iframe加载完成后可以隐藏错误信息
    errorMessage.value = ''
}

// 重试加载
const retryLoad = async () => {
    errorMessage.value = ''
    if (!url.value) {
        await fetchData()
        if (url.value) {
            await showPreview()
        }
        return
    }
    await showPreview()
}

// 计算属性：生成用于显示的页码数组
const pagesArray = computed(() => {
    // 确保至少生成一个页面，即使PDF页数为0或未加载
    const pageCount = Math.max(numPages.value || 0, 1)
    return Array.from({ length: pageCount }, (_, i) => i + 1)
})

// 显示PDF预览
const showPreview = async () => {
    // 重置错误状态
    errorMessage.value = ''
    isLoading.value = true

    try {
        // 如果已有PDF URL，则使用 iframe 预览
        if (url.value) {
            pdfSource.value = url.value
            numPages.value = 1
        } else {
            throw new Error('无法加载PDF：URL未提供')
        }
    } catch (error) {
        handlePdfError(error)
    } finally {
        // 重置加载状态
        isLoading.value = false
    }
}


// 后退
const back = () => {
    try {
        router.back()
    } catch (error) {
        console.error('返回失败:', error)
        ElMessage.error('返回失败，正在尝试备用方案...')
        // 备选方案：使用window.history.back()
        setTimeout(() => {
            window.history.back()
        }, 500)
    }
}

// 下一步
const next = async () => {
    if (!agreementReady.value) {
        ElMessage.warning('协议尚未加载完成，请重试后再继续')
        return
    }
    if (!checked.value) {
        ElMessage.warning('请先阅读并勾选相关协议')
        return
    }

    // 根据店铺类型确定下一步路径
    // 供应商入驻（shopType 2）已迁至 supplier，此处跨端跳转
    if (shopType.value === 2) {
        const { safeOpenUrl } = useAppConfig()
        safeOpenUrl(
            '/supplier/#/join/step1',
            {
                joinId: joinId.value,
                shopType: shopType.value,
                origin: route.query.origin || 0,
            },
            '_self'
        )
        return
    }

    // 商家入驻（shopType 1/3）走本端后续流程
    const path = '/join/step3'
    const queryParams = {
        joinId: joinId.value,
        shopType: shopType.value,
        origin: route.query.origin || 0,
    }

    // 显示加载提示
    const loadingMessage = ElMessage({
        message: '正在跳转...',
        type: 'info',
        duration: 0,
        showClose: true
    })

    try {
        await router.push({
            path,
            query: queryParams
        })
    } catch (error) {
        console.error('路由跳转失败:', error)
    } finally {
        // 清理加载提示
        setTimeout(() => {
            loadingMessage.close()
        }, 300)
    }
}
</script>

<style scoped lang="scss">
.pdf-container {
    display: flex;
    padding: 20px;
    overflow-y: auto;
    height: 750px;
    flex-direction: column;
    align-items: center;
}

.pdf-embed {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pdf-placeholder {
    padding: 60px 40px;
    text-align: center;
    color: #666;
    min-height: 300px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.pdf-error-message {
    padding: 40px;
    text-align: center;
    color: #f56c6c;
    background-color: #fef0f0;
    border-radius: 4px;
    margin: 20px;
    border: 1px solid #fbc4c4;
}

.loading-spinner {
    margin-top: 20px;
    width: 30px;
    height: 30px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #1890ff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

button:disabled {
    background-color: #d9d9d9 !important;
    cursor: not-allowed;
    opacity: 0.8;
}

.pdf-control-btn {
    padding: 8px 14px;
    background-color: #1890ff;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.pdf-control-btn:hover {
    background-color: #1677ff;
}

.pdf-link {
    color: #1890ff;
    text-decoration: underline;
    cursor: pointer;
    font-size: 16px;
    margin-top: 10px;
    display: inline-block;
}

.pdf-link:hover {
    color: #40a9ff;
}

.pdf-all-pages {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
    align-items: center;
}

.pdf-embed-page {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    width: 720px;
    max-width: 100%;
}

.pdf-iframe-wrap {
    width: 100%;
    display: flex;
    justify-content: center;
    padding: 10px 0 30px;
}

.pdf-iframe {
    width: 720px;
    height: 980px;
    border: 0;
}

.bottom-btn-box {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
}
</style>
