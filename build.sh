#!bin/bash
mvn clean package
cp target/DemoApplication-1.0-jar-with-dependencies.jar . 
aws s3 cp target/DemoApplication-1.0-jar-with-dependencies.jar s3://338918620411-account-bucket/DemoApplication-1.0-jar-with-dependencies.jar