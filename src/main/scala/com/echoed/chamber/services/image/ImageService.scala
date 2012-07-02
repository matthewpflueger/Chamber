package com.echoed.chamber.services.image

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait ImageService {

    def processImage(image: Image): Future[ProcessImageResponse]

    def startProcessImage(image: Image): Future[StartProcessImageResponse]
}
