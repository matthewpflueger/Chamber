package com.echoed.chamber.filters

import javax.servlet.ServletOutputStream
import java.io.{DataOutputStream, OutputStream}

class FilterOutputStream(output: OutputStream) extends ServletOutputStream {

    val stream = new DataOutputStream(output)

    def write(b: Int) {
        stream.write(b);
    }

    override def write(b: Array[Byte]) {
        stream.write(b);
    }

    override def write(b: Array[Byte], off: Int, len: Int) {
        stream.write(b, off, len);
    }

}
