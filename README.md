# Amazon CodeGuru Profiler Sample Application

This package contains a simple Sales System application that creates and lists the orders created by the customers.

## Before you begin
### Create Profiling Group

* Go to the CodeGuru Profiler [console](https://console.aws.amazon.com/codeguru/profiler/)
* Click “Create Profiling Group”
* In the text box, provide a name for your profiling group
* Click “Create a profiling group”
* Follow the instructions on the next page to add permissions for the roles/users being used by the Agent

## Create an EC2 instance to run this application on AWS
* [Create](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/launching-instance.html) an EC2 instance
* [Connect](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AccessingInstances.html) to your instance

### Prerequisites

* Install Java using the instructions [here](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/setup-install.html#java-dg-java-env)
* Install Maven using the instructions [here](http://maven.apache.org/)
* Install git using the instructions [here](https://git-scm.com/downloads)

## Integrate the application with the CodeGuru Profiler

* git clone <https://github.com/aws-samples/aws-codeguru-profiler-sample-application.git>
* Navigate to the root directory `cd aws-codeguru-profiler-sample-application`
* Change the line number 36 (shown below) on file `src/main/java/com/company/sample/application/SalesSystem.java` to submit profiling data the profiling group just created.
    ```java
    Profiler.builder().profilingGroupName("<Insert the profiling group name here>")
    ```
* Build the package using `mvn package`
* Run the application
  * If using an EC2 instance profile role
    ```bash
    mvn exec:java -Dexec.mainClass=com.company.sample.application.SalesSystem
    ```
  * If not using an EC2 instance profile role, pass the credentials as environment variables before running the application
    ```bash
    export AWS_ACCESS_KEY_ID=<AccessKeyId>
    export AWS_SECRET_ACCESS_KEY=<SecretAccessKey>

    mvn exec:java -Dexec.mainClass=com.company.sample.application.SalesSystem
    ```
* A few seconds after the program is started you should see the following message on the standard output:
    ```
    INFO: Profiling scheduled, sampling rate is PT1S
    ```
* After 5 to 10 minutes you should see the following message denoting a successful report of profiling data.
    ```
    INFO: Successfully reported profile
    ```
* Go to the Amazon CodeGuru Profiler [Console](https://console.aws.amazon.com/codeguru/profiler/) and click your profiling group in order to see the profiling data.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.
