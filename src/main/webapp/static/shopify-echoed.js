function echoed_share(store_id, order_id, item_id,item_name,item_type,item_brand,item_image,item_url,bought_on,item_price){
	var url ="http://v1-api.echoed.com/echo?"
			    + "retailerId=" + encodeURIComponent(store_id)
				+ "&customerId="  + encodeURIComponent(order_id)
				+ "&productId=" + encodeURIComponent(item_id)
				+ "&orderId=" + encodeURIComponent(order_id)
				+ "&imageUrl=" + encodeURIComponent(item_image)
				+ "&landingPageUrl=" + encodeURIComponent(item_url)
                + "&boughtOn=" + encodeURIComponent(bought_on)
				+ "&price=" + encodeURIComponent(item_price);				
	window.open(url,'Echoed','width=960,height=500,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
	return false;
}


