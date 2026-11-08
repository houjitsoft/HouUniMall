<template>
    <el-scrollbar height="70vh">
        <el-form label-width="100px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="标题">
                <el-input v-model="content.topTitle" placeholder="请输入名称" class="w-[230px]" />
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
                    v-model="content.topSubtitle"
                    placeholder="请输入名称"
                    class="w-[230px]"
                />
            </el-form-item>
            <el-form-item label="副标题颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.topSubTitleColor"
                        defaultColor="#666666"
                    ></color-picker>
                </view>
            </el-form-item>
            <el-form-item label="榜单设置">
                <div class="flex-1">
                    <del-wrap
                        v-for="(item, index) in content.data"
                        :key="index"
                        @close="handleDelete(index)"
                        class="max-w-[400px]"
                    >
                        <div class="w-full p-4 mt-4 bg-fill-light">
                            <el-form-item label="榜单图片">
                                <material-picker
                                    v-model="item.cover"
                                    upload-class="bg-body"
                                    exclude-domain
                                    :limit="1"
                                >
                                    <template #upload>
                                        <div
                                            class="upload-btn"
                                            :style="{
                                                width: '100px',
                                                height: '100px'
                                            }"
                                        >
                                            <icon :size="25" name="el-icon-Plus" />
                                            <span class="mt-1">封面图片</span>
                                        </div>
                                    </template>
                                </material-picker>
                                <material-picker
                                    v-model="item.list_image"
                                    upload-class="bg-body"
                                    exclude-domain
                                    :limit="1"
                                >
                                    <template #upload>
                                        <div
                                            class="upload-btn"
                                            :style="{
                                                width: '100px',
                                                height: '100px'
                                            }"
                                        >
                                            <icon :size="25" name="el-icon-Plus" />
                                            <span class="mt-1">榜单背景图</span>
                                        </div>
                                    </template>
                                </material-picker>
                            </el-form-item>
                            <el-form-item label="榜单标题" class="mt-2">
                                <el-input v-model="item.title" placeholder="请输入名称" />
                            </el-form-item>
                            <el-form-item label="副标题" class="mt-2">
                                <el-input v-model="item.subhead" placeholder="请输入名称" />
                            </el-form-item>
                            <el-form-item label="榜单背景" class="mt-2">
                                <div>
                                    <el-radio-group v-model="item.background_type">
                                        <el-radio :label="1">背景颜色</el-radio>
                                        <el-radio :label="2">背景图片</el-radio>
                                    </el-radio-group>

                                    <div>
                                        <el-input
                                            v-if="item.background_type == 1"
                                            v-model="item.background_color"
                                            placeholder="请输入"
                                        >
                                            <template #append>
                                                <el-color-picker v-model="item.background_color" />
                                            </template>
                                        </el-input>

                                        <template v-else-if="item.background_type == 2">
                                            <material-picker
                                                v-model="item.background_image"
                                                upload-class="bg-body"
                                                exclude-domain
                                                :limit="1"
                                            >
                                            </material-picker>
                                            <div class="form-tips">推荐尺寸： 216px * 336px</div>
                                        </template>
                                    </div>
                                </div>
                            </el-form-item>
                            <el-form-item class="mt-2" label="链接地址">
                                <link-picker v-model="item.link" />
                            </el-form-item>
                        </div>
                    </del-wrap>
                </div>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleAdd">添加榜单</el-button>
            </el-form-item>
        </el-form>
    </el-scrollbar>
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

const handleAdd = () => {
    props.content.data.push({
        cover: '',
        background_image: '',
        title: '榜单标题',
        subhead: '副标题',
        background_color: 'red',
        link: {}
    })
}
const handleDelete = (index) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss">
.el-input-group__append,
.el-input-group__prepend {
    padding: 0;
}
</style>
