addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.13")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

resolvers += Resolver.sbtPluginRepo("releases")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.7"
