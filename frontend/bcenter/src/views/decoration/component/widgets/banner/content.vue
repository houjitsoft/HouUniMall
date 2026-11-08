<template>
    <div class="banner">
        <div class="banner-image">
            <decoration-img
                width="100%"
                :height="content.image_type == 2 ? '550px' : imageHeght + 'px'"
                :src="getImage"
                fit="cover"
                @Load="handleLoad"
            />
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'
import type options from './options'
import DecorationImg from '../../decoration-img.vue'
type OptionsType = ReturnType<typeof options>
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    }
})
const imageHeght = ref(170)
const handleLoad = (height: any) => {
    imageHeght.value = height / 2
}
const getImage = computed(() => {
    const { data } = props.content
    if (Array.isArray(data)) {
        return data[0] ? data[0].image : ''
    }
    return ''
})
</script>

<style lang="scss" scoped></style>
