package com.echoed.chamber.controllers.partner.magentogo

import org.hibernate.validator.constraints.{URL, NotBlank, Email}
import com.echoed.chamber.domain.partner.magentogo.MagentoGoPartner


case class RegisterForm(
        var name: String = null,
        var email: String = null,
        var phone: String = null,
        var businessName: String = null,
        var website: String = null,
        var apiUser: String = null,
        var apiKey: String = null) {


    def this() = {
        this(null)
    }

    def createPartner = new MagentoGoPartner(
            name,
            email,
            phone,
            website,
            businessName,
            apiUser,
            website + "/api/index/index/",
            apiKey)


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

    @NotBlank
    def getApiKey = apiKey
    def setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

}
