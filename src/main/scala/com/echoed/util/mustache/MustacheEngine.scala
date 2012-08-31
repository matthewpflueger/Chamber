package com.echoed.util.mustache

import com.github.mustachejava.{MustacheException, DefaultMustacheFactory}
import scala.reflect.BeanProperty
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader
import java.io.{StringReader, StringWriter, InputStreamReader, Reader}
import java.util.{Map => JMap}
import org.slf4j.LoggerFactory

class MustacheEngine(
            @BeanProperty var prefix: String = "",
            @BeanProperty var suffix: String = "",
            @BeanProperty var cache: Boolean = true,
            var resourceLoader: ResourceLoader = null)
        extends DefaultMustacheFactory
        with ResourceLoaderAware {

    private final val logger = LoggerFactory.getLogger(classOf[MustacheEngine])

    def this() = this("", "", true, null)

    setObjectHandler(new EchoedObjectHandler())

    override def getReader(resourceName: String): Reader = {
        var rn = resourceName
        rn = if (!rn.startsWith(prefix)) prefix + rn else rn
        rn = if (!rn.endsWith(suffix)) rn + suffix else rn

        val resource = resourceLoader.getResource(rn)
        if (!resource.exists) throw new MustacheException("No template exists named: " + rn)
        logger.debug("Found template {}", rn)
        new InputStreamReader(resource.getInputStream)
    }

    override def compile(templateName: String) = {
        if (cache) super.compile(templateName)
        else super.compile(getReader(templateName), templateName)
    }

    def execute(templateName: String, model: JMap[String, AnyRef]) = {
        val writer = new StringWriter()
        compile(templateName).execute(writer, model)
        writer.toString
    }

    def setResourceLoader(loader: ResourceLoader) {
        resourceLoader = loader
    }
}

object MustacheEngineTest extends App {

    case class TestClassWithOption(hello: String, world: Option[String] = None)

    val m = new java.util.HashMap[String, Object]()
    m.put("test", new TestClassWithOption("HELLO", Some("WORLD")))

//    val t = new DefaultMustacheFactory().compile(new StringReader("{{#test}}{{hello}}{{world}}{{/test}}!"), "test")
    val f = new DefaultMustacheFactory()
    f.setObjectHandler(new EchoedObjectHandler)
//    val t = f.compile(new StringReader("Test: {{test.hello}}{{test.world}}!"), "example")
    val t = f.compile(new StringReader("Test: {{#test}}{{hello}} {{world}}{{/test}}!"), "example")
    val w = new StringWriter(1024)
//    t.execute(w, Map("test" -> new TestClassWithOption("HELLO")))
    t.execute(w, m)
    w.flush()
    println(w.toString)
}