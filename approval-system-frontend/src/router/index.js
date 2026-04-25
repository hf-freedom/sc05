import Vue from 'vue'
import VueRouter from 'vue-router'
import UserList from '../views/UserList.vue'
import TemplateList from '../views/TemplateList.vue'
import SubmitApproval from '../views/SubmitApproval.vue'
import MyApproval from '../views/MyApproval.vue'
import ApprovalDetail from '../views/ApprovalDetail.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/users'
  },
  {
    path: '/users',
    name: 'UserList',
    component: UserList,
    meta: { title: '用户管理' }
  },
  {
    path: '/templates',
    name: 'TemplateList',
    component: TemplateList,
    meta: { title: '审批模板管理' }
  },
  {
    path: '/submit',
    name: 'SubmitApproval',
    component: SubmitApproval,
    meta: { title: '提交审批' }
  },
  {
    path: '/my-approval',
    name: 'MyApproval',
    component: MyApproval,
    meta: { title: '我的审批' }
  },
  {
    path: '/detail/:formId',
    name: 'ApprovalDetail',
    component: ApprovalDetail,
    meta: { title: '审批详情' }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '动态审批流系统'
  next()
})

export default router
