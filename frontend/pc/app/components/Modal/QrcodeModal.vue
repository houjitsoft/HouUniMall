<template>
    <PopupModal :model-value="visible" :title="title" :width="width" content-class="qrcode-modal"
        @update:model-value="onUpdate">
        <div v-if="qrcodeUrl" class="qrcode-box">
            <div v-show="imageLoading" class="qrcode-loading" aria-busy="true">
                <div class="qrcode-spinner" />
                <p class="qrcode-loading-text">{{ $t('加载中...') }}</p>
            </div>
            <img ref="qrcodeImgRef" :src="qrcodeUrl" alt="扫码" class="qrcode-img"
                :class="{ 'qrcode-img--pending': imageLoading }" @load="onImageDone" @error="onImageDone" />
        </div>
        <div v-else class="qrcode-placeholder">{{ $t('暂无二维码') }}</div>
        <!-- 可选说明文案（如下载 APP 需手机浏览器打开等） -->
        <p v-if="tip" class="qrcode-tip">{{ tip }}</p>
    </PopupModal>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import PopupModal from '~/components/Modal/PopupModal.vue'

const props = withDefaults(
    defineProps<{
        visible: boolean
        title?: string
        qrcode?: string
        /** 弹窗宽度，支持 px、%（如 '400px'、'90%'） */
        width?: string
        /** 二维码下方提示语，可选；用于场景说明（如请用手机浏览器扫码） */
        tip?: string
    }>(),
    {
        title: '扫码支付',
        width: '400px'
    }
)

const emit = defineEmits<{ (e: 'close'): void }>()

const onUpdate = (v: boolean) => {
    if (!v) emit('close')
}

/** qrcode 支持：base64（直接显示）| URL/文本（通过 qrserver 生成二维码） */
const qrcodeUrl = computed(() => {
    if (!props.qrcode) return ''
    const q = props.qrcode.trim()
    if (q.startsWith('data:image') || q.startsWith('data:')) return q
    const clean = q.replace(/\s/g, '')
    if (/^[A-Za-z0-9+/=]+$/.test(clean)) return `data:image/png;base64,${clean}`
    return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(props.qrcode)}`
})

/** 图片是否仍在加载（外链/base64 均会触发 load） */
const imageLoading = ref(false)
const qrcodeImgRef = ref<HTMLImageElement | null>(null)

watch(
    qrcodeUrl,
    async (url) => {
        if (!url) {
            imageLoading.value = false
            return
        }
        imageLoading.value = true
        await nextTick()
        const el = qrcodeImgRef.value
        if (el?.complete && el.naturalWidth > 0) {
            imageLoading.value = false
        }
    },
    { immediate: true }
)

/**
 * 图片加载完成或失败时隐藏加载层
 * @returns {void}
 */
function onImageDone() {
    imageLoading.value = false
}
</script>

<style lang="scss" scoped>
:deep(.qrcode-modal .modal-body) {
    padding: 30px;
    text-align: center;
}

.qrcode-box {
    position: relative;
    width: 200px;
    height: 200px;
    margin: 0 auto;
}

.qrcode-loading {
    position: absolute;
    inset: 0;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f5 100%);
    border-radius: 8px;
    border: 1px solid #e8eaed;
}

.qrcode-spinner {
    width: 36px;
    height: 36px;
    border: 3px solid #e0e4e8;
    border-top-color: var(--color-primary, #409eff);
    border-radius: 50%;
    animation: qrcode-spin 0.75s linear infinite;
}

.qrcode-loading-text {
    margin: 0;
    font-size: 13px;
    color: #868e96;
}

@keyframes qrcode-spin {
    to {
        transform: rotate(360deg);
    }
}

.qrcode-img {
    width: 200px;
    height: 200px;
    display: block;
    margin: 0 auto;
    border-radius: 8px;
    transition: opacity 0.35s ease;
}

.qrcode-img--pending {
    opacity: 0;
    pointer-events: none;
}

.qrcode-placeholder {
    width: 200px;
    height: 200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f5 100%);
    color: #999;
    font-size: 14px;
    border-radius: 8px;
    border: 1px dashed #e0e0e0;
}

.qrcode-tip {
    margin: 16px 0 0;
    padding: 0 8px;
    font-size: 13px;
    line-height: 1.55;
    color: #868e96;
    text-align: center;
}
</style>
