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
  scalaVersion := "2.12.4",
  scalacOptions ++= defaultScalacOptions,
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1",
    "org.scalaz" %% "scalaz-core" % "7.2.16",
    "org.scalatest" %% "scalatest" % "3.0.3" % "test",
    "org.mockito" % "mockito-core" % "2.12.0" % "test"
  )
)

val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ),
  PB.protoSources in Compile += file("proto"),
  libraryDependencies ++= Seq(
    "com.google.protobuf" % "protobuf-java" % com.trueaccord.scalapb.compiler.Version.protobufVersion % "protobuf",
    "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-services" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
  )
)

lazy val domain = (project in file("modules/domain"))
  .settings(commonSettings)
  .settings(protoSettings)

lazy val app = (project in file("modules/app"))
  .settings(commonSettings)
  .dependsOn(domain)
  .enablePlugins(JavaServerAppPackaging)
