<template>
    <el-form-item label="商家名称">
        {{ modelValue.name }}
    </el-form-item>
    <el-form-item label="商家类型">
        {{ modelValue.type_text }}
    </el-form-item>
    <el-form-item label="主营类目">
        {{ modelValue.shop_category.name }}
    </el-form-item>
    <el-form-item label="商家评分">
        <el-rate
            v-model="modelValue.score"
            disabled
            class="el__rate"
            show-score
            text-color="#ff9900"
            score-template="{value} 分"
        />
    </el-form-item>
    <el-form-item label="店铺LOGO" prop="logo">
        <material-picker v-model="modelValue.logo" :limit="1" />
    </el-form-item>
    <el-form-item label="联系人" prop="contact">
        <div class="w-80">
            <el-input v-model="modelValue.contact" placeholder="请输入联系人名称" clearable />
        </div>
    </el-form-item>
    <el-form-item label="联系电话" prop="mobile">
        <div>
            <div class="flex" v-for="(item, index) in modelValue.mobile" :key="index">
                <DelWrap @close="removeMobile(index)">
                    <el-input
                        class="mt-2 w-80"
                        v-model="modelValue.mobile[index]"
                        placeholder="请输入联系人电话"
                        clearable
                    />
                </DelWrap>
                <el-button
                    type="primary"
                    link
                    class="ml-2"
                    @click.prevent="addMobile"
                    v-if="index == 0"
                    >+添加多个号码</el-button
                >
            </div>
            <div class="form-tips">填写联系人手机号码，商家通知会短信发送至联系人手机</div>
        </div>
    </el-form-item>
    <el-form-item label="商家简介">
        <div class="w-80">
            <el-input
                v-model="modelValue.desc"
                placeholder="请输入商家简介"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }"
                show-word-limit
                clearable
            />
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
const props = withDefaults(
    defineProps<{
        modelValue?: any
    }>(),
    {
        modelValue: {}
    }
)

const addMobile = () => {
    props.modelValue.mobile.push('')
}

const removeMobile = (index: number) => {
    if (props.modelValue.mobile.length <= 1) return
    props.modelValue.mobile.splice(index, 1)
}
</script>

<style>
.el__rate .el-rate__item {
    line-height: 10px;
}
</style>
