<template>
  <div class="approval-detail">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>审批详情</h2>
      <el-button @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
    </div>

    <el-card v-if="form" style="margin-bottom: 20px;">
      <div slot="header">
        <span>基本信息</span>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="审批标题">{{ form.title }}</el-descriptions-item>
        <el-descriptions-item label="审批类型">{{ form.approvalType }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ getUserName(form.applicantId) }}</el-descriptions-item>
        <el-descriptions-item label="审批金额">¥{{ form.amount ? form.amount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">v{{ form.version }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDate(form.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(form.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批内容" :span="2">
          {{ form.content || '无' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="form.currentApproverIds && form.currentApproverIds.length > 0" label="当前审批人" :span="2">
          <el-tag v-for="approverId in form.currentApproverIds" :key="approverId" style="margin-right: 5px;">
            {{ getUserName(approverId) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="instance" style="margin-bottom: 20px;">
      <div slot="header">
        <span>审批流程</span>
      </div>
      <el-steps :active="getCurrentStep()" align-center finish-status="success">
        <el-step
          v-for="(node, index) in instance.nodes"
          :key="index"
          :title="node.nodeName"
          :description="getNodeDescription(node)">
          <template slot="icon">
            <i :class="getNodeIcon(node)"></i>
          </template>
        </el-step>
      </el-steps>

      <div v-if="instance.nodes && instance.nodes.length > 0" style="margin-top: 30px;">
        <el-table :data="instance.nodes" border stripe>
          <el-table-column prop="nodeOrder" label="节点顺序" width="100" align="center"></el-table-column>
          <el-table-column prop="nodeName" label="节点名称" width="200"></el-table-column>
          <el-table-column label="审批方式" width="200">
            <template slot-scope="scope">
              {{ getSignTypeText(scope.row.signType) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="150">
            <template slot-scope="scope">
              <el-tag :type="getNodeStatusType(scope.row, scope.$index)">{{ getNodeStatusText(scope.row, scope.$index) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审批人">
            <template slot-scope="scope">
              <div v-if="scope.row.approverIds && scope.row.approverIds.length > 0">
                <el-tag
                  v-for="approverId in scope.row.approverIds"
                  :key="approverId"
                  :type="getApproverTagType(scope.row, approverId)"
                  style="margin-right: 5px; margin-bottom: 5px;">
                  {{ getUserName(approverId) }}
                  <span v-if="scope.row.approvedIds && scope.row.approvedIds.includes(approverId)">(已通过)</span>
                  <span v-else-if="scope.row.rejectedIds && scope.row.rejectedIds.includes(approverId)">(已驳回)</span>
                </el-tag>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card>
      <div slot="header">
        <span>审批日志</span>
      </div>
      <el-timeline v-if="logs && logs.length > 0">
        <el-timeline-item
          v-for="(log, index) in logs"
          :key="index"
          :timestamp="formatDate(log.actionTime)"
          placement="top">
          <el-card>
            <h4>
              <el-tag :type="getActionType(log.action)">{{ getActionText(log.action) }}</el-tag>
              <span style="margin-left: 10px;">{{ log.approverName || log.approverId }}</span>
            </h4>
            <p v-if="log.nodeName">节点：{{ log.nodeName }}</p>
            <p v-if="log.comment">意见：{{ log.comment }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批日志"></el-empty>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ApprovalDetail',
  data() {
    return {
      formId: '',
      form: null,
      instance: null,
      logs: [],
      users: []
    }
  },
  created() {
    this.formId = this.$route.params.formId
    this.loadUsers()
    this.loadFormDetail()
  },
  methods: {
    loadUsers() {
      this.$http.get('/users').then(res => {
        if (res.data.success && res.data.data) {
          this.users = res.data.data
        }
      })
    },
    loadFormDetail() {
      this.$http.get(`/forms/${this.formId}`).then(res => {
        if (res.data.success && res.data.data) {
          this.form = res.data.data
        }
      })

      this.$http.get(`/forms/${this.formId}/instance`).then(res => {
        if (res.data.success && res.data.data) {
          this.instance = res.data.data
        }
      })

      this.$http.get(`/forms/${this.formId}/logs`).then(res => {
        if (res.data.success && res.data.data) {
          this.logs = res.data.data
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
    getCurrentStep() {
      if (!this.instance) return 0
      return this.instance.currentNodeIndex || 0
    },
    getNodeDescription(node) {
      return this.getSignTypeText(node.signType)
    },
    getSignTypeText(signType) {
      if (signType === 'AND_SIGN') return '会签（所有人通过）'
      if (signType === 'OR_SIGN') return '或签（一人通过即可）'
      return '未设置'
    },
    getNodeIcon(node) {
      if (node.status === 'APPROVED') return 'el-icon-circle-check'
      if (node.status === 'REJECTED') return 'el-icon-circle-close'
      if (node.status === 'IN_PROGRESS') return 'el-icon-loading'
      return 'el-icon-more'
    },
    getNodeStatusType(node, index) {
      const currentIndex = this.instance ? this.instance.currentNodeIndex : 0
      if (index < currentIndex) return 'success'
      if (index === currentIndex) return 'warning'
      return 'info'
    },
    getNodeStatusText(node, index) {
      const currentIndex = this.instance ? this.instance.currentNodeIndex : 0
      if (node.status === 'APPROVED') return '已通过'
      if (node.status === 'REJECTED') return '已驳回'
      if (node.status === 'IN_PROGRESS') return '审批中'
      if (index > currentIndex) return '待审批'
      return '未开始'
    },
    getApproverTagType(node, approverId) {
      if (node.approvedIds && node.approvedIds.includes(approverId)) return 'success'
      if (node.rejectedIds && node.rejectedIds.includes(approverId)) return 'danger'
      return ''
    },
    getActionType(action) {
      const map = {
        'SUBMIT': 'primary',
        'APPROVE': 'success',
        'REJECT': 'danger',
        'WITHDRAW': 'warning',
        'RESUBMIT': 'primary'
      }
      return map[action] || 'info'
    },
    getActionText(action) {
      const map = {
        'SUBMIT': '提交',
        'APPROVE': '通过',
        'REJECT': '驳回',
        'WITHDRAW': '撤回',
        'RESUBMIT': '重新提交'
      }
      return map[action] || action
    }
  }
}
</script>

<style scoped>
.approval-detail {
  padding: 10px;
}
</style>
