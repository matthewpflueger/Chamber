package com.echoed.chamber.services.partner.bigcommerce


import dispatch._
import dispatch.nio._
import com.echoed.util.ScalaJson._
import java.util.{Map => JMap}

import org.joda.time.format.DateTimeFormat


object DispatcherJSONTest extends App {


    val h = new Http
    val endpoint = url("https://store-cf57a.mybigcommerce.com/api/v2/")
    val apiUser = "api"
    val apiToken = "31adb4bd5e9093a2273ef4d13ada81f0"

    //"29"


/*
{ "availability" : "available",
  "availability_description" : "",
  "bin_picking_number" : "",
  "brand" : { "resource" : "/brands/0",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/brands/0.json"
    },
  "brand_id" : 0,
  "categories" : [ 10 ],
  "condition" : "New",
  "configurable_fields" : { "resource" : "/products/30/configurablefields",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/configurablefields.json"
    },
  "cost_price" : "0.0000",
  "custom_fields" : { "resource" : "/products/30/customfields",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/customfields.json"
    },
  "custom_url" : "/another-black-watch/",
  "date_created" : "Sun, 13 May 2012 23:51:45 +0000",
  "date_last_imported" : "",
  "date_modified" : "Sun, 13 May 2012 23:51:45 +0000",
  "depth" : "1.0000",
  "description" : "<p>Another fantastic black watch</p>",
  "discount_rules" : { "resource" : "/products/30/discountrules",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/discountrules.json"
    },
  "event_date_end" : "",
  "event_date_field_name" : "Delivery Date",
  "event_date_start" : "",
  "event_date_type" : "none",
  "fixed_cost_shipping_price" : "10.0000",
  "height" : "1.0000",
  "id" : 30,
  "images" : { "resource" : "/products/30/images",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/images.json"
    },
  "inventory_level" : 0,
  "inventory_tracking" : "none",
  "inventory_warning_level" : 0,
  "is_condition_shown" : false,
  "is_featured" : false,
  "is_free_shipping" : false,
  "is_open_graph_thumbnail" : true,
  "is_preorder_only" : false,
  "is_price_hidden" : false,
  "is_visible" : true,
  "layout_file" : "product.html",
  "meta_description" : "",
  "meta_keywords" : "",
  "myob_asset_account" : "",
  "myob_expense_account" : "",
  "myob_income_account" : "",
  "name" : "Another Black Watch",
  "open_graph_description" : "",
  "open_graph_title" : "",
  "open_graph_type" : "product",
  "option_set" : null,
  "option_set_display" : "right",
  "option_set_id" : null,
  "options" : { "resource" : "/products/30/options",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/options.json"
    },
  "order_quantity_maximum" : 0,
  "order_quantity_minimum" : 0,
  "page_title" : "",
  "peachtree_gl_account" : "",
  "preorder_message" : "",
  "preorder_release_date" : "",
  "price" : "100.0000",
  "price_hidden_label" : "",
  "rating_count" : 0,
  "rating_total" : 0,
  "related_products" : "-1",
  "retail_price" : "0.0000",
  "rules" : { "resource" : "/products/30/rules",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/rules.json"
    },
  "sale_price" : "0.0000",
  "search_keywords" : "",
  "sku" : "2222",
  "skus" : { "resource" : "/products/30/skus",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/skus.json"
    },
  "sort_order" : 0,
  "tax_class" : { "resource" : "/taxclasses/0",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/taxclasses/0.json"
    },
  "tax_class_id" : 0,
  "total_sold" : 0,
  "type" : "physical",
  "upc" : "",
  "videos" : { "resource" : "/products/30/videos",
      "url" : "https://store-cf57a.mybigcommerce.com/api/v2/products/30/videos.json"
    },
  "view_count" : 1,
  "warranty" : "",
  "weight" : "1.0000",
  "width" : "1.0000"
}
 */
//    h(endpoint
//            .as_!(apiUser, apiToken)
//            / "products/30" //29
//            <:< Map("Accept" -> "application/json")
//            >\ "utf-8"
//            >>  { res =>
//        val node = parse[JsonNode](res)
////        val node = parse[Array[JMap[String, AnyRef]]](res)
////        val node = parse[Array[JMap[String, AnyRef]]](res)
//        println("Json is %s" format node)
//        node
//
////        val node = ScalaJson.parse[JsonNode](res)
////        println("Json is %s" format node)
////        val time = node.findValue("time")
////        println("Time is: %s" format time)
////        node
//    } >! {
//        case e => println("Received error!: %s" format e)
//    })


/*
{id=116, product_id=29, image_file=m/910/screen_shot_2012_04_09_at_2.51.53_pm__94418.png, is_thumbnail=true, sort_order=0, date_created=Sat, 12 May 2012 19:10:27 +0000}
*/
//    h(endpoint
//            .as_!(apiUser, apiToken)
//            / "products/29/images"
//            <:< Map("Accept" -> "application/json")
//            >\ "utf-8"
//            >>  { res =>
//        val node = parse[Array[JMap[String, AnyRef]]](res)
//        node.filter(_("is_thumbnail").toString == "true").foreach { image => println("Image is %s" format image) }
////                        orderActor ! EchoItem(
////        node.filter(_("is_thumbnail") == "false"))
//        println("Json is %s" format node)
//        node
//
////        val node = ScalaJson.parse[JsonNode](res)
////        println("Json is %s" format node)
////        val time = node.findValue("time")
////        println("Time is: %s" format time)
////        node
//    } >! {
//        case e => println("Received error!: %s" format e)
//    })


/*
[ { "applied_discounts" : [  ],
    "base_cost_price" : "0.0000",
    "base_price" : "100.0000",
    "base_total" : "100.0000",
    "base_wrapping_cost" : "0.0000",
    "bin_picking_number" : "",
    "configurable_fields" : [  ],
    "cost_price_ex_tax" : "0.0000",
    "cost_price_inc_tax" : "0.0000",
    "cost_price_tax" : "0.0000",
    "ebay_item_id" : "",
    "ebay_transaction_id" : "",
    "event_date" : "",
    "event_name" : null,
    "fixed_shipping_cost" : "0.0000",
    "id" : 1,
    "is_bundled_product " : false,
    "is_refunded" : false,
    "name" : "Black Watch",
    "option_set_id" : null,
    "order_address_id" : 1,
    "order_id" : 100,
    "parent_order_product_id" : null,
    "price_ex_tax" : "100.0000",
    "price_inc_tax" : "100.0000",
    "price_tax" : "0.0000",
    "product_id" : 29,
    "product_options" : [  ],
    "quantity" : 1,
    "quantity_shipped" : 0,
    "refund_amount" : "0.0000",
    "return_id" : 0,
    "sku" : "11111",
    "total_ex_tax" : "100.0000",
    "total_inc_tax" : "100.0000",
    "total_tax" : "0.0000",
    "type" : "physical",
    "weight" : "0.5",
    "wrapping_cost_ex_tax" : "0.0000",
    "wrapping_cost_inc_tax" : "0.0000",
    "wrapping_cost_tax" : "0.0000",
    "wrapping_message" : "",
    "wrapping_name" : ""
  } ]
 */

    //example: Mon, 14 May 2012 18:11:26 +0000
    val formatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")
    val date = formatter.parseDateTime("Mon, 14 May 2012 18:11:26 +0000").toDate
    println("Date is %s" format date)

    h(endpoint
            .as_!(apiUser, apiToken)
            / "orders/107/products"
            <:< Map("Accept" -> "application/json")
            >\ "utf-8"
            >>  { res =>
        val node = parse[Array[JMap[String, AnyRef]]](res)
//        val node = parse[JsonNode](res)
        println("Json is %s" format node)

//        val node = parse[Array[JMap[String, AnyRef]]](res)
//        println("Json is %s" format node)
        //val id = node("id") //findValue("time")
        //println("Order id is: %s" format id)
        //val products = node("products")
        //println("Products are %s: %s" format(products.getClass, products))
        node

//        val node = ScalaJson.parse[JsonNode](res)
//        println("Json is %s" format node)
//        val time = node.findValue("time")
//        println("Time is: %s" format time)
//        node
    } >! {
        case e => println("Received error!: %s" format e)
    })



//    h(endpoint
//            .as_!(apiUser, apiToken)
//            / "time"
//            <:< Map("Accept" -> "application/json")
//            >\ "utf-8"
//            >>  { res =>
//        val node = parse[Map[String, String]](res)
//        println("Json is %s" format node)
//        val time = node("time") //findValue("time")
//        println("Time is: %s" format time)
//        node
//
////        val node = ScalaJson.parse[JsonNode](res)
////        println("Json is %s" format node)
////        val time = node.findValue("time")
////        println("Time is: %s" format time)
////        node
//    } >! {
//        case e => println("Received error!: %s" format e)
//    })



    println("Sleeping for 5 seconds")
    Thread.sleep(5000)
    println("Exiting")
    h.shutdown
}

