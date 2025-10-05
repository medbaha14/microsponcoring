export const environment = {
  production: true,
  apiUrl: '/api',
  authUrl: '/api/auth',
  usersUrl: '/api/users',
  paymentsUrl: '/api/payments',
  uploadUrl: '/api/upload',
  companiesUrl: '/api/companies-non-profits',
  sponsorsUrl: '/api/sponsors',
  recognitionBenefitsUrl: '/api/recognition-benefits',
  invoicesUrl: '/api/invoices',
  pageCustomizationsUrl: '/api/page-customizations',
  bankAccountsUrl: '/api/bank-accounts',
  notificationsUrl: '/api/notifications',
  wsUrl: 'ws://microsponsoring.local/ws-notifications',
  baseUrl: '/',
  // Build information for Kubernetes deployment
  buildInfo: {
    version: '1.0.0',
    buildTime: new Date().toISOString(),
    environment: 'production',
    buildNumber: 'k8s-deployment'
  }
};