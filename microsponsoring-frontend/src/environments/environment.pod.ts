export const environment = {
  production: true,
  // LoadBalancer URLs - Replace <backend-lb-ip> and <frontend-lb-ip> with actual IPs after deployment
  apiUrl: 'http://<backend-lb-ip>:8080/api',
  authUrl: 'http://<backend-lb-ip>:8080/api/auth',
  usersUrl: 'http://<backend-lb-ip>:8080/api/users',
  paymentsUrl: 'http://<backend-lb-ip>:8080/api/payments',
  uploadUrl: 'http://<backend-lb-ip>:8080/api/upload',
  companiesUrl: 'http://<backend-lb-ip>:8080/api/companies-non-profits',
  sponsorsUrl: 'http://<backend-lb-ip>:8080/api/sponsors',
  recognitionBenefitsUrl: 'http://<backend-lb-ip>:8080/api/recognition-benefits',
  invoicesUrl: 'http://<backend-lb-ip>:8080/api/invoices',
  pageCustomizationsUrl: 'http://<backend-lb-ip>:8080/api/page-customizations',
  bankAccountsUrl: 'http://<backend-lb-ip>:8080/api/bank-accounts',
  notificationsUrl: 'http://<backend-lb-ip>:8080/api/notifications',
  wsUrl: 'ws://<backend-lb-ip>:8081/ws-notifications',
  imageUrl: 'http://<image-lb-ip>',
  baseUrl: 'http://<frontend-lb-ip>'
};
