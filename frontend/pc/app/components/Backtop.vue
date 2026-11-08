<template>
    <div class="backtop" :class="{ 'backtop--visible': state.isVisible }" @click="scrollToTop">
        <div class="backtop-icon">🔝</div>
    </div>
</template>

<script setup lang="ts">
import { onBeforeMount, onMounted, reactive, onUnmounted } from 'vue'

const emits = defineEmits([])
const props = defineProps({
    // 滚动多少像素后显示返回顶部按钮
    showThreshold: {
        type: Number,
        default: 300
    },
    // 动画持续时间（毫秒）
    duration: {
        type: Number,
        default: 500
    }
})

const state = reactive({
    isVisible: false
})

// 监听滚动事件
const handleScroll = () => {
    state.isVisible = window.scrollY > props.showThreshold
}

// 滚动到顶部
const scrollToTop = () => {
    const startPosition = window.scrollY
    const startTime = performance.now()

    const animateScroll = (currentTime: number) => {
        const elapsedTime = currentTime - startTime
        const progress = Math.min(elapsedTime / props.duration, 1)
        const easeProgress = easeInOutCubic(progress)

        window.scrollTo({
            top: startPosition * (1 - easeProgress)
        })

        if (progress < 1) {
            requestAnimationFrame(animateScroll)
        }
    }

    requestAnimationFrame(animateScroll)
}

// 缓动函数（ easeInOutCubic）
const easeInOutCubic = (t: number) => {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
}

onMounted(() => {
    window.addEventListener('scroll', handleScroll)
    // 初始化时检查滚动位置
    handleScroll()
})

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
})

defineExpose({ state })

</script>
<style lang="scss" scoped>
.backtop {
    position: fixed;
    bottom: 30px;
    right: 20px;
    width: 50px;
    height: 50px;
    background-color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    cursor: pointer;
    opacity: 0;
    visibility: hidden;
    transform: translateY(20px);
    transition: all 0.3s ease;
    z-index: 1001;

    &--visible {
        opacity: 1;
        visibility: visible;
        transform: translateY(0);
    }

    &:hover {
        background-color: #f8f8f8;
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
        transform: translateY(-2px);
    }

    &:active {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }

    .backtop-icon {
        font-size: 22px;
        color: #333;
        font-weight: bold;
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .backtop {
        bottom: 20px;
        right: 20px;
        width: 44px;
        height: 44px;

        .backtop-icon {
            font-size: 18px;
        }
    }
}

@media (max-width: 480px) {
    .backtop {
        bottom: 15px;
        right: 15px;
        width: 40px;
        height: 40px;

        .backtop-icon {
            font-size: 16px;
        }
    }
}
</style>