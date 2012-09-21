package com.echoed.chamber.controllers

import scala.reflect.BeanProperty
import org.springframework.beans.factory.annotation.Required

class Views {

    @Required @BeanProperty var siteUrl: String = _
    @Required @BeanProperty var secureSiteUrl: String = _
    @Required @BeanProperty var errorView: String = _
    
    @Required @BeanProperty var echoCloseViewUrl: String = _
    @Required @BeanProperty var echoEchoedViewUrl: String = _
    @Required @BeanProperty var echoRegisterView: String = _
    
    @Required @BeanProperty var echoJsView: String = _
    @Required @BeanProperty var echoJsErrorView: String = _
    @Required @BeanProperty var echoJsNotActiveView: String = _
    @Required @BeanProperty var echoLoginView: String = _
    @Required @BeanProperty var echoCouponView: String = _
    @Required @BeanProperty var echoLoginNotNeededView: String = _
    @Required @BeanProperty var echoRegisterUrl: String = _
    @Required @BeanProperty var echoEchoedUrl: String = _
    
    @Required @BeanProperty var echoConfirmView: String = _
    @Required @BeanProperty var echoFinishView: String = _
    @Required @BeanProperty var echoIframe: String = _
    @Required @BeanProperty var echoAuthComplete: String = _
    
    @Required @BeanProperty var echoRedirectView: String = _
    
    @Required @BeanProperty var productGraphUrl: String = _
    @Required @BeanProperty var buttonView: String = _
    @Required @BeanProperty var loginView: String = _
    @Required @BeanProperty var confirmView: String = _
    @Required @BeanProperty var echoItView: String = _
    
    @Required @BeanProperty var facebookAddRedirectUrl: String = _
    @Required @BeanProperty var facebookLoginRedirectUrl: String = _
    @Required @BeanProperty var logoutUrl: String = _
    
    @Required @BeanProperty var postLoginView: String = _
    @Required @BeanProperty var postAddView: String = _
    @Required @BeanProperty var facebookLoginErrorView: String = _
    @Required @BeanProperty var postLogoutView: String = _
    @Required @BeanProperty var closetView: String = _
    
    @Required @BeanProperty var storyGraphUrl: String = _
    
    @Required @BeanProperty var partnerLoginErrorView: String = _
    @Required @BeanProperty var partnerLoginView: String = _
    
    @Required @BeanProperty var activateView: String = _
    @Required @BeanProperty var postActivateView: String = _
    
    @Required @BeanProperty var registerView: String = _
    @Required @BeanProperty var postRegisterView: String = _
    
    @Required @BeanProperty var adminLoginView: String = _
    @Required @BeanProperty var adminDashboardUrl: String = _
    @Required @BeanProperty var adminDashboardView: String = _
    @Required @BeanProperty var adminDashboardErrorView: String = _

    @Required @BeanProperty var updatePartnerUserView: String = _
    @Required @BeanProperty var postUpdatePartnerUserView: String = _

    @Required @BeanProperty var partnerDashboardView: String = _
    @Required @BeanProperty var partnerDashboardErrorView: String = _

    @Required @BeanProperty var shopifyIntegrationView: String = _

    @Required @BeanProperty var networkSolutionsRegisterView: String = _
    @Required @BeanProperty var networkSolutionsPostAuthView: String = _
    @Required @BeanProperty var networkSolutionsSuccessUrl: String = _

    @Required @BeanProperty var bigCommerceRegisterView: String = _
    @Required @BeanProperty var bigCommercePostRegisterView: String = _
    
    @Required @BeanProperty var magentoGoRegisterView: String = _
    @Required @BeanProperty var magentoGoPostRegisterView: String = _
    
    @Required @BeanProperty var websitesView: String = _
    @Required @BeanProperty var contactUsView: String = _
    @Required @BeanProperty var whatIsEchoedView: String = _
    @Required @BeanProperty var privacyView: String = _
    @Required @BeanProperty var termsView: String = _
    @Required @BeanProperty var guidelinesView: String = _
    @Required @BeanProperty var storyTellingView: String = _
    
    @Required @BeanProperty var facebookGraphProductView: String = _
    @Required @BeanProperty var facebookGraphStoryView: String = _
    
    @Required @BeanProperty var widgetJsView: String = _
    @Required @BeanProperty var widgetIframeView: String = _
    @Required @BeanProperty var widgetAppJsView: String = _
    @Required @BeanProperty var widgetAppIFrameView: String = _

    @Required @BeanProperty var loginEmailView: String = _
    @Required @BeanProperty var loginRegisterView: String = _
    @Required @BeanProperty var loginResetView: String = _
    @Required @BeanProperty var loginResetPostView: String = _
    @Required @BeanProperty var loginResetPasswordView: String = _

}
