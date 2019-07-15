# Moneygr2

Build with `pack`

```
./pack-build.sh --publish
```

Build with `cf local`

```
./mvnw clean package -DskipTests=true
cf local stage moneygr2 -p ./target/moneygr2-0.0.1-SNAPSHOT.jar
cf local export moneygr2 -r making/moneygr2
docker push making/moneygr2

export MYSQL_HOST=
export MYSQL_DATABASE=
export MYSQL_USERNAME=
export MYSQL_PASSWORD=

docker run --rm -p 8080:8080 \
  -e spring.datasource.url=jdbc:mysql://${MYSQL_HOST}:3306/${MYSQL_DATABASE}?useSSL=false \
  -e spring.datasource.username=${MYSQL_USERNAME} \
  -e spring.datasource.password=${MYSQL_PASSWORD} \
  --memory=512m \
  making/moneygr2 
```