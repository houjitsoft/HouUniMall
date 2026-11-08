<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                选项卡
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选项卡设置</div>
            <div
                class="w-full p-2 rounded-lg border-box mb-2"
                :class="{ active: activeIndex == index }"
                v-for="(item, index) in content.data"
                :key="item.title"
                @click="activeIndex = index"
            >
                {{ item.title }}
            </div>
            <el-button type="primary" @click="handleAdd" class="w-full mt-4">添加选项卡</el-button>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>选项卡内容</span>
            </div>
            <div class="flex-1 w-full bg-[#f7f7f7] p-2 rounded-lg pt-6">
                <el-form-item label="标题">
                    <el-input
                        v-model="content.data[activeIndex].title"
                        placeholder="请输入标题"
                        show-word-limit
                        maxlength="6"
                    ></el-input>
                </el-form-item>
                <el-form-item label="副标题" class="mt-4">
                    <el-input
                        v-model="content.data[activeIndex].sub_title"
                        placeholder="请输入副标题"
                        show-word-limit
                        maxlength="8"
                    ></el-input>
                </el-form-item>
                <el-form-item label="商品来源" class="mt-4">
                    <div>
                        <el-radio-group v-model="content.data[activeIndex].goods_source">
                            <!-- <el-radio :label="1">自动获取</el-radio> -->
                            <el-radio :label="1">手动选择</el-radio>
                        </el-radio-group>
                        <div class="flex-1">
                            <goods-select
                                v-model="content.data[activeIndex].goodsLists"
                            ></goods-select>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="显示数量" class="mt-4">
                    <el-slider
                        v-model="content.data[activeIndex].goods_num"
                        show-input
                        :max="100"
                    />
                </el-form-item>
            </div>
        </el-card>
        <!-- <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>商品排序</span>
            </div>
            <el-form-item label="排序方式" class="mt-4">
                <div>
                    <div>
                        <el-radio-group v-model="content.sort_type">
                            <el-radio :label="1">默认排序</el-radio>
                            <el-radio :label="2">自定义排序</el-radio>
                        </el-radio-group>
                    </div>
                    <div>
                        <el-select
                            v-model="content.use_sort"
                            v-if="content.sort_type == 2"
                            class="w-full"
                        >
                            <el-option label="综合" :value="1" />
                            <el-option label="按销量" :value="2" />
                            <el-option label="访问量" :value="3" />
                            <el-option label="价格升序" :value="4" />
                            <el-option label="价格降序" :value="5" />
                            <el-option label="成交转化率" :value="6" />
                        </el-select>
                    </div>

                    <div class="form-tips">商品将按照系统预设的顺序进行展示</div>
                </div>
            </el-form-item>
        </el-card> -->
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>选项风格</span>
            </div>
            <el-radio-group v-model="content.choice_type">
                <el-radio :label="1" border>风格1</el-radio>
                <!-- <el-radio :label="2" border>风格2</el-radio> -->
            </el-radio-group>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>展示方式</span>
            </div>
            <el-radio-group v-model="content.show_type">
                <el-radio :label="1" border>单列</el-radio>
                <el-radio :label="2" border>双列</el-radio>
            </el-radio-group>
        </el-card>
    </el-form>
</template>
<script setup>
// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref } from 'vue'
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
const activeIndex = ref(0)
const handleAdd = () => {
    props.content.data.push({
        title: '口碑推荐',
        sub_title: '好货专区',
        goods_source: 1,
        goods_num: 10
    })
}
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
</style>
