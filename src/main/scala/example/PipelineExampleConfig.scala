package example

import scala.util.Try

import com.typesafe.config.Config

import org.apache.beam.sdk.options.{
  Description,
  PipelineOptionsFactory,
  ValueProvider
}
import org.apache.beam.sdk.options.Validation.Required

import com.spotify.scio.options.ScioOptions

case class PipelineExampleConfig(
    inputPath: String,
    outputTable: String) {

  override def toString =
    s"PipelineExampleConfig { inputPath: $inputPath,outputTable: $outputTable}"
}

object PipelineExampleConfig {

  def apply(config: Config, cmdlineArgs: Array[String]): Try[PipelineExampleConfig] =
    for {
      inputPath <- Try(config getString "example.inputPath")
      outputTable <- Try(config getString "example.outputTable")
    } yield {

      PipelineOptionsFactory.register(classOf[PipelineExampleOptions])

      Try {
        PipelineOptionsFactory
          .fromArgs(cmdlineArgs: _*)
          .withValidation
          .as(classOf[PipelineExampleOptions])
      }.map(options =>
        PipelineExampleConfig(
          options.getInputPath.get,
          options.getOutputTable.get
        )
      ).getOrElse(PipelineExampleConfig(inputPath, outputTable))
    }
}

trait PipelineExampleOptions extends ScioOptions {

  @Description("The input path to read the file")
  @Required
  def getInputPath: ValueProvider[String]

  def setInputPath(value: ValueProvider[String]): Unit

  @Description("The output table to save result")
  @Required
  def getOutputTable: ValueProvider[String]

  def setOutputTable(value: ValueProvider[String]): Unit
}
