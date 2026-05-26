<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTaskStatus } from '../api/stats'

const router = useRouter()
const loading = ref(false)
const taskInfo = ref({
  lastRunTime: '--',
  processedCount: 0,
  status: 'normal',
  statusText: '正常',
  errorMessage: ''
})

const statusType = (status) => {
  const map = {
    normal: 'success',
    error: 'danger',
    running: 'warning'
  }
  return map[status] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTaskStatus()
    if (res.data) {
      taskInfo.value = {
        lastRunTime: res.data.lastRunTime || '--',
        processedCount: res.data.processedCount || 0,
        status: res.data.status || 'normal',
        statusText: res.data.statusText || '正常',
        errorMessage: res.data.errorMessage || ''
      }
    }
  } catch (e) {
    console.error('获取任务状态失败:', e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/dashboard')
}

onMounted(fetchData)
</script>

<template>
  <div class="task-status-page">
    <header class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft" class="back-btn">
          返回看板
        </el-button>
        <h1 class="page-title">定时任务状态</h1>
      </div>
      <el-button type="primary" @click="fetchData" :icon="Refresh">
        刷新
      </el-button>
    </header>

    <main class="page-content" v-loading="loading">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="status-card">
            <div class="card-icon" style="background: #409eff15; color: #409eff">
              <el-icon :size="36"><Timer /></el-icon>
            </div>
            <div class="card-label">最近执行时间</div>
            <div class="card-value">{{ taskInfo.lastRunTime }}</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="status-card">
            <div class="card-icon" style="background: #67c23a15; color: #67c23a">
              <el-icon :size="36"><Document /></el-icon>
            </div>
            <div class="card-label">处理数据量</div>
            <div class="card-value">{{ taskInfo.processedCount.toLocaleString() }} 条</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="status-card">
            <div class="card-icon" :style="{
              background: taskInfo.status === 'normal' ? '#67c23a15' : '#f56c6c15',
              color: taskInfo.status === 'normal' ? '#67c23a' : '#f56c6c'
            }">
              <el-icon :size="36">
                <component :is="taskInfo.status === 'normal' ? 'CircleCheckFilled' : 'CircleCloseFilled'" />
              </el-icon>
            </div>
            <div class="card-label">任务状态</div>
            <div class="card-value">
              <el-tag :type="statusType(taskInfo.status)" size="large" effect="dark">
                {{ taskInfo.statusText }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="taskInfo.errorMessage" shadow="hover" class="error-card">
        <template #header>
          <div class="error-header">
            <el-icon :size="20" style="color: #f56c6c"><WarningFilled /></el-icon>
            <span>错误信息</span>
          </div>
        </template>
        <div class="error-content">{{ taskInfo.errorMessage }}</div>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.task-status-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border-radius: 8px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
}

.status-card {
  text-align: center;
  padding: 32px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
}

.card-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.error-card {
  margin-top: 20px;
  border-radius: 12px;
  border: 1px solid #f56c6c30;
}

.error-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #f56c6c;
}

.error-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  padding: 12px 16px;
  background: #fef0f0;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .task-status-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .page-title {
    font-size: 18px;
  }
}
</style>
