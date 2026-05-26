<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: 600;">文档管理</span>
        </div>
      </template>

      <el-upload
        class="upload-area"
        drag
        action="/api/documents/upload"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        accept=".pdf,.txt"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">仅支持 PDF / TXT 格式文件</div>
        </template>
      </el-upload>

      <el-table :data="documentList" style="margin-top: 20px;" stripe>
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="createTime" label="上传时间" width="200" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocumentList, deleteDocument } from '../api/document'

const documentList = ref([])

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const fetchDocuments = async () => {
  try {
    const res = await getDocumentList()
    documentList.value = res.data || res || []
  } catch (e) {
    // error handled by interceptor
  }
}

const beforeUpload = (file) => {
  const isValidType = file.type === 'application/pdf' || file.type === 'text/plain'
  if (!isValidType) {
    ElMessage.error('仅支持 PDF 和 TXT 格式文件')
    return false
  }
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  ElMessage.success('上传成功')
  fetchDocuments()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除文档「${row.fileName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      fetchDocuments()
    } catch (e) {
      // error handled by interceptor
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  padding: 30px 0;
}
</style>
