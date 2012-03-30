package com.echoed.chamber.dao

import com.echoed.chamber.domain.Image
import java.util.Date

trait ImageDao {

    def insert(image: Image): Int

    def update(image: Image): Int

    def findByUrl(url: String): Image

    def findById(id: String): Image

    def findUnprocessed(lastProcessedBeforeDate: Date): Image

    def deleteByUrl(url: String): Int
}
