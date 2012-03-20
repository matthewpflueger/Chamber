package com.echoed.chamber.services.image

import com.echoed.chamber.domain.Image
import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import akka.actor.Channel


sealed trait ImageMessage extends Message

sealed case class ImageException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.image.{ImageMessage => IM}
import com.echoed.chamber.services.image.{ImageException => IE}


case class GrabImage(image: Image) extends IM

case class GrabImageResponse(
        message: GrabImage,
        value: Either[IE, Image]) extends IM with RM[Image, GrabImage, IE]

case class GrabImageException(
        image: Image,
        m: String = "Error grabbing image",
        c: Throwable = null) extends ImageException(m, c)

case class GrabLowPriorityImage(image: Image) extends IM

case class GrabLowPriorityImageResponse(
        message: GrabLowPriorityImage,
        value: Either[IE, Image]) extends IM with RM[Image, GrabLowPriorityImage, IE]


private[image] case class OriginalImageUploadError(error: Throwable) extends IM
private[image] case class OriginalImageUploaded(url: String) extends IM

private[image] case class SizedImageUploadError(error: Throwable) extends IM
private[image] case class SizedImageUploaded(url: String) extends IM

private[image] case class ThumbImageUploadError(error: Throwable) extends IM
private[image] case class ThumbImageUploaded(url: String) extends IM
