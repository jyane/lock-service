addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.12")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

// workarround for https://github.com/sbt/sbt/issues/3432
resolvers += Resolver.sbtPluginRepo("releases")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.6"
