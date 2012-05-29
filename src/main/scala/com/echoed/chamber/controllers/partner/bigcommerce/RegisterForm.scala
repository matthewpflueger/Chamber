package com.echoed.chamber.controllers.partner.bigcommerce

import org.hibernate.validator.constraints.{URL, NotBlank, Email}
import com.echoed.chamber.domain.partner.bigcommerce.BigCommercePartner


case class RegisterForm(
        var name: String = null,
        var email: String = null,
        var phone: String = null,
        var businessName: String = null,
        var website: String = null,
        var apiUser: String = null,
        var apiPath: String = null,
        var apiToken: String = null) {


    def this() = {
        this(null)
    }

    def createPartner = new BigCommercePartner(
            name,
            email,
            phone,
            website,
            businessName,
            apiUser,
            apiPath,
            apiToken)


    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) {
        this.email = email
    }

    @NotBlank
    def getName = name
    def setName(name: String) {
        this.name = name
    }

    @NotBlank
    def getPhone = phone
    def setPhone(phone: String) {
        this.phone = phone
    }

    @URL(protocol = "http")
    @NotBlank
    def getWebsite() = website
    def setWebsite(website: String) { this.website = website }

    @NotBlank
    def getBusinessName = businessName
    def setBusinessName(businessName: String) {
        this.businessName = businessName
    }

    @NotBlank
    def getApiUser = apiUser
    def setApiUser(apiUser: String) {
        this.apiUser = apiUser
    }

    @URL(protocol = "http")
    @NotBlank
    def getApiPath() = website
    def setApiPath(apiPath: String) { this.apiPath = apiPath }

    @NotBlank
    def getApiToken = apiToken
    def setApiToken(apiToken: String) {
        this.apiToken = apiToken
    }

}
