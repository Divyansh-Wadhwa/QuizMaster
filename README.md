# Quiz Application

A microservices-based quiz application with student and admin portals.

## Architecture

### Backend (Spring Boot Microservices)
- **auth-quiz-service** (Port 8080) - Student authentication and user management
- **question-bank-service** (Port 8081) - Quiz question management
- **result-service** (Port 8082) - Results and scoring

### Frontend
- HTML/CSS/JavaScript
- Student portal: Login, Dashboard, Quiz, Results
- Admin portal: User management, Quiz hosting

### Database
- **MongoDB** (Port 27017)
- Database name: `quiz_application`
- Collections: students, questions, results

## Prerequisites

- **Java 17 or higher**
- **Maven 3.9+**
- **MongoDB 4.4+**

## Setup Instructions

### 1. Install MongoDB

**Windows (using Chocolatey):**
```powershell
choco install mongodb
```

**Or download from:** https://www.mongodb.com/try/download/community

### 2. Start MongoDB

```powershell
# Start MongoDB service
net start MongoDB

# Or run manually
mongod --dbpath C:\data\db
```

### 3. Build All Services

```powershell
# auth-quiz-service
cd auth-quiz-service
mvn clean package -DskipTests

# question-bank-service  
cd ..\question-bank-service
mvn clean package -DskipTests

# result-service
cd ..\result-service
mvn clean package -DskipTests
```

### 4. Run Services

**Terminal 1 - Auth Service:**
```powershell
cd auth-quiz-service
mvn spring-boot:run
```

**Terminal 2 - Question Bank Service:**
```powershell
cd question-bank-service
mvn spring-boot:run
```

**Terminal 3 - Result Service:**
```powershell
cd result-service
mvn spring-boot:run
```

### 5. Open Frontend

Open `online-quiz-frontend/index.html` in a web browser using Live Server or similar.

## API Endpoints

### Auth Service (8080)
- `POST /api/auth/register` - Register new student
- `POST /api/auth/login` - Student login
- `GET /api/auth/admin/users` - Get all users
- `PUT /api/auth/admin/users/{id}` - Update user
- `DELETE /api/auth/admin/users/{id}` - Delete user

### Question Bank Service (8081)
- `GET /api/questions/quiz/all` - Get all quizzes
- `GET /api/questions/quiz/{quizId}` - Get questions for quiz
- `POST /api/questions/add` - Add new question
- `PUT /api/questions/{id}` - Update question
- `DELETE /api/questions/{id}` - Delete question

### Result Service (8082)
- `POST /api/results/submit` - Submit quiz results
- `GET /api/results/quiz/{quizId}` - Get results for quiz
- `GET /api/results/{id}` - Get specific result
- `DELETE /api/results/quiz/{quizId}` - Delete quiz results

## Database Collections

### students
```javascript
{
  _id: ObjectId,
  username: String,
  email: String,
  password: String,
  createdAt: Date
}
```

### questions
```javascript
{
  _id: ObjectId,
  questionText: String,
  optionA: String,
  optionB: String,
  optionC: String,
  optionD: String,
  correctAnswer: String,
  quizId: String,
  quizName: String,
  hostUsername: String
}
```

### results
```javascript
{
  _id: ObjectId,
  studentUsername: String,
  quizId: String,
  totalQuestions: Number,
  correctAnswers: Number,
  score: Number
}
```

## Configuration

All services connect to MongoDB at:
- **URI:** `mongodb://localhost:27017/quiz_application`
- **Database:** `quiz_application`

## 🚀 Live Demo

**Test it out:** https://quiz-master-lziv.vercel.app/

### Deployed Services
- **Frontend:** https://quiz-master-lziv.vercel.app/
- **Auth Service:** https://quizmaster-auth.onrender.com/
- **Question Bank:** https://quizmaster-0z97.onrender.com/
- **Result Service:** https://quiz-result-service.onrender.com/

## Troubleshooting

**MongoDB connection error:**
- Ensure MongoDB is running: `net start MongoDB`
- Check if port 27017 is available

**Port already in use:**
- Change ports in `application.properties` files

**Compilation errors:**
- Ensure Java 17+ is installed
- Run `mvn clean install` in each service directory
