// Frontend Environment Configuration Template
// Replace the placeholder IPs with actual LoadBalancer IPs after deployment

export const environment = {
  production: true,
  // LoadBalancer URLs - Replace placeholders with actual IPs
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

// Example with actual IPs (replace with your real IPs):
// export const environment = {
//   production: true,
//   apiUrl: 'http://192.168.1.100:8080/api',
//   authUrl: 'http://192.168.1.100:8080/api/auth',
//   usersUrl: 'http://192.168.1.100:8080/api/users',
//   paymentsUrl: 'http://192.168.1.100:8080/api/payments',
//   uploadUrl: 'http://192.168.1.100:8080/api/upload',
//   companiesUrl: 'http://192.168.1.100:8080/api/companies-non-profits',
//   sponsorsUrl: 'http://192.168.1.100:8080/api/sponsors',
//   recognitionBenefitsUrl: 'http://192.168.1.100:8080/api/recognition-benefits',
//   invoicesUrl: 'http://192.168.1.100:8080/api/invoices',
//   pageCustomizationsUrl: 'http://192.168.1.100:8080/api/page-customizations',
//   bankAccountsUrl: 'http://192.168.1.100:8080/api/bank-accounts',
//   notificationsUrl: 'http://192.168.1.100:8080/api/notifications',
//   wsUrl: 'ws://192.168.1.100:8081/ws-notifications',
//   imageUrl: 'http://192.168.1.101',
//   baseUrl: 'http://192.168.1.102'
// };
