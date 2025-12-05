# HopeMeds Backend API

A comprehensive Node.js/Express backend API for the HopeMeds pharmacy application, built with MongoDB.

## Features

- **User Authentication**: JWT-based authentication with role-based access control
- **Pharmacy Management**: Complete pharmacy system with medicine stock tracking
- **Order Management**: Full order lifecycle from creation to delivery
- **Location Services**: Geospatial queries for pharmacy and delivery location
- **File Upload**: Support for prescription uploads
- **Real-time Updates**: Order status tracking and notifications

## Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB with Mongoose ODM
- **Authentication**: JWT (JSON Web Tokens)
- **Validation**: Express Validator
- **Security**: bcryptjs for password hashing
- **File Upload**: Multer (planned)

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- MongoDB Atlas account or local MongoDB instance
- npm or yarn package manager

### Installation

1. Clone the repository
```bash
cd backend
```

2. Install dependencies
```bash
npm install
```

3. Create environment file
```bash
cp .env.example .env
```

4. Update environment variables in `.env`
```env
NODE_ENV=development
PORT=5000
MONGODB_URI=mongodb+srv://Ayari:ta5wira55@ayari-list.zjtgaz3.mongodb.net/HopeMeds
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
```

5. Start the server
```bash
npm start
# or for development
npm run dev
```

## API Endpoints

### Authentication Routes (`/api/auth`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/register` | Register new user | Public |
| POST | `/login` | User login | Public |
| GET | `/me` | Get current user | Private |
| PUT | `/update-profile` | Update user profile | Private |
| POST | `/change-password` | Change password | Private |

### Pharmacy Routes (`/api/pharmacies`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| GET | `/` | Get all pharmacies (with filters) | Public |
| GET | `/:id` | Get pharmacy by ID | Public |
| GET | `/:id/stock` | Get pharmacy stock for medicine | Public |
| POST | `/` | Create new pharmacy | Admin |
| PUT | `/:id/stock` | Update pharmacy stock | Admin |
| GET | `/search/medicines` | Search medicines across pharmacies | Public |

### Order Routes (`/api/orders`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/` | Create new order | Private |
| GET | `/` | Get user orders | Private |
| GET | `/:id` | Get order by ID | Private |
| PUT | `/:id/cancel` | Cancel order | Private |
| PUT | `/:id/rate` | Rate delivered order | Private |
| GET | `/driver/available` | Get available orders for drivers | Driver |
| PUT | `/:id/assign-driver` | Assign driver to order | Driver |
| PUT | `/:id/update-status` | Update order status | Driver/Admin |

## Data Models

### User
```javascript
{
  name: String,
  email: String (unique),
  password: String (hashed),
  role: String (customer/driver/admin),
  phone: String,
  address: String,
  latitude: Number,
  longitude: Number,
  isActive: Boolean,
  lastLogin: Date
}
```

### Pharmacy
```javascript
{
  name: String,
  address: String,
  phone: String,
  latitude: Number,
  longitude: Number,
  stock: [{
    medicine: ObjectId,
    stock: Number,
    price: Number
  }],
  services: [String],
  rating: Number,
  isActive: Boolean
}
```

### Medicine
```javascript
{
  name: String,
  genericName: String,
  description: String,
  category: String,
  dosage: String,
  manufacturer: String,
  requiresPrescription: Boolean,
  price: Number,
  isActive: Boolean
}
```

### Order
```javascript
{
  orderId: String (auto-generated),
  customer: ObjectId,
  pharmacy: ObjectId,
  items: [{
    medicine: ObjectId,
    quantity: Number,
    price: Number
  }],
  status: String,
  totalAmount: Number,
  deliveryAddress: String,
  driver: ObjectId,
  trackingHistory: [{
    status: String,
    timestamp: Date,
    note: String
  }]
}
```

## Authentication

The API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Error Handling

The API returns consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "errors": ["Detailed error messages"] // for validation errors
}
```

## Success Responses

All successful responses follow this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "pagination": { /* pagination info for lists */ }
}
```

## Sample API Usage

### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+216 12 345 678",
  "address": "123 Main St, Tunis"
}
```

### Create Order
```bash
POST /api/orders
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "pharmacy": "60d5ecb74b24c72b8c8b4567",
  "items": [
    {
      "medicine": "60d5ecb74b24c72b8c8b4568",
      "quantity": 2
    }
  ],
  "deliveryAddress": "456 Delivery St, Tunis",
  "deliveryLatitude": 36.8065,
  "deliveryLongitude": 10.1815
}
```

## Development

### Project Structure
```
backend/
├── models/          # Database models
├── routes/          # API routes
├── middleware/      # Custom middleware
├── controllers/     # Route controllers (future)
├── utils/          # Utility functions
├── uploads/        # File uploads directory
├── server.js       # Main server file
├── package.json    # Dependencies
├── .env           # Environment variables
└── README.md      # This file
```

### Available Scripts
- `npm start` - Start production server
- `npm run dev` - Start development server with nodemon
- `npm test` - Run tests (not implemented yet)

## Deployment

1. Set environment variables for production
2. Use a process manager like PM2
3. Set up MongoDB replica set for production
4. Configure reverse proxy (nginx)
5. Set up SSL certificates

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

MIT License - see LICENSE file for details

## Support

For support, email support@hopemeds.com or create an issue in the repository.