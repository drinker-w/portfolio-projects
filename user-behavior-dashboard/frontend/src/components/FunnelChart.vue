<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFunnel } from '../api/stats'

const chartRef = ref(null)
const loading = ref(false)
let chart = null

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFunnel()
    if (res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('获取漏斗数据失败:', e)
  } finally {
    loading.value = false
  }
}

const updateChart = (data) => {
  if (!chart) return

  // 后端返回 stage + userCount，需要转换成 name + value + rate
  const maxCount = data.length > 0 ? data[0].userCount : 1
  const funnelData = data.map((item, index) => ({
    name: item.stage,
    value: item.userCount,
    rate: index === 0 ? 100 : ((item.userCount / maxCount) * 100).toFixed(1)
  }))

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
      formatter: (params) => {
        const item = funnelData[params.dataIndex]
        return `<div style="font-weight:600;margin-bottom:8px">${params.name}</div>
                <div>数量: ${item.value.toLocaleString()}</div>
                <div>转化率: ${item.rate}%</div>`
      }
    },
    series: [
      {
        name: '转化漏斗',
        type: 'funnel',
        left: '10%',
        top: '10%',
        bottom: '10%',
        width: '80%',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '100%',
        sort: 'descending',
        gap: 4,
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => {
            const item = funnelData[params.dataIndex]
            return `${params.name}\n${item.value.toLocaleString()} (${item.rate}%)`
          },
          fontSize: 14,
          color: '#fff'
        },
        labelLine: { show: false },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          label: { fontSize: 16 }
        },
        data: funnelData.map((item, index) => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: ['#409eff', '#67c23a', '#e6a23c'][index] },
              { offset: 1, color: ['#79bbff', '#95d475', '#eebe77'][index] }
            ])
          }
        }))
      }
    ]
  })
}

const initChart = () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    fetchData()
  }
}

const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

defineExpose({ fetchData })
</script>

<template>
  <el-card shadow="hover" class="chart-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span class="card-title">转化漏斗</span>
      </div>
    </template>
    <div ref="chartRef" class="chart-container"></div>
  </el-card>
</template>

<style scoped>
.chart-card {
  border-radius: 12px;
  border: none;
}

.chart-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
