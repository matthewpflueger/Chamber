package com.echoed.chamber.dao

import java.util.{List => JList}
import com.echoed.chamber.domain.{Image, EchoPossibility}

trait ImageDao {

    def update(image: Image): Int

    def updateAll(image: Image): Int

    def updateAllUnprocessed(image: Image): Int

    def findProcessedByOriginalUrl(url: String): Image

}
