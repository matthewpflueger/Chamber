package com.echoed.chamber.dao

import com.echoed.chamber.domain.ChapterImage

import java.util.{List => JList}

trait ChapterImageDao {

    def insert(chapterImage: ChapterImage): Int

    def findByChapterId(chapterId: String): JList[ChapterImage]

    def deleteById(id: String): Int

    def deleteByChapterId(chapterId: String): Int
}
