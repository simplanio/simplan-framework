package com.intuit.data.simplan.global.domain

import com.intuit.data.simplan.global.qualifiedstring.QualifiedParameterManager

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 06-Jan-2022 at 10:21 AM
  */
class QualifiedParam(val qualifiedString: String) extends Serializable {

  def this(qualifier: String, string: String) = this(s"""$qualifier($string)""")

  private val pattern = """^(\w*)\((.*)\)""".r

  val (qualifier: String, string: String, referenceQs: Option[QualifiedParam]) = pattern.findFirstMatchIn(qualifiedString) match {
    case Some(value) => {
      val q = value.group(1)
      val v = value.group(2)
      val qs = pattern.findFirstMatchIn(v) match {
        case Some(_) => Some(new QualifiedParam(v))
        case None    => None
      }
      (q, v, qs)
    }
    case None => ("raw", qualifiedString, None)
  }

  def resolve: String = resolveAs[String]

  def resolveAs[T]: T = QualifiedParameterManager.resolve[T](this)
}
