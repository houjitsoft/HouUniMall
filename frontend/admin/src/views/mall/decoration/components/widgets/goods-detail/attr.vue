<template>
    <el-form label-width="80px">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商品信息
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">顶部导航</div>
            <el-form-item label="导航样式" class="mt-2">
                <el-radio-group v-model="content.navigation_bar">
                    <!-- <el-radio :label="1">原生 //TODO</el-radio> -->
                    <el-radio :label="1">自定义</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="顶部菜单" class="mt-2">
                <el-checkbox-group v-model="content.top_menu" class="">
                    <el-checkbox :label="1">首页</el-checkbox>
                    <el-checkbox :label="2">搜索</el-checkbox>
                    <el-checkbox :label="3">购物车</el-checkbox>
                    <el-checkbox :label="4">我的收藏</el-checkbox>
                    <el-checkbox :label="5">个人中心</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
            <el-form-item label="顶部分享" class="mt-2">
                <el-radio-group v-model="content.top_share">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品主图</div>
            <el-form-item label="商品主图" class="mt-2">
                <el-radio-group v-model="content.goods_image">
                    <el-radio :label="1">固定方图</el-radio>
                    <el-radio :label="2">高度自适应</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">详情样式</div>
            <div class="mt-4 flex justify-between">
                <div
                    class="card"
                    :class="{ active: content.activeIndex == 1 }"
                    @click="content.activeIndex = 1"
                >
                    普通
                </div>
                <div
                    class="card"
                    :class="{ active: content.activeIndex == 2 }"
                    @click="content.activeIndex = 2"
                >
                    会员
                </div>
                <div
                    class="card"
                    :class="{ active: content.activeIndex == 3 }"
                    @click="content.activeIndex = 3"
                >
                    秒杀
                </div>
                <div
                    class="card"
                    :class="{ active: content.activeIndex == 4 }"
                    @click="content.activeIndex = 4"
                >
                    拼团
                </div>
            </div>
            <div v-if="content.activeIndex == 2" class="mt-4">
                <el-form-item label="商品风格">
                    <el-radio-group v-model="content.vip_style.style">
                        <el-radio :label="1">风格1</el-radio>
                        <el-radio :label="2">风格2</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="活动标题" class="mt-2" v-if="content.vip_style.style == 2">
                    <el-input
                        v-model="content.vip_style.title"
                        maxlength="6"
                        clearable
                        show-word-limit
                    ></el-input>
                </el-form-item>
                <el-form-item label="背景图片" class="mt-2" v-if="content.vip_style.style == 2">
                    <div>
                        <el-radio-group v-model="content.vip_style.image_type">
                            <el-radio :label="1">系统默认</el-radio>
                            <!-- <el-radio :label="2">自定义</el-radio> -->
                        </el-radio-group>
                        <material-picker
                            v-if="content.vip_style.image_type == 2"
                            v-model="content.vip_style.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips" v-if="content.vip_style.image_type == 2">
                            建议图片尺寸：750px*120px
                        </div>
                    </div>
                </el-form-item>
            </div>
            <div v-if="content.activeIndex == 3" class="mt-4">
                <el-form-item label="活动标题" class="mt-2">
                    <el-input
                        v-model="content.seckill_style.title"
                        maxlength="6"
                        clearable
                        show-word-limit
                    ></el-input>
                </el-form-item>
                <el-form-item label="背景图片" class="mt-2">
                    <div>
                        <el-radio-group v-model="content.seckill_style.image_type">
                            <el-radio :label="1">系统默认</el-radio>
                            <!-- <el-radio :label="2">自定义</el-radio> -->
                        </el-radio-group>
                        <material-picker
                            v-if="content.seckill_style.image_type == 2"
                            v-model="content.seckill_style.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips" v-if="content.seckill_style.image_type == 2">
                            建议图片尺寸：750px*120px
                        </div>
                    </div>
                </el-form-item>
            </div>
            <div v-if="content.activeIndex == 4" class="mt-4">
                <el-form-item label="活动标题" class="mt-2">
                    <el-input
                        v-model="content.team_style.title"
                        maxlength="6"
                        clearable
                        show-word-limit
                    ></el-input>
                </el-form-item>
                <el-form-item label="背景图片" class="mt-2">
                    <div>
                        <el-radio-group v-model="content.team_style.image_type">
                            <el-radio :label="1">系统默认</el-radio>
                            <!-- <el-radio :label="2">自定义</el-radio> -->
                        </el-radio-group>
                        <material-picker
                            v-if="content.team_style.image_type == 2"
                            v-model="content.team_style.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips" v-if="content.team_style.image_type == 2">
                            建议图片尺寸：750px*120px
                        </div>
                    </div>
                </el-form-item>
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品信息</div>
            <el-form-item label="商品信息" class="mt-4">
                <el-checkbox-group v-model="content.info" class="">
                    <el-checkbox :label="1">销量</el-checkbox>
                    <el-checkbox :label="2">库存</el-checkbox>
                    <el-checkbox :label="3">浏览量</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
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
const activeIndex = ref(1)
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
.card {
    width: 100px;
    height: 40px;
    line-height: 40px;
    border-radius: 5px;
    border: 1px solid rgba(237, 241, 243, 1);
    text-align: center;
    cursor: pointer;
}
.active {
    border: 1px solid rgba(64, 115, 250, 1);
}
</style>
