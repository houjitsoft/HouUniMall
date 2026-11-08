<template>
    <div>
        <div class="max-w-[400px]" v-for="(item, index) in modelValue" :key="index">
            <div class="form-tips">{{ navTitle[index] }}</div>
            <div class="flex items-center w-full p-4 mb-4 bg-fill-light">
                <div class="flex-1 ml-3">
                    <div class="flex">
                        <span class="flex-none mr-3 text-tx-regular">菜单图标</span>
                        <material-picker
                            v-model="item.image"
                            upload-class="bg-body"
                            size="60px"
                            exclude-domain
                        >
                            <template #upload>
                                <div class="upload-btn w-[60px] h-[60px]">
                                    <icon name="el-icon-Plus" :size="20" />
                                </div>
                            </template>
                        </material-picker>
                    </div>
                    <div class="flex mt-[18px]">
                        <span class="flex-none mr-3 text-tx-regular">菜单名称</span>
                        <!-- <link-picker v-model="item.link" /> -->
                        <el-input v-model="item.name" class="pr-2"></el-input>
                        <el-checkbox class="m" label="显示" v-model="item.display"></el-checkbox>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'
// import useAppStore from '@/stores/modules/app'

// const appStore = useAppStore()
/**
 * 图片地址拼接。不调 appStore.getImageUrl()：该 Pinia action 在装修编辑器
 * 这个懒加载 chunk 里取不到（报 "o.getImageUrl is not a function"）。
 * 直接读 state 的 oss_domain 自己拼，逻辑与 store action 一致。
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

const props = defineProps({
    modelValue: {
        type: Array,
        default: () => []
    },
    max: {
        type: Number,
        default: 10
    },
    min: {
        type: Number,
        default: 1
    }
})

const navTitle = ['待付款', '待发货', '待收货', '商品评价', '售后退款']

const handleAdd = () => {
    if (props.modelValue?.length < props.max) {
        props.modelValue.push({
            image: '',
            name: '导航名称',
            link: {}
        })
    } else {
        feedback.msgError(`最多添加${props.max}个`)
    }
}
const handleDelete = (index) => {
    if (props.modelValue?.length <= props.min) {
        return feedback.msgError(`最少保留${props.min}个`)
    }
    props.modelValue.splice(index, 1)
}
</script>

<style lang="scss" scoped></style>
