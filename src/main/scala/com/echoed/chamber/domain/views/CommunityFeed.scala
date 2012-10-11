package com.echoed.chamber.domain.views

import java.util.{List => JList}
import com.echoed.chamber.domain.Community

case class CommunityFeed(communities: JList[Community])
