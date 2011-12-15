package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class EchoedUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        email: String,
        screenName: String,
        facebookUserId: String,
        facebookId: String,
        twitterUserId: String,
        twitterId: String) {

    def this(
            name: String,
            email: String,
            screenName: String,
            facebookUserId: String,
            facebookId: String,
            twitterUserId: String,
            twitterId: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name,
        email,
        screenName,
        facebookUserId,
        facebookId,
        twitterUserId,
        twitterId)
    
    def this(
            id:String, 
            name:String, 
            email:String,
            screenName: String, 
            facebookUserId: String,
            facebookId:String, 
            twitterUserId: String,
            twitterId: String) = this(
        id,
        null,
        null,
        name,
        email,
        screenName,
        facebookUserId,
        facebookId,
        twitterUserId,
        twitterId)

    def this(id: String, name: String, email: String) = this(
        id,
        null,
        null,
        name,
        email,
        null,
        null,
        null,
        null,
        null)

    def this(facebookUser: FacebookUser) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        facebookUser.name,
        facebookUser.email,
        null,
        facebookUser.id,
        facebookUser.facebookId,
        null,
        null)

    def this(twitterUser: TwitterUser) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        twitterUser.name,
        null,
        twitterUser.screenName,
        null,
        null,
        twitterUser.id,
        twitterUser.twitterId)
    
    def assignFacebookUser(facebookUserId:String,facebookId: String) = {
        if(this.facebookId == null && this.facebookUserId == null){
            this.copy(facebookId = facebookId, facebookUserId = facebookUserId)
        }
        else{
            this
        }

    }
    
    def assignTwitterUser( twitterUserId:String, twitterId: String ) = {
        if(this.twitterId == null && this.twitterUserId == null){
            this.copy(twitterId = twitterId, twitterUserId = twitterUserId)
        }
        else{
            this
        }
    }
}
