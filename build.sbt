resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val sv = "2.11.6"

lazy val root = (project in file(".")).
  settings (
    name := "Migration Tests for Obey",
    scalaVersion := sv,
    ObeyPlugin.obeyRules := "+ {AkkaMigration}",
    libraryDependencies += "org.scala-lang" % "scala-actors" % "2.11.6",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11",
    scalacOptions ++= Seq("-deprecation")
  ) dependsOn(rules) enablePlugins(ObeyPlugin)
