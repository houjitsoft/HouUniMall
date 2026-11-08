<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                秒杀
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择商品</div>
            <el-form-item label="商品来源">
                <el-radio-group v-model="content.getType">
                    <el-radio :label="1">自动获取</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="显示数量">
                <el-slider v-model="content.showNums" show-input :max="20" />
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品排序</div>
            <el-form-item label="排序方式">
                <div class="w-full">
                    <el-radio-group v-model="content.sortType">
                        <el-radio :label="1">默认排序</el-radio>
                        <el-radio :label="2">自定义排序</el-radio>
                    </el-radio-group>
                    <div class="w-full">
                        <el-select
                            v-model="content.sort"
                            class="w-full"
                            v-if="content.sortType == 2"
                        >
                            <el-option label="综合" value="" />
                            <el-option label="按销量" value="sale_desc" />
                            <el-option label="访问量" value="visit_desc" />
                            <el-option label="价格降序" value=" price_desc" />
                            <el-option label="价格升序" value=" price_asc" />
                            <el-option label="成交转化率" value="sale_change" />
                        </el-select>
                    </div>
                    <div class="form-tips">商品将按照系统预设的顺序进行展示</div>
                </div>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示方式</div>
            <el-radio-group v-model="content.showType">
                <el-radio :label="1" border class="w-[120px] mb-2">单列</el-radio>
                <el-radio :label="2" border class="w-[120px] mb-2">两列</el-radio>
                <el-radio :label="3" border class="w-[120px] mb-2">横向滑动-A</el-radio>
                <el-radio :label="4" border class="w-[120px] mb-2">横向滑动-B</el-radio>
            </el-radio-group>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">头部设置</div>
            <el-form-item label="标题类型">
                <div>
                    <el-radio-group v-model="content.titleType">
                        <el-radio :label="1">文字</el-radio>
                        <el-radio :label="2">图片</el-radio>
                    </el-radio-group>
                    <material-picker
                        v-model="content.titile_img"
                        upload-class="bg-body"
                        exclude-domain
                        v-if="content.titleType == 2"
                    />
                    <div class="form-tips" v-if="content.titleType == 2">
                        建议图片尺寸：56px*20px
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="标题文字">
                <el-input
                    v-model="content.title"
                    show-word-limit
                    maxlength="6"
                    placeholder="请输入标题文字"
                ></el-input>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">更多按钮</div>
            <el-form-item label="右侧按钮">
                <el-radio-group v-model="content.rightBtn">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="按钮文字">
                <el-input
                    v-model="content.rightTitle"
                    show-word-limit
                    maxlength="4"
                    placeholder="请输入标题文字"
                ></el-input>
            </el-form-item>
            <el-form-item label="按钮文字">
                <color-picker v-model="content.rightcolor" defaultColor="#88888" />
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>
import feedback from '@/utils/feedback'
// import goodsSelect from '@/components/goods-select.vue'
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
:deep(.el-card__body) {
    width: 100%;
}
</style>
