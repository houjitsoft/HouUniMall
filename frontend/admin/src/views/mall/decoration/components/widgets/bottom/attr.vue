<template>
    <el-form label-width="80px">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                底部菜单
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">左侧模块</div>
            <el-form-item label="显示内容">
                <el-checkbox-group v-model="content.content" class="" @change="handleChange">
                    <el-checkbox :label="1" :disabled="disabled(1)">首页</el-checkbox>
                    <el-checkbox :label="2" :disabled="disabled(2)">客服</el-checkbox>
                    <el-checkbox :label="3" :disabled="disabled(3)">收藏</el-checkbox>
                    <el-checkbox :label="4" :disabled="disabled(4)">购物车</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">右侧模块</div>
            <div class="bg-[#f8f8f8] p-4 rounded-lg">
                <div class="font-medium">加入购物车按钮</div>
                <div class="mt-4">
                    <el-form-item label="加购按钮">
                        <el-radio-group v-model="content.add_btn.show">
                            <el-radio :label="1" size="medium">显示</el-radio>
                            <el-radio :label="0" size="medium">隐藏</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="内容">
                        <el-input
                            v-model="content.add_btn.title"
                            maxlength="6"
                            clearable
                            show-word-limit
                        ></el-input>
                    </el-form-item>
                </div>
            </div>
            <div class="bg-[#f8f8f8] p-4 rounded-lg mt-4">
                <div class="font-medium">立即购买按钮</div>
                <div class="mt-4">
                    <el-form-item label="加购按钮">
                        <el-radio-group v-model="content.buy_btn.show">
                            <el-radio :label="1" size="medium">显示</el-radio>
                            <el-radio :label="0" size="medium">隐藏</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="内容">
                        <el-input
                            v-model="content.buy_btn.title"
                            maxlength="6"
                            clearable
                            show-word-limit
                        ></el-input>
                    </el-form-item>
                </div>
            </div>
        </el-card>
    </el-form>
</template>
<script setup>
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
const handleChange = (e) => {
    console.log(e)
}
const disabled = (index) => {
    if (props.content.content.includes(index)) {
        return false
    } else if (props.content.content.length > 2) {
        return true
    }
}
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
</style>
