package com.echoed.chamber.util

import scala.reflect.BeanProperty


class FacebookPaging() {

    @BeanProperty var next: String = null
    @BeanProperty var previous: String = null

}
