import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val lwjglPlugin = "com.github.philcali" % "sbt-lwjgl-plugin" % "2.0.4"
}

