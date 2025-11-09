// Global authentication middleware for SAAS Manager Admin Panel
export default defineNuxtRouteMiddleware((to, from) => {
  const { checkAuth } = useSaasAuth()
  
  // Public routes that don't require authentication
  const publicRoutes = ['/login']
  
  const isPublicRoute = publicRoutes.includes(to.path)
  const isAuthenticated = checkAuth()

  // If user is authenticated and trying to access login page, redirect to dashboard
  if (isAuthenticated && to.path === '/login') {
    return navigateTo('/')
  }

  // If user is not authenticated and trying to access protected route, redirect to login
  if (!isAuthenticated && !isPublicRoute) {
    return navigateTo('/login')
  }

  // Allow navigation
  return
})
