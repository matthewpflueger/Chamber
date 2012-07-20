package com.echoed.chamber.dao

import com.echoed.chamber.domain.Tag
import java.util.{List => JList}

trait TagDao {

    def insert(tag: Tag): Int

    def update(tag: Tag): Int

    def findById(tagId: String): Tag

    def getTags: JList[Tag]

}
