
# Audio Alchemists - Backend

This repository contains the backend code for Audio Alchemists, a collaborative online music composition platform. The backend is built using Spring Boot, Spring Security, Spring Data JPA, and WebSockets, providing a robust and scalable foundation for real-time collaborative music creation.

## Features

* **User Management:** User registration, login, logout, role-based authorization, and user profiles.
* **Project Management:** Create, edit, delete, and share musical projects.
* **Track Management:** Create, edit, and delete individual tracks within a project, each with a specific instrument and musical sequence.
* **Real-time Collaboration:** Collaborate with others on projects in real-time using WebSockets, seeing each other's changes instantly.
* **Version Control:** Track project versions and revert to previous states.
* **Commenting System:** Provide feedback and discuss ideas within a project.
* **Search and Discovery:** Search for projects based on metadata and discover new music.
* **Recommendation System:** Receive personalized project recommendations.
* **RESTful API:** Well-defined API endpoints for all functionalities.
* **Security:** Secure authentication and authorization using JWT.

## Technologies Used

* **Spring Boot:** Framework for building stand-alone, production-grade Spring-based applications.
* **Spring Security:** Framework for providing authentication and authorization.
* **Spring Data JPA:** Simplifies database access and interaction.
* **Spring WebSockets:** Enables real-time communication between clients and the server.
* **PostgreSQL:** Relational database for storing application data.
* **JWT (JSON Web Token):** Standard for secure authentication.
* **Maven/Gradle:** Build automation tool.


## Getting Started

1. **Prerequisites:**
    * Java JDK 17 or later
    * PostgreSQL installed and running
    * Maven or Gradle installed

2. **Clone the repository:**
   ```bash
   git clone https://github.com/okiyitooo/Audio-Alchemists-Backend.git
   ```

3. **Database Setup:**
    * Create a PostgreSQL database named `collaborative_music` (or configure the database name in the application properties).
    * Run the SQL script located in `src/main/resources/db/schema.sql` to create the necessary tables.

4. **Configuration:**
    * Configure the database connection properties in `src/main/resources/application.properties` (or `application.yml`).
    * Configure other necessary properties like JWT secret, server port, etc.

5. **Build and Run:**
    * Navigate to the project directory in your terminal.
    * Use Maven: `mvn spring-boot:run`
    * Use Gradle: `./gradlew bootRun`

6. **API Documentation:**
    * Access Swagger UI at `http://localhost:8080/swagger-ui/index.html` (or the configured port) after running the application.  (Make sure to include the Springdoc OpenAPI dependency).


## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is licensed under the [MIT License](LICENSE).


## Contact

Kanaetochi Okiyi - Kanaetochi.okiyi@udc.edu


## Acknowledgments

