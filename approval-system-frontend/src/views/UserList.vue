<template>
  <div class="user-list">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <i class="el-icon-plus"></i> 添加用户
      </el-button>
    </div>

    <el-table :data="users" border stripe style="width: 100%;">
      <el-table-column prop="name" label="姓名" width="120"></el-table-column>
      <el-table-column prop="department" label="部门" width="150"></el-table-column>
      <el-table-column prop="role" label="角色" width="120"></el-table-column>
      <el-table-column label="直属上级" width="150">
        <template slot-scope="scope">
          {{ getManagerName(scope.row.directManagerId) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" placeholder="请输入部门"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="普通员工" value="普通员工"></el-option>
            <el-option label="部门主管" value="部门主管"></el-option>
            <el-option label="财务" value="财务"></el-option>
            <el-option label="总经理" value="总经理"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="直属上级">
          <el-select v-model="form.directManagerId" placeholder="请选择直属上级" style="width: 100%;" clearable>
            <el-option
              v-for="user in users.filter(u => u.userId !== form.userId)"
              :key="user.userId"
              :label="user.name"
              :value="user.userId">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import EventBus from '../eventBus'

export default {
  name: 'UserList',
  data() {
    return {
      users: [],
      dialogVisible: false,
      dialogTitle: '添加用户',
      form: {
        userId: '',
        name: '',
        department: '',
        role: '',
        directManagerId: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        department: [
          { required: true, message: '请输入部门', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.loadUsers()
  },
  methods: {
    loadUsers() {
      this.$http.get('/users').then(res => {
        if (res.data.success && res.data.data) {
          this.users = res.data.data
        }
      })
    },
    getManagerName(managerId) {
      if (!managerId) return '-'
      const manager = this.users.find(u => u.userId === managerId)
      return manager ? manager.name : '-'
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    openAddDialog() {
      this.dialogTitle = '添加用户'
      this.form = {
        userId: '',
        name: '',
        department: '',
        role: '',
        directManagerId: ''
      }
      this.dialogVisible = true
    },
    openEditDialog(user) {
      this.dialogTitle = '编辑用户'
      this.form = { ...user }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.userId) {
            this.$http.put(`/users/${this.form.userId}`, this.form).then(res => {
              if (res.data.success) {
                this.$message.success('更新成功')
                this.dialogVisible = false
                this.loadUsers()
                EventBus.$emit('users-changed')
              } else {
                this.$message.error(res.data.message || '更新失败')
              }
            })
          } else {
            this.$http.post('/users', this.form).then(res => {
              if (res.data.success) {
                this.$message.success('添加成功')
                this.dialogVisible = false
                this.loadUsers()
                EventBus.$emit('users-changed')
              } else {
                this.$message.error('添加失败')
              }
            })
          }
        }
      })
    },
    handleDelete(user) {
      this.$confirm(`确定要删除用户 "${user.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete(`/users/${user.userId}`).then(res => {
          if (res.data.success) {
            this.$message.success('删除成功')
            this.loadUsers()
            EventBus.$emit('users-changed')
          } else {
            this.$message.error(res.data.message || '删除失败')
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-list {
  padding: 10px;
}
</style>
