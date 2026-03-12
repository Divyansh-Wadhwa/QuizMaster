# MongoDB Database Schema

Database: `quiz_application`

## Collections

### 1. students
```javascript
{
  _id: ObjectId,
  username: String (unique, indexed),
  email: String (unique, indexed),
  password: String,
  createdAt: Date,
  updatedAt: Date
}
```

### 2. questions
```javascript
{
  _id: ObjectId,
  questionText: String,
  optionA: String,
  optionB: String,
  optionC: String,
  optionD: String,
  correctAnswer: String, // 'A', 'B', 'C', or 'D'
  quizId: String,
  quizName: String,
  hostUsername: String,
  createdAt: Date,
  updatedAt: Date
}
```

### 3. results
```javascript
{
  _id: ObjectId,
  studentUsername: String,
  quizId: String,
  totalQuestions: Number,
  correctAnswers: Number,
  score: Number,
  createdAt: Date,
  updatedAt: Date
}
```

## Indexes

Create these indexes for better performance:

```javascript
// students collection
db.students.createIndex({ "username": 1 }, { unique: true })
db.students.createIndex({ "email": 1 }, { unique: true })

// questions collection
db.questions.createIndex({ "quizId": 1 })
db.questions.createIndex({ "hostUsername": 1 })

// results collection
db.results.createIndex({ "quizId": 1 })
db.results.createIndex({ "studentUsername": 1 })
db.results.createIndex({ "quizId": 1, "studentUsername": 1 }, { unique: true })
```

## Setup Instructions

1. Install MongoDB Community Edition from https://www.mongodb.com/try/download/community
2. Start MongoDB service:
   ```bash
   # Windows
   net start MongoDB
   
   # Or run mongod directly
   mongod --dbpath C:\data\db
   ```
3. The application will automatically create collections when you start the microservices
4. Indexes will be created automatically by Spring Data MongoDB based on @Indexed annotations
