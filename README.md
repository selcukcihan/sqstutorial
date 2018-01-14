## AWS SQS Sample Code
This is an example java application to demonstrate [aws sqs](https://aws.amazon.com/sqs/) usage. It is fully functional and close to production quality. This is a command-line application, with the ability to create sqs queues and send/receive messages on them.

## How to run this?
In order to run this application, you need to [build it first](https://spring.io/guides/gs/gradle/). Tested on Gradle 4.4.1, you can build and run the application as follows:
1. `gradle wrapper --gradle-version 2.13`
2. `./gradlew build`
3. `./gradlew --console=plain -q run`

## How to setup aws?
You need to have an [aws account](https://aws.amazon.com). Then you need to follow [these instructions](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html).
