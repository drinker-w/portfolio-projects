<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getRegion } from '../api/stats'

const chartRef = ref(null)
const loading = ref(false)
let chart = null

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRegion()
    if (res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('获取地域数据失败:', e)
  } finally {
    loading.value = false
  }
}

const updateChart = (data) => {
  if (!chart) return

  // 后端返回 region + pvCount，需要转换成 name + value
  const sortedData = [...data].sort((a, b) => b.pvCount - a.pvCount)
  const regions = sortedData.map(item => item.region)
  const values = sortedData.map(item => item.pvCount)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: regions,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: {
        color: '#666',
        rotate: 30,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '用户数',
        type: 'bar',
        barWidth: '60%',
        data: values,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#337ecc' },
              { offset: 1, color: '#409eff' }
            ])
          }
        },
        label: {
          show: true,
          position: 'top',
          color: '#666',
          fontSize: 12,
          formatter: (params) => params.value.toLocaleString()
        }
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
        <span class="card-title">地域分布</span>
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
  height: 300px;
}
</style>
