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
  scalacOptions ++= defaultScalacOptions
)

val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ),
  PB.protoSources in Compile += file("proto"),
  libraryDependencies ++= Seq(
    "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
  )
)

lazy val domain = (project in file("modules/domain"))
  .settings(commonSettings)
  .settings(protoSettings)

lazy val app = (project in file("modules/app"))
  .settings(commonSettings)
  .settings(protoSettings)
  .dependsOn(domain)
  .enablePlugins(JavaServerAppPackaging)
