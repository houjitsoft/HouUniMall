<template>
  <div class="theme-config-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="card">
        <!-- PC商城主题配置 -->
        <el-tab-pane label="PC商城主题配置" name="pc">
          <div class="theme-section">
            <div class="tip-box">
              <el-alert
                title="温馨提示"
                type="info"
                :closable="false"
                show-icon
              >
                设置PC商城主题色
              </el-alert>
            </div>

            <div class="config-section">
              <div class="config-item">
                <label class="config-label">自定义：</label>
                <el-color-picker v-model="pcTheme.customColor" show-alpha />
                <el-button type="primary" size="small" style="margin-left: 12px" @click="confirmPcCustomColor">确认</el-button>
              </div>

              <div class="config-item">
                <label class="config-label">主题色：</label>
                <div class="theme-colors">
                  <div
                    v-for="color in themeColors"
                    :key="color"
                    class="color-box"
                    :class="{ active: pcTheme.selectedColor === color }"
                    :style="{ backgroundColor: color }"
                    @click="selectPcColor(color)"
                  />
                </div>
              </div>
            </div>

            <!-- 预览区域 -->
            <div class="preview-section">
              <div class="preview-header" :style="{ backgroundColor: pcTheme.selectedColor }">
                <div class="preview-logo">
                  <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 160 60'%3E%3Ccircle cx='30' cy='30' r='20' fill='white'/%3E%3Ctext x='60' y='40' font-family='Arial' font-size='20' fill='white'%3Eunimall数字商业系统%3C/text%3E%3C/svg%3E" alt="Logo">
                </div>
                <div class="preview-nav">
                  <span class="nav-item">Hi~欢迎到我们平台B2B商城</span>
                  <span class="nav-link">请登录</span>
                  <span class="nav-link">免费注册</span>
                  <span class="nav-item">我的订单</span>
                  <span class="nav-item">我的消息</span>
                  <span class="nav-item">我的钱包</span>
                  <span class="nav-item">我的收藏</span>
                  <span class="nav-item">购物车</span>
                  <span class="nav-item">卖家中心</span>
                  <span class="nav-item">商家中心</span>
                </div>
              </div>
              <div class="preview-search" :style="{ borderColor: pcTheme.selectedColor }">
                <el-select v-model="searchType" size="large" style="width: 120px">
                  <el-option label="商品" value="goods" />
                </el-select>
                <el-input placeholder="请输入产品名称 订货编码" size="large" />
                <el-button type="primary" size="large" :style="{ backgroundColor: pcTheme.selectedColor, borderColor: pcTheme.selectedColor }">搜索</el-button>
                <div class="search-icons">
                  <el-icon><Camera /></el-icon>
                  <span>购物车</span>
                  <span>批量下单</span>
                  <span>AI助手</span>
                </div>
              </div>
              <div class="preview-category" :style="{ backgroundColor: pcTheme.selectedColor }">
                <div class="category-btn">所有商品分类</div>
                <div class="category-nav">
                  <span>首页</span>
                  <span>组合促销</span>
                  <span>商家店铺</span>
                  <span>特色商家</span>
                  <span>满减满折</span>
                  <span>企业官网</span>
                  <span>特色馆</span>
                  <span>积分商城</span>
                  <span>头条资讯</span>
                </div>
              </div>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="savePcTheme">保存配置</el-button>
              <el-button @click="resetPcTheme">重置</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 移动端商城主题配置 -->
        <el-tab-pane label="移动端商城主题配置" name="mobile">
          <div class="theme-section">
            <div class="tip-box">
              <el-alert
                title="温馨提示"
                type="info"
                :closable="false"
                show-icon
              >
                设置移动端商城主题色
              </el-alert>
            </div>

            <div class="config-section">
              <div class="config-item">
                <label class="config-label">自定义：</label>
                <el-color-picker v-model="mobileTheme.customColor" show-alpha />
                <el-button type="primary" size="small" style="margin-left: 12px" @click="confirmMobileCustomColor">确认</el-button>
              </div>

              <div class="config-item">
                <label class="config-label">主题色：</label>
                <div class="theme-colors">
                  <div
                    v-for="color in themeColors"
                    :key="color"
                    class="color-box"
                    :class="{ active: mobileTheme.selectedColor === color }"
                    :style="{ backgroundColor: color }"
                    @click="selectMobileColor(color)"
                  />
                </div>
              </div>
            </div>

            <!-- 移动端预览区域 -->
            <div class="mobile-preview-section">
              <div class="mobile-frame">
                <div class="mobile-header" :style="{ backgroundColor: mobileTheme.selectedColor }">
                  <div class="mobile-title">商城首页</div>
                  <div class="mobile-search">
                    <el-input placeholder="搜索商品" size="small" />
                  </div>
                </div>
                <div class="mobile-content">
                  <div class="mobile-banner" :style="{ backgroundColor: mobileTheme.selectedColor + '40' }">
                    轮播图区域
                  </div>
                  <div class="mobile-category">
                    <div class="category-item" v-for="i in 4" :key="i">
                      <div class="category-icon" :style="{ backgroundColor: mobileTheme.selectedColor + '20' }">图标</div>
                      <div>分类{{ i }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="saveMobileTheme">保存配置</el-button>
              <el-button @click="resetMobileTheme">重置</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('pc')
const searchType = ref('goods')

// 预设主题色
const themeColors = [
  '#5677fc', // 蓝色
  '#7c4dff', // 紫色
  '#66bb6a', // 绿色
  '#4db6ac', // 青色
  '#ffca28', // 黄色
  '#a1887f', // 棕色
  '#ff7043', // 橙色
  '#ef5350'  // 红色
]

// PC端主题配置
const pcTheme = reactive({
  customColor: '#5677fc',
  selectedColor: '#5677fc'
})

// 移动端主题配置
const mobileTheme = reactive({
  customColor: '#5677fc',
  selectedColor: '#5677fc'
})

// 确认PC端自定义颜色
const confirmPcCustomColor = () => {
  pcTheme.selectedColor = pcTheme.customColor
  ElMessage.success('自定义颜色已应用')
}

// 选择PC端预设颜色
const selectPcColor = (color) => {
  pcTheme.selectedColor = color
  pcTheme.customColor = color
}

// 确认移动端自定义颜色
const confirmMobileCustomColor = () => {
  mobileTheme.selectedColor = mobileTheme.customColor
  ElMessage.success('自定义颜色已应用')
}

// 选择移动端预设颜色
const selectMobileColor = (color) => {
  mobileTheme.selectedColor = color
  mobileTheme.customColor = color
}

// 保存PC端主题
const savePcTheme = async () => {
  try {
    // TODO: 调用后端接口保存配置
    // await request.post('/web/mall/theme/save', {
    //   platform: 'pc',
    //   themeColor: pcTheme.selectedColor
    // })
    ElMessage.success('PC端主题配置保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 重置PC端主题
const resetPcTheme = () => {
  pcTheme.customColor = '#5677fc'
  pcTheme.selectedColor = '#5677fc'
  ElMessage.info('已重置为默认主题')
}

// 保存移动端主题
const saveMobileTheme = async () => {
  try {
    // TODO: 调用后端接口保存配置
    // await request.post('/web/mall/theme/save', {
    //   platform: 'mobile',
    //   themeColor: mobileTheme.selectedColor
    // })
    ElMessage.success('移动端主题配置保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 重置移动端主题
const resetMobileTheme = () => {
  mobileTheme.customColor = '#5677fc'
  mobileTheme.selectedColor = '#5677fc'
  ElMessage.info('已重置为默认主题')
}

// 页面加载时获取主题配置
const loadThemeConfig = async () => {
  try {
    // TODO: 调用后端接口获取配置
    // const res = await request.get('/web/mall/theme/get')
    // if (res.code === 1) {
    //   pcTheme.selectedColor = res.data.pcThemeColor || '#5677fc'
    //   pcTheme.customColor = res.data.pcThemeColor || '#5677fc'
    //   mobileTheme.selectedColor = res.data.mobileThemeColor || '#5677fc'
    //   mobileTheme.customColor = res.data.mobileThemeColor || '#5677fc'
    // }
  } catch (error) {
    console.error('加载主题配置失败:', error)
  }
}

// 初始化加载
loadThemeConfig()
</script>

<style scoped lang="scss">
.theme-config-container {
  padding: 20px;

  .theme-section {
    padding: 20px 0;
  }

  .tip-box {
    margin-bottom: 30px;
  }

  .config-section {
    margin-bottom: 40px;

    .config-item {
      display: flex;
      align-items: center;
      margin-bottom: 24px;

      .config-label {
        width: 100px;
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .theme-colors {
        display: flex;
        gap: 12px;

        .color-box {
          width: 50px;
          height: 50px;
          border-radius: 4px;
          cursor: pointer;
          border: 3px solid transparent;
          transition: all 0.3s;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }

          &.active {
            border-color: #303133;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
          }
        }
      }
    }
  }

  .preview-section {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 30px;

    .preview-header {
      padding: 12px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: white;
      border-radius: 4px 4px 0 0;

      .preview-logo img {
        height: 40px;
      }

      .preview-nav {
        display: flex;
        gap: 20px;
        font-size: 13px;

        .nav-item, .nav-link {
          cursor: pointer;
          &:hover {
            opacity: 0.8;
          }
        }

        .nav-link {
          color: #ffeb3b;
        }
      }
    }

    .preview-search {
      background: white;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 12px;
      border: 2px solid #ddd;
      border-top: none;

      .search-icons {
        display: flex;
        gap: 16px;
        margin-left: auto;
        align-items: center;
        color: #666;
        font-size: 14px;

        span {
          cursor: pointer;
          &:hover {
            color: #409EFF;
          }
        }
      }
    }

    .preview-category {
      padding: 12px 20px;
      display: flex;
      align-items: center;
      gap: 30px;
      color: white;
      font-size: 14px;

      .category-btn {
        background: rgba(0, 0, 0, 0.2);
        padding: 8px 20px;
        border-radius: 4px;
        cursor: pointer;
      }

      .category-nav {
        display: flex;
        gap: 24px;

        span {
          cursor: pointer;
          &:hover {
            opacity: 0.8;
          }
        }
      }
    }
  }

  .mobile-preview-section {
    display: flex;
    justify-content: center;
    margin-bottom: 30px;
    padding: 40px 0;
    background: #f5f7fa;
    border-radius: 8px;

    .mobile-frame {
      width: 375px;
      background: white;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

      .mobile-header {
        padding: 16px;
        color: white;

        .mobile-title {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 12px;
          text-align: center;
        }

        .mobile-search {
          :deep(.el-input__wrapper) {
            background: rgba(255, 255, 255, 0.9);
          }
        }
      }

      .mobile-content {
        padding: 16px;

        .mobile-banner {
          height: 160px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 16px;
          color: #666;
          font-size: 14px;
        }

        .mobile-category {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 16px;

          .category-item {
            text-align: center;
            font-size: 12px;
            color: #666;

            .category-icon {
              width: 50px;
              height: 50px;
              margin: 0 auto 8px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 11px;
            }
          }
        }
      }
    }
  }

  .action-buttons {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    .el-button {
      min-width: 120px;
    }
  }
}

@media (max-width: 991px) {
  .theme-config-container {
    padding: 12px !important;
  }

  .theme-config-container .config-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .theme-config-container .config-label {
    width: auto;
  }

  .theme-config-container .theme-colors {
    flex-wrap: wrap;
  }

  .theme-config-container .preview-section,
  .theme-config-container .mobile-preview-section {
    padding: 12px;
    overflow-x: auto;
  }

  .theme-config-container .preview-header,
  .theme-config-container .preview-nav,
  .theme-config-container .preview-search,
  .theme-config-container .preview-category,
  .theme-config-container .category-nav {
    flex-wrap: wrap;
    gap: 8px;
  }

  .theme-config-container .mobile-frame {
    width: 100%;
    max-width: 375px;
  }

  .theme-config-container .action-buttons .el-button {
    width: 100%;
    min-width: 0;
  }
}
</style>
