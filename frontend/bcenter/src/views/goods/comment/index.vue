<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：用户评价的列表，管理员可以回复用户的评价，允许修改已回复过的内容；设置该条评论是否要在前端隐藏或显示。"
                :closable="false"
                show-icon
            />

            <el-form
                ref="formRef"
                class="mb-[-16px] mt-[16px]"
                :model="queryParams"
                :inline="true"
            >
                <el-form-item label="评价信息">
                    <el-input
                        v-model="queryParams.key_word"
                         placeholder="请输入"
                        class="w-[320px]"
                        @keyup.enter="resetPage" 
                    >
                        <template #prepend>
                            <el-select v-model="queryParams.key_word_type" placeholder="选择" style="width: 100px">
                                <el-option label="商品名称" :value="1" />
                                <el-option label="用户编号" :value="2" />
                                <el-option label="用户昵称" :value="3" />
                            </el-select>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="评价等级">
                    <el-select class="w-[280px]" v-model="queryParams.level">
                        <el-option label="全部" value="" />
                        <el-option label="好评" value="good" />
                        <el-option label="中评" value="middle" />
                        <el-option label="差评" value="bad" />
                    </el-select>
                </el-form-item>
                <el-form-item label="评价状态">
                    <el-select class="w-[280px]" v-model="queryParams.is_show">
                        <el-option label="全部" value="" />
                        <el-option label="显示" :value="1" />
                        <el-option label="隐藏" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item label="评价时间">
                    <daterange-picker
                        v-model:startTime="queryParams.start_time"
                        v-model:endTime="queryParams.end_time"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage"
                        >查询</el-button
                    >
                    <el-button @click="resetParams">重置</el-button>
                    <ai-agent-panel
                        class="ml-2"
                        code="comment_sentiment"
                        label="AI 分析本页评价"
                        :payload="buildCommentPayload"
                    />
                </el-form-item>
            </el-form>
        </el-card>
        <el-card
            class="!border-none mt-4"
            :body-style="{ 'padding-top': '10px' }"
            shadow="never"
        >
            <!-- 选项卡 -->
            <el-tabs
                v-model="queryParams.is_reply"
                class="demo-tabs"
                @tab-change="changeTabs"
            >
                <template
                    v-for="(tabsItem, tabsIndex) in tabsParams.TabsEnumMap" :key="tabsIndex"
                >
                    <el-tab-pane :label="`${tabsItem.label}(${pager.extend[tabsItem.type] || 0 })`"
                        :name="tabsItem.name" />
                </template>
            </el-tabs>

            <!-- 表格 -->
            <el-table
                size="large"
                v-loading="pager.loading"
                :data="pager.lists"
            >
                <el-table-column label="评价商品" min-width="180">
                    <template #default="{ row }">
                        <div class="flex">
                            <el-image
                                fit="cover"
                                :src="row.goods_image"
                                class="flex-none w-[58px] h-[58px]"
                            />
                            <div class="ml-4 overflow-hidden">
                                <el-tooltip
                                    effect="dark"
                                    :content="row.goods_name"
                                    placement="top"
                                >
                                    <div class="text-base line-2">
                                        {{ row.goods_name }}
                                    </div>
                                </el-tooltip>
                                <div class="form-tips">{{ row?.goods_sku_value?.join(' ') }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="买家信息" min-width="130">
                    <template #default="{ row }">
                        <div>{{ row.nickname }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="评价等级" width="135">
                    <template #default="{ row }">
                        <div>{{ row.level_text }} </div>
                        <el-rate v-model="row.score" disabled size="small" />
                    </template>
                </el-table-column>
                <el-table-column label="评价内容" min-width="240">
                    <template #default="{ row }">
                        <div class="comment-content">
                            {{ row.content }}
                        </div>
                        <div class="mt-2">
                            <div
                                v-for="item in row.images"
                                :key="item.id"
                                class="inline mr-2"
                            >
                                <el-image
                                    style="width: 60px; height: 60px"
                                    :src="item"
                                    :preview-src-list="[item]"
                                    :hide-on-click-modal="true"
                                    :preview-teleported="true"
                                    :fit="'cover'"
                                ></el-image>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="回复内容" min-width="120">
                    <template #default="{ row }">
                        <div>{{ row.reply_content || '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="状态" min-width="100">
                    <template #default="{ row }">
                        <el-tag v-if="!row.is_show" type="error">隐藏</el-tag>
                        <el-tag v-else type="success">显示</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="回复状态" min-width="90">
                    <template #default="{ row }">
                        <el-tag v-if="row?.is_reply == 1" type="success" >已回复</el-tag>
                        <el-tag type="error" v-else>待回复</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="评价时间" prop="create_time" min-width="120" />
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['mall.goodsComment/set_reply']"
                            type="primary"
                            link
                            @click="handleEdit(row)"
                        >
                            {{ row.is_reply ? '修改回复' : '回复' }}
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>

        <edit-popup
            v-if="showEdit"
            ref="editRef"
            @success="getLists"
            @close="showEdit = false"
        />
    </div>
</template>
<script lang="ts" setup name="consumerLists">
import AiAgentPanel from '@/components/ai-agent-panel/index.vue'
import { usePaging } from "@/hooks/usePaging";
import { getRoutePath } from "@/router";
import { getCommentLists, apiCommentShow } from "@/api/goods/comment";
import feedback from "@/utils/feedback";
import EditPopup from "./edit.vue";
const editRef = shallowRef<InstanceType<typeof EditPopup>>();
const showEdit = ref(false);

// Tab类型
enum TabsEnum {
    ALL = "all",
    IS_REPLY = "is_reply",
    HAS_REPLY = "has_reply",
}

const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: "全部评价",
            name: "",
            type: TabsEnum.ALL,
        },
        {
            label: "待回复评价",
            name: 0,
            type: TabsEnum.IS_REPLY,
        },
        {
            label: "已回复",
            name: 1,
            type: TabsEnum.HAS_REPLY,
        },
    ],
});

const queryParams = reactive({
    is_reply: "",
    key_word: "",
    key_word_type: 1,
    level: "",
    is_show: "",
    start_time: "",
    end_time: "",
});

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getCommentLists,
    params: queryParams,
});

const changeTabs = (name: string) => {
    queryParams.is_reply = name;
    getLists();
};

const changeStatus = async (is_show: any, id: number) => {
    try {
        await apiCommentShow({ id, is_show })
        getLists()
    } catch (error) {
        getLists()
        console.log(error)
    }
}

const handleEdit = async (data: any) => {
    showEdit.value = true;
    await nextTick();
    editRef.value?.open(data.id, data.reply_content);
};

getLists();

/** 把当前页评价交给「评论情感分析」智能体；只取已展示的数据，不额外请求 */
const buildCommentPayload = () => {
    const rows = pager.lists || []
    if (!rows.length) return ''
    const lines = rows.slice(0, 30).map((row: any, i: number) => {
        const content = String(row.content || '').replace(/\s+/g, ' ').slice(0, 120)
        return `${i + 1}. 商品：${row.goods_name || '未知'} | 评分：${row.score ?? '未评分'}（${row.level_text || ''}） | 内容：${content || '（无文字）'}`
    })
    return `请分析以下 ${lines.length} 条商品评价的情感与问题分布：\n${lines.join('\n')}`
}
</script>
