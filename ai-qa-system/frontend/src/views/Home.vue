<template>
  <div class="home-container">
    <div class="home-header">
      <div class="logo">AI智能问答系统</div>
      <div class="user-info">
        <el-icon><User /></el-icon>
        <span>{{ username }}</span>
        <el-button type="danger" text size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    <div class="home-body">
      <div class="home-aside">
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          router
        >
          <el-menu-item index="/document">
            <el-icon><Document /></el-icon>
            <span>文档管理</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能问答</span>
          </el-menu-item>
          <el-menu-item index="/history">
            <el-icon><Clock /></el-icon>
            <span>对话历史</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="home-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const username = localStorage.getItem('username') || '用户'
const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    router.push('/login')
  }).catch(() => {})
}
</script>
