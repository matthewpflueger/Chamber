package com.echoed.chamber.services.echoeduser

import akka.dispatch.Future
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

trait EchoedUserServiceLocator {

    def getEchoedUserServiceWithId(id: String): Future[LocateWithIdResponse]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService): Future[LocateWithFacebookServiceResponse]

    def getEchoedUserServiceWithTwitterService(twitterService: TwitterService): Future[LocateWithTwitterServiceResponse]

    def logout(id: String): Future[LogoutResponse]

    def initStory(
            echoedUserId: String,
            storyId: Option[String] = None,
            echoId: Option[String] = None,
            partnerId: Option[String] = None): Future[InitStoryResponse]

    def createStory(
            echoedUserId: String,
            title: String,
            imageId: String,
            partnerId: Option[String] =None,
            echoId: Option[String] = None,
            productInfo: Option[String] = None): Future[CreateStoryResponse]

    def updateStory(
            echoedUserId: String,
            storyId: String,
            title: String,
            imageIds: String): Future[UpdateStoryResponse]

    def createChapter(
            echoedUserId: String,
            storyId: String,
            title: String,
            text: String,
            imageIds: Option[Array[String]] = None): Future[CreateChapterResponse]

    def updateChapter(
            echoedUserId: String,
            chapterId: String,
            title: String,
            text: String,
            imageIds: Option[Array[String]] = None): Future[UpdateChapterResponse]

    def createComment(
            echoedUserId: String,
            storyId: String,
            chapterId: String,
            text: String,
            parentCommentId: Option[String] = None): Future[CreateCommentResponse]

    def tagStory(
            echoedUserId: String,
            storyId: String,
            tagId: String): Future[TagStoryResponse]
}
