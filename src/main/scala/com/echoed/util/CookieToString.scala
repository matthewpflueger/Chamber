package com.echoed.util

import javax.servlet.http.Cookie

trait CookieToString { this: Cookie =>

    override def toString() = {
        "Cookie(name=%s, value=%s, maxAge=%s, path=%s, domain=%s, httpOnly=%s)" format(
                getName(),
                getValue(),
                getMaxAge(),
                getPath(),
                getDomain(),
                isHttpOnly)
    }
}
