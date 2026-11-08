<template>
    <view class="canvas-part skeleton" :style="style">
        <canvas canvas-id="qrcode" :style="style"></canvas>
    </view>
</template>

<script setup>
import {
    computed,
    getCurrentInstance,
    onMounted,
    ref,
    watch
} from 'vue';
import UQRCode from 'uqrcodejs';

const instance = getCurrentInstance();

const props = defineProps({
    size: {
        type: Number,
        default: 200
    },
    margin: {
        type: Number,
        default: 10
    },
    content: {
        type: String,
        default: ''
    }
});

watch(props.content, (newVal) => {
    if (newVal) {
        console.log("二维码：", newVal);

        initQrcode(newVal);
    }
}, {
    deep: true
});

// 样式
const style = computed(() => {
    const size = `${props.size}rpx`;
    return {
        width: size,
        height: size
    };
});

// Canvas图片地址
const canvasImg = ref('');

onMounted(() => {
    if (props.content) {
        initQrcode(props.content);
    }
});

/**
 * 初始化二维码
 * @param content 二维码内容
 */
const initQrcode = (content) => {
    const pxSize = uni.upx2px(props.size);
    const qr = new UQRCode();
    qr.setOptions({
        data: content,
        size: pxSize,
        margin: props.margin
    });
    qr.make();
    const ctx = uni.createCanvasContext('qrcode', instance?.proxy);
    qr.canvasContext = ctx;
    qr.draw(); // 不需要转图片
};

// 导出初始化方法，使用ref方式调用组件
defineExpose({
    initQrcode
});
</script>

<style scoped lang="scss">
.canvas-part {
    width: 200rpx;
    height: 200rpx;
    margin: 0 auto;
    position: relative;

    .canvas-img {
        width: 100%;
        height: 100%;
    }

    .canvas-code {
        position: absolute;
        left: 999999px;
    }
}

.skeleton {
    background: linear-gradient(90deg, #f0f2f5 25%, #e6e8eb 37%, #f0f2f5 63%);
    background-size: 400% 100%;
    animation: el-skeleton-loading 1.4s ease infinite;
    border-radius: 8rpx;
}

@keyframes el-skeleton-loading {
    0% {
        background-position: 100% 50%;
    }

    to {
        background-position: 0 50%;
    }
}
</style>