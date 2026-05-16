import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '../stores/admin'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import BooksView from '../views/BooksView.vue'
import BookDetailView from '../views/BookDetailView.vue'
import BooklistsView from '../views/BooklistsView.vue'
import BooklistDetailView from '../views/BooklistDetailView.vue'
import ActivitiesView from '../views/ActivitiesView.vue'
import MyActivitiesView from '../views/MyActivitiesView.vue'
import MessagesView from '../views/MessagesView.vue'
import ProfileView from '../views/ProfileView.vue'
import AuthView from '../views/AuthView.vue'
import AdminLayoutView from '../views/admin/AdminLayoutView.vue'
import AdminLoginView from '../views/admin/AdminLoginView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminBooksView from '../views/admin/AdminBooksView.vue'
import AdminBooklistsView from '../views/admin/AdminBooklistsView.vue'
import AdminActivitiesView from '../views/admin/AdminActivitiesView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/auth', name: 'auth', component: AuthView, meta: { layout: 'auth' } },
    { path: '/books', name: 'books', component: BooksView },
    { path: '/books/:id', name: 'book-detail', component: BookDetailView },
    { path: '/booklists', name: 'booklists', component: BooklistsView },
    { path: '/booklists/:id', name: 'booklist-detail', component: BooklistDetailView },
    { path: '/activities', name: 'activities', component: ActivitiesView },
    { path: '/my-activities', name: 'my-activities', component: MyActivitiesView },
    { path: '/messages', name: 'messages', component: MessagesView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/user/:userId', name: 'user-profile', component: ProfileView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView, meta: { layout: 'admin' } },
    {
      path: '/admin',
      component: AdminLayoutView,
      meta: { requiresAdmin: true, layout: 'admin' },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboardView, meta: { title: '后台概览' } },
        { path: 'books', name: 'admin-books', component: AdminBooksView, meta: { title: '图书管理' } },
        { path: 'booklists', name: 'admin-booklists', component: AdminBooklistsView, meta: { title: '书单管理' } },
        { path: 'activities', name: 'admin-activities', component: AdminActivitiesView, meta: { title: '活动管理' } },
        { path: 'users', name: 'admin-users', component: AdminUsersView, meta: { title: '用户管理' } }
      ]
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const adminStore = useAdminStore()
  const authStore = useAuthStore()
  const needsAdmin = to.matched.some((record) => Boolean(record.meta?.requiresAdmin))

  if (needsAdmin && !adminStore.isAdminAuthed && !authStore.isAdmin) {
    return {
      name: 'auth',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.name === 'admin-login' && (adminStore.isAdminAuthed || authStore.isAdmin)) {
    return { name: 'admin-dashboard' }
  }

  return true
})

export default router
