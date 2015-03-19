name := "ocrtest"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
	"org.im4java"                   % "im4java"           % "1.4.0",
	"com.typesafe.akka"             % "akka-actor_2.11"   % "2.3.9",
	"ch.qos.logback"                % "logback-classic"   % "1.1.2",
	"com.rockymadden.stringmetric" %% "stringmetric-core" % "0.27.4",
	"commons-io"                    % "commons-io"        % "2.4"
	)