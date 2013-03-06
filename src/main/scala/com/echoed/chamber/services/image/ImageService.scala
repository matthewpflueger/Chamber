package com.echoed.chamber.services.image

import com.echoed.chamber.services.EchoedService
import org.openqa.selenium.{OutputType, TakesScreenshot, WebDriver}
import java.io.File
import java.util.concurrent.{TimeUnit, ThreadLocalRandom}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxBinary}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.{HttpMultipartMode, MultipartEntity}
import com.echoed.util.{CloudinaryUtil, ScalaObjectMapper, UUID}
import org.apache.http.entity.mime.content.{FileBody, StringBody}
import scala.io.Source
import com.echoed.chamber.domain.Image
import scala.util.Try

class ImageService(cloudinaryUtil: CloudinaryUtil) extends EchoedService {

    def handle = {
        case msg @ Capture(link) =>
            //so we should be using url2png.com for screen capture and general website checking service
            //WebDriver cannot and will probably never be able to get status codes so unless we want to fetch
            //the page twice we will have to live with bad pages (this is something url2png.com solves for us)
            //plus the below is a ton of blocking operations which need to be better managed this:
            //See "Blocking Needs Careful Management" section of http://doc.akka.io/docs/akka/snapshot/general/actor-systems.html

            var xvfb: Process = null
            var driver: WebDriver = null
            var pageTitle: String = null
            var screenShot: File = null
            var croppedScreenShot: File = null
            //        var bytes: Array[Byte] = null

            val xfvbExec = "/usr/bin/Xvfb"
            val timeout = 20

            try {
                val display = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE - 1)
                log.debug("Starting up {} :{}", xfvbExec, display)
                xvfb = Runtime.getRuntime().exec("%s :%s" format(xfvbExec, display))

                log.debug("Starting Firefox on display {}", display)
                val firefox = new FirefoxBinary()
                firefox.setEnvironmentProperty("DISPLAY", ":" + display)
                driver = new FirefoxDriver(firefox, null)
                driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS)

                log.debug("Fetching {}", link.url)
                driver.get(link.url)
                pageTitle = driver.getTitle
                if (pageTitle == "Problem loading page") {
                    log.warning("Problem loading {}", link.url)
                    sender ! CaptureResponse(msg, Right(link.copy(pageTitle = Some(pageTitle))))
                    throw new BreakOutException("Problem loading %s" format link.url)
//                    return link.copy(pageTitle = Some(pageTitle))
                }
                //            driver.asInstanceOf[JavascriptExecutor].executeScript()

                log.debug("Taking screenshot of {}", link.url)
                screenShot = driver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
                //            bytes = driver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.BYTES)
                croppedScreenShot = File.createTempFile("cropped", ".png")
                croppedScreenShot.deleteOnExit()

                val cmd = "convert %s -crop 1920x1080+0+0 %s" format(
                    screenShot.getAbsolutePath,
                    croppedScreenShot.getAbsolutePath)
                log.debug("Cropping screenshot of {} using command {}", link.url, cmd)

                import scala.sys.process._
                val stderr = new StringBuilder()
                val exitCode = cmd.!(ProcessLogger(
                    line => stdout.append(line).append("\n"),
                    line => stderr.append(line).append("\n")))
                if (exitCode > 0) {
                    log.warning("Could not crop screenshot of {}, exit code of command was {}, error output {}", link.url, exitCode, stderr)
                    sender ! CaptureResponse(msg, Right(link.copy(pageTitle = Some(pageTitle))))
                    throw new BreakOutException("""
                        Could not crop screenshot of %s,
                        exit code of command was %s,
                        error output %s""" format(link.url, exitCode, stderr))
//                    return link.copy(pageTitle = Some(pageTitle))
                }


                log.debug("Uploading to Cloudinary cropped screenshot of {}", link.url)
                val client = new DefaultHttpClient()
                val post = new HttpPost(cloudinaryUtil.endpoint)
                val multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
                val params = Map(
                    "timestamp" -> System.currentTimeMillis().toString,
                    "public_id" -> UUID(),
                    "tags" -> "%s,%s".format(link.echoedUserId, link.storyId))
                val signature = cloudinaryUtil.sign(params)
                (params ++ Map(
                    "api_key" -> cloudinaryUtil.apiKey,
                    "signature" -> signature)).foreach { case (key, value) => multipart.addPart(key, new StringBody(value)) }
                //        multipart.addPart("file", new ByteArrayBody(bytes, "image/png", link.url))
                multipart.addPart("file", new FileBody(croppedScreenShot, "image/png", link.url))
                post.setEntity(multipart)
                val resp = client.execute(post)
                log.debug("Upload to Cloudinary of cropped screenshot of {} is {}", link.url, resp.getStatusLine.getStatusCode)
                val json = Source.fromInputStream(resp.getEntity.getContent, "US-ASCII").mkString
                log.debug("Response to upload to Cloudinary of screenshot of {} is {}", link.url, json)
                val map = ScalaObjectMapper(json, classOf[Map[String, Any]])
                val image = Image(
                    map("public_id").asInstanceOf[String],
                    map("url").asInstanceOf[String],
                    map("width").asInstanceOf[Int],
                    map("height").asInstanceOf[Int],
                    cloudinaryUtil.name)
//                ep(StoryImageCreated(image))
//                link.copy(pageTitle = Some(pageTitle), imageId = Some(image.id), image = image)
                sender ! CaptureResponse(msg, Right(link.copy(
                    pageTitle = Some(pageTitle),
                    imageId = Some(image.id),
                    image = image)))
            } catch {
                case e: BreakOutException => log.warning(e.message)
            } finally {
                Try(driver.quit())
                Try(xvfb.destroy())
                Try(screenShot.delete())
                Try(croppedScreenShot.delete())
            }
    }
}

case class BreakOutException(message: String) extends Exception(message)
