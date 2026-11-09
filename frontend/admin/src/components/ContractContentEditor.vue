<template>
  <div class="contract-content-unified">
    <!-- 合同封面：甲方/乙方/编号/签约时间 -->
    <div class="contract-cover-parties">
      <el-form-item prop="partyAName" label-width="0" class="party-form-item">
        <div class="party-line">
          <span class="party-tag">甲　　方：</span>
          <el-input
            :model-value="partyAName"
            class="party-name-input"
            placeholder="请输入甲方名称"
            :maxlength="PARTY_NAME_MAX_LENGTH"
            @update:model-value="$emit('update:partyAName', $event)"
            @blur="handlePartyBlur('A')"
          />
        </div>
      </el-form-item>
      <el-form-item prop="partyBName" label-width="0" class="party-form-item">
        <div class="party-line">
          <span class="party-tag">乙　　方：</span>
          <el-input
            :model-value="partyBName"
            class="party-name-input"
            placeholder="请输入乙方名称"
            :maxlength="PARTY_NAME_MAX_LENGTH"
            @update:model-value="$emit('update:partyBName', $event)"
            @blur="handlePartyBlur('B')"
          />
        </div>
      </el-form-item>
      <div class="party-meta-line">
        <span class="party-tag">合同编号：</span>
        <span class="party-meta-text">{{ contractNumber || contractNumberPlaceholder }}</span>
      </div>
      <div class="party-meta-line">
        <span class="party-tag">签约时间：</span>
        <span class="party-meta-text">{{ signDateDisplay || signDatePlaceholder }}</span>
      </div>
    </div>

    <!-- 合同正文 -->
    <RichEditor
      :model-value="contractContent"
      :height="height"
      embedded
      @update:model-value="$emit('update:contractContent', $event)"
    />
  </div>
</template>

<script setup>
import RichEditor from '@/components/RichEditor.vue'
import { normalizePartyName, PARTY_NAME_MAX_LENGTH } from '@/utils/contractParties'

const props = defineProps({
  partyAName: {
    type: String,
    default: ''
  },
  partyBName: {
    type: String,
    default: ''
  },
  contractContent: {
    type: String,
    default: ''
  },
  contractNumber: {
    type: String,
    default: ''
  },
  signDateDisplay: {
    type: String,
    default: ''
  },
  contractNumberPlaceholder: {
    type: String,
    default: '请先生成合同编号'
  },
  signDatePlaceholder: {
    type: String,
    default: '请选择签署日期'
  },
  height: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['update:partyAName', 'update:partyBName', 'update:contractContent'])

// 失焦即按统一规则清洗，界面所见与最终保存值保持一致（新增、编辑共用）
const handlePartyBlur = (side) => {
  const prop = side === 'A' ? 'partyAName' : 'partyBName'
  const normalized = normalizePartyName(props[prop])
  if (normalized !== props[prop]) {
    emit(`update:${prop}`, normalized)
  }
}
</script>

<style scoped>
.contract-content-unified {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}

.contract-cover-parties {
  max-width: 540px;
  margin: 0 auto;
  padding: 28px 32px 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.contract-cover-parties .party-form-item {
  margin-bottom: 0;
}

.contract-cover-parties .party-form-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
}

/* 校验提示改为占位显示，避免绝对定位被封面区裁切 / 与下一行重叠 */
.contract-cover-parties .party-form-item :deep(.el-form-item__error) {
  position: static;
  display: block;
  padding-top: 2px;
  line-height: 16px;
  font-size: 12px;
  text-align: center;
}

.contract-cover-parties .party-line {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  width: 100%;
  margin-bottom: 20px;
}

.contract-cover-parties .party-tag {
  flex-shrink: 0;
  width: 96px;
  font-size: 16px;
  line-height: 32px;
  color: #000;
  text-align: right;
  letter-spacing: 1px;
  font-family: 'SimSun', 'Songti SC', 'STSong', serif;
}

.contract-cover-parties .party-name-input {
  flex: 1;
  max-width: 360px;
}

.contract-cover-parties .party-name-input :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
  border: none;
  border-bottom: 1px solid #000;
  border-radius: 0;
  padding: 0 4px 2px;
}

.contract-cover-parties .party-name-input :deep(.el-input__inner) {
  font-size: 16px;
  color: #000;
  text-align: center;
  font-family: 'SimSun', 'Songti SC', 'STSong', serif;
}

.contract-cover-parties .party-meta-line {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 12px;
  font-size: 15px;
  color: #303133;
}

.contract-cover-parties .party-meta-line:last-child {
  margin-bottom: 0;
}

.contract-cover-parties .party-meta-text {
  min-width: 260px;
  text-align: left;
  font-size: 15px;
  color: #303133;
  font-family: 'SimSun', 'Songti SC', 'STSong', serif;
}
</style>
