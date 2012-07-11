package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller


import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import com.echoed.util.BlobStore
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.Image
import com.echoed.chamber.services.image.{StartProcessImageResponse, ImageService}
import org.springframework.web.context.request.async.DeferredResult
import io.Source
import java.util.Date
import java.text.SimpleDateFormat
import java.net.URLDecoder
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream


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
        handleUpload(request, request.getHeader("X-File-Name"), request.getInputStream)

    }

    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def uploadIE(
            request: HttpServletRequest,
            @RequestParam("file") file: MultipartFile) = {
        handleUpload(request, file.getOriginalFilename, file.getInputStream)
    }

    private def handleUpload(
            request: HttpServletRequest,
            name: String,
            inputStream: InputStream) = {

        val eu = cookieManager.findEchoedUserCookie(request).get
        //we url decode because the image service will again encode :(  really the image service should not be encoding anything...
        val fileName = URLDecoder.decode(Option(name).get, "UTF-8")
        val i = new Image(fileName)
        val contentType = "image/%s" format i.ext
        val bytes = Source.fromInputStream(inputStream)(scala.io.Codec.ISO8859).map(_.toByte).toArray


        val result = new DeferredResult(ImageUploadStatus())
        val dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())

        logger.debug("Image %s of type %s being uploaded for EchoedUser %s" format(fileName, contentType, eu))
        blobStore.store(
                bytes,
                eu + "_" + dateString + "_" + fileName ,
                contentType).onSuccess {
            case url =>
                logger.debug("Successfully stored %s of type %s for EchoedUser %s" format(fileName, contentType, eu))
                imageService.startProcessImage(new Image(url)).onSuccess {
                    case StartProcessImageResponse(_, Right(image)) =>
                        logger.debug("Successfully processed {} for EchoedUser {}", fileName, eu)
                        result.set(ImageUploadStatus(true, image.preferredUrl, image.id))
            }
        }

        result
    }
}

case class ImageUploadStatus(success: Boolean = false, url: String = "", id: String = "")
