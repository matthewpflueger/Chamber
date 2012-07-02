package com.echoed.chamber.services.image

import com.echoed.chamber.domain.Image
import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import java.awt.image.BufferedImage


sealed trait ImageMessage extends Message

sealed case class ImageException(image: Image, message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.image.{ImageMessage => IM}
import com.echoed.chamber.services.image.{ImageException => IE}


case class StartProcessImage(image: Image) extends IM
case class StartProcessImageResponse(
        message: StartProcessImage,
        value: Either[IE, Image]) extends IM with RM[Image, StartProcessImage, IE]

case class ProcessImage(image: Image) extends IM
case class ProcessImageResponse(
        message: ProcessImage,
        value: Either[IE, Image]) extends IM with RM[Image, ProcessImage, IE]

case class ProcessImageException(
        _image: Image,
        _message: String = "Error processing image",
        _cause: Throwable = null) extends IE(_image, _message, _cause)

case class InvalidImageFormat(
        _image: Image,
        _message: String,
        _cause: Throwable = null) extends IE(_image, _message, _cause)

object InvalidImageFormat {
    def apply(image: Image, format: String) = new InvalidImageFormat(image, "Invalid image format %s" format(format))
}

case class InvalidImageWidth(
        _image: Image,
        _message: String,
        _cause: Throwable = null) extends IE(_image, _message, _cause)

object InvalidImageWidth {
    def apply(image: Image, width: Int) = new InvalidImageWidth(image, "Invalid image width %s" format width)
}

case class ImageNotFound(
        _image: Image,
        _message: String = "Image not found",
        _cause: Throwable = null) extends IE(_image, _message, _cause)


case class ProcessLowPriorityImage(image: Image) extends IM

case class ProcessLowPriorityImageResponse(
        message: ProcessLowPriorityImage,
        value: Either[IE, Image]) extends IM with RM[Image, ProcessLowPriorityImage, IE]


private[image] case class ProcessOriginalImage(image: Image) extends IM
private[image] case class ProcessSizedImage(image: Image) extends IM
private[image] case class ProcessThumbnailImage(image: Image) extends IM

private[image] case class FindUnprocessedImage() extends IM


private[image] case class ImageStoreError(image: Image, error: Throwable, message: Option[String] = None) extends IM

private[image] case class ReloadBlobStore(image: Image, imageInfo: ImageInfo, success: String => Unit) extends IM

private[image] case class ImageInfo(
            bytes: Array[Byte],
            bufferedImage: BufferedImage,
            fileName: String,
            width: Int,
            height: Int,
            contentType: String,
            metadata: Option[Map[String, String]],
            md5: String,
            ext: String)
