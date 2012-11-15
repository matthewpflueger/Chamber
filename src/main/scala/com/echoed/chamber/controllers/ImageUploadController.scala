package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import com.echoed.util.BlobStore
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.domain.Image
import com.echoed.chamber.services.image.ProcessImage
import org.springframework.web.context.request.async.DeferredResult
import io.Source
import java.util.Date
import java.text.SimpleDateFormat
import java.net.URLDecoder
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import org.springframework.beans.factory.annotation.Autowired


@Controller
@RequestMapping(Array("/image"))
class ImageUploadController extends EchoedController {

    @Autowired var blobStore: BlobStore = _

    @RequestMapping(
            method = Array(RequestMethod.POST),
            headers = Array("content-type=application/octet-stream"))
    @ResponseBody
    def upload(
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest) = {
        handleUpload(eucc, request, request.getHeader("X-File-Name"), request.getInputStream)
    }

    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def uploadIE(
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest,
            @RequestParam("qqfile") file: MultipartFile) = {
        handleUpload(eucc, request, file.getOriginalFilename, file.getInputStream)
    }

    private def handleUpload(
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest,
            name: String,
            inputStream: InputStream) = {

        //we url decode because the image service will again encode :(  really the image service should not be encoding anything...
        val fileName = URLDecoder.decode(Option(name).get, "UTF-8")
        val i = new Image(fileName)
        val contentType = "image/%s" format i.ext
        val bytes = Source.fromInputStream(inputStream)(scala.io.Codec.ISO8859).map(_.toByte).toArray


        val result = new DeferredResult(ImageUploadStatus())
        val dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())

        log.debug("Image %s of type %s being uploaded for %s" format(fileName, contentType, eucc))
        blobStore.store(
                bytes,
                eucc.id + "_" + dateString + "_" + fileName ,
                contentType).onSuccess {
            case url =>
                log.debug("Successfully stored %s of type %s for %s" format(fileName, contentType, eucc))
                val image = new Image(url)
                mp(ProcessImage(Left(image)))
                result.set(ImageUploadStatus(true, image.preferredUrl, image.id))
        }

        result
    }
}

case class ImageUploadStatus(success: Boolean = false, url: String = "", id: String = "")
