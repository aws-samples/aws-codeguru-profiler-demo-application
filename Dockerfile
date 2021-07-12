# Dockerfile
FROM 338918620411.dkr.ecr.eu-west-1.amazonaws.com/openjdk8:latest
ARG ISSUE_FLAG
ARG IAM_ROLE
ARG SQS_URL
ARG S3_BUCKET
ENV ISSUE_FLAG_ENV=${ISSUE_FLAG}
ENV IAM_ROLE_ENV=${IAM_ROLE}
ENV SQS_URL_ENV=${SQS_URL}
ENV S3_BUCKET_ENV=${S3_BUCKET}
ENV DEMO_HOME=/usr/demo
COPY DemoApplication-1.0-jar-with-dependencies.jar $DEMO_HOME/
WORKDIR $DEMO_HOME
CMD java -jar DemoApplication-1.0-jar-with-dependencies.jar ${ISSUE_FLAG_ENV} ${IAM_ROLE_ENV} ${SQS_URL_ENV} ${S3_BUCKET_ENV}