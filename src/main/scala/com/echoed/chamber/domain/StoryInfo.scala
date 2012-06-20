package com.echoed.chamber.domain

import partner.{StoryPrompts, Partner}
import views.StoryFull

case class StoryInfo(
        echoedUser: EchoedUser,
        echo: Echo,
        partner: Partner,
        storyPrompts: StoryPrompts,
        storyFull: StoryFull)

