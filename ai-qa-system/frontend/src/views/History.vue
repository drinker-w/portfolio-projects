<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: 600;">对话历史</span>
          <el-input
            v-model="searchText"
            placeholder="搜索对话..."
            clearable
            style="width: 260px;"
            prefix-icon="Search"
          />
        </div>
      </template>

      <el-table :data="filteredHistory" stripe v-loading="tableLoading">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 16px 24px;">
              <div class="history-detail">
                <div class="detail-label">问题：</div>
                <div class="detail-content question-text">{{ row.question }}</div>
              </div>
              <div class="history-detail" style="margin-top: 12px;">
                <div class="detail-label">回答：</div>
                <div class="detail-content answer-text">{{ row.answer }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="documentName" label="关联文档" min-width="150" show-overflow-tooltip />
        <el-table-column prop="question" label="问题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提问时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!tableLoading && historyList.length === 0" description="暂无对话记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChatHistory, deleteChatHistory } from '../api/chat'

const historyList = ref([])
const searchText = ref('')
const tableLoading = ref(false)

const filteredHistory = computed(() => {
  if (!searchText.value) return historyList.value
  const keyword = searchText.value.toLowerCase()
  return historyList.value.filter(
    item =>
      (item.question && item.question.toLowerCase().includes(keyword)) ||
      (item.answer && item.answer.toLowerCase().includes(keyword))
  )
})

const fetchHistory = async () => {
  tableLoading.value = true
  try {
    const res = await getChatHistory()
    historyList.value = res.data || res || []
  } catch (e) {
    // error handled by interceptor
  } finally {
    tableLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条对话记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteChatHistory(row.id)
      ElMessage.success('删除成功')
      fetchHistory()
    } catch (e) {
      // error handled by interceptor
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-detail {
  display: flex;
  gap: 8px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.detail-content {
  color: #333;
  line-height: 1.6;
}

.question-text {
  color: #409eff;
}

.answer-text {
  background: #f9f9f9;
  padding: 12px;
  border-radius: 6px;
  white-space: pre-wrap;
}
</style>
