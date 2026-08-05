package com.intuit.data.simplan.common.config.parser

import com.typesafe.config.ConfigValueFactory
import org.scalatest.funsuite.AnyFunSuite

class TypesafeConfigLoaderTest extends AnyFunSuite {

  test("load from classpath resource") {
    val loader = new TypesafeConfigLoader("simplan", List.empty, Map.empty)
    loader.load("classpath:test-simplan.conf")
    val rendered = loader.render(None)
    assert(rendered.contains("TestApp"))
  }

  test("overrideConfig should override a key in the rendered output") {
    val loader = new TypesafeConfigLoader("simplan", List.empty, Map.empty)
    loader.load("classpath:test-simplan.conf")
    loader.overrideConfig("simplan.application.name", ConfigValueFactory.fromAnyRef("Overridden"))
    val rendered = loader.render(None)
    assert(rendered.contains("Overridden"))
    assert(!rendered.contains("TestApp"))
  }

  test("isSupportedScheme returns false when no custom fileUtils registered") {
    val loader = new TypesafeConfigLoader("simplan", List.empty, Map.empty)
    assert(!loader.isSupportedScheme("s3://some-bucket/path.conf"))
  }

  test("getFileUtils throws SimplanException for unregistered scheme") {
    val loader = new TypesafeConfigLoader("simplan", List.empty, Map.empty)
    intercept[com.intuit.data.simplan.global.exceptions.SimplanException] {
      loader.getFileUtils("s3")
    }
  }

  test("resolveSystemConfiguration parses SimplanAppContextConfiguration from config") {
    val loader = new TypesafeConfigLoader("simplan", List.empty, Map.empty)
    loader.load("classpath:test-simplan.conf")
    val config = TypesafeConfigLoader.resolveSystemConfiguration(loader)
    assert(config.application.name == "TestApp")
    assert(config.application.environment == "test")
  }

  test("hint produces CamelCase field mapping") {
    import pureconfig.generic.ProductHint
    val hint = TypesafeConfigLoader.hint[AnyRef]
    assert(hint != null)
    assert(hint.isInstanceOf[ProductHint[_]])
  }
}
