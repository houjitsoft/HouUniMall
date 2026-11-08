<template>
    <el-image :style="styles" v-bind="props" :src="getImageUrl(src)" @load="handleLoad">
        <template #placeholder>
            <div class="image-slot"></div>
        </template>
        <template #error>
            <div class="image-slot">
                <icon name="el-icon-Picture" :size="30" />
            </div>
        </template>
    </el-image>
</template>

<script setup>
import { computed } from 'vue'
import { addUnit } from '@/utils/util'
import { imageProps } from 'element-plus'
import useAppStore from '@/stores/modules/app'
const emit = defineEmits(['Load'])

const props = defineProps({
    width: {
        type: [String, Number],
        default: 'auto'
    },
    height: {
        type: [String, Number],
        default: 'auto'
    },
    radius: {
        type: [String, Number],
        default: 0
    },
    ...imageProps
})
const handleLoad = (e) => {
    emit('Load', e.target.naturalHeight)
}
const appStore = useAppStore()
/**
 * 图片地址拼接。
 * 注意：不能调 appStore.getImageUrl() —— 该 Pinia action 在装修编辑器这个
 * 懒加载 chunk 里取不到（压缩后报 "o.getImageUrl is not a function"）。
 * 这里直接读 state 里的 oss_domain 自己拼，逻辑与 store action 完全一致，
 * 且不依赖 this 绑定与 action 注册时机。
 */
const getImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    let domain = ''
    try {
        domain = appStore.config?.oss_domain || ''
    } catch (e) {
        domain = ''
    }
    return domain + url
}
const styles = computed(() => {
    return {
        width: addUnit(props.width),
        height: addUnit(props.height),
        borderRadius: addUnit(props.radius)
    }
})
</script>

<style lang="scss" scoped>
.el-image {
    display: block;

    .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        background: #fafafa;
        color: #909399;
    }
}
</style>
