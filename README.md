gcloud scala
============

[![Build Status](https://travis-ci.org/bebauer/gcloud-scala.svg?branch=master)](https://travis-ci.org/bebauer/gcloud-scala) [ ![Download](https://api.bintray.com/packages/bebauer/maven/gcloud-scala-pubsub/images/download.svg) ](https://bintray.com/bebauer/maven/gcloud-scala-pubsub/_latestVersion)

**gcloud scala** provides scala libraries for google cloud applications.

# Features

- PubSub Client Library
- PubSub Testkit

# Usage

## Add Dependencies

```
// Add resolver for https://dl.bintray.com/bebauer/maven/
resolvers += Resolver.bintrayRepo("bebauer", "maven")

// Add dependencies

// Pub/Sub
"gcloud-scala" %% "gcloud-scala-pubsub" % "version"
"gcloud-scala" %% "gcloud-scala-pubsub-testkit" % "version" % Test
```

## Imports

```
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
```

# License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0