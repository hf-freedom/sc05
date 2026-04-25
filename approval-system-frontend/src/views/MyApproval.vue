<template>
  <div class="my-approval">
    <h2>我的审批</h2>

    <el-tabs v-model="activeTab" style="margin-top: 20px;">
      <el-tab-pane label="我提交的" name="submitted">
        <el-table :data="submittedForms" border stripe style="width: 100%;">
          <el-table-column prop="title" label="审批标题" width="200"></el-table-column>
          <el-table-column prop="approvalType" label="审批类型" width="120"></el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="viewDetail(scope.row)">查看详情</el-button>
              <el-button
                v-if="scope.row.status === 'IN_PROGRESS'"
                type="warning"
                size="mini"
                @click="handleWithdraw(scope.row)">
                撤回
              </el-button>
              <el-button
                v-if="scope.row.status === 'REJECTED'"
                type="success"
                size="mini"
                @click="openResubmitDialog(scope.row)">
                重新提交
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="待我审批" name="pending">
        <el-table :data="pendingForms" border stripe style="width: 100%;">
          <el-table-column prop="title" label="审批标题" width="200"></el-table-column>
          <el-table-column prop="approvalType" label="审批类型" width="120"></el-table-column>
          <el-table-column label="申请人" width="120">
            <template slot-scope="scope">
              {{ getUserName(scope.row.applicantId) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="viewDetail(scope.row)">查看详情</el-button>
              <el-button type="success" size="mini" @click="openApproveDialog(scope.row)">通过</el-button>
              <el-button type="danger" size="mini" @click="openRejectDialog(scope.row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="全部审批" name="all">
        <el-table :data="allForms" border stripe style="width: 100%;">
          <el-table-column prop="title" label="审批标题" width="200"></el-table-column>
          <el-table-column prop="approvalType" label="审批类型" width="120"></el-table-column>
          <el-table-column label="申请人" width="120">
            <template slot-scope="scope">
              {{ getUserName(scope.row.applicantId) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="viewDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="审批通过" :visible.sync="approveDialogVisible" width="400px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见（可选）">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleApprove">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="审批驳回" :visible.sync="rejectDialogVisible" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="danger" @click="handleReject">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="重新提交" :visible.sync="resubmitDialogVisible" width="500px">
      <el-form :model="resubmitForm" label-width="100px">
        <el-form-item label="审批标题">
          <el-input v-model="resubmitForm.title"></el-input>
        </el-form-item>
        <el-form-item label="审批金额">
          <el-input-number v-model="resubmitForm.amount" :precision="2" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="审批内容">
          <el-input v-model="resubmitForm.content" type="textarea" :rows="4"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="resubmitDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleResubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MyApproval',
  data() {
    return {
      activeTab: 'submitted',
      users: [],
      submittedForms: [],
      pendingForms: [],
      allForms: [],
      currentForm: null,
      approveDialogVisible: false,
      rejectDialogVisible: false,
      resubmitDialogVisible: false,
      approveForm: {
        comment: ''
      },
      rejectForm: {
        comment: ''
      },
      resubmitForm: {
        title: '',
        amount: null,
        content: ''
      }
    }
  },
  created() {
    this.loadUsers()
    this.loadData()
  },
  methods: {
    getCurrentUser() {
      const userStr = localStorage.getItem('currentUser')
      if (userStr) {
        return JSON.parse(userStr)
      }
      return null
    },
    loadUsers() {
      this.$http.get('/users').then(res => {
        if (res.data.success && res.data.data) {
          this.users = res.data.data
        }
      })
    },
    loadData() {
      const user = this.getCurrentUser()
      if (!user) {
        return
      }

      this.$http.get(`/forms/applicant/${user.userId}`).then(res => {
        if (res.data.success && res.data.data) {
          this.submittedForms = res.data.data
        }
      })

      this.$http.get(`/forms/approver/${user.userId}`).then(res => {
        if (res.data.success && res.data.data) {
          this.pendingForms = res.data.data
        }
      })

      this.$http.get('/forms').then(res => {
        if (res.data.success && res.data.data) {
          this.allForms = res.data.data
        }
      })
    },
    getUserName(userId) {
      if (!userId) return '-'
      const user = this.users.find(u => u.userId === userId)
      return user ? user.name : '-'
    },
    getStatusText(status) {
      const map = {
        'DRAFT': '草稿',
        'IN_PROGRESS': '审批中',
        'APPROVED': '审批通过',
        'REJECTED': '审批驳回',
        'WITHDRAWN': '已撤回'
      }
      return map[status] || status
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'IN_PROGRESS': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return map[status] || 'info'
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    viewDetail(form) {
      this.$router.push(`/detail/${form.formId}`)
    },
    openApproveDialog(form) {
      this.currentForm = form
      this.approveForm.comment = ''
      this.approveDialogVisible = true
    },
    openRejectDialog(form) {
      this.currentForm = form
      this.rejectForm.comment = ''
      this.rejectDialogVisible = true
    },
    openResubmitDialog(form) {
      this.currentForm = form
      this.resubmitForm.title = form.title
      this.resubmitForm.amount = form.amount
      this.resubmitForm.content = form.content
      this.resubmitDialogVisible = true
    },
    handleApprove() {
      const user = this.getCurrentUser()
      if (!user) {
        this.$message.error('请先选择当前用户')
        return
      }

      this.$http.post(`/forms/${this.currentForm.formId}/approve`, null, {
        params: {
          approverId: user.userId,
          comment: this.approveForm.comment
        }
      }).then(res => {
        if (res.data.success) {
          this.$message.success('审批通过')
          this.approveDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.data.message || '操作失败')
        }
      })
    },
    handleReject() {
      const user = this.getCurrentUser()
      if (!user) {
        this.$message.error('请先选择当前用户')
        return
      }

      this.$http.post(`/forms/${this.currentForm.formId}/reject`, null, {
        params: {
          approverId: user.userId,
          comment: this.rejectForm.comment
        }
      }).then(res => {
        if (res.data.success) {
          this.$message.success('已驳回')
          this.rejectDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.data.message || '操作失败')
        }
      })
    },
    handleWithdraw(form) {
      const user = this.getCurrentUser()
      if (!user) {
        this.$message.error('请先选择当前用户')
        return
      }

      this.$confirm('确定要撤回该审批单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post(`/forms/${form.formId}/withdraw`, null, {
          params: { applicantId: user.userId }
        }).then(res => {
          if (res.data.success) {
            this.$message.success('已撤回')
            this.loadData()
          } else {
            this.$message.error(res.data.message || '操作失败')
          }
        })
      }).catch(() => {})
    },
    handleResubmit() {
      const user = this.getCurrentUser()
      if (!user) {
        this.$message.error('请先选择当前用户')
        return
      }

      this.$http.post(`/forms/${this.currentForm.formId}/resubmit`, this.resubmitForm, {
        params: { applicantId: user.userId }
      }).then(res => {
        if (res.data.success) {
          this.$message.success('重新提交成功')
          this.resubmitDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.data.message || '操作失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.my-approval {
  padding: 10px;
}
</style>
