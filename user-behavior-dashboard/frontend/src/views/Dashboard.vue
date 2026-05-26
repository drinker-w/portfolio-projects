<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import OverviewCard from '../components/OverviewCard.vue'
import PvUvChart from '../components/PvUvChart.vue'
import FunnelChart from '../components/FunnelChart.vue'
import RegionMap from '../components/RegionMap.vue'

const router = useRouter()
const overviewRef = ref(null)
const pvUvRef = ref(null)
const funnelRef = ref(null)
const regionRef = ref(null)

const refreshAll = () => {
  overviewRef.value?.fetchData()
  pvUvRef.value?.fetchData()
  funnelRef.value?.fetchData()
  regionRef.value?.fetchData()
}

const goToTaskStatus = () => {
  router.push('/task-status')
}
</script>

<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <div class="header-left">
        <h1 class="header-title">用户行为分析仪表盘</h1>
        <span class="header-subtitle">实时数据监控与分析</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="refreshAll" :icon="Refresh">
          刷新数据
        </el-button>
        <el-button @click="goToTaskStatus" :icon="Timer">
          定时任务状态
        </el-button>
      </div>
    </header>

    <main class="dashboard-content">
      <OverviewCard ref="overviewRef" />

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="12">
          <PvUvChart ref="pvUvRef" />
        </el-col>
        <el-col :xs="24" :lg="12">
          <FunnelChart ref="funnelRef" />
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="24">
          <RegionMap ref="regionRef" />
        </el-col>
      </el-row>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 24px 32px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: 1px;
}

.header-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.header-right {
  display: flex;
  gap: 12px;
}

.header-right .el-button {
  border-radius: 8px;
  font-weight: 500;
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
}

.chart-row {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }

  .header-title {
    font-size: 22px;
  }

  .header-right {
    width: 100%;
  }

  .header-right .el-button {
    flex: 1;
  }
}
</style>
