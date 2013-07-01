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

// The current house post code and number
var postCode = null;
var number = null;

// A pointer to the map
var map = null;

// Private mode ?
var privateMode = 0;

var timeout;

// When everything is ready, call init
$().ready(init);

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

/**
 * Check if the code for the house is ok
 */
function checkPrivateCode() {
	$('#myModal').modal('hide');
	
	privateMode = 0;

	if ($("#privateCode").val() == '1234') {
		privateMode = 1;
		$("#switchButton").hide();
	}
		
	// Load the comment widget
	loadCommentWidget(identifier);
}

/**
 * Map widget, shows the contour of the building on the map
 */
function loadMapWidget(identifier) {
	$.ajax({
		url : "http://" + window.location.host + "/widget/map/" + identifier,
		success : function(json) {
			// Set the marker
			var la = json.lat;
			var lo = json.long;
			L.marker([ parseFloat(la), parseFloat(lo) ]).addTo(map);
			map.setView([ parseFloat(la), parseFloat(lo) ], 16);

			// Add the polygon
			var polystr = json.polygon;
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
		},
		error : function() {
		},
	});
}

/**
 * Photos widget
 */
function loadPhotosWidget(identifier) {
	$("#myCarousel").hide();
	$("#carousel-content").empty();
	$.ajax({
		url : "http://" + window.location.host + "/widget/photo/" + identifier,
		success : function(json) {
			$.each(json.images, function(index, value) {
				var div = $("<div/>").attr('class',
						'item' + (index == 1 ? ' active' : ''));
				var img = $("<img/>").attr('src', value).attr('class',
						'img-polaroid');
				img.appendTo(div);
				div.appendTo($("#carousel-content"));
			});
			$("#myCarousel").show();
		},
		error : function() {
		},
	});
}

/**
 * Data widget, shows basic information
 */
function loadDataWidget(identifier) {
	$("#dataWidget").hide();
	$("#bouwjaar").empty();
	$("#straat").empty();
	$.ajax({
		url : "http://" + window.location.host + "/widget/data/" + identifier,
		success : function(json) {
			$("<p/>").text(json.construction).appendTo("#bouwjaar");
			$("<p/>").text(json.street).appendTo("#straat");
			$("#dataWidget").show();
		},
		error : function() {
		},
	});
}

/**
 * Data widget, shows basic information
 */
function loadCommentWidget(identifier) {
	$("#commentWidget").hide();
	$("#comment").empty();
	$("#textComment").text("");

	if (privateMode == 0)
		return;

	$.ajax({
		url : "http://" + window.location.host + "/widget/comment/"
				+ identifier,
		success : function(json) {
			console.log(json.comment);
			$("#textComment").html(json.comment);
			$("#commentWidget").show();
		},
		error : function() {
		},
	});
}

/**
 * Display the relevant information for a house
 */
function displayHouse(postCode, number) {
	privateMode = 0;
	
	// Fill in the form in the navigation bar
	$('#postcode').attr('placeholder', postCode).val(postCode);
	$('#number').attr('placeholder', number).val(number);
	identifier = postCode + '-' + number;

	// Load the mapWidget
	loadMapWidget(identifier);

	// Load the photosWidget
	loadPhotosWidget(identifier);

	// Load the dataWidget
	loadDataWidget(identifier);

	// Load the comment widget
	loadCommentWidget(identifier);
}

/**
 * Function called when all the javascript is ready to rock
 */
function init() {
	// Configure Ajax queries
	$.ajaxSetup({
		datatype : "json"
	});

	// Create the map
	var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
	var osmAttrib = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors';
	var osm = L.tileLayer(osmUrl, {
		maxZoom : 18,
		attribution : osmAttrib
	});
	map = L.map('map').addLayer(osm);

	// Create the text editor
	$('#textComment').bind(
			'textchange',
			function() {
				clearTimeout(timeout);
				$('#ajaxFired').html('<p class="muted">Typing...</p>');
				var self = this;
				timeout = setTimeout(function() {
					$.ajax({
						method : "PUT",
						url : "http://" + window.location.host
								+ "/widget/comment/" + identifier + "?comment="
								+ $(self).val(),
						success : function() {
							$('#ajaxFired').html(
									'<p class="muted">Changes saved !</p>');
						}
					});
				}, 1000);
			});

	// $(".editor").jqte({
	// change : function() {
	// console.log($("#textComment").text());
	// }
	// });

	// Get the parameters for the house
	postCode = GetURLParameter('postcode');
	number = GetURLParameter('number');

	if (postCode != null && number != null && postCode != '' && number != '') {
		// Adapt the view
		$("#welcomepage").hide();
		$("#housepage").show();
		$("#form-top").show();

		// Display the house
		displayHouse(postCode, number);
	} else {
		$("#housepage").hide();
		$("#welcomepage").show();
	}

}
