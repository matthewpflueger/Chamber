package com.echoed.chamber.services.image

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait ImageService {

    def grabImage(image: Image): Future[GrabImageResponse]

}
