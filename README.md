
## Spring Security Implementation Details

This project delves deep into Spring Security, demonstrating a step-by-step approach to building a secure web application. It covers foundational security concepts, advanced customisations, database integration for user management, robust password security, stateless authentication using JSON Web Tokens (JWTs), and fine-tuning the security filter chain.

### Core Spring Security Concepts & Customisation

*   **Default Behaviour**: Initially, Spring Security provides a **default login form** with a generated username (`user`) and a password printed in the console. Requests are processed through a **filter chain** before reaching controllers, and Spring Security injects its own filters into this chain to manage security.
*   **Session Management**: Upon successful login, a **session ID** is generated and stored in a cookie. This session ID allows the server to maintain user sessions across multiple requests, preventing the need to log in for each subsequent request. Session IDs can be inspected in browser developer tools and change upon new logins or logouts. The `HttpServletRequest.getSession().getId()` method can be used to programmatically retrieve the session ID.
*   **Custom Credentials**: Beyond the default, **custom usernames and passwords** can be configured directly in `application.properties` using `spring.security.user.name` and `spring.security.user.password`.
*   **REST Client Access**: The application supports login and access to secured endpoints via **REST clients** (e.g., Postman) using **HTTP Basic authentication**. Initially, requests without credentials receive a `401 Unauthorized` status, which changes to `200 OK` upon providing correct basic authentication.
*   **Custom Configuration (`SecurityConfig`)**: For advanced customisation, a dedicated **`SecurityConfig` class** is created, annotated with `@Configuration` and `@EnableWebSecurity`. This class defines a `SecurityFilterChain` bean, providing a powerful **builder pattern** (`http.csrf().disable().authorizeHttpRequests()...`) to chain multiple security configurations for readability and functionality.
    *   It allows **disabling CSRF** (`http.csrf(Customizer.disable())`).
    *   It mandates authentication for **all HTTP requests** by default (`request.anyRequest().authenticated()`).
    *   It enables **form-based login** for web browsers (`http.formLogin(Customizer.withDefaults())`).
    *   It enables **HTTP Basic authentication** for REST API access (`http.httpBasic(Customizer.withDefaults())`).
    *   It supports configuring **stateless sessions** (`http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`), which means credentials must be sent with every request, and a new session ID is generated each time.

### Authentication and Authorisation Enhancements

*   **CSRF (Cross-Site Request Forgery) Protection**:
    *   Spring Security **by default protects against CSRF attacks** by requiring a unique **CSRF token** for state-changing HTTP methods (POST, PUT, DELETE).
    *   An endpoint (`/csrf-token`) was implemented to **retrieve the CSRF token** programmatically using `HttpServletRequest.getAttribute("_csrf")`.
    *   The client must include this token in requests via the `X-CSRF-TOKEN` header.
    *   CSRF protection can be explicitly **disabled** when the application is designed to be **stateless**.

### User Management and Password Security

*   **Database User Verification**: User credentials are no longer hardcoded but are retrieved from a **PostgreSQL database**.
    *   A custom **`UserDetailsService`** implementation (`MyUserDetailsService`) is responsible for fetching user details from the database.
    *   A **JPA Repository** (`UserRepo`) is used to interface with the database for data retrieval (e.g., `findByUsername`).
    *   The **`DaoAuthenticationProvider`** is configured to use this custom `UserDetailsService`.
    *   A **`UserPrincipal` class** was created to implement Spring Security's `UserDetails` interface, encapsulating user information and account status from the `Users` database entity.
    *   **Database configuration** details (URL, username, password) for PostgreSQL are specified in `application.properties`.
*   **Bcrypt Password Encryption**:
    *   **Security Principle**: Passwords are never stored in plain text. Instead, **hashing** is used, converting plain-text passwords into one-way **hash values**.
    *   **Algorithm**: **Bcrypt** is employed as the hashing algorithm. Bcrypt includes "rounds" (iterations), such as 10 or 12, to significantly increase the computational cost of brute-force attacks.
    *   **Implementation**: A `BCryptPasswordEncoder` is used to **encode** passwords before saving them to the database during user registration via the `UserService`. During login, the `DaoAuthenticationProvider` uses `BCryptPasswordEncoder` to verify the provided password against the stored hash, ensuring plain-text passwords are never exposed or compared.

### Stateless Authentication with JWT (JSON Web Tokens)

*   **Addressing Session Limitations**: JWTs were adopted to overcome the limitations of session-based authentication in distributed environments by enabling **stateless authentication**, where no server-side session needs to be maintained.
*   **JWT Structure**: A JWT is a compact, URL-safe representation of **claims** (user data, issuance, and expiration dates) transferred between parties. It consists of a **Header**, **Payload** (containing claims), and a **Signature**. The signature ensures the token's integrity and authenticity.
*   **Token Generation**:
    *   **Dependencies**: The JJWT API, Implementation, and Jackson converter libraries were integrated.
    *   An **`AuthenticationManager` bean** was configured to explicitly handle the authentication process.
    *   A dedicated `/login` endpoint handles user credentials. Upon successful authentication, a **`JWTService`** generates the JWT by building claims (e.g., username as subject, issuance and expiration times) and **signing** the token with a securely generated secret key (e.g., using `HmacSHA256`).
    *   The `/login` and `/register` endpoints were configured to `permit All` to allow access without prior authentication.
*   **Token Validation**:
    *   A custom **`JwtFilter`** (extending `OncePerRequestFilter`) is integrated into the Spring Security filter chain, positioned **before** the `UsernamePasswordAuthenticationFilter`.
    *   This filter extracts the JWT from the `Authorization` header (`Bearer <token>`) of incoming requests.
    *   The `JWTService` then **validates the token** by verifying its signature, checking its expiration, and extracting the user's details (e.g., username, claims).
    *   If the token is valid and the user is not already authenticated, a `UsernamePasswordAuthenticationToken` is created from the `UserDetails` fetched via `MyUserDetailsService` and set in the `SecurityContextHolder`, allowing the request to proceed to the intended resource without requiring further credential checks.

---