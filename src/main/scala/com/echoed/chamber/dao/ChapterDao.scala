package com.echoed.chamber.dao

import com.echoed.chamber.domain.Chapter
import java.util.{List => JList}

trait ChapterDao {

    def insert(chapter: Chapter): Int

    def findByStoryId(storyId: String): JList[Chapter]

    def deleteByStoryId(storyId: String): Int

    def findById(id: String): Chapter
}
