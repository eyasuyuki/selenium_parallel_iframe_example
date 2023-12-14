Selenium parallel iframe example
====

# Important notice

This example does not run parallelized.

Because browser does not run multi-thread.

To see: https://stackoverflow.com/questions/28093347/is-selenium-webdriver-thread-safe

# Install google-chrome and chromedriver

```shell
brew install google-chrome chromedriver
```

# How to run

## Run local http server

```shell
cd src/test/__files
go run main.go
```

## Run App

```shell
mvn compile
mvn exec:java
```

