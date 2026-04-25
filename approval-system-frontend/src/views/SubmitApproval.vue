<template>
  <div class="submit-approval">
    <h2>提交审批</h2>

    <el-card style="margin-top: 20px;">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px" style="max-width: 600px;">
        <el-form-item label="选择审批类型" prop="templateId">
          <el-select v-model="form.templateId" placeholder="请选择审批类型" style="width: 100%;" @change="handleTemplateChange">
            <el-option
              v-for="template in templates"
              :key="template.templateId"
              :label="template.templateName + ' - ' + template.approvalType"
              :value="template.templateId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审批标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入审批标题"></el-input>
        </el-form-item>
        <el-form-item label="审批金额" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :min="0" style="width: 100%;" placeholder="请输入金额"></el-input-number>
        </el-form-item>
        <el-form-item label="审批内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入审批内容详情">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交审批</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="selectedTemplate" style="margin-top: 20px;">
      <div slot="header">
        <span>审批流程预览（根据配置金额可能有所不同）</span>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="(node, index) in selectedTemplate.nodes"
          :key="index"
          :timestamp="getSignTypeText(node.signType)"
          placement="top">
          <el-card>
            <h4>{{ node.nodeName }}</h4>
            <p>审批人规则：{{ getRuleTypeText(node.approverRuleType) }}</p>
            <p v-if="node.minAmount !== null || node.maxAmount !== null">
              金额范围：{{ node.minAmount !== null ? node.minAmount : '不限' }} - {{ node.maxAmount !== null ? node.maxAmount : '不限' }}
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'SubmitApproval',
  data() {
    return {
      templates: [],
      selectedTemplate: null,
      form: {
        templateId: '',
        title: '',
        content: '',
        amount: null
      },
      rules: {
        templateId: [
          { required: true, message: '请选择审批类型', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入审批标题', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入审批金额', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadTemplates()
  },
  methods: {
    loadTemplates() {
      this.$http.get('/templates').then(res => {
        if (res.data.success && res.data.data) {
          this.templates = res.data.data
        }
      })
    },
    handleTemplateChange(templateId) {
      this.selectedTemplate = this.templates.find(t => t.templateId === templateId)
    },
    getSignTypeText(signType) {
      if (signType === 'AND_SIGN') return '会签（所有人通过）'
      if (signType === 'OR_SIGN') return '或签（一人通过即可）'
      return '未设置'
    },
    getRuleTypeText(ruleType) {
      const map = {
        'DIRECT_MANAGER': '直属上级',
        'DEPARTMENT_HEAD': '部门主管',
        'FINANCE_ROLE': '财务角色',
        'FIXED_USER': '固定用户'
      }
      return map[ruleType] || '未设置'
    },
    getCurrentUser() {
      const userStr = localStorage.getItem('currentUser')
      if (userStr) {
        return JSON.parse(userStr)
      }
      return null
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const user = this.getCurrentUser()
          if (!user) {
            this.$message.error('请先选择当前用户')
            return
          }

          this.$http.post('/forms/submit-direct', this.form, {
            params: { applicantId: user.userId }
          }).then(res => {
            if (res.data.success) {
              this.$message.success('提交成功！')
              this.$router.push('/my-approval')
            } else {
              this.$message.error(res.data.message || '提交失败')
            }
          })
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.selectedTemplate = null
    }
  }
}
</script>

<style scoped>
.submit-approval {
  padding: 10px;
}
</style>
