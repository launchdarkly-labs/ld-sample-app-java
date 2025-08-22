# LaunchDarkly Sample App for Java and JavaScript

## Requirements

* Java 17 or higher
* Maven 3.9 or higher
* LaunchDarkly Flags:
  - **Release: UI Enhancements**, key: `release-ui-enhancements`
  - **Release: Home Page Slider**, key: `release-home-page-slider`
  - **Coffee Promo 1**, key: `coffee-promo-1`
  - **Coffee Promo 2**, key: `coffee-promo-2`
  - **Banner Text**, key: `banner-text`

## Setup

To get started, clone this repo locally

```
git clone https://github.com/launchdarkly-labs/ld-sample-app-java.git
cd ld-sample-app-java
```

Add LaunchDarkly keys

* Rename `.env.example` to `.env`
* In the `.env` file, replace the fake keys with your LaunchDarkly SDK key and client-side key

Build the application

```
mvn package
```

Initially launch the app to create file dependencies

```
java -jar target/javaapp-1.0.0.jar
```

Stop the web server

* Press **Control+C** (**^C**)

Build the application again

```
mvn package
```


## Run

To run the site:

```
java -jar target/javaapp-1.0.0.jar
```

In your browser, navigate to:

```
http://localhost:3000
```

Enjoy!