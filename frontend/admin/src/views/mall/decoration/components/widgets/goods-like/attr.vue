<template>
    <div>
        <el-form label-width="70px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">关闭</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="选择商品">
                <el-radio-group v-model="content.goods_type">
                    <el-radio :label="1">自动获取</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="显示数量">
                <div>
                    <div class="pb-[30px] flex">
                        <el-slider class="mr-[10px]" style="width: 200px" v-model="content.goods_num" :min="1"
                            :max="20">
                        </el-slider>
                        <el-input-number style="width: 90px" v-model="content.goods_num" controls-position="right"
                            :min="1" :max="20"></el-input-number>
                    </div>
                    <div
                        class="w-[300px] text-primary px-[12px] pt-[8px] pb-[14px] leading-5 rounded-[3px] bg-primary-light-9">
                        根据系统算法，优先显示同分类且销量高且排序在前的商品。优先级为：销量>浏览量>创建时间来排序
                    </div>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'
const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    }
})

</script>

<style lang="scss" scoped>

</style>
