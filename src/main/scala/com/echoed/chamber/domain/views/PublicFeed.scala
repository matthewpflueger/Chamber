package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/30/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */

case class PublicFeed(
    echoes: JList[EchoViewPublic]
) {
    def this() = this(new ArrayList[EchoViewPublic])
}
