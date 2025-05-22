FROM openjdk:17
WORKDIR /app
COPY . .

# Derleme sırasında iki jar'ı classpath'e dahil et
RUN javac -cp "postgresql-42.7.3.jar:json-20230227.jar" Main.java

# Çalıştırma sırasında da iki jar'ı classpath'e dahil et
CMD ["java", "-cp", ".:postgresql-42.7.3.jar:json-20230227.jar", "Main"]
