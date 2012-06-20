package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller


import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import com.echoed.util.BlobStore
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.Image
import com.echoed.chamber.services.image.{ProcessImageResponse, ImageService}
import org.springframework.web.context.request.async.DeferredResult
import io.Source
import java.util.Date
import java.text.SimpleDateFormat


@Controller
@RequestMapping(Array("/image"))
class ImageUploadController {

    private final val logger = LoggerFactory.getLogger(classOf[ImageUploadController])

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var blobStore: BlobStore = _
    @BeanProperty var imageService: ImageService = _

    @RequestMapping(
            method = Array(RequestMethod.POST),
            headers = Array("content-type=application/octet-stream"))
    @ResponseBody
    def upload(request: HttpServletRequest) = {
        val eu = cookieManager.findEchoedUserCookie(request).get
        val fileName = Option(request.getHeader("X-File-Name")).get
        val i = new Image(fileName)
        val contentType = "image/%s" format i.ext
        val bytes = Source.fromInputStream(request.getInputStream)(scala.io.Codec.ISO8859).map(_.toByte).toArray


        val result = new DeferredResult(ImageUploadStatus())
        val dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())

        logger.debug("Image %s of type %s being uploaded for EchoedUser %s" format(fileName, contentType, eu))
        blobStore.store(
                bytes,
                eu + "_" + dateString + "_" + fileName ,
                contentType).onSuccess {
            case url =>
                logger.debug("Successfully stored %s of type %s for EchoedUser %s" format(fileName, contentType, eu))
                imageService.processImage(new Image(url)).onSuccess {
                    case ProcessImageResponse(_, Right(image)) =>
                        logger.debug("Successfully processed {} for EchoedUser {}", fileName, eu)
                        result.set(ImageUploadStatus(true, image.sizedUrl, image.id))
            }
        }

        result
    }
    /*
    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def upload(
            //@RequestPart("meta-data") metaData: MetaData,
            @RequestPart("file-data") file: MultipartFile,
            request: HttpServletRequest) = {
        val eu = cookieManager.findEchoedUserCookie(request).get
        val fileName = file.getOriginalFilename
        val contentType = file.getContentType

        val result = new DeferredResult("error")

        logger.debug("Image %s of type %s being uploaded for EchoedUser %s" format(fileName, contentType, eu))
        blobStore.store(
                file.getBytes,
                eu + fileName,
                file.getContentType).onSuccess {
            case url =>
                logger.debug("Successfully stored %s of type %s for EchoedUser %s" format(fileName, contentType, eu))
                imageService.processImage(new Image(url)).onSuccess {
                    case ProcessImageResponse(_, Right(image)) =>
                        logger.debug("Successfully processed {} for EchoedUser {}", fileName, eu)
                        result.set(image.sizedUrl)
            }
        }

        result
    } */

}

case class ImageUploadStatus(success: Boolean = false, url: String = "", id: String = "")
