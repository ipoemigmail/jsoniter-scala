package com.github.plokhotnyuk.jsoniter_scala.benchmark

import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

import com.avsystem.commons.serialization.json._
import com.github.plokhotnyuk.jsoniter_scala.benchmark.AVSystemCodecs._
import com.github.plokhotnyuk.jsoniter_scala.benchmark.CirceEncodersDecoders._
import com.github.plokhotnyuk.jsoniter_scala.benchmark.JacksonSerDesers._
import com.github.plokhotnyuk.jsoniter_scala.benchmark.JsoniterScalaCodecs._
import com.github.plokhotnyuk.jsoniter_scala.benchmark.UPickleReaderWriters._
import com.github.plokhotnyuk.jsoniter_scala.core._
import io.circe.parser._
import io.circe.syntax._
import org.openjdk.jmh.annotations.{Benchmark, Param, Setup}
import play.api.libs.json.Json

class ArrayOfDurationsBenchmark extends CommonParams {
  @Param(Array("1", "10", "100", "1000", "10000", "100000", "1000000"))
  var size: Int = 1000
  var obj: Array[Duration] = _
  var jsonString: String = _
  var jsonBytes: Array[Byte] = _
  var preallocatedBuf: Array[Byte] = _

  @Setup
  def setup(): Unit = {
    obj = (1 to size).map { i =>
      val x = Math.abs((i * 372036854775807L) / Math.pow(10, i % 18).toLong)
      val y = Math.abs(i * Math.pow(10, i % 10)).toInt
      Duration.ofSeconds(x, y)
    }.toArray
    jsonString = obj.mkString("[\"", "\",\"", "\"]")
    jsonBytes = jsonString.getBytes(UTF_8)
    preallocatedBuf = new Array[Byte](jsonBytes.length + 100/*to avoid possible out of bounds error*/)
  }

  @Benchmark
  def readAVSystemGenCodec(): Array[Duration] = JsonStringInput.read[Array[Duration]](new String(jsonBytes, UTF_8))

  @Benchmark
  def readCirce(): Array[Duration] = decode[Array[Duration]](new String(jsonBytes, UTF_8)).fold(throw _, identity)

  @Benchmark
  def readJacksonScala(): Array[Duration] = jacksonMapper.readValue[Array[Duration]](jsonBytes)

  @Benchmark
  def readJsoniterScala(): Array[Duration] = readFromArray[Array[Duration]](jsonBytes)

  @Benchmark
  def readPlayJson(): Array[Duration] = Json.parse(jsonBytes).as[Array[Duration]]

  @Benchmark
  def readUPickle(): Array[Duration] = read[Array[Duration]](jsonBytes)

  @Benchmark
  def writeAVSystemGenCodec(): Array[Byte] = JsonStringOutput.write(obj).getBytes(UTF_8)

  @Benchmark
  def writeCirce(): Array[Byte] = printer.pretty(obj.asJson).getBytes(UTF_8)

  @Benchmark
  def writeJacksonScala(): Array[Byte] = jacksonMapper.writeValueAsBytes(obj)

  @Benchmark
  def writeJsoniterScala(): Array[Byte] = writeToArray(obj)

  @Benchmark
  def writeJsoniterScalaPrealloc(): Int = writeToSubArray(obj, preallocatedBuf, 0, preallocatedBuf.length)

  @Benchmark
  def writePlayJson(): Array[Byte] = Json.toBytes(Json.toJson(obj))

  @Benchmark
  def writeUPickle(): Array[Byte] = write(obj).getBytes(UTF_8)
}