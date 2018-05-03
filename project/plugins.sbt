addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

resolvers += Resolver.sbtPluginRepo("releases")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.7.4"
