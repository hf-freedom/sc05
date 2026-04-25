<template>
  <div id="app">
    <el-container>
      <el-header style="background-color: #409EFF; color: white; padding: 0 20px; display: flex; align-items: center; justify-content: space-between;">
        <span style="font-size: 20px; font-weight: bold;">动态审批流系统</span>
        <div style="display: flex; align-items: center;">
          <span style="margin-right: 10px; color: white;">当前用户：</span>
          <el-select
            v-model="currentUser"
            placeholder="请先创建用户"
            @change="handleUserChange"
            style="width: 280px;"
            :clearable="false"
            filterable
            popper-class="user-select-popper">
            <el-option
              v-for="user in users"
              :key="user.userId"
              :label="user.name + ' (' + user.department + ' - ' + user.role + ')'"
              :value="user.userId">
            </el-option>
          </el-select>
          <el-button
            v-if="users.length === 0"
            type="success"
            size="mini"
            style="margin-left: 10px;"
            @click="$router.push('/users')">
            去创建用户
          </el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" style="background-color: #f5f7fa; border-right: 1px solid #e6e6e6;">
          <el-menu
            router
            :default-active="$route.path"
            background-color="#f5f7fa"
            text-color="#333"
            active-text-color="#409EFF"
            style="border-right: none;">
            <el-menu-item index="/users">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/templates">
              <i class="el-icon-document"></i>
              <span>审批模板</span>
            </el-menu-item>
            <el-menu-item index="/submit">
              <i class="el-icon-edit"></i>
              <span>提交审批</span>
            </el-menu-item>
            <el-menu-item index="/my-approval">
              <i class="el-icon-s-order"></i>
              <span>我的审批</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main style="background-color: #fff; padding: 20px;">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import EventBus from './eventBus'

export default {
  name: 'App',
  data() {
    return {
      currentUser: '',
      users: []
    }
  },
  created() {
    this.loadUsers()
    EventBus.$on('users-changed', () => {
      this.loadUsers(true)
    })
  },
  beforeDestroy() {
    EventBus.$off('users-changed')
  },
  watch: {
    '$route': function() {
      this.loadUsers(true)
    }
  },
  methods: {
    loadUsers(preserveSelection = false) {
      const previousUserId = this.currentUser
      
      this.$http.get('/users').then(res => {
        if (res.data.success && res.data.data) {
          this.users = res.data.data
          
          if (this.users.length > 0) {
            let targetUserId = null
            
            if (preserveSelection && previousUserId) {
              const userStillExists = this.users.find(u => u.userId === previousUserId)
              if (userStillExists) {
                targetUserId = previousUserId
              }
            }
            
            if (!targetUserId) {
              const storedUserStr = localStorage.getItem('currentUser')
              if (storedUserStr) {
                try {
                  const storedUser = JSON.parse(storedUserStr)
                  const userStillExists = this.users.find(u => u.userId === storedUser.userId)
                  if (userStillExists) {
                    targetUserId = storedUser.userId
                  }
                } catch (e) {
                  // ignore parse error
                }
              }
            }
            
            if (!targetUserId) {
              targetUserId = this.users[0].userId
            }
            
            if (this.currentUser !== targetUserId) {
              this.currentUser = targetUserId
            }
            this.handleUserChange(this.currentUser, false)
          }
        }
      })
    },
    handleUserChange(userId, saveToStorage = true) {
      const user = this.users.find(u => u.userId === userId)
      if (user && saveToStorage) {
        localStorage.setItem('currentUser', JSON.stringify(user))
      }
    }
  }
}
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.el-container {
  height: 100%;
}
</style>
