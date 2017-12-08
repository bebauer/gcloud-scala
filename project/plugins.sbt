addSbtPlugin("io.get-coursier"   % "sbt-coursier" % "1.0.0-RC13")
addSbtPlugin("com.dwijnand"      % "sbt-dynver"   % "2.0.0")
addSbtPlugin("com.thesamet"      % "sbt-protoc"   % "0.99.12")
addSbtPlugin("org.foundweekends" % "sbt-bintray"  % "0.5.1")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.6"
