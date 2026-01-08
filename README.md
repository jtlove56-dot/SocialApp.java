# SocialApp.java
Java Swing social media app with login, posts, likes, dark mode, and persistent user sessions.

# Loop — Java GUI Social Media App

Loop is a desktop-based social media application built in Java using Swing.  
It simulates core social media features such as user accounts, posting updates, liking posts, and persistent sessions.

## Features
- User registration & secure login
- Persistent user data and posts (serialization)
- Multi-user social feed
- Posts sorted newest → oldest
- Like button on posts
- Dark mode toggle (persists across restarts)
- Auto-login for last logged-in user
- Profile page with sign-out

## Technologies Used
- Java 20
- Java Swing (GUI)
- Object Serialization
- MVC-style organization
- IntelliJ IDEA

## How to Run
1. Clone the repository
2. Open the project in IntelliJ IDEA
3. Run `SocialApp.java`

## Notes
- User data is stored locally using `.ser` files
- These files are ignored by GitHub to prevent test data upload

## Future Improvements
- Password hashing
- Profile editing
- Post deletion
- Comments
- Database backend

## Author
JT Love
