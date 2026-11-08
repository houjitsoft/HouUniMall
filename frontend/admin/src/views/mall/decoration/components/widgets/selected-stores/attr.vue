<template>
    <div>
        <el-form label-width="100px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <div class="muted text-lg font-medium">店铺</div>
            <el-form-item label="店铺">
                <el-radio-group v-model="content.getType">
                    <el-radio :label="1">自动获取</el-radio>
                    <el-radio :label="0" v-show="false">手动添加</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="显示数量">
                <div class="w-[400px]">
                    <div class="flex">
                        <el-slider
                            class="w-[250px]"
                            v-model="content.showNum"
                            :max="10"
                            :min="1"
                            show-input
                        />
                    </div>
                    <!-- <div class="p-2 bg-primary-light-9 rounded text-primary mt-2">
                        <div>
                            根据系统算法，优先显示销量高且排序在前的商品。优先级为：销量>浏览量>创建时间来排序
                        </div>
                    </div> -->
                </div>
            </el-form-item>
            <div class="muted text-lg font-medium">展示风格</div>
            <el-form-item label="排序方式">
                <div>
                    <el-radio-group v-model="content.sortType">
                        <el-radio label="1" size="medium" border>列表模式</el-radio>
                        <el-radio label="2" size="medium" border>商品模式</el-radio>
                    </el-radio-group>
                </div>
            </el-form-item>
            <el-form-item label="店铺排序">
                <div>
                    <el-select v-model="content.sort" placeholder="综合">
                        <el-option label="综合" value="1"> 综合</el-option>
                        <el-option label="按销量" value="2"> 按销量</el-option>
                        <el-option label="访问量" value="3"> 访问量</el-option>
                        <el-option label="在售商品数" value="4"> 在售商品数</el-option>
                        <el-option label="入驻时间升序" value="5"> 入驻时间升序</el-option>
                        <el-option label="入驻时间降序" value="6"> 入驻时间降序</el-option>
                        <el-option label="店铺成交转化率" value="7"> 店铺成交转化率</el-option>
                    </el-select>
                </div>
            </el-form-item>
            <div class="muted text-lg font-medium">头部设置</div>

            <el-form-item label="标题">
                <el-input
                    class="w-[230px]"
                    v-model="content.title"
                    show-word-limit
                    maxlength="10"
                ></el-input>
            </el-form-item>
            <el-form-item label="标题颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.topTitleColor"
                        defaultColor="#000000"
                    ></color-picker>
                </view>
            </el-form-item>
            <el-form-item label="副标题">
                <el-input
                    class="w-[230px]"
                    v-model="content.subtitle"
                    show-word-limit
                    maxlength="15"
                ></el-input>
            </el-form-item>
            <el-form-item label="副标题颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.topSubTitleColor"
                        defaultColor="#666666"
                    ></color-picker>
                </view>
            </el-form-item>
            <div class="muted text-lg font-medium">更多按钮</div>
            <el-form-item label="按钮显示">
                <el-radio-group v-model="content.moreenabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="按钮文字">
                <el-input
                    class="w-[230px]"
                    v-model="content.moretitle"
                    show-word-limit
                    maxlength="10"
                ></el-input>
            </el-form-item>
            <el-form-item label="按钮颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.moreTitleColor"
                        defaultColor="#666666"
                    ></color-picker>
                </view>
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

<style lang="scss" scoped></style>
