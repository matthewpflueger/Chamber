package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._

import java.util.{ArrayList, List => JList}


case class StoryFull(
        id: String,
        story: Story,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: JList[Comment]) {

    def this(id:String, story: Story) = this(
            id,
            story,
            new ArrayList[Chapter],
            new ArrayList[ChapterImage],
            new ArrayList[Comment])
}

