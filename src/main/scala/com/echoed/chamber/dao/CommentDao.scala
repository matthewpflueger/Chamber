package com.echoed.chamber.dao

import com.echoed.chamber.domain.Comment

import java.util.{List => JList}

trait CommentDao {

    def findByStoryId(storyId: String): JList[Comment]

    def deleteById(id: String): Int

    def insert(comment: Comment): Int

}
