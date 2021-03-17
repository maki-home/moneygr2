# Moneygr2

Build with `pack`

```
./mvnw clean package -DskipTests=true
pack build ghcr.io/maki-home/moneygr -p target/moneygr2-0.0.1-SNAPSHOT.jar --builder paketobuildpacks/builder:base

export MYSQL_HOST=
export MYSQL_DATABASE=
export MYSQL_USERNAME=
export MYSQL_PASSWORD=

docker run --rm -p 8080:8080 \
  -e spring.datasource.url=jdbc:mysql://${MYSQL_HOST}:3306/${MYSQL_DATABASE}?useSSL=false \
  -e spring.datasource.username=${MYSQL_USERNAME} \
  -e spring.datasource.password=${MYSQL_PASSWORD} \
  --memory=512m \
  ghcr.io/maki-home/moneygr 
```