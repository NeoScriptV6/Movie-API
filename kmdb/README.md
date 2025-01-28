# Movie-API
## Overview
The **Movies API** is a Spring Boot application that provides a RESTful interface for managing a movie database. This system supports operations for Movies, Actors, and Genres, offering CRUD functionality, advanced filtering, and validation mechanisms. The API leverages Spring Boot, Spring Data JPA, and SQLite for efficient development and storage.

## Features

- **CRUD Operations**: Create, Read, Update, and Delete operations for Movies, Genres, and Actors.
- **Search Functionality**: Filter movies by genre or release year and retrieve actors associated with specific movies.
- **Relationships**: Many-to-Many relationships between Movies and Genres, and Movies and Actors.
- **Input Validation**: Ensure data integrity using Java Bean Validation.
- **Error Handling**: Custom exceptions and global error handling for improved user experience.

# Setup and Installation Instructions
### Prerequisites
- Java 17 or later (java -version to check)
- Maven 3.8 or later (mvn -version to check)
- SQLite or any JDBC-compatible database (sqlite3 --version to check)
- IDE like IntelliJ IDEA or Eclipse

### 2. **Clone the Repository**
   ```
    git clone https://gitea.kood.tech/andersmaitalberg/kmdb.git  

     cd kmdb
   ```

### 3. **Database**

Ensure an SQLite database file is present and check propereties if all is correct and it has installed the dependencies:
- spring.application.name=Movies
- spring.datasource.url=jdbc:sqlite:movies.db
- spring.datasource.driver-class-name=org.sqlite.JDBC
- spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
- spring.jpa.hibernate.ddl-auto=update
- spring.jackson.date-format=yyyy-MM-dd
- spring.jackson.serialization.write-dates-as-timestamps=false

### 4. **Running Application**
   _Once everything is set up, you can run the Spring Boot application by executing:_
   ```
    mvn spring-boot:run
   ```

## API Endpoints
_API includes the following endpoints for Movies, Genres and Actors:_
### Movies
- Create a Movie: POST /api/movies
- Get All Movies: GET api/movies
- Get Movie by id: GET /api/movies/{id}
- Update Movie: PATCH /api/movies/{id}
- Delete Movie: DELETE /api/movies/{id}
- Filter by Genre: GET /api/movies?genre={genreId}
- Filter by Year: GET /api/movies?year={releaseYear}
- Get Actors in a Movie: GET /api/movies/{movieId}/actors

### Genres
- Create a Genre: POST /api/genres
- Get All Genres: GET /api/genres
- Get Genre by ID: GET /api/genres/{id}
- Update Genre: PATCH /api/genres/{id}
- Delete Genre: DELETE /api/genres/{id}
### Actors
- Create an Actor: POST /api/actors
- Get All Actors: GET /api/actors
- Get Actor by ID: GET /api/actors/{id}
- Update Actor: PATCH /api/actors/{id}
- Delete Actor: DELETE /api/actors/{id}
- Filter by Name: GET /api/actors?name={name}

_Use Postman to test the API. Create a collection named Movie Database API and set up requests for all the CRUD operations:_
- POST for creating new entities.
- GET for retrieving entities.
- PATCH for updating entities.
- DELETE for removing entities.

## Usage Guide
Once the API is running, you can interact with the system using HTTP requests via Postman. 
This guide will cover some of available endpoints and their respective functions.
- ### **Create a new Genre**

**Endpoint:** POST [/api/genres](http://localhost:8080/api/genres)

**Request Body(JSON):**
   ```
    {
      "name": "History"
    }
   ```

- ### **Retrieve a Genre by ID**<br>
    **Endpoint:** GET [/api/genres/{id}](http://localhost:8080/api/genres)<br>
    **(Example):**
  ```
    {
     "id": 1,
     "name": "Drama"
    }
  ```

- ### **Retrieve all Genres**<br>
**Endpoint:** GET [/api/genres](http://localhost:8080/api/genres)<br>
**Response (Example):**
  ```
    [
      {
      "id": 1,
      "name": "Drama"
      },
     {
     "id": 2,
     "name": "Action"
     }

     etc,etc.
    ]
  ```

- ### **Update a Genre**
**Endpoint:** PATCH [api/genres/{id}](http://localhost:8080/api/genres/11)<br>
**Request Body (Example):**
  ```
{
"name": "Thriller"
}
  ```

- ### **Delete a Genre**

**Endpoint:** DELETE [/api/genres/{id}](http://localhost:8080/api/genres/11)<br>
**Response:**
  ```
   400 Bad Request if the genre is associated with any movies with a custom message
   204 No Content if the genre was successfully deleted.
  ```
**Force Delete:**
  ```
    If a genre is associated with movies but you still want to delete it, use:
  ```
**Endpoint:** DELETE [/api/genres/{id}](http://localhost:8080/api/genres/11?force=true)
  ```
    Response: 204 No Content
  ```
### **Movie API**
- ### **Create new Movie**
**Endpoint:** POST [/api/movies](http://localhost:8080/api/movies)<br>
**Request Body (JSON):**
  ```
    {
  "title": "A new movie that is aweosme",
  "releaseYear": 1900,
  "duration": 69,
  "actorIds": [1,2],  
  "genreIds": [1,2]      
}
  ```
- ### **Retrieve all Movies**
- **Endpoint:** [GET /api/movies](http://localhost:8080/api/movies)<br>
- **Response (Example):**
  ```
   {
    "content": [
        {
            "id": 1,
            "title": "The Shawshank Redemption",
            "releaseYear": 1994,
            "duration": 142,
            "actorIds": [
                1,
                2
            ],
            "genreIds": [
                1
            ],
            "actors": [
                "Michelle Pfeiffer",
                "Morgan Freeman"
            ],
            "genres": [
                "Drama"
            ]
        },
        {
            "id": 2,
            "title": "The Godfather",
            "releaseYear": 1993,
            "duration": 175,
            "actorIds": [
                3,
                4
            ],
            "genreIds": [
                1,
                4
            ],
            "actors": [
                "Marlon Brando",
                "Al Pacino"
            ],
            "genres": [
                "Drama",
                "Crime"
            ],

            etc etc
        }
    ]
   }
  ```
- ### **Retrieve a Movie by ID**
- **Endpoint:** GET [/api/movies/{id}](http://localhost:8080/api/movies/1)<br>
- **Response (Example):**
  ```
    {
  "id": 1,
  "title": "Inception",
  "releaseYear": 2010,
  "duration": 148,
  "genres": [
    {
      "id": 1,
      "name": "Action"
    },
    {
      "id": 2,
      "name": "Sci-Fi"
    }
  ],
  "actors": [
    {
      "id": 1,
      "name": "Leonardo DiCaprio"
    },
    {
      "id": 3,
      "name": "Tom Hardy"
    }
  ]
    }
  ```
- ### **Retrieve Movies by Genre**
- **Endpoint:** GET [/api/movies?genre={genreId}](http://localhost:8080/api/movies?genre=4)<br>
- **Response (Example):**
  ```
    [
    {
        "id": 2,
        "title": "The Godfather",
        "releaseYear": 1993,
        "duration": 175,
        "actorIds": [
            3,
            4
        ],
        "genreIds": [
            1,
            4
        ],
        "actors": [
            "Marlon Brando",
            "Al Pacino"
        ],
        "genres": [
            "Drama",
            "Crime"
        ]
    },
    {
        "id": 3,
        "title": "The Dark Knight",
        "releaseYear": 2008,
        "duration": 152,
        "actorIds": [
            5,
            6
        ],
        "genreIds": [
            1,
            3,
            4
        ],
        "actors": [
            "Christian Bale",
            "Heath Ledger"
        ],
        "genres": [
            "Drama",
            "Thriller",
            "Crime"
        ]
    },
    etc etc
    ]

  ```
### **Actor API**
- ### **Create a new Actor**
- **Endpoint:** [POST /api/actors](http://localhost:8080/api/actors)<br>
- **Response (JSON):**
  ```
    {
    "name": "John Doe",
    "birthDate": "1992-09-10"
    }
  ```
- ### **Update an Actor**
- **Endpoint:** PATCH [/api/actors/{id}](http://localhost:8080/api/actors/1)<br>
- **Response (JSON):**
- **Partial change**
  ```
    {
     "name": "He is so cool"
    }
  ```