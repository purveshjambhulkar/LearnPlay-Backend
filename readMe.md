
# LearnPlay Educational Platform

An interactive educational platform that transforms learning materials into engaging games with various question types.

## Project Structure

This project is divided into two main parts:

### Frontend
- React-based web application
- TypeScript for type safety
- Tailwind CSS for styling
- Interactive components using shadcn/ui
- Located in the `frontend/` directory

### Backend
- Spring Boot application
- Java 17
- JWT authentication
- RESTful API endpoints
- Located in the `backend/` directory

## Features

- Multiple question types: Multiple Choice, True/False, Order, Matching, Fill in the Blanks
- Wave-based gameplay with difficulty progression
- User accounts with progress tracking
- Content upload and transformation into quiz questions
- Dashboard for performance tracking

## Getting Started

### Frontend

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend application will start at http://localhost:8080

### Backend

1. Navigate to the backend directory:
```bash
cd backend
```

2. Run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

The backend API will be available at http://localhost:8081/api

## Authentication

The application uses JWT-based authentication:
- Register at `/auth/signup`
- Login at `/auth/signin`
- Tokens should be included in the Authorization header for protected endpoints

## License

MIT
