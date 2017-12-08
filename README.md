gcloud scala
============

[![Build Status](https://travis-ci.org/bebauer/gcloud-scala.svg?branch=master)](https://travis-ci.org/bebauer/gcloud-scala)

**gcloud scala** provides scala libraries for google cloud applications.

# Features

- PubSub Client Library
- PubSub Testkit

# Usage

```
// Add resolver for https://dl.bintray.com/bebauer/maven/
resolvers += Resolver.bintrayRepo("bebauer", "maven")

// Add dependencies

// Pub/Sub
"gcloud-scala" %% "gcloud-scala-pubsub" % "0.1.2"
"gcloud-scala" %% "gcloud-scala-pubsub-testkit" % "0.1.2" % Test
```

# License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0