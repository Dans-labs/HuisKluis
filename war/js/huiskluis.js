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

// E-mail of logged in user
var user_email = null;

// E-mail for the displayed house owner
var owner_email = null;

// Confidential comment about the house
var description = null;

// The current house post code and number
var postCode = null;
var number = null;

// A pointer to the map
var map = null;

function getEmailCallback(obj) {
	// Prepare the text for the log out button
	var email = '';
	if (obj['email']) {
		email = 'Log out ' + obj['email'];
	}

	// Show the log out
	$("#logoutButton").text(email);
	$("#logoutButton").show();

	user_email = obj['email'];

	// Update the private section
	updatePrivateSection();
}

function signinCallback(authResult) {
	if (authResult['access_token']) {
		// Successfully authorized

		// Hide the sign in button
		$("#signinButton").hide();

		// Load the oauth2 libraries to enable the userinfo methods.
		gapi.client.load('oauth2', 'v2', function() {
			var request = gapi.client.oauth2.userinfo.get();
			request.execute(getEmailCallback);
		});

	} else if (authResult['error']) {
		// There was an error.
		user_email = "";
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
			user_email = "";
			updatePrivateSection();
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
	$("#bouwjaar").empty();
	$("#straat").empty();
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

	// Get the mail of the house owner
	claim_prop = $.rdf.resource('pilod:claimedBy', {
		namespaces : ns
	});
	r = s.where("?s " + claim_prop + " ?owner").each(function() {
		owner_email = this.owner.value;
	});

	// Get the confidential comment about the house
	description_prop = $.rdf.resource('pilod:description', {
		namespaces : ns
	});
	r = s.where("?s " + description_prop + " ?description").each(function() {
		description = this.description.value;
	});

	// Update controls for the private section
	updatePrivateSection();
}

/**
 * 
 */
function updatePrivateSection() {
	$("#private-div").empty();

	// Users have to log in to continue with this section
	if (user_email == "") {
		dlgdiv = $("<div/>")
				.attr("class", "alert alert-info")
				.text(
						"Please log in with your Google+ account to diplay the content of this section");
		$("<button/>").attr("class", "close").attr("data-dismiss", "alert")
				.text("x").appendTo(dlgdiv);
		dlgdiv.appendTo("#private-div");
		return;
	}

	if (owner_email == "NONE") {
		// There is no owner for this house, show the claim button
		btn = $("<button/>").attr("class", "btn btn-info").attr("type",
				"button").text("Claim this house");
		btn.click(function() {
			url = "http://" + window.location.host + "/claim/" + postCode + "/"
					+ number + "?email=" + user_email;
			btn.hide();
			// Send a claim request
			$.ajax({
				method : "PUT",
				url : url,
				success : function() {
					loadHouse();
					updatePrivateSection();
				}
			});
		});
		btn.appendTo("#private-div");
	} else {
		if (owner_email == user_email) {
			// The user of this house is logged in, show the private data
			dlgdiv = $("<div/>")
					.attr("class", "alert alert-info")
					.text(
							"Hello! You claimed this house, feel free to add some confidential information about it");
			$("<button/>").attr("class", "close").attr("data-dismiss", "alert")
					.text("x").appendTo(dlgdiv);
			dlgdiv.appendTo("#private-div");
			
			var txtarea = $("<textarea/>").attr("rows", 3).attr("id",
					"description").val("");
			if (description != "NONE")
				txtarea.val(description);
			txtarea.appendTo("#private-div");
			txtarea.change(function () {
				$("#saveButton").removeAttr('disabled');		
			});
			$("<br/>").appendTo("#private-div");
			var saveBtn = $("<button/>").attr("class", "btn btn-primary btn-mini")
					.attr("type", "button").attr("id", "saveButton").text("Save changes");
			saveBtn.click(function() {
				url = "http://" + window.location.host + "/claim/" + postCode
						+ "/" + number + "?description="
						+ $("#description").val();
				// Send a new description
				$.ajax({
					method : "PUT",
					url : url,
					success : function() {
						saveBtn.attr("disabled", "disabled");
					}
				});
			});
			saveBtn.appendTo("#private-div");
			
		} else {
			// The user logged in is not the one who claimed this house
			dlgdiv = $("<div/>")
					.attr("class", "alert alert-error")
					.text(
							owner_email
									+ " claimed this house, you do not have access to the private data.");
			$("<button/>").attr("class", "close").attr("data-dismiss", "alert")
					.text("x").appendTo(dlgdiv);
			dlgdiv.appendTo("#private-div");
		}
	}
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

function loadHouse() {
	// We need to wait for the authentication first
	if (user_email == null) {
		setTimeout(loadHouse, 250, postCode, number);
		return;
	}

	// Fill in the form
	$('#postcode').attr('placeholder', postCode).val(postCode);
	$('#number').attr('placeholder', number).val(number);

	// Add an history entry
	// var stateObj = {
	// postCode : postCode,
	// number : number
	// };
	// history.pushState(stateObj, postCode + ", " + number,
	// "index.html?postcode=" + postCode + "&number=" + number);

	// Hide the default header
	$('#welcome').hide();

	// Show the progress bar
	$('#progress').show();

	// Compose the URL for the data
	var data_url = "http://" + window.location.host + "/data/" + postCode + "/"
			+ number;
	$('#rdflink').attr('href', data_url);

	if (user_email != "")
		data_url = data_url + "?email=" + user_email;

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
		error : function() {
			$('#progress').hide();
			$('#error').show();
		},
	});
}

function init() {
	// Configure Ajax queries
	$.ajaxSetup({
		datatype : "xml"
	});

	// Create the map
	var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
	var osmAttrib = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors';
	var osm = L.tileLayer(osmUrl, {
		maxZoom : 18,
		attribution : osmAttrib
	});
	map = L.map('map').addLayer(osm);

	// Get the parameters for the house
	postCode = GetURLParameter('postcode');
	number = GetURLParameter('number');

	if (postCode != null && number != null) {
		// If we get valid data, load it
		loadHouse();
	}

}

$().ready(init);
