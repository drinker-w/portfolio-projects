<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '../api/stats'

const cards = ref([
  { title: '今日PV', value: 0, change: 0, icon: 'View', color: '#409eff' },
  { title: '今日UV', value: 0, change: 0, icon: 'User', color: '#67c23a' },
  { title: '转化率', value: 0, change: 0, icon: 'TrendCharts', color: '#e6a23c', unit: '%' },
  { title: '活跃用户数', value: 0, change: 0, icon: 'UserFilled', color: '#f56c6c' }
])

const loading = ref(false)

const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOverview()
    if (res.data) {
      // 后端返回 totalPv, totalUv, conversionRate
      cards.value[0].value = res.data.totalPv || 0
      cards.value[0].change = 0
      cards.value[1].value = res.data.totalUv || 0
      cards.value[1].change = 0
      cards.value[2].value = res.data.conversionRate || 0
      cards.value[2].change = 0
      cards.value[3].value = res.data.totalUv || 0
      cards.value[3].change = 0
    }
  } catch (e) {
    console.error('获取概览数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

defineExpose({ fetchData })
</script>

<template>
  <div class="overview-container">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="(card, index) in cards" :key="index">
        <el-card shadow="hover" class="overview-card" v-loading="loading">
          <div class="card-content">
            <div class="card-icon" :style="{ background: card.color + '15', color: card.color }">
              <el-icon :size="28">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value">
                {{ card.unit === '%' ? card.value.toFixed(1) + '%' : formatNumber(card.value) }}
              </div>
              <div class="card-change" :class="card.change >= 0 ? 'up' : 'down'">
                <span>{{ card.change >= 0 ? '↑' : '↓' }}</span>
                <span>{{ Math.abs(card.change).toFixed(1) }}%</span>
                <span class="change-label">较昨日</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.overview-container {
  margin-bottom: 20px;
}

.overview-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.overview-card :deep(.el-card__body) {
  padding: 20px;
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.card-change {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-change.up {
  color: var(--success-color);
}

.card-change.down {
  color: var(--danger-color);
}

.change-label {
  color: var(--text-secondary);
  margin-left: 4px;
}

@media (max-width: 768px) {
  .card-value {
    font-size: 22px;
  }
}
</style>
