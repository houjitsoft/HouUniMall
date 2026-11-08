<template>
  <div>
    <el-card class="!border-none" shadow="never">
      <el-alert
        type="warning"
        title="温馨提示：用户评价的列表，管理员可以回复用户的评价，允许修改已回复过的内容；设置该条评论是否要在前端隐藏或显示。"
        :closable="false"
        show-icon
      />

      <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
        <el-form-item label="评价信息">
          <el-input
            v-model="queryParams.key_word"
            placeholder="请输入"
            class="w-[320px]"
            @keyup.enter="handleSearch"
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
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
            @change="onDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
      <!-- 选项卡 -->
      <el-tabs v-model="queryParams.is_reply" @tab-change="handleTabChange">
        <el-tab-pane
          :label="`全部评价(${tabCount.all || 0})`"
          name=""
        />
        <el-tab-pane
          :label="`待回复评价(${tabCount.is_reply || 0})`"
          :name="0"
        />
        <el-tab-pane
          :label="`已回复(${tabCount.has_reply || 0})`"
          :name="1"
        />
      </el-tabs>

      <!-- 表格 -->
      <el-table size="large" v-loading="loading" :data="list">
        <el-table-column label="评价商品" min-width="180">
          <template #default="{ row }">
            <div class="flex">
              <el-image
                fit="cover"
                :src="row.goods_image"
                class="flex-none w-[58px] h-[58px]"
              />
              <div class="ml-4 overflow-hidden">
                <el-tooltip effect="dark" :content="row.goods_name" placement="top">
                  <div class="text-base line-clamp-2">{{ row.goods_name }}</div>
                </el-tooltip>
                <div class="text-gray-400 text-sm">{{ Array.isArray(row.goods_sku_value) ? row.goods_sku_value.join(',') : '' }}</div>
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
            <div>{{ row.level_text }}</div>
            <el-rate v-model="row.score" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="240">
          <template #default="{ row }">
            <div>{{ row.content }}</div>
            <div class="mt-2">
              <div v-for="(item, idx) in row.images" :key="idx" class="inline mr-2">
                <el-image
                  style="width: 60px; height: 60px"
                  :src="item"
                  :preview-src-list="[item]"
                  :hide-on-click-modal="true"
                  :preview-teleported="true"
                  fit="cover"
                />
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
            <el-switch
              v-model="row.is_show"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleShow($event, row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column label="回复状态" min-width="90">
          <template #default="{ row }">
            <el-tag v-if="row.is_reply == 1" type="success">已回复</el-tag>
            <el-tag type="danger" v-else>待回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评价时间" prop="create_time" min-width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleReply(row)">
              {{ row.is_reply ? '修改回复' : '回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 回复评价弹窗 -->
    <el-dialog v-model="replyVisible" title="回复评价" width="550px">
      <el-form :model="replyForm" label-width="84px" :rules="replyRules" ref="replyFormRef">
        <el-form-item label="回复内容" prop="reply_content">
          <el-input
            v-model="replyForm.reply_content"
            placeholder="请输入回复内容"
            type="textarea"
            :autosize="{ minRows: 10, maxRows: 10 }"
            show-word-limit
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySubmitting" @click="submitReply">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mallCommentList, mallCommentSetIsShow, mallCommentReply } from '@/api/mall/goodsAdmin.js'

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** tab 数量统计 */
const tabCount = reactive({ all: 0, is_reply: 0, has_reply: 0 })
/** 日期范围 */
const dateRange = ref([])

/** 查询参数 */
const queryParams = reactive({
  is_reply: '',
  key_word: '',
  key_word_type: 1,
  level: '',
  is_show: '',
  start_time: '',
  end_time: '',
  pageNo: 1,
  pageSize: 15
})

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 日期范围变化处理
 * @param {Array} val - 日期范围数组
 */
const onDateChange = (val) => {
  queryParams.start_time = val ? val[0] : ''
  queryParams.end_time = val ? val[1] : ''
}

/**
 * 获取列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await mallCommentList({
      ...queryParams,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize
    })
    if (res && res.code === 1 && res.data) {
      list.value = res.data.lists || []
      pagination.total = res.data.count || 0
      // 更新 tab 统计
      if (res.data.extend) {
        tabCount.all = res.data.extend.all || 0
        tabCount.is_reply = res.data.extend.is_reply || 0
        tabCount.has_reply = res.data.extend.has_reply || 0
      }
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 查询（重置到第一页）
 */
const handleSearch = () => {
  pagination.pageNo = 1
  fetchList()
}

/**
 * 重置查询参数
 */
const handleReset = () => {
  queryParams.key_word = ''
  queryParams.key_word_type = 1
  queryParams.level = ''
  queryParams.is_show = ''
  queryParams.start_time = ''
  queryParams.end_time = ''
  queryParams.is_reply = ''
  dateRange.value = []
  handleSearch()
}

/**
 * 切换 Tab
 * @param {string|number} name - tab name
 */
const handleTabChange = (name) => {
  queryParams.is_reply = name
  pagination.pageNo = 1
  fetchList()
}

/**
 * 切换评价显示状态
 * @param {number} is_show - 显示状态 1/0
 * @param {number} id - 评价ID
 */
const handleToggleShow = async (is_show, id) => {
  try {
    const res = await mallCommentSetIsShow({ id, is_show })
    if (res && res.code === 1) {
      ElMessage.success('已更新')
    } else {
      ElMessage.error(res?.message || '操作失败')
      fetchList()
    }
  } catch (e) {
    fetchList()
  }
}

/** 回复弹窗 */
const replyVisible = ref(false)
const replySubmitting = ref(false)
const replyFormRef = ref(null)
const replyForm = reactive({ id: null, reply_content: '' })
const replyRules = {
  reply_content: [{ required: true, message: '请输入回复内容', trigger: ['blur'] }]
}

/**
 * 打开回复弹窗
 * @param {object} row - 当前评价行
 */
const handleReply = (row) => {
  replyForm.id = row.id
  replyForm.reply_content = row.reply_content || ''
  replyVisible.value = true
}

/**
 * 提交回复
 */
const submitReply = async () => {
  await replyFormRef.value?.validate()
  replySubmitting.value = true
  try {
    const res = await mallCommentReply({ id: replyForm.id, reply_content: replyForm.reply_content })
    if (res && res.code === 1) {
      ElMessage.success('回复成功')
      replyVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.message || '回复失败')
    }
  } finally {
    replySubmitting.value = false
  }
}

onMounted(fetchList)
</script>
