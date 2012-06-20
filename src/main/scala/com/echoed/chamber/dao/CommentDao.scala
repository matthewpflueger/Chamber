package com.echoed.chamber.dao

import com.echoed.chamber.domain.Comment

import java.util.{List => JList}
import org.apache.ibatis.annotations.Param

trait CommentDao {

    def findByStoryId(storyId: String): JList[Comment]

    def findByIdAndChapterId(
            @Param("id") id: String,
            @Param("chapterId") chapterId: String): Comment

    def deleteById(id: String): Int

    def insert(comment: Comment): Int

}
