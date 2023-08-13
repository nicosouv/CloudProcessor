import sbt.Keys.*
import sbt.*
import sbt.plugins.JvmPlugin

object Compiler extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = JvmPlugin

  val scala213 = "2.13.10"

  override def projectSettings = Seq(
    scalaVersion := scala213,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-unchecked",
      "-deprecation",
      "-feature",
      // "-language:higherKinds",
      "-Xlint",
      "-g:vars",
      "-Werror",
      "-Wnumeric-widen",
      "-Wdead-code",
      "-Wvalue-discard",
      "-Wunused",
      "-Wmacros:after",
      "-Woctal-literal",
      "-Wextra-implicit",
      "-Xlint:-byname-implicit"
    ),
    Test / scalacOptions ~= {
      _.filterNot(_ == "-Werror")
    },
    Compile / console / scalacOptions ~= {
      _.filterNot { opt =>
        opt.startsWith("-P") || opt.startsWith("-X") || opt.startsWith("-W")
      }
    },
    Test / console / scalacOptions ~= {
      _.filterNot { opt =>
        opt.startsWith("-P") || opt.startsWith("-X") || opt.startsWith("-W")
      }
    }
  )
}
