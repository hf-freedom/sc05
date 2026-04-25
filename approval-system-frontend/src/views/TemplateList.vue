<template>
  <div class="template-list">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>审批模板管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <i class="el-icon-plus"></i> 添加模板
      </el-button>
    </div>

    <el-table :data="templates" border stripe style="width: 100%;">
      <el-table-column prop="templateName" label="模板名称" width="200"></el-table-column>
      <el-table-column prop="approvalType" label="审批类型" width="150"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column label="节点数量" width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.nodes ? scope.row.nodes.length : 0 }}
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="审批类型" prop="approvalType">
          <el-select v-model="form.approvalType" placeholder="请选择审批类型" style="width: 100%;">
            <el-option label="报销审批" value="报销审批"></el-option>
            <el-option label="采购审批" value="采购审批"></el-option>
            <el-option label="合同审批" value="合同审批"></el-option>
            <el-option label="请假审批" value="请假审批"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述"></el-input>
        </el-form-item>

        <el-form-item label="审批节点">
          <div style="width: 100%;">
            <el-card v-for="(node, index) in form.nodes" :key="index" style="margin-bottom: 15px;">
              <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
                <span>节点 {{ index + 1 }}</span>
                <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="removeNode(index)"></el-button>
              </div>
              <el-form :model="node" label-width="100px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="节点名称">
                      <el-input v-model="node.nodeName" placeholder="如：部门主管审批"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="节点顺序">
                      <el-input-number v-model="node.nodeOrder" :min="1" :max="100"></el-input-number>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="审批方式">
                      <el-select v-model="node.signType" placeholder="请选择" style="width: 100%;">
                        <el-option label="会签（所有人通过）" :value="'AND_SIGN'"></el-option>
                        <el-option label="或签（一人通过即可）" :value="'OR_SIGN'"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="审批人规则">
                      <el-select v-model="node.approverRuleType" placeholder="请选择" style="width: 100%;">
                        <el-option label="直属上级" :value="'DIRECT_MANAGER'"></el-option>
                        <el-option label="部门主管" :value="'DEPARTMENT_HEAD'"></el-option>
                        <el-option label="财务角色" :value="'FINANCE_ROLE'"></el-option>
                        <el-option label="固定用户" :value="'FIXED_USER'"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20" v-if="node.approverRuleType === 'FIXED_USER'">
                  <el-col :span="24">
                    <el-form-item label="选择用户">
                      <el-select v-model="node.fixedUserIds" multiple placeholder="请选择审批用户" style="width: 100%;">
                        <el-option
                          v-for="user in users"
                          :key="user.userId"
                          :label="user.name + ' (' + user.department + ')'"
                          :value="user.userId">
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="最小金额">
                      <el-input-number v-model="node.minAmount" :precision="2" :min="0" placeholder="不限制"></el-input-number>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="最大金额">
                      <el-input-number v-model="node.maxAmount" :precision="2" :min="0" placeholder="不限制"></el-input-number>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>
            <el-button type="primary" size="small" icon="el-icon-plus" @click="addNode">添加节点</el-button>
          </div>
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
export default {
  name: 'TemplateList',
  data() {
    return {
      templates: [],
      users: [],
      dialogVisible: false,
      dialogTitle: '添加模板',
      form: {
        templateId: '',
        templateName: '',
        approvalType: '',
        description: '',
        nodes: []
      },
      rules: {
        templateName: [
          { required: true, message: '请输入模板名称', trigger: 'blur' }
        ],
        approvalType: [
          { required: true, message: '请选择审批类型', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.loadTemplates()
    this.loadUsers()
  },
  methods: {
    loadTemplates() {
      this.$http.get('/templates').then(res => {
        if (res.data.success && res.data.data) {
          this.templates = res.data.data
        }
      })
    },
    loadUsers() {
      this.$http.get('/users').then(res => {
        if (res.data.success && res.data.data) {
          this.users = res.data.data
        }
      })
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    openAddDialog() {
      this.dialogTitle = '添加模板'
      this.form = {
        templateId: '',
        templateName: '',
        approvalType: '',
        description: '',
        nodes: []
      }
      this.dialogVisible = true
    },
    openEditDialog(template) {
      this.dialogTitle = '编辑模板'
      this.form = JSON.parse(JSON.stringify(template))
      this.dialogVisible = true
    },
    addNode() {
      this.form.nodes.push({
        nodeId: '',
        templateId: this.form.templateId,
        nodeName: '',
        nodeOrder: this.form.nodes.length + 1,
        signType: 'AND_SIGN',
        approverRuleType: 'DIRECT_MANAGER',
        fixedUserIds: [],
        minAmount: null,
        maxAmount: null
      })
    },
    removeNode(index) {
      this.form.nodes.splice(index, 1)
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.templateId) {
            this.$http.put(`/templates/${this.form.templateId}`, this.form).then(res => {
              if (res.data.success) {
                this.$message.success('更新成功')
                this.dialogVisible = false
                this.loadTemplates()
              } else {
                this.$message.error(res.data.message || '更新失败')
              }
            })
          } else {
            this.$http.post('/templates', this.form).then(res => {
              if (res.data.success) {
                this.$message.success('添加成功')
                this.dialogVisible = false
                this.loadTemplates()
              } else {
                this.$message.error('添加失败')
              }
            })
          }
        }
      })
    },
    handleDelete(template) {
      this.$confirm(`确定要删除模板 "${template.templateName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete(`/templates/${template.templateId}`).then(res => {
          if (res.data.success) {
            this.$message.success('删除成功')
            this.loadTemplates()
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
.template-list {
  padding: 10px;
}
</style>
