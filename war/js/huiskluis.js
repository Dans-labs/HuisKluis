// Namespaces
var ns = {
	ont : 'http://linkeddata.few.vu.nl/ontology.owl#',
	geo : 'http://www.geonames.org/ontology#',
	wgs84 : 'http://www.w3.org/2003/01/geo/wgs84_pos#',
	owl : 'http://www.w3.org/2002/07/owl#',
	dbpedia : 'http://dbpedia.org/resource/',
	rdfs : 'http://www.w3.org/2000/01/rdf-schema#',
	pilod : 'http://www.example.org#'
}

// Token
var token = null;

function getEmailCallback(obj) {
	var email = '';
	if (obj['email']) {
		email = 'Log out ' + obj['email'];
	}
	$("#logoutButton").text(email);
	$("#logoutButton").show();
}

function signinCallback(authResult) {
	if (authResult['access_token']) {
		// Successfully authorized

		// Save the token
		token = gapi.auth.getToken();

		// Hide the button
		$("#signinButton").hide();

		// Load the oauth2 libraries to enable the userinfo methods.
		gapi.client.load('oauth2', 'v2', function() {
			var request = gapi.client.oauth2.userinfo.get();
			request.execute(getEmailCallback);
		});

	} else if (authResult['error']) {
		// There was an error.
		// Possible error codes:
		// "access_denied" - User denied access to your app
		// "immediate_failed" - Could not automatially log in the user
		// console.log('There was an error: ' + authResult['error']);
	}
}

function disconnectUser() {
	var token = gapi.auth.getToken();
	var revokeUrl = 'https://accounts.google.com/o/oauth2/revoke?token='
			+ token['access_token'];
	
	// Perform an asynchronous GET request.
	$.ajax({
		type : 'GET',
		url : revokeUrl,
		async : false,
		contentType : "application/json",
		dataType : 'jsonp',
		success : function(nullResponse) {
			$("#logoutButton").hide();
			$("#signinButton").show();
		},
		error : function(e) {
			// Handle the error
			// console.log(e);
			// You could point users to manually disconnect if unsuccessful
			// https://plus.google.com/apps
		}
	});
}

function processHouseData(xml) {
	// Create the triple store
	s = $.rdf();
	s.databank.load(xml);

	// Create the map
	var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
	var osmAttrib = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors';
	var osm = L.tileLayer(osmUrl, {
		maxZoom : 18,
		attribution : osmAttrib
	});
	var map = L.map('map').addLayer(osm);

	// Get the latitude and longitude of the house
	lat_prop = $.rdf.resource('wgs84:lat', {
		namespaces : ns
	});
	lng_prop = $.rdf.resource('wgs84:long', {
		namespaces : ns
	});
	r = s.where("?s " + lat_prop + " ?lat").where("?s " + lng_prop + " ?lng")
			.each(function() {
				la = this.lat.value;
				lo = this.lng.value;
				L.marker([ parseFloat(la), parseFloat(lo) ]).addTo(map);
				map.setView([ parseFloat(la), parseFloat(lo) ], 15);
			});

	// Get the polygon of the building
	polygon_prop = $.rdf.resource('pilod:polygon', {
		namespaces : ns
	});
	r = s.where("?s " + polygon_prop + " ?poly").each(function() {
		var polystr = this.poly.value;
		var regex = /(\d\.[^ ]*) (\d\d\.[^,)]*)/g;
		var points = [];
		while (match = regex.exec(polystr)) {
			var point = [];
			point.push(parseFloat(match[2]));
			point.push(parseFloat(match[1]));
			points.push(point);
		}
		var polygon = L.polygon(points, {
			color : 'red'
		}).addTo(map);
	});

	// Get the construction year and the street name
	cstr_prop = $.rdf.resource('pilod:construction', {
		namespaces : ns
	});
	street_prop = $.rdf.resource('pilod:street', {
		namespaces : ns
	});
	r = s.where("?s " + cstr_prop + " ?cstr").where(
			"?s " + street_prop + " ?street").each(function() {
		cstr = this.cstr.value;
		street = this.street.value;
		$("<p/>").text(cstr).appendTo("#bouwjaar");
		$("<p/>").text(street).appendTo("#straat");
	});

}

// Code from
// http://jquerybyexample.blogspot.com/2012/06/get-url-parameters-using-jquery.html
function GetURLParameter(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for ( var i = 0; i < sURLVariables.length; i++) {
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam)
			return sParameterName[1];
	}
	;
}

function loadHouse(postCode, number) {
	// Fill in the form
	$('#postcode').attr('placeholder', postCode);
	$('#number').attr('placeholder', number);

	// Hide the default header
	$('#welcome').hide();

	// Show the progress bar
	$('#progress').show();

	// Compose the URL for the data
	var data_url = "http://" + window.location.host + "/data/" + postCode + "/"
			+ number;
	$('#rdflink').attr('href', data_url);

	// Load the data
	$.ajax({
		url : data_url,
		success : function(xml) {
			// Hide the progress bar
			$('#progress').hide();

			// Show the data
			$('#house').show();

			// Process the house data
			processHouseData(xml);
		},
	});

}

function init() {
	// Configure Ajax queries
	$.ajaxSetup({
		datatype : "xml"
	});

	// Get the parameters for the house
	var postCode = GetURLParameter('postcode');
	var number = GetURLParameter('number');

	if (postCode != null && number != null) {
		// If we get valid data, load it
		loadHouse(postCode, number);
	}

}

$().ready(init);
