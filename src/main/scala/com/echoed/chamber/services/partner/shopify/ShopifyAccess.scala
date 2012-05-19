package com.echoed.chamber.services.partner.shopify

import akka.dispatch.Future


trait ShopifyAccess {

    def fetchPassword(shop: String, signature: String, t: String, timeStamp: String): Future[FetchPasswordResponse]

    def fetchShop(shop: String, password: String): Future[FetchShopResponse]

    def fetchShopFromToken(shop: String, signature: String, t: String, timeStamp: String): Future[FetchShopFromTokenResponse]

    def fetchOrder(shop: String, password: String, orderId: Int): Future[FetchOrderResponse]

    def fetchProducts(shop: String, password: String): Future[FetchProductsResponse]

    def fetchProduct(shop: String, password: String, productId: Int): Future[FetchProductResponse]
}
