package com.echoed.chamber.dao

import com.echoed.chamber.domain.Tag

trait TagDao {

    def insert(tag: Tag): Int

    def findById(tagId: String): Tag

}
