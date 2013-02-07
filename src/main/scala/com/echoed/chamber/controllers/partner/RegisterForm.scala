package com.echoed.chamber.controllers.partner

import com.echoed.chamber.domain.partner.{PartnerSettings, PartnerUser, Partner}
import java.util.{UUID, Date}
import org.hibernate.validator.constraints.{NotBlank, Email, URL}
import org.springframework.format.annotation.NumberFormat.Style
import javax.validation.constraints._
import org.springframework.format.annotation.{DateTimeFormat, NumberFormat}
import org.springframework.format.annotation.DateTimeFormat.ISO
import scala.reflect.BeanProperty

class RegisterForm {


    var userName: String = _

    @NotBlank
    def getUserName = userName
    def setUserName(userName: String) { this.userName = userName }


    var email: String = _

    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) { this.email = email }


    var siteName: String = _

    @NotBlank
    def getSiteName = siteName
    def setSiteName(siteName: String) { this.siteName = siteName }


    var siteUrl: String = _

    @URL(protocol = "http", message = "must be a valid url, for example http://yourdomain.com")
    @NotBlank
    def getSiteUrl() = siteUrl
    def setSiteUrl(siteUrl: String) { this.siteUrl = siteUrl }


    var shortName: String = _

    @NotBlank
    @Pattern(regexp = "\\w+", message = "must be only letters and numbers")
    def getShortName = shortName
    def setShortName(shortName: String) { this.shortName = shortName }


    var community: String = "Other"

    @NotBlank
    def getCommunity = community
    def setCommunity(community: String) { }


    var communities = List(Community(community, Some(true)))
    @BeanProperty var communitiesList: String = _
}

case class Community(name: String, selected: Option[Boolean] = None)
