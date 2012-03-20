package com.echoed.util

import scala.reflect.BeanProperty
import java.io.File
import org.slf4j.LoggerFactory
import java.util.{HashMap => JMap, HashSet => JSet}
import org.jclouds.blobstore.options.GetOptions
import org.jclouds.blobstore.{AsyncBlobStore, BlobStoreContext, BlobStoreContextFactory}
import com.google.inject.Module
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule
import com.google.common.util.concurrent.{FutureCallback, Futures}

//import org.jclouds.concurrent.FutureIterables._ //awaitCompletion;

object BlobStore extends App {

    val blobStore = new BlobStore()
    blobStore.provider = "cloudfiles-us"
    blobStore.username = "echoedinc"
    blobStore.credential = "753478208613b74742190a7f5e07113c"
    blobStore.container = "products"
    blobStore.init()

//    blobStore.storeit(args(0))
    blobStore.storeit("/home/mpflueger/workspace/chamber/src/main/webapp/images/Pic3.jpg")
}

class BlobStore {

    private val logger = LoggerFactory.getLogger(classOf[BlobStore])

    @BeanProperty var provider: String = _
    @BeanProperty var username: String = _
    @BeanProperty var credential: String = _
    @BeanProperty var container: String = _

    var context: BlobStoreContext = _
    var asyncBlobStore: AsyncBlobStore = _

    def init() {
        val modules = new JSet[Module]()
        modules.add(new SLF4JLoggingModule())
        context = new BlobStoreContextFactory().createContext(provider, username, credential, modules)
        asyncBlobStore = context.getAsyncBlobStore
    }

    def store(
                bytes: Array[Byte],
                fileName: String,
                contentType: String,
                callback: Either[Throwable, String] => Unit,
                metadata: Option[Map[String, String]] = None) {
        val metadata = new JMap[String, String]()
        metadata.put("X-Object-Meta-test-key", "test value")
        metadata.put("test-key", "test value")
        val blob = asyncBlobStore.blobBuilder(fileName)
                .userMetadata(metadata)
                .payload(bytes)
                .contentDisposition("attachment; filename=%s" format fileName)
                .contentType(contentType)
                .contentLength(bytes.length)
                .calculateMD5().build();


        blob.getMetadata.getContentMetadata.getContentMD5

        val future = asyncBlobStore.putBlob(container, blob)
        Futures.addCallback(future, new FutureCallback[String]() {
            def onSuccess(result: String) {
                try {
                    logger.debug("Received success result {} for upload of {}", result, fileName)
                    val url = blob.getMetadata.getUri.toURL.toExternalForm
                    callback(Right(url))
                    logger.debug("Successful callback for {} with {}", fileName, url)
                } catch {
                    case e => logger.error("Callback threw an error for %s" format(fileName), e)
                }
            }

            def onFailure(t: Throwable) {
                try {
                    logger.error("Received error result for upload of %s" format fileName, t)
                    callback(Left(t))
                } catch {
                    case e => logger.error("Callback threw an error for %s" format(fileName), e)
                }
            }
        },
        context.utils.userExecutor())
    }


    def storeit(filePath: String) {
        val file = new File(filePath)
        file.ensuring(_.exists, "%s does not exist" format filePath)
        file.ensuring(_.canRead, "%s cannot be read" format filePath)
        file.ensuring(_.isFile, "%s is not a file" format filePath)
        file.getName.ensuring(_.length > 0, "%s does not have a name" format filePath)

        val ext = file.getName.substring(
                file.getName.lastIndexOf('.').ensuring(_ > -1, "%s does not have a file extension" format filePath) + 1)


        val metadata = new JMap[String, String]()
        metadata.put("X-Object-Meta-test-key", "test value")
        metadata.put("test-key", "test value")
        val blob = asyncBlobStore.blobBuilder(file.getName)
                .userMetadata(metadata)
                .payload(file)
                .contentDisposition("attachment; filename=%s" format file.getName)
                .contentType("image/%s" format ext)
                .contentLength(file.length())
                .calculateMD5().build();


        blob.getMetadata.getContentMetadata.getContentMD5

        val future = asyncBlobStore.putBlob(container, blob)

        future.addListener(new Runnable {
            def run() {
                try {
                    logger.debug("Uploaded {}", filePath)
//                    asyncBlobStore.getBlob(container, file.getName).addListener(new Runnable)
                    context.close()
                } catch {
                    case e => logger.error("Error uploading %s" format filePath, e)
                }
            }
        },
        context.utils().userExecutor())

    }
}
