package com.echoed.util

import scala.reflect.BeanProperty
import java.io.File
import org.slf4j.LoggerFactory
import java.util.{HashSet => JSet}
import org.jclouds.blobstore.{AsyncBlobStore, BlobStoreContext, BlobStoreContextFactory}
import com.google.inject.Module
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule
import com.google.common.util.concurrent.{FutureCallback, Futures}
import io.Source
import scala.collection.JavaConversions._
import akka.dispatch.{DefaultCompletableFuture, CompletableFuture}
import akka.util.Duration


class BlobStore {

    private val logger = LoggerFactory.getLogger(classOf[BlobStore])

    @BeanProperty var provider: String = _
    @BeanProperty var username: String = _
    @BeanProperty var credential: String = _

    @BeanProperty var container: String = _
    @BeanProperty var containerUrl: String = _

    var context: BlobStoreContext = _
    var asyncBlobStore: AsyncBlobStore = _

    def init() {
        val modules = new JSet[Module]()
        modules.add(new SLF4JLoggingModule())
        context = new BlobStoreContextFactory().createContext(provider, username, credential, modules)
        asyncBlobStore = context.getAsyncBlobStore
    }

    def destroy() {
        context.close()
    }

    def store(
            bytes: Array[Byte],
            fileName: String,
            contentType: String,
            metadata: Option[Map[String, String]] = None) = {

        val blob = asyncBlobStore.blobBuilder(fileName)
                .userMetadata(mapAsJavaMap(metadata.getOrElse(Map.empty[String, String])))
                .payload(bytes)
                .contentDisposition("attachment; filename=%s" format fileName)
                .contentType(contentType)
                .contentLength(bytes.length)
                .calculateMD5().build();

        val future = new DefaultCompletableFuture[String]()

        Futures.addCallback(asyncBlobStore.putBlob(container, blob),
            new FutureCallback[String]() {
                def onSuccess(result: String) {
                    logger.debug("Received success result {} for upload of {}", result, fileName)
                    val url = "%s/%s" format(containerUrl, fileName)
                    future.completeWithResult(url)
                }

                def onFailure(t: Throwable) {
                    logger.error("Received error result for upload of %s" format fileName, t)
                    future.completeWithException(t)
                }
            },
            context.utils.userExecutor())

        future
    }


    def storeFile(
            filePath: String,
            contentType: String,
            metadata: Option[Map[String, String]] = None) = {

        val file = new File(filePath)
        file.ensuring(_.exists, "%s does not exist" format filePath)
        file.ensuring(_.canRead, "%s cannot be read" format filePath)
        file.ensuring(_.isFile, "%s is not a file" format filePath)
        file.getName.ensuring(_.length > 0, "%s does not have a name" format filePath)

        val bytes = Source.fromFile(file)(scala.io.Codec.ISO8859).map(_.toByte).toArray

        store(
            bytes,
            file.getName,
            contentType,
            metadata)
    }


    def delete(fileName: String) = {
        val future = new DefaultCompletableFuture[Boolean]()

        Futures.addCallback(asyncBlobStore.removeBlob(container, fileName),
            new FutureCallback[Void]() {
                def onSuccess(result: Void) {
                    logger.debug("Received success for deletion of {}", fileName)
                    future.completeWithResult(true)
                }

                def onFailure(t: Throwable) {
                    logger.error("Received error result for deletion of %s" format fileName, t)
                    future.completeWithException(t)
                }
            },
            context.utils.userExecutor())

        future
    }


    def exists(fileName: String) = {
        val future = new DefaultCompletableFuture[Boolean]()

        Futures.addCallback(asyncBlobStore.blobExists(container, fileName),
            new FutureCallback[java.lang.Boolean]() {
                def onSuccess(result: java.lang.Boolean) {
                    logger.debug("Received success result of {} for exists of {}", result, fileName)
                    future.completeWithResult(result)
                }

                def onFailure(t: Throwable) {
                    logger.error("Received error result for exists of %s" format fileName, t)
                    future.completeWithException(t)
                }
            },
            context.utils.userExecutor())

        future
    }

}


object BlobStore extends App {

    val blobStore = new BlobStore()
    blobStore.provider = "cloudfiles-us"
    blobStore.username = "echoedinc"
    blobStore.credential = "753478208613b74742190a7f5e07113c"
    blobStore.container = "products"
    blobStore.containerUrl = "http://products.echoed.com"
    blobStore.init()


    val contentType = "images/jpg"
    val fileName = "Pic3.jpg"
    val filePath = "/home/mpflueger/workspace/chamber/src/main/webapp/images/%s" format fileName

    blobStore
            .storeFile(filePath, contentType)
            .flatMap(blobStore.delete(_))
            .onComplete(_ => blobStore.destroy)
            .await(Duration(30, "seconds"))
}

