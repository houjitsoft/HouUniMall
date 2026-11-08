<template>
    <!-- 放大镜容器 -->
    <div class="magnifier-container" @mouseenter="showMagnifier" @mouseleave="hideMagnifier" @mousemove="moveMagnifier">
        <!-- 商品大图：有 commonVideo 且当前为第一张时显示视频（自动播放、静音） -->
        <video v-if="isShowingVideo" :src="commonVideo" class="product-main-image" ref="videoRef" autoplay muted
            playsinline loop controls />
        <img v-else :src="currentImage" :alt="imageAlt" class="product-main-image" ref="mainImageRef" />
        <!-- 放大镜遮罩（视频时不显示） -->
        <div v-show="isMagnifierVisible && !isShowingVideo" class="magnifier-cover" :style="coverStyle"></div>
    </div>
    <!-- 放大后的图片（视频时不显示） -->
    <div v-show="isMagnifierVisible && !isShowingVideo" class="magnifier-zoom" :style="[zoomStyle, zoomImageStyle]">
    </div>

    <!-- 图片缩略图 -->
    <div v-if="productImages.length > 0" class="thumbnail-swiper-wrapper">
        <swiper :slides-per-view="'auto'" :space-between="10" :grab-cursor="true">
            <swiper-slide v-for="(img, index) in productImages" :key="index">
                <div class="rank-card" :class="{ active: currentIndex === index }" @click="changeImage(index)">
                    <div class="rank-image">
                        <img :src="img" :alt="`产品图片${index}`" />
                    </div>
                </div>
            </swiper-slide>
        </swiper>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import 'swiper/css'

const props = defineProps({
    currentImage: {
        type: String,
        required: true
    },
    imageAlt: {
        type: String,
        default: '产品图片'
    },
    productImages: {
        type: Array as PropType<string[]>,
        default: () => []
    },
    currentIndex: {
        type: Number,
        default: 0
    },
    /** 商品视频：有值时作为大图第一张自动播放（静音） */
    commonVideo: {
        type: String,
        default: ''
    }
})

const emits = defineEmits(['image-change'])

// refs
const mainImageRef = ref<HTMLImageElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)

// 是否显示视频（commonVideo 有值且当前为第一张）
const isShowingVideo = computed(() => !!props.commonVideo && props.currentIndex === 0)

// 状态
const isMagnifierVisible = ref(false)
const coverStyle = ref({})
const zoomStyle = ref({})
const zoomImageStyle = ref({})

// 图片真实尺寸
const naturalImageSize = { width: 0, height: 0 }

// 常量：放大倍数
const ZOOM_LEVEL = 2

// 初始化图片尺寸
const initImageSize = () => {
    if (!props.currentImage) return
    const img = new Image()
    img.src = props.currentImage
    img.onload = () => {
        naturalImageSize.width = img.naturalWidth
        naturalImageSize.height = img.naturalHeight
    }
}

const showMagnifier = () => {
    // 视频模式或图片未加载时不显示放大镜
    if (props.commonVideo && props.currentIndex === 0) return
    if (naturalImageSize.width === 0) return
    isMagnifierVisible.value = true
}

const hideMagnifier = () => {
    isMagnifierVisible.value = false
}

const moveMagnifier = (e: MouseEvent) => {
    const container = e.currentTarget as HTMLElement
    const rect = container.getBoundingClientRect()
    const img = mainImageRef.value
    if (!img) return

    const imgRect = img.getBoundingClientRect()

    // 鼠标在容器内的坐标
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top

    // === 遮罩计算 ===
    const coverWidth = rect.width / ZOOM_LEVEL
    const coverHeight = rect.height / ZOOM_LEVEL

    let coverX = x - coverWidth / 2
    let coverY = y - coverHeight / 2

    const maxX = rect.width - coverWidth
    const maxY = rect.height - coverHeight
    coverX = Math.max(0, Math.min(coverX, maxX))
    coverY = Math.max(0, Math.min(coverY, maxY))

    coverStyle.value = {
        left: `${coverX}px`,
        top: `${coverY}px`,
        width: `${coverWidth}px`,
        height: `${coverHeight}px`
    }

    // === 放大区域位置 ===
    const zoomSize = rect.width
    zoomStyle.value = {
        left: `${rect.right + 10}px`,
        top: `${rect.top}px`,
        width: `${zoomSize}px`,
        height: `${zoomSize}px`
    }

    // === 背景图计算 ===
    // 图片在容器中的偏移（因 object-fit: contain）
    const imgOffsetX = (rect.width - imgRect.width) / 2
    const imgOffsetY = (rect.height - imgRect.height) / 2

    // 鼠标在图片渲染区域内的坐标
    const xInImg = x - imgOffsetX
    const yInImg = y - imgOffsetY

    // 映射到原图真实坐标（比例换算）
    const realX = (xInImg / imgRect.width) * naturalImageSize.width
    const realY = (yInImg / imgRect.height) * naturalImageSize.height

    // 背景图总尺寸
    const bgWidth = naturalImageSize.width * ZOOM_LEVEL
    const bgHeight = naturalImageSize.height * ZOOM_LEVEL

    // 背景图应显示的中心点对应的左上角偏移
    const bgPosX = realX * ZOOM_LEVEL - zoomSize / 2
    const bgPosY = realY * ZOOM_LEVEL - zoomSize / 2

    // 边界限制
    const maxBgPosX = bgWidth - zoomSize
    const maxBgPosY = bgHeight - zoomSize
    const finalBgPosX = Math.max(0, Math.min(bgPosX, maxBgPosX))
    const finalBgPosY = Math.max(0, Math.min(bgPosY, maxBgPosY))

    zoomImageStyle.value = {
        backgroundImage: `url(${props.currentImage})`,
        backgroundSize: `${bgWidth}px ${bgHeight}px`,
        backgroundPosition: `-${finalBgPosX}px -${finalBgPosY}px`,
        backgroundRepeat: 'no-repeat'
    }
}

const changeImage = (index: number) => {
    if (index >= 0 && index < props.productImages.length) {
        emits('image-change', index)
    }
}

// 切换图片时重新初始化尺寸
watch([() => props.currentImage, () => props.currentIndex, () => props.commonVideo], () => {
    if (!isShowingVideo.value && props.currentImage) {
        initImageSize()
    }
})

onMounted(() => {
    if (!isShowingVideo.value && props.currentImage) {
        initImageSize()
    }
})
</script>

<style lang="scss" scoped>
.magnifier-container {
    position: relative;
    width: 350px;
    height: 350px;
    border: 1px solid #e8e8e8;
    overflow: hidden;
    cursor: crosshair;

    .product-main-image {
        width: 100%;
        height: 100%;
        object-fit: contain;
        display: block;
    }

    video.product-main-image {
        background: #000;
    }

    .magnifier-cover {
        position: absolute;
        background-color: rgba(255, 255, 255, 0.5);
        border: 1px solid #ddd;
        pointer-events: none;
        z-index: 10;
    }
}

.magnifier-zoom {
    position: fixed;
    z-index: 999;
    border: 1px solid #e8e8e8;
    overflow: hidden;
    pointer-events: none;
}

.thumbnail-swiper-wrapper {
    width: 350px;
    margin-top: 15px;
    position: relative;
    overflow: hidden;

    :deep(.swiper-slide) {
        width: auto !important;
    }
}

.rank-card {
    width: 60px;
    background-color: #f8f9fa;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    overflow: hidden;
    border: 2px solid transparent;

    &:hover {
        background-color: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.active {
        border-color: var(--color);
        background-color: white;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .rank-image {
        width: 100%;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }
}
</style>
