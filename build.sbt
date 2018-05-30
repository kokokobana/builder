name := "builder"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
//  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

val scalaJsReactVersion = "1.2.0"
val monocleVersion = "1.5.1-cats"
val diodeVersion = "1.1.3.120"
val scalaCssVersion = "0.5.5"
val circeVersion = "0.10.0-M1"

libraryDependencies ++= Seq(
  "com.github.japgolly.scalajs-react" %%% "core" % scalaJsReactVersion,
  "com.github.japgolly.scalajs-react" %%% "ext-monocle-cats" % scalaJsReactVersion,
  "io.suzaku" %%% "diode" % "1.1.3",
  "io.suzaku" %%% "diode-react" % "1.1.3.120",
  "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
  "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
  "com.github.julien-truffaut" %%% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %%% "monocle-macro" % monocleVersion,
  "io.circe" %%% "circe-core" % circeVersion,
  "io.circe" %%% "circe-generic" % circeVersion,
  "io.circe" %%% "circe-parser" % circeVersion,
  "com.payalabs" %%% "scalajs-react-bridge" % "0.6.0",
  "org.typelevel" %%% "cats-core" % "1.1.0",
  "com.chuusai" %%% "shapeless" % "2.3.3",
  "org.scala-js" %%% "scalajs-dom" % "0.9.5",

  // these need to be built locally
  "org.bitbucket.wakfuthesaurus" %%% "renderer" % "0.1-SNAPSHOT",
  "org.bitbucket.wakfuthesaurus" %%% "shared" % "0.2-SNAPSHOT"
)

npmDependencies in Compile ++= Seq(
  "react" -> "16.4.0",
  "react-dom" -> "16.4.0",
  "onsenui" -> "2.10.1",
  "react-onsenui" -> "1.11.0",
  "react-infinite-scroller" -> "1.1.4",
  "react-responsive-select" -> "4.0.0"
)
npmDevDependencies in Compile ++= Seq(
  "webpack-closure-compiler" -> "2.1.6"
)
scalaJSLinkerConfig ~= {
  _.withSourceMap(false)
}

webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()

version in webpack := "4.9.1"
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.webpack.config.js")
webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js")

scalaJSUseMainModuleInitializer := true

addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)
