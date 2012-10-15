package com.echoed.chamber.domain

import partner.{StoryPrompts, Partner}
import views.{StoryCommunities, StoryFull}

case class StoryInfo(
        echoedUser: EchoedUser,
        echo: Echo,
        partner: Partner,
        storyPrompts: StoryPrompts,
        communities: StoryCommunities,
        storyFull: StoryFull)

