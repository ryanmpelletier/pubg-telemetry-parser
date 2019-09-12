# pubg-telemetry-parser

This project is designed to be used in conjunction with my pubg_ml repository. 
The goal of this project is to generate training data from PUBG telemetry JSON files which can train a TensorFlow model.
The goal is to predict which players will live and which will die from PUBG matches.

This also is run as a server to parse telemetry for arbitrary games and ask the docker TensorFlow server for predictions.

## configuration
Modify src/main/resources/application.yml to configure before building. By default training data will not be built.

## building and running
```
gradle clean build
java -jar build/libs/pubg-telemetry-parser-0.0.1-SNAPSHOT.jar
```
