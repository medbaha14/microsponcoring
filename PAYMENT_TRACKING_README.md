# 💳 Payment Tracking & Transaction Upload System

## Overview
A comprehensive payment tracking and transaction upload system for sponsors in the Microsponsoring platform. This system allows sponsors to track all their payments, upload supporting documents, and generate reports.

## 🚀 Features

### Backend Features
- **PaymentTransaction Model**: Complete transaction tracking with status, type, and file support
- **RESTful API**: Full CRUD operations for payment transactions
- **File Upload/Download**: Secure file handling for transaction documents
- **Search & Filtering**: Advanced filtering by status, type, date range, and search terms
- **Analytics**: Transaction summaries and statistics
- **Security**: Role-based access control (SPONSOR and ADMIN roles)

### Frontend Features
- **Payment Dashboard**: Modern, responsive interface for transaction management
- **Real-time Updates**: Live transaction status tracking
- **Advanced Filtering**: Multiple filter options for finding transactions
- **File Management**: Upload and download transaction documents
- **Export Functionality**: CSV export for accounting purposes
- **Dark Mode Support**: Consistent with application theme

## 📁 File Structure

### Backend Files
```
microsponsoring-backend/src/main/java/com/example/microsponsoringbackend/
├── model/
│   └── PaymentTransaction.java                    # Transaction entity
├── repository/
│   └── PaymentTransactionRepository.java          # Data access layer
├── service/
│   └── PaymentTransactionService.java             # Business logic
├── controller/
│   ├── PaymentTransactionController.java          # REST endpoints
│   └── SampleDataController.java                 # Sample data creation
└── config/
    └── SecurityConfig.java                       # Security configuration
```

### Frontend Files
```
microsponsoring-frontend/src/app/
├── models/
│   └── payment-transaction.model.ts              # TypeScript interfaces
├── services/
│   └── payment-transaction.service.ts            # API service
├── dashboard/sponsor/payment-tracking/
│   ├── payment-tracking.component.ts             # Main component
│   ├── payment-tracking.component.html           # Template
│   └── payment-tracking.component.css            # Styles
└── app.routes.ts                                 # Routing configuration
```

## 🔧 API Endpoints

### Payment Transactions
- `GET /api/payment-transactions/sponsor/{sponsorId}` - Get all transactions
- `GET /api/payment-transactions/sponsor/{sponsorId}/summary` - Get transaction summary
- `GET /api/payment-transactions/sponsor/{sponsorId}/status/{status}` - Filter by status
- `GET /api/payment-transactions/sponsor/{sponsorId}/date-range` - Filter by date range
- `POST /api/payment-transactions` - Create new transaction
- `PUT /api/payment-transactions/{id}` - Update transaction
- `DELETE /api/payment-transactions/{id}` - Delete transaction
- `POST /api/payment-transactions/{id}/upload` - Upload transaction file
- `GET /api/payment-transactions/{id}/download` - Download transaction file
- `GET /api/payment-transactions/sponsor/{sponsorId}/search` - Search transactions

### Sample Data (Admin Only)
- `POST /api/sample-data/payment-transactions/{sponsorId}` - Create sample transactions

## 🎯 Transaction Statuses
- **PENDING**: Transaction is waiting to be processed
- **PROCESSING**: Transaction is being processed
- **COMPLETED**: Transaction completed successfully
- **FAILED**: Transaction failed
- **CANCELLED**: Transaction was cancelled
- **REFUNDED**: Transaction was refunded

## 📊 Transaction Types
- **SPONSORSHIP**: Regular sponsorship payment
- **REFUND**: Refund transaction
- **ADJUSTMENT**: Manual adjustment
- **FEE**: Platform or processing fee
- **OTHER**: Other transaction types

## 🔐 Security
- JWT authentication required for all endpoints
- Role-based access control (SPONSOR and ADMIN roles)
- Secure file upload with validation
- File type and size restrictions

## 🚀 Getting Started

### 1. Backend Setup
The backend endpoints are automatically available when the application starts. No additional configuration required.

### 2. Frontend Setup
The payment tracking component is automatically available in the sponsor dashboard. Navigate to "Payment Tracking" in the sponsor sidebar.

### 3. Creating Sample Data (Admin)
To create sample payment transactions for testing:
```bash
POST /api/sample-data/payment-transactions/{sponsorId}
```

### 4. Using the System
1. **Access**: Navigate to "Payment Tracking" in the sponsor dashboard
2. **View Transactions**: See all your payment history with filters
3. **Upload Documents**: Click "Upload" on any transaction to add supporting files
4. **Search & Filter**: Use the search bar and filters to find specific transactions
5. **Export Data**: Use the "Export CSV" button to download transaction data

## 📱 Responsive Design
The payment tracking system is fully responsive and works on:
- Desktop computers
- Tablets
- Mobile phones

## 🌙 Dark Mode
The system supports dark mode and automatically adapts to the user's theme preference.

## 🔄 Real-time Updates
- Transaction status updates in real-time
- Live filtering and search
- Automatic pagination updates

## 📈 Analytics Features
- Monthly spending charts
- Transaction summary statistics
- Status distribution tracking
- Recent activity monitoring

## 🛠️ Technical Details

### Database Schema
The `payment_transactions` table includes:
- Transaction ID (UUID)
- Sponsor reference
- Company reference
- Amount and currency
- Payment method
- Transaction references
- Status and type
- File upload metadata
- Timestamps

### File Upload
- Supported formats: PDF, JPG, JPEG, PNG, DOC, DOCX
- Maximum file size: 10MB
- Secure file storage in `src/main/resources/transactions/`
- File metadata tracking

### Performance
- Pagination for large transaction lists
- Efficient database queries with proper indexing
- Lazy loading for better performance
- Optimized file handling

## 🐛 Troubleshooting

### Common Issues
1. **File Upload Fails**: Check file size and format restrictions
2. **Transactions Not Loading**: Verify sponsor ID and authentication
3. **Export Issues**: Ensure browser allows file downloads

### Debug Mode
Enable debug logging in the backend to troubleshoot issues:
```properties
logging.level.com.example.microsponsoringbackend=DEBUG
```

## 🔮 Future Enhancements
- Bulk transaction upload via CSV
- Advanced reporting and analytics
- Email notifications for transaction status changes
- Integration with external accounting systems
- Mobile app support
- Real-time notifications

## 📞 Support
For technical support or questions about the payment tracking system, please contact the development team.

---

**Last Updated**: January 2025
**Version**: 1.0.0
**Status**: Production Ready ✅
