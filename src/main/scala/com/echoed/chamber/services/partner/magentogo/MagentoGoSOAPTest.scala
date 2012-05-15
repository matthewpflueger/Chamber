package com.echoed.chamber.services.partner.magentogo

import dispatch.nio.Http
import dispatch.{Request, url}
import scala.Some
import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import com.echoed.chamber.services.partner.PartnerService
import java.util.concurrent.ConcurrentHashMap
import com.echoed.chamber.services.facebook.FacebookService
import akka.actor.{Actor, ActorRef}
import xml.{Node, Text, NodeSeq}


object MagentoGoSOAPTest extends App {

/*
http://go.magento.com/support/kb/entry/name/setting-web-services/
v1 WSDL fetched from: http://echoed.gostorego.com/api/index/index/?wsdl=1
v2 WSDL fetched from: http://echoed.gostorego.com/api/v2_soap?wsdl=1
*/

    val h = new Http
    val urn = "urn:Magento"
    val endpoint = url("http://echoed.gostorego.com/api/index/index/") //?wsdl=1
//    val endpoint = url("http://echoed.gostorego.com/api/v2_soap/index/") //?wsdl=1")
    val username = "api"
    val apiKey = "Ech0ed1nc"

    /*
       Success example:
       <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope" xmlns:ns1="urn:Magento" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:enc="http://www.w3.org/2003/05/soap-encoding"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:loginResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>loginReturn</rpc:result><loginReturn xsi:type="xsd:string">a1890b013b4e623a1fea0ab8e56968e4</loginReturn></ns1:loginResponse></env:Body></env:Envelope>

       Failure example:
       <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body><env:Fault><env:Code><env:Value>5</env:Value></env:Code><env:Reason><env:Text>Session expired. Try to relogin.</env:Text></env:Reason></env:Fault></env:Body></env:Envelope>
    */
    def login(username: String, apiKey: String) =
        wrap(
            <login xmlns={urn}>
                <username>{username}</username>
                <apiKey>{apiKey}</apiKey>
            </login>)


    /*
    <env:Envelope xmlns:ns2="http://xml.apache.org/xml-soap" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:multiCallResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>multiCallReturn</rpc:result><multiCallReturn xsi:type="ns1:FixedArray" enc:arraySize="1" enc:itemType="xsd:anyType"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">state</key><value xsi:type="xsd:string">new</value></item><item><key xsi:type="xsd:string">status</key><value xsi:type="xsd:string">pending</value></item><item><key xsi:type="xsd:string">coupon_code</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">protect_code</key><value xsi:type="xsd:string">786921</value></item><item><key xsi:type="xsd:string">shipping_description</key><value xsi:type="xsd:string">Flat Rate - Fixed</value></item><item><key xsi:type="xsd:string">is_virtual</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">base_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_discount_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_discount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_grand_total</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_shipping_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_shipping_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_subtotal_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_to_global_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">base_to_order_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">base_total_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_invoiced_cost</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_offline_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_online_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_qty_ordered</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">grand_total</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">shipping_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">shipping_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">store_to_base_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">store_to_order_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">subtotal</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">subtotal_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">subtotal_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">subtotal_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_offline_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_online_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_qty_ordered</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">can_ship_partially</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">can_ship_partially_item</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_is_guest</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">customer_note_notify</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">billing_address_id</key><value xsi:type="xsd:string">7</value></item><item><key xsi:type="xsd:string">customer_group_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">edit_increment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">email_sent</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">forced_do_shipment_with_invoice</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">payment_authorization_expiration</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paypal_ipn_customer_notified</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_id</key><value xsi:type="xsd:string">5</value></item><item><key xsi:type="xsd:string">shipping_address_id</key><value xsi:type="xsd:string">8</value></item><item><key xsi:type="xsd:string">adjustment_negative</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">adjustment_positive</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_adjustment_negative</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_adjustment_positive</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_subtotal_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_total_due</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">payment_authorization_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">subtotal_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">total_due</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">customer_dob</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">increment_id</key><value xsi:type="xsd:string">100000004</value></item><item><key xsi:type="xsd:string">applied_rule_ids</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">base_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">customer_email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">customer_firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">customer_lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">customer_middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_taxvat</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_description</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">ext_customer_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ext_order_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">global_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">hold_before_state</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hold_before_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">order_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">original_increment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_child_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_child_real_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_parent_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_parent_real_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">remote_ip</key><value xsi:type="xsd:string">98.14.233.243</value></item><item><key xsi:type="xsd:string">shipping_method</key><value xsi:type="xsd:string">flatrate_flatrate</value></item><item><key xsi:type="xsd:string">store_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">store_name</key><value xsi:type="xsd:string">Main Website
Main Store
English</value></item><item><key xsi:type="xsd:string">x_forwarded_for</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_note</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">updated_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">total_item_count</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">customer_gender</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_rate</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_percent</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">custbalance_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_base_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">real_order_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_code</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_multi_payment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tracking_numbers</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_hold</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_custbalance_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">shipping_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_shipping_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_incl_tax</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_shipping_incl_tax</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_customer_balance_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">customer_balance_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_customer_balance_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_customer_balance_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_customer_balance_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards</key><value xsi:type="xsd:string">a:0:{}</value></item><item><key xsi:type="xsd:string">base_gift_cards_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">gift_cards_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_gift_cards_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_gift_cards_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_test</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">order_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">shipping_address</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">customer_address_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">region_id</key><value xsi:type="xsd:string">43</value></item><item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">fax</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">region</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">postcode</key><value xsi:type="xsd:string">10025</value></item><item><key xsi:type="xsd:string">lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">street</key><value xsi:type="xsd:string">205 W 96th St #4J</value></item><item><key xsi:type="xsd:string">city</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">telephone</key><value xsi:type="xsd:string">3473710855</value></item><item><key xsi:type="xsd:string">country_id</key><value xsi:type="xsd:string">US</value></item><item><key xsi:type="xsd:string">firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">address_type</key><value xsi:type="xsd:string">shipping</value></item><item><key xsi:type="xsd:string">prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">company</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">address_id</key><value xsi:type="xsd:string">8</value></item><item><key xsi:type="xsd:string">tax_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item></value></item><item><key xsi:type="xsd:string">billing_address</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">customer_address_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">region_id</key><value xsi:type="xsd:string">43</value></item><item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">fax</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">region</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">postcode</key><value xsi:type="xsd:string">10025</value></item><item><key xsi:type="xsd:string">lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">street</key><value xsi:type="xsd:string">205 W 96th St #4J</value></item><item><key xsi:type="xsd:string">city</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">telephone</key><value xsi:type="xsd:string">3473710855</value></item><item><key xsi:type="xsd:string">country_id</key><value xsi:type="xsd:string">US</value></item><item><key xsi:type="xsd:string">firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">address_type</key><value xsi:type="xsd:string">billing</value></item><item><key xsi:type="xsd:string">prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">company</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">address_id</key><value xsi:type="xsd:string">7</value></item><item><key xsi:type="xsd:string">tax_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item></value></item><item><key xsi:type="xsd:string">items</key><value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">item_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">order_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">parent_item_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_item_id</key><value xsi:type="xsd:string">5</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">updated_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">product_id</key><value xsi:type="xsd:string">168</value></item><item><key xsi:type="xsd:string">product_type</key><value xsi:type="xsd:string">simple</value></item><item><key xsi:type="xsd:string">product_options</key><value xsi:type="xsd:string">a:5:{s:15:&quot;info_buyRequest&quot;;a:4:{s:4:&quot;uenc&quot;;s:60:&quot;aHR0cDovL2VjaG9lZC5nb3N0b3JlZ28uY29tL2JsYWNrLXdhdGNoLmh0bWw,&quot;;s:7:&quot;product&quot;;s:3:&quot;168&quot;;s:15:&quot;related_product&quot;;s:0:&quot;&quot;;s:3:&quot;qty&quot;;s:1:&quot;1&quot;;}s:17:&quot;giftcard_lifetime&quot;;N;s:22:&quot;giftcard_is_redeemable&quot;;i:0;s:23:&quot;giftcard_email_template&quot;;N;s:13:&quot;giftcard_type&quot;;N;}</value></item><item><key xsi:type="xsd:string">weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">is_virtual</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">sku</key><value xsi:type="xsd:string">11111</value></item><item><key xsi:type="xsd:string">name</key><value xsi:type="xsd:string">Black Watch</value></item><item><key xsi:type="xsd:string">description</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">applied_rule_ids</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">additional_data</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">free_shipping</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">is_qty_decimal</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">no_discount</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">qty_backordered</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">qty_canceled</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_ordered</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">qty_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_shipped</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_cost</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">original_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_original_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">tax_percent</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_percent</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">amount_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_amount_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">row_total</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_row_total</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">row_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_row_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">row_weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_available</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">base_tax_before_discount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_before_discount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">weee_tax_applied</key><value xsi:type="xsd:string">a:0:{}</value></item><item><key xsi:type="xsd:string">weee_tax_applied_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_applied_row_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_applied_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_applied_row_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_row_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_row_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">ext_order_item_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">locked_do_invoice</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">locked_do_ship</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_nominal</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">price_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_price_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">row_total_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_row_total_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item></item></value></item><item><key xsi:type="xsd:string">payment</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">base_shipping_captured</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_captured</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_authorized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_paid_online</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_refunded_online</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">amount_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_authorized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_ordered</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_ordered</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_amount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ideal_transaction_checked</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_payment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">additional_data</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_exp_month</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">cc_ss_start_year</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">echeck_bank_name</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">method</key><value xsi:type="xsd:string">cashondelivery</value></item><item><key xsi:type="xsd:string">cc_debug_request_body</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_secure_verify</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cybersource_token</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ideal_issuer_title</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">protection_eligibility</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_approval</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_last4</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_status_description</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_type</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paybox_question_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_debug_response_serialized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_ss_start_month</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">echeck_account_type</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">last_trans_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_cid_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_owner</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_type</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">ideal_issuer_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">po_number</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_exp_year</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">cc_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_routing_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">account_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">anet_trans_method</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_debug_response_body</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_ss_issue</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_account_name</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_avs_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_number_enc</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_trans_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">flo2cash_account_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paybox_request_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">address_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_raw_request</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_raw_response</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_payment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">additional_information</key><value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType"></value></item><item><key xsi:type="xsd:string">payment_id</key><value xsi:type="xsd:string">4</value></item></value></item><item><key xsi:type="xsd:string">status_history</key><value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">is_customer_notified</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">is_visible_on_front</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">comment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">status</key><value xsi:type="xsd:string">pending</value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">entity_name</key><value xsi:type="xsd:string">order</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item></item></value></item></item></multiCallReturn></ns1:multiCallResponse></env:Body></env:Envelope>

//xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Array">
     */
    def multi(session: String, order: String, product: String) =
        wrap(
            <multiCall xmlns={urn}>
                <sessionId>{session}</sessionId>
                <calls>
                    <item xsi:type="ns2:Array" enc:arraySize="2"><item>sales_order.info</item><item>{order}</item></item>
                    <item xsi:type="ns2:Array" enc:arraySize="2"><item>catalog_product.info</item><item>{product}</item></item>
                    <item xsi:type="ns2:Array" enc:arraySize="2"><item>product_media.list</item><item>{product}</item></item>
                </calls>
            </multiCall>)

    /*
        Success example:
        <env:Envelope xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:ns2="http://xml.apache.org/xml-soap" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:callResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>callReturn</rpc:result><callReturn xsi:type="ns2:Map"><item><key xsi:type="xsd:string">state</key><value xsi:type="xsd:string">new</value></item><item><key xsi:type="xsd:string">status</key><value xsi:type="xsd:string">pending</value></item><item><key xsi:type="xsd:string">coupon_code</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">protect_code</key><value xsi:type="xsd:string">786921</value></item><item><key xsi:type="xsd:string">shipping_description</key><value xsi:type="xsd:string">Flat Rate - Fixed</value></item><item><key xsi:type="xsd:string">is_virtual</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item>
        <item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">base_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_discount_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_discount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_grand_total</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_shipping_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_shipping_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_subtotal_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_subtotal_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_to_global_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">base_to_order_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">base_total_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_invoiced_cost</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_offline_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_online_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_qty_ordered</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">grand_total</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">shipping_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">shipping_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">store_to_base_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">store_to_order_rate</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">subtotal</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">subtotal_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">subtotal_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">subtotal_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_offline_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_online_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">total_qty_ordered</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">can_ship_partially</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">can_ship_partially_item</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_is_guest</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">customer_note_notify</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">billing_address_id</key><value xsi:type="xsd:string">7</value></item><item><key xsi:type="xsd:string">customer_group_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">edit_increment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">email_sent</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">forced_do_shipment_with_invoice</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">payment_authorization_expiration</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paypal_ipn_customer_notified</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_id</key><value xsi:type="xsd:string">5</value></item><item><key xsi:type="xsd:string">shipping_address_id</key><value xsi:type="xsd:string">8</value></item><item><key xsi:type="xsd:string">adjustment_negative</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">adjustment_positive</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_adjustment_negative</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_adjustment_positive</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_subtotal_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_total_due</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">payment_authorization_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">subtotal_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">total_due</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">customer_dob</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">increment_id</key><value xsi:type="xsd:string">100000004</value></item><item><key xsi:type="xsd:string">applied_rule_ids</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">base_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">customer_email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">customer_firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">customer_lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">customer_middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_taxvat</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">discount_description</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">ext_customer_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ext_order_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">global_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">hold_before_state</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hold_before_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">order_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">original_increment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_child_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_child_real_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_parent_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">relation_parent_real_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">remote_ip</key><value xsi:type="xsd:string">98.14.233.243</value></item><item><key xsi:type="xsd:string">shipping_method</key><value xsi:type="xsd:string">flatrate_flatrate</value></item><item><key xsi:type="xsd:string">store_currency_code</key><value xsi:type="xsd:string">USD</value></item><item><key xsi:type="xsd:string">store_name</key><value xsi:type="xsd:string">Main Website Main Store English</value></item><item><key xsi:type="xsd:string">x_forwarded_for</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_note</key><value xsi:nil="true"></value></item>
        <item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">updated_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">total_item_count</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">customer_gender</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_rate</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_percent</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">custbalance_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_base_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">real_order_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">currency_code</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_multi_payment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tracking_numbers</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_hold</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_custbalance_amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">shipping_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_shipping_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_incl_tax</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_shipping_incl_tax</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">base_customer_balance_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">customer_balance_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_customer_balance_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_customer_balance_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_customer_balance_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_balance_total_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards</key><value xsi:type="xsd:string">a:0:{}</value></item><item><key xsi:type="xsd:string">base_gift_cards_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">gift_cards_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_gift_cards_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_gift_cards_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_cards_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_test</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">order_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">shipping_address</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">customer_address_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">region_id</key><value xsi:type="xsd:string">43</value></item><item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">fax</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">region</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">postcode</key><value xsi:type="xsd:string">10025</value></item><item><key xsi:type="xsd:string">lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">street</key><value xsi:type="xsd:string">205 W 96th St #4J</value></item><item><key xsi:type="xsd:string">city</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">telephone</key><value xsi:type="xsd:string">3473710855</value></item><item><key xsi:type="xsd:string">country_id</key><value xsi:type="xsd:string">US</value></item><item><key xsi:type="xsd:string">firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">address_type</key><value xsi:type="xsd:string">shipping</value></item><item><key xsi:type="xsd:string">prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">company</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">address_id</key><value xsi:type="xsd:string">8</value></item><item><key xsi:type="xsd:string">tax_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item></value></item><item><key xsi:type="xsd:string">billing_address</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">customer_address_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">quote_address_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">region_id</key><value xsi:type="xsd:string">43</value></item><item><key xsi:type="xsd:string">customer_id</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">fax</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">region</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">postcode</key><value xsi:type="xsd:string">10025</value></item><item><key xsi:type="xsd:string">lastname</key><value xsi:type="xsd:string">Pflueger</value></item><item><key xsi:type="xsd:string">street</key><value xsi:type="xsd:string">205 W 96th St #4J</value></item><item><key xsi:type="xsd:string">city</key><value xsi:type="xsd:string">New York</value></item><item><key xsi:type="xsd:string">email</key><value xsi:type="xsd:string">mpflueger@echoed.com</value></item><item><key xsi:type="xsd:string">telephone</key><value xsi:type="xsd:string">3473710855</value></item><item><key xsi:type="xsd:string">country_id</key><value xsi:type="xsd:string">US</value></item><item><key xsi:type="xsd:string">firstname</key><value xsi:type="xsd:string">Matthew</value></item><item><key xsi:type="xsd:string">address_type</key><value xsi:type="xsd:string">billing</value></item><item><key xsi:type="xsd:string">prefix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">middlename</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">suffix</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">company</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">address_id</key><value xsi:type="xsd:string">7</value></item><item><key xsi:type="xsd:string">tax_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item></value></item><item><key xsi:type="xsd:string">items</key><value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">item_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">order_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">parent_item_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_item_id</key><value xsi:type="xsd:string">5</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">updated_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item>
        <item><key xsi:type="xsd:string">product_id</key><value xsi:type="xsd:string">168</value></item><item><key xsi:type="xsd:string">product_type</key><value xsi:type="xsd:string">simple</value></item><item><key xsi:type="xsd:string">product_options</key><value xsi:type="xsd:string">a:5:{s:15:&quot;info_buyRequest&quot;;a:4:{s:4:&quot;uenc&quot;;s:60:&quot;aHR0cDovL2VjaG9lZC5nb3N0b3JlZ28uY29tL2JsYWNrLXdhdGNoLmh0bWw,&quot;;s:7:&quot;product&quot;;s:3:&quot;168&quot;;s:15:&quot;related_product&quot;;s:0:&quot;&quot;;s:3:&quot;qty&quot;;s:1:&quot;1&quot;;}s:17:&quot;giftcard_lifetime&quot;;N;s:22:&quot;giftcard_is_redeemable&quot;;i:0;s:23:&quot;giftcard_email_template&quot;;N;s:13:&quot;giftcard_type&quot;;N;}</value></item><item><key xsi:type="xsd:string">weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">is_virtual</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">sku</key><value xsi:type="xsd:string">11111</value></item>
        <item><key xsi:type="xsd:string">name</key><value xsi:type="xsd:string">Black Watch</value></item>
        <item><key xsi:type="xsd:string">description</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">applied_rule_ids</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">additional_data</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">free_shipping</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">is_qty_decimal</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">no_discount</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">qty_backordered</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">qty_canceled</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_ordered</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">qty_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">qty_shipped</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_cost</key><value xsi:nil="true"></value></item>
        <item><key xsi:type="xsd:string">price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">original_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_original_price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">tax_percent</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">tax_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_tax_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_percent</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">discount_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_discount_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">amount_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_amount_refunded</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">row_total</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_row_total</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">row_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_row_invoiced</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">row_weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">gift_message_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_available</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">base_tax_before_discount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_before_discount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">weee_tax_applied</key><value xsi:type="xsd:string">a:0:{}</value></item><item><key xsi:type="xsd:string">weee_tax_applied_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_applied_row_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_applied_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_applied_row_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">weee_tax_row_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_weee_tax_row_disposition</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">ext_order_item_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">locked_do_invoice</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">locked_do_ship</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">base_hidden_tax_amount</key><value xsi:type="xsd:string">0.0000</value></item><item><key xsi:type="xsd:string">hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_invoiced</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_hidden_tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">is_nominal</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">hidden_tax_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tax_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">price_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_price_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">row_total_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">base_row_total_incl_tax</key><value xsi:type="xsd:string">100.0000</value></item></item></value></item><item><key xsi:type="xsd:string">payment</key><value xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">base_shipping_captured</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_captured</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_authorized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_paid_online</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_refunded_online</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">shipping_amount</key><value xsi:type="xsd:string">5.0000</value></item><item><key xsi:type="xsd:string">amount_paid</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_authorized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_ordered</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">shipping_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">base_amount_refunded</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount_ordered</key><value xsi:type="xsd:string">105.0000</value></item><item><key xsi:type="xsd:string">base_amount_canceled</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ideal_transaction_checked</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">quote_payment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">additional_data</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_exp_month</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">cc_ss_start_year</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">echeck_bank_name</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">method</key><value xsi:type="xsd:string">cashondelivery</value></item><item><key xsi:type="xsd:string">cc_debug_request_body</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_secure_verify</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cybersource_token</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">ideal_issuer_title</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">protection_eligibility</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_approval</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_last4</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_status_description</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_type</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paybox_question_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_debug_response_serialized</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_ss_start_month</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">echeck_account_type</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">last_trans_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_cid_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_owner</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_type</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">ideal_issuer_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">po_number</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_exp_year</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">cc_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_routing_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">account_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">anet_trans_method</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_debug_response_body</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_ss_issue</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">echeck_account_name</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_avs_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_number_enc</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">cc_trans_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">flo2cash_account_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">paybox_request_number</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">address_status</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_raw_request</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cc_raw_response</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">customer_payment_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">amount</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">additional_information</key><value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType"></value></item><item><key xsi:type="xsd:string">payment_id</key><value xsi:type="xsd:string">4</value></item></value></item><item><key xsi:type="xsd:string">status_history</key><value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">parent_id</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">is_customer_notified</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">is_visible_on_front</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">comment</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">status</key><value xsi:type="xsd:string">pending</value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-21 02:05:05</value></item><item><key xsi:type="xsd:string">entity_name</key><value xsi:type="xsd:string">order</value></item><item><key xsi:type="xsd:string">store_id</key><value xsi:type="xsd:string">1</value></item></item></value></item></callReturn></ns1:callResponse></env:Body></env:Envelope>

        Failure example:
    */
    def order(session: String, order: String) =
        wrap(
            <call>
                <sessionId>{session}</sessionId>
                <resourcePath>sales_order.info</resourcePath>
                <args>{order}</args>
            </call>)

    /*
        Success example:
        <env:Envelope xmlns:ns2="http://xml.apache.org/xml-soap" xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:callResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>callReturn</rpc:result><callReturn xsi:type="ns2:Map"><item><key xsi:type="xsd:string">product_id</key><value xsi:type="xsd:string">168</value></item><item><key xsi:type="xsd:string">sku</key><value xsi:type="xsd:string">11111</value></item><item><key xsi:type="xsd:string">set</key><value xsi:type="xsd:string">9</value></item><item><key xsi:type="xsd:string">type</key><value xsi:type="xsd:string">simple</value></item><item><key xsi:type="xsd:string">categories</key><value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType"></value></item><item><key xsi:type="xsd:string">websites</key><value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="xsd:string"><item xsi:type="xsd:string">1</item></value></item><item><key xsi:type="xsd:string">type_id</key><value xsi:type="xsd:string">simple</value></item><item><key xsi:type="xsd:string">name</key><value xsi:type="xsd:string">Black Watch</value></item><item><key xsi:type="xsd:string">weight</key><value xsi:type="xsd:string">1.0000</value></item><item><key xsi:type="xsd:string">status</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">tax_class_id</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">url_key</key><value xsi:type="xsd:string">black-watch</value></item><item><key xsi:type="xsd:string">visibility</key><value xsi:type="xsd:string">4</value></item><item><key xsi:type="xsd:string">old_id</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">gift_message_available</key><value xsi:type="xsd:string">2</value></item><item><key xsi:type="xsd:string">manufacturer</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">color</key><value xsi:type="xsd:string">24</value></item><item><key xsi:type="xsd:string">url_path</key><value xsi:type="xsd:string">black-watch.html</value></item><item><key xsi:type="xsd:string">news_from_date</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">news_to_date</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">required_options</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">has_options</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">image_label</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">small_image_label</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">thumbnail_label</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">created_at</key><value xsi:type="xsd:string">2012-05-15 19:56:22</value></item><item><key xsi:type="xsd:string">updated_at</key><value xsi:type="xsd:string">2012-05-15 19:56:22</value></item><item><key xsi:type="xsd:string">is_imported</key><value xsi:type="xsd:string">0</value></item><item><key xsi:type="xsd:string">price</key><value xsi:type="xsd:string">100.0000</value></item><item><key xsi:type="xsd:string">minimal_price</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">cost</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">tier_price</key><value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType"></value></item><item><key xsi:type="xsd:string">special_price</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">special_from_date</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">special_to_date</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">enable_googlecheckout</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">meta_title</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">meta_keyword</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">meta_description</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">description</key><value xsi:type="xsd:string">This is an amazing black watch</value></item><item><key xsi:type="xsd:string">short_description</key><value xsi:type="xsd:string">This is an amazing black watch</value></item><item><key xsi:type="xsd:string">custom_design</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">custom_design_from</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">custom_design_to</key><value xsi:nil="true"></value></item><item><key xsi:type="xsd:string">custom_layout_update</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">options_container</key><value xsi:type="xsd:string">container2</value></item><item><key xsi:type="xsd:string">page_layout</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">designeditor_theme_id</key><value xsi:type="xsd:string">0</value></item></callReturn></ns1:callResponse></env:Body></env:Envelope>

        Failure example:
    */
    def product(session: String, product: String) =
        wrap(
            <call>
                <sessionId>{session}</sessionId>
                <resourcePath>catalog_product.info</resourcePath>
                <args>{product}</args>
            </call>)


    /*
        Success example:
        <env:Envelope xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:ns2="http://xml.apache.org/xml-soap" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:callResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>callReturn</rpc:result><callReturn xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map"><item xsi:type="ns2:Map"><item><key xsi:type="xsd:string">file</key><value xsi:type="xsd:string">/h/t/htc-touch-diamond.jpg</value></item><item><key xsi:type="xsd:string">label</key><value xsi:type="xsd:string"></value></item><item><key xsi:type="xsd:string">position</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">exclude</key><value xsi:type="xsd:string">1</value></item><item><key xsi:type="xsd:string">url</key><value xsi:type="xsd:string">http://echoed.gostorego.com/media/s4/fb/2b/23/74/83/e8/catalog/product/h/t/htc-touch-diamond.jpg</value></item><item><key xsi:type="xsd:string">types</key><value xsi:type="enc:Array" enc:arraySize="3" enc:itemType="xsd:string"><item xsi:type="xsd:string">thumbnail</item><item xsi:type="xsd:string">small_image</item><item xsi:type="xsd:string">image</item></value></item></item></callReturn></ns1:callResponse></env:Body></env:Envelope>

        Failure example:
        <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body><env:Fault><env:Code><env:Value>103</env:Value></env:Code><env:Reason><env:Text>Requested image not exists in product images' gallery.</env:Text></env:Reason></env:Fault></env:Body></env:Envelope>

        No images example:
        <env:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope"><env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc"><ns1:callResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><rpc:result>callReturn</rpc:result><callReturn xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType"></callReturn></ns1:callResponse></env:Body></env:Envelope>
    */
    def image(session: String, product: String) =
        wrap(
            <call>
                <sessionId>{session}</sessionId>
                <resourcePath>product_media.list</resourcePath>
                <args>{product}</args>
            </call>)


    def wrap(body: NodeSeq, userToken: Option[String] = None): Request = {
        val content =
            <env:Envelope
                    xmlns="http://schemas.xmlsoap.org/wsdl/"
                    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                    xmlns:enc="http://schemas.xmlsoap.org/soap/encoding/"
                    xmlns:env="http://www.w3.org/2003/05/soap-envelope"
                    xmlns:ns1={urn}
                    xmlns:ns2="http://xml.apache.org/xml-soap">
                <env:Body>
                    {body}
                </env:Body>
            </env:Envelope>

        endpoint.<<(content.toString, "application/soap+xml; charset=utf-8")
    }


    h(login(username, apiKey) <> { res =>
        println("login: %s" format res)
    } >! {
        case e => println("Received login error!: %s" format e)
    })

//    h(order("854649a5e644fdd6ad84872f0967067a", "100000004") <> { res =>
//        println("order: %s" format res)
//    } >! {
//        case e => println("Received order error!: %s" format e)
//    })


//    h(product("854649a5e644fdd6ad84872f0967067a", "168") <> { res =>
//        println("product: %s" format res)
//    } >! {
//        case e => println("Received product error!: %s" format e)
//    })


//    h(image("854649a5e644fdd6ad84872f0967067a", "168") <> { res =>
//        println("image: %s" format res)
//    } >! {
//        case e => println("Received image error!: %s" format e)
//    })


//    val ns = <foo id="bar"/>
//    println("%s" format ns \ "@id")


    import collection.mutable.{Map => MMap}
    import collection.mutable.{Buffer => MList}

    def value(node: Node) = {
        val valueType = node.attributes.value.toString

        if (valueType.endsWith("Map")) asMap(node)
        else if (valueType.endsWith("Array")) asList(node)
        else node.text
    }

    def asList(node: Node): List[Any] = {
        val list = MList[Any]()
        node.child.foreach { i =>
            list.append(value(i))
        }
        list.toList
    }

    def asMap(node: Node): Map[String, Any] = {
        val map = MMap[String, Any]()
        node.child.foreach { i =>
            val key = (i \ "key").head.text
            map(key) = value((i \ "value").head)
        }
        map.toMap
    }

    h(multi("033acd1206a104b2dffc9f3b9f33ae3a", "100000004", "166") <> { res =>
        val list = asList((res \\ "multiCallReturn").head)
        println("Converted multiCall into list %s" format list)
//        (res \\ "multiCallReturn" \ "item").foreach { i =>
//            println("Array item %s\n\n\n\n" format i)
//            println("Item is of type %s\n\n\n\n" format i.attributes.value)
////            println("Attribute type is %s" format i.attributes.foreach("_.key"%s"))
//            //of type %s and length %s\n\n%s\n\n\n\n" format(i \ "@xsi:type", i.length, i)))
//        }
    } >! {
        case e => println("Received mulit error!: %s" format e)
    })


//    var nodeTest = <test>test</test>
//    println("nodeTest.child %s" format nodeTest.child)
//    nodeTest = nodeTest.copy(child = Text("hello")) //<hello>hello</hello>)
//    println("nodeTest: %s" format nodeTest)

    //println("Sleeping for 5 seconds")
    Thread.sleep(5000)
    //println("Exiting")
    h.shutdown



}

/*

<env:Envelope xmlns:ns2="http://xml.apache.org/xml-soap" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:enc="http://www.w3.org/2003/05/soap-encoding" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:ns1="urn:Magento" xmlns:env="http://www.w3.org/2003/05/soap-envelope">
  <env:Body xmlns:rpc="http://www.w3.org/2003/05/soap-rpc">
    <ns1:multiCallResponse env:encodingStyle="http://www.w3.org/2003/05/soap-encoding">
      <rpc:result>multiCallReturn</rpc:result>
      <multiCallReturn xsi:type="ns1:FixedArray" enc:arraySize="3" enc:itemType="xsd:anyType">
        <item xsi:type="ns2:Map">
          <item>
            <key xsi:type="xsd:string">state</key>
            <value xsi:type="xsd:string">new</value>
          </item>
          <item>
            <key xsi:type="xsd:string">status</key>
            <value xsi:type="xsd:string">pending</value>
          </item>
          <item>
            <key xsi:type="xsd:string">coupon_code</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">protect_code</key>
            <value xsi:type="xsd:string">786921</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_description</key>
            <value xsi:type="xsd:string">Flat Rate - Fixed</value>
          </item>
          <item>
            <key xsi:type="xsd:string">is_virtual</key>
            <value xsi:type="xsd:string">0</value>
          </item>
          <item>
            <key xsi:type="xsd:string">store_id</key>
            <value xsi:type="xsd:string">1</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_id</key>
            <value xsi:type="xsd:string">2</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_discount_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_discount_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_discount_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_discount_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_grand_total</key>
            <value xsi:type="xsd:string">105.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_amount</key>
            <value xsi:type="xsd:string">5.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_subtotal</key>
            <value xsi:type="xsd:string">100.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_subtotal_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_subtotal_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_subtotal_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_tax_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_tax_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_to_global_rate</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_to_order_rate</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_total_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_invoiced_cost</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_offline_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_online_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_paid</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_qty_ordered</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_total_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">discount_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">discount_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">discount_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">discount_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">grand_total</key>
            <value xsi:type="xsd:string">105.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_amount</key>
            <value xsi:type="xsd:string">5.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">shipping_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">shipping_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">shipping_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">store_to_base_rate</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">store_to_order_rate</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">subtotal</key>
            <value xsi:type="xsd:string">100.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">subtotal_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">subtotal_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">subtotal_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">tax_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">tax_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_canceled</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_offline_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_online_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_paid</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">total_qty_ordered</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">total_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">can_ship_partially</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">can_ship_partially_item</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_is_guest</key>
            <value xsi:type="xsd:string">0</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_note_notify</key>
            <value xsi:type="xsd:string">1</value>
          </item>
          <item>
            <key xsi:type="xsd:string">billing_address_id</key>
            <value xsi:type="xsd:string">7</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_group_id</key>
            <value xsi:type="xsd:string">1</value>
          </item>
          <item>
            <key xsi:type="xsd:string">edit_increment</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">email_sent</key>
            <value xsi:type="xsd:string">1</value>
          </item>
          <item>
            <key xsi:type="xsd:string">forced_do_shipment_with_invoice</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">gift_message_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">payment_authorization_expiration</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">paypal_ipn_customer_notified</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">quote_address_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">quote_id</key>
            <value xsi:type="xsd:string">5</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_address_id</key>
            <value xsi:type="xsd:string">8</value>
          </item>
          <item>
            <key xsi:type="xsd:string">adjustment_negative</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">adjustment_positive</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_adjustment_negative</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_adjustment_positive</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_discount_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_subtotal_incl_tax</key>
            <value xsi:type="xsd:string">100.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_total_due</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">payment_authorization_amount</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">shipping_discount_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">subtotal_incl_tax</key>
            <value xsi:type="xsd:string">100.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">total_due</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">weight</key>
            <value xsi:type="xsd:string">1.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_dob</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">increment_id</key>
            <value xsi:type="xsd:string">100000004</value>
          </item>
          <item>
            <key xsi:type="xsd:string">applied_rule_ids</key>
            <value xsi:type="xsd:string">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_currency_code</key>
            <value xsi:type="xsd:string">USD</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_email</key>
            <value xsi:type="xsd:string">mpflueger@echoed.com</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_firstname</key>
            <value xsi:type="xsd:string">Matthew</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_lastname</key>
            <value xsi:type="xsd:string">Pflueger</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_middlename</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_prefix</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_suffix</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_taxvat</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">discount_description</key>
            <value xsi:type="xsd:string">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">ext_customer_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">ext_order_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">global_currency_code</key>
            <value xsi:type="xsd:string">USD</value>
          </item>
          <item>
            <key xsi:type="xsd:string">hold_before_state</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">hold_before_status</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">order_currency_code</key>
            <value xsi:type="xsd:string">USD</value>
          </item>
          <item>
            <key xsi:type="xsd:string">original_increment_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">relation_child_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">relation_child_real_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">relation_parent_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">relation_parent_real_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">remote_ip</key>
            <value xsi:type="xsd:string">98.14.233.243</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_method</key>
            <value xsi:type="xsd:string">flatrate_flatrate</value>
          </item>
          <item>
            <key xsi:type="xsd:string">store_currency_code</key>
            <value xsi:type="xsd:string">USD</value>
          </item>
          <item>
            <key xsi:type="xsd:string">store_name</key>
            <value xsi:type="xsd:string">Main Website Main Store English</value>
          </item>
          <item>
            <key xsi:type="xsd:string">x_forwarded_for</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_note</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">created_at</key>
            <value xsi:type="xsd:string">2012-05-21 02:05:05</value>
          </item>
          <item>
            <key xsi:type="xsd:string">updated_at</key>
            <value xsi:type="xsd:string">2012-05-21 02:05:05</value>
          </item>
          <item>
            <key xsi:type="xsd:string">total_item_count</key>
            <value xsi:type="xsd:string">1</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_gender</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">currency_rate</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">tax_percent</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">custbalance_amount</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">currency_base_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">real_order_id</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">currency_code</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">is_multi_payment</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">tracking_numbers</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">is_hold</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_custbalance_amount</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">hidden_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_hidden_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_hidden_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_hidden_tax_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">hidden_tax_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_hidden_tax_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">hidden_tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_hidden_tax_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">shipping_incl_tax</key>
            <value xsi:type="xsd:string">5.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_shipping_incl_tax</key>
            <value xsi:type="xsd:string">5.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_customer_balance_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">customer_balance_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_customer_balance_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_balance_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_customer_balance_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_balance_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_customer_balance_total_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">customer_balance_total_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">gift_cards</key>
            <value xsi:type="xsd:string">a:0:{}</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_gift_cards_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">gift_cards_amount</key>
            <value xsi:type="xsd:string">0.0000</value>
          </item>
          <item>
            <key xsi:type="xsd:string">base_gift_cards_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">gift_cards_invoiced</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">base_gift_cards_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">gift_cards_refunded</key>
            <value xsi:nil="true">
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">is_test</key>
            <value xsi:type="xsd:string">0</value>
          </item>
          <item>
            <key xsi:type="xsd:string">order_id</key>
            <value xsi:type="xsd:string">4</value>
          </item>
          <item>
            <key xsi:type="xsd:string">shipping_address</key>
            <value xsi:type="ns2:Map">
              <item>
                <key xsi:type="xsd:string">parent_id</key>
                <value xsi:type="xsd:string">4</value>
              </item>
              <item>
                <key xsi:type="xsd:string">customer_address_id</key>
                <value xsi:type="xsd:string">1</value>
              </item>
              <item>
                <key xsi:type="xsd:string">quote_address_id</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">region_id</key>
                <value xsi:type="xsd:string">43</value>
              </item>
              <item>
                <key xsi:type="xsd:string">customer_id</key>
                <value xsi:type="xsd:string">2</value>
              </item>
              <item>
                <key xsi:type="xsd:string">fax</key>
                <value xsi:type="xsd:string">mpflueger@echoed.com</value>
              </item>
              <item>
                <key xsi:type="xsd:string">region</key>
                <value xsi:type="xsd:string">New York</value>
              </item>
              <item>
                <key xsi:type="xsd:string">postcode</key>
                <value xsi:type="xsd:string">10025</value>
              </item>
              <item>
                <key xsi:type="xsd:string">lastname</key>
                <value xsi:type="xsd:string">Pflueger</value>
              </item>
              <item>
                <key xsi:type="xsd:string">street</key>
                <value xsi:type="xsd:string">205 W 96th St #4J</value>
              </item>
              <item>
                <key xsi:type="xsd:string">city</key>
                <value xsi:type="xsd:string">New York</value>
              </item>
              <item>
                <key xsi:type="xsd:string">email</key>
                <value xsi:type="xsd:string">mpflueger@echoed.com</value>
              </item>
              <item>
                <key xsi:type="xsd:string">telephone</key>
                <value xsi:type="xsd:string">3473710855</value>
              </item>
              <item>
                <key xsi:type="xsd:string">country_id</key>
                <value xsi:type="xsd:string">US</value>
              </item>
              <item>
                <key xsi:type="xsd:string">firstname</key>
                <value xsi:type="xsd:string">Matthew</value>
              </item>
              <item>
                <key xsi:type="xsd:string">address_type</key>
                <value xsi:type="xsd:string">shipping</value>
              </item>
              <item>
                <key xsi:type="xsd:string">prefix</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">middlename</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">suffix</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">company</key>
                <value xsi:type="xsd:string">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">address_id</key>
                <value xsi:type="xsd:string">8</value>
              </item>
              <item>
                <key xsi:type="xsd:string">tax_id</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">gift_message_id</key>
                <value xsi:nil="true">
              </value>
            </item>
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">billing_address</key>
            <value xsi:type="ns2:Map">
              <item>
                <key xsi:type="xsd:string">parent_id</key>
                <value xsi:type="xsd:string">4</value>
              </item>
              <item>
                <key xsi:type="xsd:string">customer_address_id</key>
                <value xsi:type="xsd:string">1</value>
              </item>
              <item>
                <key xsi:type="xsd:string">quote_address_id</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">region_id</key>
                <value xsi:type="xsd:string">43</value>
              </item>
              <item>
                <key xsi:type="xsd:string">customer_id</key>
                <value xsi:type="xsd:string">2</value>
              </item>
              <item>
                <key xsi:type="xsd:string">fax</key>
                <value xsi:type="xsd:string">mpflueger@echoed.com</value>
              </item>
              <item>
                <key xsi:type="xsd:string">region</key>
                <value xsi:type="xsd:string">New York</value>
              </item>
              <item>
                <key xsi:type="xsd:string">postcode</key>
                <value xsi:type="xsd:string">10025</value>
              </item>
              <item>
                <key xsi:type="xsd:string">lastname</key>
                <value xsi:type="xsd:string">Pflueger</value>
              </item>
              <item>
                <key xsi:type="xsd:string">street</key>
                <value xsi:type="xsd:string">205 W 96th St #4J</value>
              </item>
              <item>
                <key xsi:type="xsd:string">city</key>
                <value xsi:type="xsd:string">New York</value>
              </item>
              <item>
                <key xsi:type="xsd:string">email</key>
                <value xsi:type="xsd:string">mpflueger@echoed.com</value>
              </item>
              <item>
                <key xsi:type="xsd:string">telephone</key>
                <value xsi:type="xsd:string">3473710855</value>
              </item>
              <item>
                <key xsi:type="xsd:string">country_id</key>
                <value xsi:type="xsd:string">US</value>
              </item>
              <item>
                <key xsi:type="xsd:string">firstname</key>
                <value xsi:type="xsd:string">Matthew</value>
              </item>
              <item>
                <key xsi:type="xsd:string">address_type</key>
                <value xsi:type="xsd:string">billing</value>
              </item>
              <item>
                <key xsi:type="xsd:string">prefix</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">middlename</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">suffix</key>
                <value xsi:nil="true">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">company</key>
                <value xsi:type="xsd:string">
              </value>
            </item>
              <item>
                <key xsi:type="xsd:string">address_id</key>
                <value xsi:type="xsd:string">7</value>
              </item>
              <item>
                <key xsi:type="xsd:string">tax_id</key>
                <value xsi:nil="true"> </value>
              </item>
              <item>
                <key xsi:type="xsd:string">gift_message_id</key>
                <value xsi:nil="true"> </value>
              </item>
          </value>
        </item>
          <item>
            <key xsi:type="xsd:string">items</key>
            <value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map">
              <item xsi:type="ns2:Map">
                <item>
                  <key xsi:type="xsd:string">item_id</key>
                  <value xsi:type="xsd:string">4</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">order_id</key>
                  <value xsi:type="xsd:string">4</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">parent_item_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">quote_item_id</key>
                  <value xsi:type="xsd:string">5</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">store_id</key>
                  <value xsi:type="xsd:string">1</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">created_at</key>
                  <value xsi:type="xsd:string">2012-05-21 02:05:05</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">updated_at</key>
                  <value xsi:type="xsd:string">2012-05-21 02:05:05</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">product_id</key>
                  <value xsi:type="xsd:string">168</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">product_type</key>
                  <value xsi:type="xsd:string">simple</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">product_options</key>
                  <value xsi:type="xsd:string">a:5:{s:15:&quot;info_buyRequest&quot;;a:4:{s:4:&quot;uenc&quot;;s:60:&quot;aHR0cDovL2VjaG9lZC5nb3N0b3JlZ28uY29tL2JsYWNrLXdhdGNoLmh0bWw,&quot;;s:7:&quot;product&quot;;s:3:&quot;168&quot;;s:15:&quot;related_product&quot;;s:0:&quot;&quot;;s:3:&quot;qty&quot;;s:1:&quot;1&quot;;}s:17:&quot;giftcard_lifetime&quot;;N;s:22:&quot;giftcard_is_redeemable&quot;;i:0;s:23:&quot;giftcard_email_template&quot;;N;s:13:&quot;giftcard_type&quot;;N;}</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weight</key>
                  <value xsi:type="xsd:string">1.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">is_virtual</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">sku</key>
                  <value xsi:type="xsd:string">11111</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">name</key>
                  <value xsi:type="xsd:string">Black Watch</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">description</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">applied_rule_ids</key>
                  <value xsi:type="xsd:string"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">additional_data</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">free_shipping</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">is_qty_decimal</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">no_discount</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_backordered</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_canceled</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_ordered</key>
                  <value xsi:type="xsd:string">1.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_refunded</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">qty_shipped</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_cost</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">price</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_price</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">original_price</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_original_price</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_percent</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_tax_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_tax_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">discount_percent</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">discount_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_discount_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">discount_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_discount_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">amount_refunded</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_refunded</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">row_total</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_row_total</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">row_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_row_invoiced</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">row_weight</key>
                  <value xsi:type="xsd:string">1.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">gift_message_id</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">gift_message_available</key>
                  <value xsi:type="xsd:string">2</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_tax_before_discount</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_before_discount</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weee_tax_applied</key>
                  <value xsi:type="xsd:string">a:0:{}</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weee_tax_applied_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weee_tax_applied_row_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_weee_tax_applied_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_weee_tax_applied_row_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weee_tax_disposition</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weee_tax_row_disposition</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_weee_tax_disposition</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_weee_tax_row_disposition</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">ext_order_item_id</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">locked_do_invoice</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">locked_do_ship</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">hidden_tax_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_hidden_tax_amount</key>
                  <value xsi:type="xsd:string">0.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">hidden_tax_invoiced</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_hidden_tax_invoiced</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">hidden_tax_refunded</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_hidden_tax_refunded</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">is_nominal</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_canceled</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">hidden_tax_canceled</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_refunded</key>
                  <value xsi:nil="true"> </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">price_incl_tax</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_price_incl_tax</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">row_total_incl_tax</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_row_total_incl_tax</key>
                  <value xsi:type="xsd:string">100.0000</value>
                </item>
              </item>
              </value>
            </item>
            <item>
              <key xsi:type="xsd:string">payment</key>
              <value xsi:type="ns2:Map">
                <item>
                  <key xsi:type="xsd:string">parent_id</key>
                  <value xsi:type="xsd:string">4</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_shipping_captured</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">shipping_captured</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">amount_refunded</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_paid</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">amount_canceled</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_authorized</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_paid_online</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_refunded_online</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_shipping_amount</key>
                  <value xsi:type="xsd:string">5.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">shipping_amount</key>
                  <value xsi:type="xsd:string">5.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">amount_paid</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">amount_authorized</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_ordered</key>
                  <value xsi:type="xsd:string">105.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_shipping_refunded</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">shipping_refunded</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_refunded</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">amount_ordered</key>
                  <value xsi:type="xsd:string">105.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">base_amount_canceled</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">ideal_transaction_checked</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">quote_payment_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">additional_data</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_exp_month</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">cc_ss_start_year</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">echeck_bank_name</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">method</key>
                  <value xsi:type="xsd:string">cashondelivery</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">cc_debug_request_body</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_secure_verify</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cybersource_token</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">ideal_issuer_title</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">protection_eligibility</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_approval</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_last4</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_status_description</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">echeck_type</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">paybox_question_number</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_debug_response_serialized</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_ss_start_month</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">echeck_account_type</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">last_trans_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_cid_status</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_owner</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_type</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">ideal_issuer_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">po_number</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_exp_year</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">cc_status</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">echeck_routing_number</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">account_status</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">anet_trans_method</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_debug_response_body</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_ss_issue</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">echeck_account_name</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_avs_status</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_number_enc</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_trans_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">flo2cash_account_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">paybox_request_number</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">address_status</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_raw_request</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">cc_raw_response</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">customer_payment_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">amount</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">additional_information</key>
                  <value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">payment_id</key>
                  <value xsi:type="xsd:string">4</value>
                </item>
              </value>
            </item>
            <item>
              <key xsi:type="xsd:string">status_history</key>
              <value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map">
                <item xsi:type="ns2:Map">
                  <item>
                    <key xsi:type="xsd:string">parent_id</key>
                    <value xsi:type="xsd:string">4</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">is_customer_notified</key>
                    <value xsi:type="xsd:string">1</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">is_visible_on_front</key>
                    <value xsi:type="xsd:string">0</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">comment</key>
                    <value xsi:nil="true">
                  </value>
                </item>
                  <item>
                    <key xsi:type="xsd:string">status</key>
                    <value xsi:type="xsd:string">pending</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">created_at</key>
                    <value xsi:type="xsd:string">2012-05-21 02:05:05</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">entity_name</key>
                    <value xsi:type="xsd:string">order</value>
                  </item>
                  <item>
                    <key xsi:type="xsd:string">store_id</key>
                    <value xsi:type="xsd:string">1</value>
                  </item></item>
                </value>
              </item></item>
              <item xsi:type="ns2:Map">
                <item>
                  <key xsi:type="xsd:string">product_id</key>
                  <value xsi:type="xsd:string">166</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">sku</key>
                  <value xsi:type="xsd:string">HTC Touch Diamond</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">set</key>
                  <value xsi:type="xsd:string">38</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">type</key>
                  <value xsi:type="xsd:string">simple</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">categories</key>
                  <value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="xsd:string">
                    <item xsi:type="xsd:string">8</item>
                  </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">websites</key>
                  <value xsi:type="enc:Array" enc:arraySize="1" enc:itemType="xsd:string">
                    <item xsi:type="xsd:string">1</item>
                  </value>
                </item>
                <item>
                  <key xsi:type="xsd:string">type_id</key>
                  <value xsi:type="xsd:string">simple</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">name</key>
                  <value xsi:type="xsd:string">HTC Touch Diamond</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">model</key>
                  <value xsi:type="xsd:string">HTC Touch Diamond</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">weight</key>
                  <value xsi:type="xsd:string">0.3000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">manufacturer</key>
                  <value xsi:type="xsd:string">122</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">old_id</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">status</key>
                  <value xsi:type="xsd:string">1</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">tax_class_id</key>
                  <value xsi:type="xsd:string">2</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">url_key</key>
                  <value xsi:type="xsd:string">htc-touch-diamond</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">visibility</key>
                  <value xsi:type="xsd:string">4</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">gift_message_available</key>
                  <value xsi:type="xsd:string">2</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">news_from_date</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">news_to_date</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">url_path</key>
                  <value xsi:type="xsd:string">htc-touch-diamond.html</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">required_options</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">has_options</key>
                  <value xsi:type="xsd:string">0</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">image_label</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">small_image_label</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">thumbnail_label</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">created_at</key>
                  <value xsi:type="xsd:string">2008-07-25 02:22:13</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">updated_at</key>
                  <value xsi:type="xsd:string">2008-07-25 02:22:44</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">is_imported</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">price</key>
                  <value xsi:type="xsd:string">750.0000</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">cost</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">tier_price</key>
                  <value xsi:type="enc:Array" enc:arraySize="0" enc:itemType="xsd:anyType">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">special_price</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">minimal_price</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">special_from_date</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">special_to_date</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">enable_googlecheckout</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">meta_title</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">meta_keyword</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">meta_description</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">short_description</key>
                  <value xsi:type="xsd:string">Re-defining the perception of advanced mobile phones… the HTC Touch Diamond™ signals a giant leap forward in combining hi-tech prowess with intuitive usability and exhilarating design.</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">description</key>
                  <value xsi:type="xsd:string">Re-defining the perception of advanced mobile phones… the HTC Touch Diamond™ signals a giant leap forward in combining hi-tech prowess with intuitive usability and exhilarating design.

Featuring a sharp 2.8-inch touch screen housed within a stunning formation of brushed metal and flawless faceted edges, the HTC Touch Diamond is as beautiful to behold as it is to use.

With HTC’s vibrant touch-responsive user interface, TouchFLO™ 3D, and ultra-fast HSDPA internet connectivity… the HTC Touch Diamond offers a rich online experience to rival a notebook computer, allowing you to interact with Google, YouTube, and Wikipedia as freely as you would with a broadband connection.

Your contacts, favourite music, videos and photos are no longer an uninspired line of text. With TouchFLO 3D, album artwork, video stills and snapshots of your friends’ and family’s faces are brought to life for you to interact, play and launch at your fingertips.

A 3.2 megapixel auto-focus camera will help you capture the perfect moment in style and with a massive 4GB of internal storage you can keep all the files you need. The integrated ultra-sensitive GPS will help you find your destination as quickly and efficiently as a dedicated satellite navigation unit.

Style and substance in a phone are no longer mutually exclusive. The HTC Touch Diamond has arrived.</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">in_depth</key>
                  <value xsi:type="xsd:string">Re-defining the perception of advanced mobile phones… the HTC Touch Diamond™ signals a giant leap forward in combining hi-tech prowess with intuitive usability and exhilarating design.

Featuring a sharp 2.8-inch touch screen housed within a stunning formation of brushed metal and flawless faceted edges, the HTC Touch Diamond is as beautiful to behold as it is to use.

With HTC’s vibrant touch-responsive user interface, TouchFLO™ 3D, and ultra-fast HSDPA internet connectivity… the HTC Touch Diamond offers a rich online experience to rival a notebook computer, allowing you to interact with Google, YouTube, and Wikipedia as freely as you would with a broadband connection.

Your contacts, favourite music, videos and photos are no longer an uninspired line of text. With TouchFLO 3D, album artwork, video stills and snapshots of your friends’ and family’s faces are brought to life for you to interact, play and launch at your fingertips.

A 3.2 megapixel auto-focus camera will help you capture the perfect moment in style and with a massive 4GB of internal storage you can keep all the files you need. The integrated ultra-sensitive GPS will help you find your destination as quickly and efficiently as a dedicated satellite navigation unit.

Style and substance in a phone are no longer mutually exclusive. The HTC Touch Diamond has arrived.</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">dimension</key>
                  <value xsi:type="xsd:string">102 mm (L) X 51 mm (W) X 11.35 mm (T)</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">activation_information</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">color</key>
                  <value xsi:type="xsd:string">24</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">custom_design</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">custom_design_from</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">custom_design_to</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">custom_layout_update</key>
                  <value xsi:type="xsd:string">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">options_container</key>
                  <value xsi:type="xsd:string">container2</value>
                </item>
                <item>
                  <key xsi:type="xsd:string">page_layout</key>
                  <value xsi:nil="true">
                </value>
              </item>
                <item>
                  <key xsi:type="xsd:string">designeditor_theme_id</key>
                  <value xsi:nil="true">
                </value>
              </item></item>
                <item xsi:type="enc:Array" enc:arraySize="1" enc:itemType="ns2:Map">
                  <item xsi:type="ns2:Map">
                    <item>
                      <key xsi:type="xsd:string">file</key>
                      <value xsi:type="xsd:string">/h/t/htc-touch-diamond.jpg</value>
                    </item>
                    <item>
                      <key xsi:type="xsd:string">label</key>
                      <value xsi:type="xsd:string">
                    </value>
                  </item>
                    <item>
                      <key xsi:type="xsd:string">position</key>
                      <value xsi:type="xsd:string">1</value>
                    </item>
                    <item>
                      <key xsi:type="xsd:string">exclude</key>
                      <value xsi:type="xsd:string">1</value>
                    </item>
                    <item>
                      <key xsi:type="xsd:string">url</key>
                      <value xsi:type="xsd:string">http://echoed.gostorego.com/media/s4/fb/2b/23/74/83/e8/catalog/product/h/t/htc-touch-diamond.jpg</value>
                    </item>
                    <item>
                      <key xsi:type="xsd:string">types</key>
                      <value xsi:type="enc:Array" enc:arraySize="3" enc:itemType="xsd:string">
                        <item xsi:type="xsd:string">thumbnail</item>
                        <item xsi:type="xsd:string">small_image</item>
                        <item xsi:type="xsd:string">image</item>
                      </value>
                    </item></item></item>
                  </multiCallReturn>
                </ns1:multiCallResponse>
              </env:Body>
            </env:Envelope>



 */
