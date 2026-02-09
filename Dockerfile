# Use the official Gradle image with JDK 17
FROM gradle:8.11-jdk17 AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy the entire project to the working directory
COPY . .

# Run the investigation task
# This will build the necessary dependencies and execute the main class
CMD ["gradle", ":investigation:runInvestigation"]
