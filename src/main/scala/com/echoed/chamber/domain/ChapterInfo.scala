package com.echoed.chamber.domain


case class ChapterInfo(
        chapter: Chapter,
        chapterImages: List[ChapterImage] = List.empty[ChapterImage],
        links: List[Link] = List.empty[Link])

