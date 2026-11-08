<template>
  <main class="lucky-page">
    <header class="lucky-masthead">
      <div>
        <h1>活动配置</h1>
        <p class="lucky-lead">
          配置活动的展示信息、参与上限与抽奖规则。规则保存后对新发起的抽奖立即生效，已产生的抽奖记录不受影响。
        </p>
      </div>
      <div class="lucky-actions">
        <ActivitySelect v-model="activityId" placeholder="选择要配置的活动" width="240px" :clearable="false" />
        <el-button type="primary" :loading="saving" :disabled="!activityId" @click="submit">保存配置</el-button>
      </div>
    </header>

    <el-empty v-if="!loading && !activityId" description="还没有抽奖活动，请先在「抽奖活动」中新建">
      <el-button type="primary" @click="goActivityList">去新建活动</el-button>
    </el-empty>

    <template v-else>
      <section class="lucky-stats">
        <article v-for="item in statCards" :key="item.key" class="lucky-stat">
          <p class="lucky-stat__label">{{ item.label }}</p>
          <p class="lucky-stat__value">{{ item.value }}</p>
          <p class="lucky-stat__hint">{{ item.hint }}</p>
        </article>
      </section>

      <div class="lucky-grid" v-loading="loading">
        <el-card shadow="never" class="lucky-card">
          <template #header><span class="lucky-card__title">基础信息</span></template>
          <el-form label-position="top">
            <el-form-item label="活动标题" required>
              <el-input v-model="form.activityTitle" maxlength="80" show-word-limit />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="开始时间" required>
                  <el-date-picker
                    v-model="form.activityStartTime"
                    type="datetime"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束时间" required>
                  <el-date-picker
                    v-model="form.activityEndTime"
                    type="datetime"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="活动状态">
              <el-radio-group v-model="form.activityIsEnable">
                <el-radio :value="1">可用（前台可参与）</el-radio>
                <el-radio :value="2">不可用（暂停/下线）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="图文说明">
              <el-input
                v-model="form.graphicExplanation"
                type="textarea"
                :rows="4"
                maxlength="2000"
                show-word-limit
                placeholder="活动规则说明，前台活动页展示"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="lucky-card">
          <template #header><span class="lucky-card__title">参与与展示</span></template>
          <el-form label-position="top">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="参与人数是否显示">
                  <el-select v-model="form.partakeNumIsDisplay" style="width: 100%">
                    <el-option label="显示" :value="1" />
                    <el-option label="隐藏" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="虚拟参与人数">
                  <el-input-number v-model="form.partakeNum" :min="0" :max="10000" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="参与人数上限">
              <el-select v-model="form.partakeNumIsLimit" style="width: 160px">
                <el-option label="不限" :value="1" />
                <el-option label="限制" :value="2" />
              </el-select>
              <el-input-number
                v-if="form.partakeNumIsLimit === 2"
                v-model="form.partakeNumLimit"
                :min="1"
                controls-position="right"
                style="width: 200px; margin-left: 12px"
              />
            </el-form-item>
            <el-form-item label="首屏广告图">
              <el-input v-model="form.firstScreenAdvertise" placeholder="图片地址 https://..." />
            </el-form-item>
            <el-form-item label="首屏广告是否展示">
              <el-radio-group v-model="form.firstScreenIsDisplay">
                <el-radio :value="1">展示</el-radio>
                <el-radio :value="2">不展示</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="轮播中奖信息">
              <el-radio-group v-model="form.isRoundAwardInformation">
                <el-radio :value="1">开启</el-radio>
                <el-radio :value="2">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="lucky-card lucky-card--wide">
          <template #header><span class="lucky-card__title">抽奖规则</span></template>
          <el-form label-position="top">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="总抽奖机会">
                  <el-select v-model="rule.totalSweepstakesIsLimit" style="width: 100%">
                    <el-option label="不限" :value="1" />
                    <el-option label="限制" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="每人总抽奖次数">
                  <el-input-number
                    v-model="rule.totalSweepstakesLimit"
                    :min="0"
                    controls-position="right"
                    :disabled="rule.totalSweepstakesIsLimit !== 2"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="每人每日抽奖次数">
                  <el-input-number v-model="rule.winningTimesPerPerson" :min="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="中奖率（0-100）">
                  <el-input-number v-model="rule.winningRate" :min="0" :max="100" :step="0.01" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="预计参与人数">
                  <el-input-number v-model="rule.expectedNumberOfParticipants" :min="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="奖项数量">
                  <el-input :model-value="`${prizeCount} 个奖项`" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <p class="lucky-rule-tip">
              说明：奖项命中概率由「奖项设置」中的库存与排序共同决定，中奖率用于前台展示与整体概率上限控制。
            </p>
          </el-form>
        </el-card>
      </div>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ActivitySelect from '../components/ActivitySelect.vue'
import {
  loadActivityDetail,
  loadActivityLimit,
  loadDrawRecords,
  loadDrawTimes,
  loadPrizes,
  saveActivity,
  saveActivityLimit
} from '../api.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const activityId = ref(route.query.activityId ? Number(route.query.activityId) : '')
const prizeCount = ref(0)
const drawTimesCount = ref(0)
const recordCount = ref(0)
const prizeStock = reactive({ total: 0, given: 0 })

function emptyForm() {
  return {
    activityTitle: '',
    activityStartTime: '',
    activityEndTime: '',
    activityIsEnable: 1,
    graphicExplanation: '',
    partakeNumIsDisplay: 1,
    partakeNum: 0,
    partakeNumIsLimit: 1,
    partakeNumLimit: 0,
    firstScreenAdvertise: '',
    firstScreenIsDisplay: 1,
    isRoundAwardInformation: 1
  }
}

const form = reactive(emptyForm())
const rule = reactive({
  totalSweepstakesIsLimit: 1,
  totalSweepstakesLimit: 0,
  winningTimesPerPerson: 0,
  winningRate: 0,
  expectedNumberOfParticipants: 0
})

const statCards = computed(() => [
  { key: 'partake', label: '参与用户', value: drawTimesCount.value, hint: '产生过抽奖次数的用户数' },
  { key: 'record', label: '抽奖记录', value: recordCount.value, hint: '本活动累计抽奖次数' },
  { key: 'prize', label: '奖项数量', value: prizeCount.value, hint: '转盘上的奖项条目' },
  {
    key: 'stock',
    label: '剩余库存',
    value: Math.max(prizeStock.total - prizeStock.given, 0),
    hint: `已发放 ${prizeStock.given} / 共 ${prizeStock.total}`
  }
])

function fmt(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

function parseTime(value) {
  if (!value) return NaN
  return new Date(String(value).replace(' ', 'T')).getTime()
}

async function loadDetail(id) {
  if (!id) return
  loading.value = true
  try {
    const [detail, limit, prizes] = await Promise.all([
      loadActivityDetail(id),
      loadActivityLimit(id).catch(() => ({})),
      loadPrizes(id).catch(() => [])
    ])
    Object.assign(form, emptyForm(), {
      activityTitle: detail.activityTitle || '',
      activityStartTime: fmt(detail.activityStartTime),
      activityEndTime: fmt(detail.activityEndTime),
      activityIsEnable: Number(detail.activityIsEnable) === 2 ? 2 : 1,
      graphicExplanation: detail.graphicExplanation || '',
      partakeNumIsDisplay: Number(detail.partakeNumIsDisplay) === 2 ? 2 : 1,
      partakeNum: Number(detail.partakeNum) || 0,
      partakeNumIsLimit: Number(detail.partakeNumIsLimit) === 2 ? 2 : 1,
      partakeNumLimit: Number(detail.partakeNumLimit) || 0,
      firstScreenAdvertise: detail.firstScreenAdvertise || '',
      firstScreenIsDisplay: Number(detail.firstScreenIsDisplay) === 2 ? 2 : 1,
      isRoundAwardInformation: Number(detail.isRoundAwardInformation) === 2 ? 2 : 1
    })
    const source = limit && (limit.activityId || limit.totalSweepstakesIsLimit != null) ? limit : detail
    Object.assign(rule, {
      totalSweepstakesIsLimit: Number(source.totalSweepstakesIsLimit) === 2 ? 2 : 1,
      totalSweepstakesLimit: Number(source.totalSweepstakesLimit) || 0,
      winningTimesPerPerson: Number(source.winningTimesPerPerson) || 0,
      winningRate: Number(source.winningRate) || 0,
      expectedNumberOfParticipants: Number(source.expectedNumberOfParticipants) || 0
    })
    prizeCount.value = prizes.length
    prizeStock.total = prizes.reduce((sum, item) => sum + (Number(item.giftNum) || 0), 0)
    prizeStock.given = prizes.reduce((sum, item) => sum + (Number(item.giveOutGiftNum) || 0), 0)
    void loadCounters(detail)
  } catch (error) {
    ElMessage.error(error?.message || '加载活动配置失败')
  } finally {
    loading.value = false
  }
}

async function loadCounters(detail) {
  const title = detail?.activityTitle || form.activityTitle
  try {
    const times = await loadDrawTimes({ activityId: activityId.value, page: 1, limit: 1 })
    drawTimesCount.value = times.totalCount
  } catch {
    drawTimesCount.value = 0
  }
  try {
    const records = await loadDrawRecords({ activityTitle: title, page: 1, limit: 1 })
    recordCount.value = records.totalCount
  } catch {
    recordCount.value = 0
  }
}

function validate() {
  if (!form.activityTitle.trim()) {
    ElMessage.warning('请填写活动标题')
    return false
  }
  if (!form.activityStartTime || !form.activityEndTime) {
    ElMessage.warning('请选择活动开始与结束时间')
    return false
  }
  if (parseTime(form.activityEndTime) <= parseTime(form.activityStartTime)) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }
  if (form.partakeNumIsLimit === 2 && Number(form.partakeNumLimit) <= 0) {
    ElMessage.warning('限制参与人数时必须填写大于 0 的上限')
    return false
  }
  return true
}

async function submit() {
  if (!activityId.value) {
    ElMessage.warning('请先选择活动')
    return
  }
  if (!validate()) return
  saving.value = true
  try {
    await saveActivity({
      activityId: activityId.value,
      activityTitle: form.activityTitle.trim(),
      activityStartTime: form.activityStartTime,
      activityEndTime: form.activityEndTime,
      activityIsEnable: form.activityIsEnable,
      graphicExplanation: form.graphicExplanation || '',
      partakeNumIsDisplay: form.partakeNumIsDisplay,
      partakeNum: form.partakeNum,
      partakeNumIsLimit: form.partakeNumIsLimit,
      partakeNumLimit: form.partakeNumIsLimit === 2 ? form.partakeNumLimit : 0,
      firstScreenAdvertise: form.firstScreenAdvertise || '',
      firstScreenIsDisplay: form.firstScreenIsDisplay,
      isRoundAwardInformation: form.isRoundAwardInformation
    })
    await saveActivityLimit({
      activityId: activityId.value,
      totalSweepstakesIsLimit: rule.totalSweepstakesIsLimit,
      totalSweepstakesLimit: rule.totalSweepstakesIsLimit === 2 ? rule.totalSweepstakesLimit : 0,
      winningTimesPerPerson: rule.winningTimesPerPerson,
      winningRate: rule.winningRate,
      expectedNumberOfParticipants: rule.expectedNumberOfParticipants
    })
    ElMessage.success('活动配置已保存')
    await loadDetail(activityId.value)
  } catch (error) {
    ElMessage.error(error?.message || '保存配置失败')
  } finally {
    saving.value = false
  }
}

function goActivityList() {
  router.push('/plugin/userms-lucky-draw/index')
}

watch(activityId, (id) => {
  if (!id) return
  const query = { ...route.query, activityId: String(id) }
  router.replace({ path: route.path, query })
  loadDetail(id)
})

onMounted(() => loadDetail(activityId.value))
</script>

<style scoped>
.lucky-page {
  padding: 8px 16px 16px;
}
.lucky-masthead {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.lucky-masthead h1 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
}
.lucky-lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  max-width: 760px;
}
.lucky-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.lucky-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.lucky-stat {
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
}
.lucky-stat__label {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.lucky-stat__value {
  margin: 6px 0 2px;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.1;
}
.lucky-stat__hint {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.lucky-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.lucky-card--wide {
  grid-column: 1 / -1;
}
.lucky-card__title {
  font-weight: 600;
}
.lucky-rule-tip {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
@media (max-width: 1100px) {
  .lucky-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
