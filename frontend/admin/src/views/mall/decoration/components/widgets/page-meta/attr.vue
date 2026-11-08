<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                页面设置
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">背景设置</div>
            <el-form-item label="页面设置">
                <el-radio-group v-model="contentData.title_type">
                    <el-radio :label="0">继承网站风格</el-radio>
                    <el-radio :label="1">无</el-radio>
                    <el-radio :label="2">自定义背景</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="背景色" v-if="contentData.title_type == 2">
                <color-picker v-model="contentData.bg_color" defaultColor="#F8F8F8" />
            </el-form-item>
            <el-form-item label="背景图片" v-if="contentData.title_type == 2">
                <div>
                    <material-picker v-model="contentData.bg_img" :limit="1" size="100px" />
                    <div class="form-tips">建议图片尺寸：750px*高度不限</div>
                </div>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">头部设置</div>
            <el-form-item label="功能模块">
                <el-radio-group v-model="contentData.module_type">
                    <el-radio :label="1">无</el-radio>
                    <!-- <el-radio :label="2">有</el-radio> -->
                </el-radio-group>
            </el-form-item>
            <el-form-item label="头部标题" v-if="contentData.module_type == 1">
                <el-input
                    v-model="contentData.title"
                    placeholder="请输入页面标题"
                    show-word-limit
                    maxlength="6"
                ></el-input>
            </el-form-item>
            <el-form-item label="标题颜色" v-if="contentData.module_type == 1">
                <el-radio-group v-model="contentData.title_color">
                    <el-radio :label="1">黑色</el-radio>
                    <el-radio :label="2">白色</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="头部背景">
                <div class="w-full">
                    <div>
                        <el-radio-group v-model="contentData.titlebg_type">
                            <el-radio :label="1">纯色</el-radio>
                            <el-radio :label="2">背景图片</el-radio>
                        </el-radio-group>
                    </div>

                    <color-picker
                        v-model="contentData.titlebg_color"
                        defaultColor="#FFFFFF"
                        v-if="contentData.titlebg_type == 1"
                    />
                    <div v-if="contentData.titlebg_type == 2">
                        <material-picker
                            v-model="contentData.titlebg_img"
                            :limit="1"
                            size="100px"
                        />
                        <div class="form-tips">建议图片尺寸：750*190px</div>
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="滚动后头部背景">
                <div class="w-full">
                    <el-radio-group v-model="contentData.scroll_type">
                        <el-radio :label="1">纯色</el-radio>
                        <el-radio :label="2">背景图片</el-radio>
                    </el-radio-group>
                    <color-picker
                        v-model="contentData.scroll_color"
                        defaultColor="#FFFFFF"
                        v-if="contentData.scroll_type == 1"
                    />
                    <div v-if="contentData.scroll_type == 2">
                        <material-picker v-model="contentData.scroll_img" :limit="1" size="100px" />
                        <div class="form-tips">建议图片尺寸：750*190px</div>
                    </div>
                </div>
            </el-form-item>
            <template v-if="false">
                <div class="bg-[#eaeff1] p-4 rounded-lg">
                    <div class="font-medium">左侧模块</div>
                    <el-form-item label="内容" class="mt-2">
                        <el-radio-group v-model="contentData.left.content_type">
                            <el-radio :label="1">不显示</el-radio>
                            <el-radio :label="2">图片</el-radio>
                            <el-radio :label="3">搜索框</el-radio>
                            <el-radio :label="4">定位</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="填充背景">
                        <color-picker
                            v-model="contentData.left.content_bg"
                            defaultColor="#FFFFFF"
                        />
                    </el-form-item>
                    <el-form-item label="边框背景">
                        <color-picker v-model="contentData.left.border_bg" defaultColor="#EDF1F3" />
                    </el-form-item>
                    <el-form-item label="图标背景">
                        <color-picker v-model="contentData.left.icon_bg" defaultColor="#FFFFFF" />
                    </el-form-item>
                    <el-form-item label="文字颜色">
                        <color-picker
                            v-model="contentData.left.text_color"
                            defaultColor="#888888"
                        />
                    </el-form-item>
                    <el-form-item label="默认文字">
                        <el-input
                            v-model="contentData.left.text"
                            placeholder="请输入默认文字"
                            show-word-limit
                            maxlength="10"
                        ></el-input>
                    </el-form-item>
                </div>
            </template>
        </el-card>
    </el-form>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { computed } from 'vue'



const emits = defineEmits(['update:content'])
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

const contentData = computed({
    get: () => props.content,
    set: (newValue) => {
        emits('update:content', newValue)
    }
})
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
