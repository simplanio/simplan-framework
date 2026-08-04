package com.intuit.data.simplan.core.domain

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

class TableTypeTest extends AnyFunSuite {

  test("TableType should have TEMP, MANAGED and NONE values") {
    assert(TableType.TEMP != null)
    assert(TableType.MANAGED != null)
    assert(TableType.NONE != null)
  }

  test("TableType values should be retrievable by name") {
    assert(TableType.valueOf("TEMP") == TableType.TEMP)
    assert(TableType.valueOf("MANAGED") == TableType.MANAGED)
    assert(TableType.valueOf("NONE") == TableType.NONE)
  }

  test("TableType should implement java.io.Serializable") {
    assert(classOf[java.io.Serializable].isAssignableFrom(classOf[TableType]))
  }

  test("TableType enum values should be serializable and deserializable") {
    val original = TableType.MANAGED
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(original)
    oos.close()

    val ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    val deserialized = ois.readObject().asInstanceOf[TableType]
    assert(deserialized == original)
  }

  test("TableType ordinals should be stable") {
    assert(TableType.TEMP.ordinal() == 0)
    assert(TableType.MANAGED.ordinal() == 1)
    assert(TableType.NONE.ordinal() == 2)
  }
}
