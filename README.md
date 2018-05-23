# java-test
API for transferring of funds from one account to another

requires jdk8 and maven

to build with maven and run
mvn install

cd target

java -jar transfer-1.0-SNAPSHOT-jar-with-dependencies.jar

browse to http://127.0.0.1:8080

API endpoints :

POST /create example payload : {"account_id":"123456789", "balance": "1000"}

GET /list

GET /balance/{account_id}

POST /transfer example payload : {"account_from":"123456789", "account_to": "987654321", "balance_to_transfer" : "500"}

