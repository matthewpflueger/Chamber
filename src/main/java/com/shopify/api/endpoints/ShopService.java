/**
*
*
**/
// Generated On: 2011-09-01T02:13:38-04:00
package com.shopify.api.endpoints;

import com.shopify.api.handlers.ShopifyResponseHandler;
import com.shopify.api.resources.Shop;
import org.codegist.crest.annotate.*;

import java.util.List;

import static org.codegist.crest.HttpMethod.*;
import static org.codegist.crest.config.Destination.BODY;

@EndPoint("")
@ContextPath("/admin/shop")
@ResponseHandler(ShopifyResponseHandler.class)
public interface ShopService extends BaseShopifyService {

    // GET
    @Path(".json")
    Shop getShop();

}
