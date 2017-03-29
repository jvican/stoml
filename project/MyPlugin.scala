package myplugin

import sbt._
import sbt.Keys._

object MyPlugin extends AutoPlugin {
  object autoImport extends MyPluginSettings
  import autoImport._

  override def trigger: PluginTrigger = allRequirements
  override def projectSettings: Seq[Setting[_]] = Seq(
    eagerInit := {
      sys.error("I SHOULD HAVE BEEN RUN SECOND")
      // Invoking here any other task would trigger the initialization of this task.
      state.value
    }
  )
}

trait MyPluginSettings {
  val eagerInit = taskKey[Unit]("Show bug in eager init.")
}
