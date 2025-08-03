# BlogNest API Documentation

## Overview

BlogNest is a scalable blog platform built with Spring Boot microservices architecture. This document provides comprehensive API documentation for all services.

## Base URL

- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.blognest.com/api/v1`

## Authentication

All protected endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## User Service API

### Authentication Endpoints

#### POST /users/auth/login
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "Authentication successful",
  "data": {
    "token": "jwt-token-string",
    "username": "string",
    "email": "string",
    "role": "USER"
  },
  "timestamp": "2023-12-01T10:00:00"
}
```

#### POST /users/auth/register
Register a new user account.

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "role": "USER",
    "enabled": true,
    "createdAt": "2023-12-01T10:00:00"
  },
  "timestamp": "2023-12-01T10:00:00"
}
```

#### GET /users/auth/me
Get current authenticated user information.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "role": "USER",
    "enabled": true,
    "createdAt": "2023-12-01T10:00:00",
    "lastLogin": "2023-12-01T10:00:00"
  }
}
```

### User Management Endpoints

#### GET /users/{id}
Get user by ID (Admin only or own profile).

#### GET /users/username/{username}
Get user by username (Admin only or own profile).

#### GET /users
Get all users (Admin only).

#### PUT /users/{id}
Update user information.

**Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string"
}
```

#### DELETE /users/{id}
Delete user account (Admin only).

#### GET /users/search?keyword={keyword}
Search users by keyword (Admin only).

#### GET /users/role/{role}
Get users by role (Admin only).

#### GET /users/active
Get all active users (Admin only).

## Blog Service API

### Blog Post Endpoints

#### GET /blogs
Get all published blog posts with pagination.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sort`: Sort field (default: "createdAt")
- `direction`: Sort direction (default: "DESC")

**Response:**
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "string",
        "excerpt": "string",
        "authorUsername": "string",
        "viewCount": 0,
        "likeCount": 0,
        "commentCount": 0,
        "createdAt": "2023-12-01T10:00:00",
        "publishedAt": "2023-12-01T10:00:00",
        "categories": [],
        "tags": []
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0
  }
}
```

#### GET /blogs/{id}
Get blog post by ID.

#### POST /blogs
Create a new blog post.

**Request Body:**
```json
{
  "title": "string",
  "content": "string",
  "excerpt": "string",
  "featuredImage": "string",
  "status": "DRAFT",
  "categoryIds": [1, 2],
  "tagIds": [1, 2]
}
```

#### PUT /blogs/{id}
Update blog post.

#### DELETE /blogs/{id}
Delete blog post (Author or Admin only).

#### GET /blogs/search?q={query}
Search blog posts.

#### GET /blogs/category/{categoryId}
Get blog posts by category.

#### GET /blogs/tag/{tagId}
Get blog posts by tag.

#### POST /blogs/{id}/like
Like a blog post.

#### DELETE /blogs/{id}/like
Unlike a blog post.

### Category Endpoints

#### GET /categories
Get all categories.

#### POST /categories
Create a new category (Admin only).

#### PUT /categories/{id}
Update category (Admin only).

#### DELETE /categories/{id}
Delete category (Admin only).

### Tag Endpoints

#### GET /tags
Get all tags.

#### POST /tags
Create a new tag (Admin only).

#### PUT /tags/{id}
Update tag (Admin only).

#### DELETE /tags/{id}
Delete tag (Admin only).

## Comment Service API

### Comment Endpoints

#### GET /blogs/{blogId}/comments
Get comments for a blog post.

#### POST /blogs/{blogId}/comments
Add a comment to a blog post.

**Request Body:**
```json
{
  "content": "string",
  "parentId": null
}
```

#### PUT /comments/{id}
Update comment (Author only).

#### DELETE /comments/{id}
Delete comment (Author or Admin only).

#### POST /comments/{id}/like
Like a comment.

#### DELETE /comments/{id}/like
Unlike a comment.

## Notification Service API

### Notification Endpoints

#### GET /notifications
Get user notifications.

#### PUT /notifications/{id}/read
Mark notification as read.

#### DELETE /notifications/{id}
Delete notification.

#### POST /notifications/read-all
Mark all notifications as read.

## Error Responses

All endpoints return standardized error responses:

```json
{
  "status": "ERROR",
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "field": "field_name",
    "details": "Detailed error message"
  },
  "timestamp": "2023-12-01T10:00:00"
}
```

## Common Error Codes

- `VALIDATION_ERROR`: Input validation failed
- `AUTHENTICATION_ERROR`: Authentication required
- `AUTHORIZATION_ERROR`: Insufficient permissions
- `RESOURCE_NOT_FOUND`: Resource not found
- `DUPLICATE_RESOURCE`: Resource already exists
- `INTERNAL_SERVER_ERROR`: Server error

## Rate Limiting

- **Public endpoints**: 100 requests per minute
- **Authenticated endpoints**: 1000 requests per minute
- **Admin endpoints**: 5000 requests per minute

## Pagination

All list endpoints support pagination with the following parameters:

- `page`: Page number (0-based)
- `size`: Page size (default: 10, max: 100)
- `sort`: Sort field
- `direction`: Sort direction (ASC/DESC)

## WebSocket Endpoints

### Real-time Notifications

**Connection URL:** `ws://localhost:8080/ws/notifications`

**Message Types:**
- `NEW_COMMENT`: New comment notification
- `NEW_LIKE`: New like notification
- `NEW_FOLLOWER`: New follower notification

## Testing

### Test Credentials

**Admin User:**
- Username: `admin@blognest.com`
- Password: `admin123`

**Regular User:**
- Username: `user@blognest.com`
- Password: `user123`

### Postman Collection

Import the provided Postman collection for testing all endpoints:

[BlogNest API Collection](https://github.com/yourusername/blognest/blob/main/docs/BlogNest_API.postman_collection.json)

## SDKs and Libraries

### JavaScript/TypeScript

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

### Python

```python
import requests

class BlogNestAPI:
    def __init__(self, base_url="http://localhost:8080/api/v1"):
        self.base_url = base_url
        self.token = None
    
    def login(self, username, password):
        response = requests.post(f"{self.base_url}/users/auth/login", {
            "username": username,
            "password": password
        })
        data = response.json()
        self.token = data["data"]["token"]
        return data
    
    def get_headers(self):
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers
```

## Support

For API support and questions:

- **Email**: api-support@blognest.com
- **Documentation**: https://docs.blognest.com
- **GitHub Issues**: https://github.com/yourusername/blognest/issues 