package com.echoed.chamber.util

import java.util.{List => JList}

case class FacebookAllTestUsers(
    data: JList[FacebookSimpleTestUser],
    paging: FacebookPaging)
