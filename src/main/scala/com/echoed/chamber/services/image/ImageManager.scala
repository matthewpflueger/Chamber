package com.echoed.chamber.services.image

import java.net.URL
import com.google.common.io.ByteStreams
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream


class ImageManager {


    def getImageAndBytes(url: String) = {
        val connection = new URL(url).openConnection()
        connection.setConnectTimeout(5000)
        connection.setReadTimeout(5000)
        val inputStream = connection.getInputStream

        try {
            val bytes: Array[Byte] = ByteStreams.toByteArray(connection.getInputStream)
            val image = ImageIO.read(new ByteArrayInputStream(bytes))
//            ImageIO.write(image, "jpeg", )
            (image, bytes)
        } finally {
            inputStream.close()
        }


    }


}
