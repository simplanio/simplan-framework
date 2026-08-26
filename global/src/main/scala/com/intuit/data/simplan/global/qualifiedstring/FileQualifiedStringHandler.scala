package com.intuit.data.simplan.global.qualifiedstring

import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

import com.intuit.data.simplan.global.domain.QualifiedParam
import com.intuit.data.simplan.global.exceptions.QualifiedParamResolutionException

/** Resolves qualified parameters of the form `file(path)`, reading the file at `path` as a UTF-8 string. */
class FileQualifiedStringHandler extends QualifiedStringHandler {
  override val qualifier: String = "file"

  override def resolve(qualifiedString: QualifiedParam): String = {
    val path = Paths.get(qualifiedString.string.trim)
    try {
      new String(Files.readAllBytes(path), UTF_8).trim
    } catch {
      case ex: IOException =>
        throw new QualifiedParamResolutionException(
          s"""Unable to read file for qualifier "file": path=${qualifiedString.string}""",
          ex)
    }
  }
}
