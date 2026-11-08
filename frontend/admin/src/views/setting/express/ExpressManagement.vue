<template>
  <div class="express-management">
    <div class="page-header">
      <h2>{{ t('setting.express.manage.title') }}</h2>
      <p>{{ t('setting.express.manage.subtitle') }}</p>
    </div>

    <div class="management-grid">
      <el-card 
        class="management-card"
        shadow="hover"
        @click="navigateTo('/setting/express/company')"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="32"><Van /></el-icon>
            <span class="card-title">{{ t('setting.express.manage.companyCard') }}</span>
          </div>
        </template>
        <div class="card-content">
          <p>{{ t('setting.express.manage.companyDesc') }}</p>
        </div>
      </el-card>

      <el-card 
        class="management-card"
        shadow="hover"
        @click="navigateTo('/setting/express/config')"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="32"><Setting /></el-icon>
            <span class="card-title">{{ t('setting.express.manage.configCard') }}</span>
          </div>
        </template>
        <div class="card-content">
          <p>{{ t('setting.express.manage.configDesc') }}</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { useRouter } from 'vue-router'
import { Van, Setting } from '@element-plus/icons-vue'

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.express-management {
  padding: 20px;

  .page-header {
    margin-bottom: 30px;

    h2 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 24px;
      font-weight: 600;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .management-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .management-card {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 12px;

      .card-icon {
        color: #409eff;
      }

      .card-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .card-content {
      padding: 10px 0;

      p {
        margin: 0;
        color: #606266;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }
}
</style>
