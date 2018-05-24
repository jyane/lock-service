val defaultScalacOptions = Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-language:implicitConversions",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard"
)

val commonSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= defaultScalacOptions,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "org.scalaz" %% "scalaz-core" % "7.2.23",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.mockito" % "mockito-core" % "2.18.3" % "test"
  )
)

val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ),
  PB.protoSources in Compile := Seq(file("proto")),
  libraryDependencies ++= Seq(
    "com.google.protobuf" % "protobuf-java" % scalapb.compiler.Version.protobufVersion % "protobuf",
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-services" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )
)

lazy val domain = (project in file("modules/domain"))
  .settings(commonSettings)
  .settings(protoSettings)

lazy val app = (project in file("modules/app"))
  .settings(commonSettings)
  .dependsOn(domain)
  .enablePlugins(JavaServerAppPackaging)
