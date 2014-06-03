/// This file contains an abstrasction to access TinySync data through a Javascript interface.
/// It takes care of the differences between regular web and the Android WebView (which does not let you intercept non-GET requests).
/// It's only dependency is jQuery.

(function() {


	function breakoutResponse(response) {
		var res = {}
		$.each(['status', 'message', 'collection'], function(i, prop) {
			res[prop] = eval('response.' + prop + '()')
 		})
 		res.data = JSON.parse(response.dataJson())
 		return res
	}


	window.tinysync = {

		root: '/api',
		platform: 'web', // should be 'web' or 'android'

		get: function(collection, query, callback) {
			var params = jQuery.param(query)
			if (window.tinysync.platform == 'web') {
				var url = window.tinysync.root + '/' + collection + '?' + params
				console.log("Performing web get at " + url)
				$.getJSON(url, callback)
			}
			else if (window.tinysync.platform == 'android') {
				console.log("Calling tinysync API wrapper get")
				var response = window.tinysyncApi.get(collection, JSON.stringify(query))
				response = breakoutResponse(response)
				callback(response)
			}
			else {
				throw "Unknown platform " + window.tinysync.platform
			}
		},

		create: function(collection, record, callback) {
			if (window.tinysync.platform == 'web') {
				var url = window.tinysync.root + '/' + collection + '?' + params
				console.log("Performing web create at " + url)
				$.post(url, record, callback)
			}
			else if (window.tinysync.platform == 'android') {
				console.log("Calling tinysync API wrapper create")
				var response = window.tinysyncApi.create(collection, JSON.stringify(record))
				response = breakoutResponse(response)
				callback(response)
			}
			else {
				throw "Unknown platform " + window.tinysync.platform
			}
		}

	}

})();
