package com.echoed.chamber.dao

import com.echoed.chamber.domain.Chapter
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param

trait ChapterDao {

    def insert(chapter: Chapter): Int

    def findByStoryId(storyId: String): JList[Chapter]

    def deleteByStoryId(storyId: String): Int

    def findById(id: String): Chapter

    def findByIdAndEchoedUserId(
            @Param("id") id: String,
            @Param("echoedUserId") echoedUserId: String): Chapter

    def findByIdAndStoryId(
            @Param("id") id: String,
            @Param("storyId") storyId: String): Chapter

    def update(chapter: Chapter): Int
}
