<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                推荐商品
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择商品</div>
            <div class="flex-1 w-full bg-[#f7f7f7] p-2 rounded-lg pt-6">
                <el-form-item label="商品来源">
                    <div class="w-full">
                        <el-radio-group v-model="content.get_type">
                            <el-radio :label="1">自动获取</el-radio>
                            <!-- <el-radio :label="2">手动选择</el-radio> -->
                        </el-radio-group>
                        <!-- <div class="flex-1 w-full">
                            <seckillgoods-select
                                v-model="content.goodsLists"
                                v-if="content.get_type === 2"
                            ></seckillgoods-select>
                        </div> -->
                    </div>
                </el-form-item>
                <el-form-item label="显示数量" v-if="content.get_type === 1">
                    <el-slider v-model="content.showNums" show-input :max="20" />
                </el-form-item>
            </div>
        </el-card>

        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品排序</div>
            <el-form-item label="排序方式">
                <el-radio-group v-model="content.sort_type">
                    <el-radio :label="1">默认排序</el-radio>
                    <el-radio :label="2">自定义排序</el-radio>
                </el-radio-group>
                <div class="w-full">
                    <el-select v-model="content.sort" class="w-full" v-if="content.sort_type == 2">
                        <el-option label="综合" value="" />
                        <el-option label="按销量" value="sale_desc" />
                        <el-option label="访问量" value="visit_desc" />
                        <el-option label="价格降序" value=" price_desc" />
                        <el-option label="价格升序" value=" price_asc" />
                        <el-option label="成交转化率" value="sale_change" />
                    </el-select>
                </div>
            </el-form-item>
        </el-card>
    </el-form>
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
:deep(.el-card__body) {
    width: 100%;
}
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
.add-btn {
    width: 25px;
    height: 25px;
    line-height: 23px;
    border-radius: 50%;
    background-color: #1a69fc;
    text-align: center;
    color: white;
    font-size: 16px;
}
.cart-btn {
    width: 25px;
    height: 25px;
    border-radius: 50%;
    background-color: #1a69fc;
}
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
</style>
