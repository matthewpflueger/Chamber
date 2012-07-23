package com.echoed.chamber.services.echoeduser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class EchoedUserServiceLocatorActorClient extends EchoedUserServiceLocator with ActorClient {

    @BeanProperty var echoedUserServiceLocatorActor: ActorRef = _

    def actorRef = echoedUserServiceLocatorActor

    private implicit val timeout = Timeout(20 seconds)

    def getEchoedUserServiceWithId(id: String) =
            (echoedUserServiceLocatorActor ? LocateWithId(id)).mapTo[LocateWithIdResponse]

    def getEchoedUserServiceWithFacebookService(facebookService: FacebookService) =
            (echoedUserServiceLocatorActor ? LocateWithFacebookService(facebookService)).mapTo[LocateWithFacebookServiceResponse]

    def getEchoedUserServiceWithTwitterService(twitterService:TwitterService) =
            (echoedUserServiceLocatorActor ? LocateWithTwitterService(twitterService)).mapTo[LocateWithTwitterServiceResponse]

    def logout(id: String) =
            (echoedUserServiceLocatorActor ? Logout(id)).mapTo[LogoutResponse]

    def createStory(
            echoedUserId: String,
            title: String,
            imageId: String,
            partnerId: Option[String] = None,
            echoId: Option[String] = None,
            storyId: Option[String] = None) =
        (echoedUserServiceLocatorActor ? CreateStory(echoedUserId, title, imageId, partnerId, echoId, storyId)).mapTo[CreateStoryResponse]

    def updateStory(
            echoedUserId: String,
            storyId: String,
            title: String,
            imageId: String) =
        (echoedUserServiceLocatorActor ? UpdateStory(echoedUserId, storyId, title, imageId)).mapTo[UpdateStoryResponse]

    def createChapter(
            echoedUserId: String,
            storyId: String,
            title: String,
            text: String,
            imageIds: Option[Array[String]] = None) =
        (echoedUserServiceLocatorActor ? CreateChapter(
                echoedUserId,
                storyId,
                title,
                text,
                imageIds)).mapTo[CreateChapterResponse]

    def updateChapter(
            echoedUserId: String,
            chapterId: String,
            title: String,
            text: String,
            imageIds: Option[Array[String]] = None) =
        (echoedUserServiceLocatorActor ? UpdateChapter(
                echoedUserId,
                chapterId,
                title,
                text,
                imageIds)).mapTo[UpdateChapterResponse]

    def createComment(
            echoedUserId: String,
            storyId: String,
            chapterId: String,
            text: String,
            parentCommentId: Option[String] = None) =
        (echoedUserServiceLocatorActor ? CreateComment(
                echoedUserId,
                storyId,
                chapterId,
                text,
                parentCommentId)).mapTo[CreateCommentResponse]

    def initStory(
            echoedUserId: String,
            storyId: Option[String],
            echoId: Option[String],
            partnerId: Option[String]) =
        (echoedUserServiceLocatorActor ? InitStory(
                echoedUserId,
                storyId,
                echoId,
                partnerId)).mapTo[InitStoryResponse]

    def tagStory(
            echoedUserId: String,
            storyId: String,
            tagId: String ) =
        (echoedUserServiceLocatorActor ? TagStory(
                echoedUserId,
                storyId,
                tagId)).mapTo[TagStoryResponse]

}
