// A pointer to the map
var map = null;
var marker = null;
var layer = null;

// Time out for the editor
var timeout = null;

// Private mode code
var code = null;

// When everything is ready, call init
$().ready(init);

/**
 * Check if the code for the house is ok
 */
function checkPrivateCode() {
	$('#myModal').modal('hide');
	code = $("#privateCode").val();

	if (isPrivateMode()) {
		$("#switchButton").hide();

		// (re)load the comment widget
		loadCommentWidget();
		
		// show energy widget
		$("#sensorWidget").show();
	}
}

/**
 * Map widget, shows the contour of the building on the map
 */
function loadMapWidget() {
	var identifier = getIdentifier();

	// Clean the map
	if (layer != null)
		map.removeLayer(layer);
	if (marker != null)
		map.removeLayer(marker);

	$.ajax({
		url : "http://" + window.location.host + "/widget/map/" + identifier,
		success : function(json) {
			// Set the marker
			var la = json.lat;
			var lo = json.long;
			marker = L.marker([ parseFloat(la), parseFloat(lo) ])
			marker.addTo(map);
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
			layer = L.polygon(points, {
				color : 'red'
			})
			layer.addTo(map);
		},
		error : function() {
		},
	});
}

/**
 * Photos widget
 */
function loadPhotosWidget() {
	var identifier = getIdentifier();

	$("#myCarousel").hide();
	$("#carousel-content").empty();
	$.ajax({
		url : "http://" + window.location.host + "/widget/photo/" + identifier,
		success : function(json) {
			var count = 0;
			$.each(json.images, function(index, value) {
				var div = $("<div/>").attr('class',
						'item' + (index == 1 ? ' active' : ''));
				var img = $("<img/>").attr('src', value).attr('class',
						'img-polaroid');
				img.appendTo(div);
				div.appendTo($("#carousel-content"));
				count = count + 1;
			});
			if (count > 0)
				$("#myCarousel").show();
		},
		error : function() {
		},
	});
}

/**
 * Data widget, shows basic information
 */
function loadDataWidget() {
	var identifier = getIdentifier();
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
function loadCommentWidget() {
	var identifier = getIdentifier();
	$("#commentWidget").hide();
	$("#comment").empty();
	$("#textComment").text("");

	if (!isPrivateMode())
		return;

	$.ajax({
		url : "http://" + window.location.host + "/widget/comment/"
				+ identifier,
		success : function(json) {
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
function displayHouse() {
	var State = History.getState();
	var postCode = State.data['postCode'];
	var number = State.data['number'];

	// Fill in the form in the navigation bar
	$('#formTopPostCode').attr('placeholder', postCode).val(postCode);
	$('#formTopNumber').attr('placeholder', number).val(number);
	var identifier = postCode + '-' + number;

	if (identifier == 'undefined-undefined') {
		$("#housepage").hide();
		$("#form-top").hide();
		$("#welcomepage").show();
	} else {
		$("#welcomepage").hide();
		$("#housepage").show();
		$("#form-top").show();

		// Load the mapWidget
		loadMapWidget();

		// Load the photosWidget
		loadPhotosWidget();

		// Load the dataWidget
		loadDataWidget();

		// Load the comment widget
		loadCommentWidget();
	}
}

/**
 * Function called when all the javascript is ready to rock
 */
function init() {
	// Configure Ajax queries
	$.ajaxSetup({
		datatype : "json"
	});

	// Bind to StateChange Event of the history
	History.Adapter.bind(window, 'statechange', function() {
		displayHouse();
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
								+ "/widget/comment/" + getIdentifier()
								+ "?comment=" + $(self).val(),
						success : function() {
							$('#ajaxFired').html(
									'<p class="muted">Changes saved !</p>');
						}
					});
				}, 1000);
			});

	var State = History.getState();
	var postCode = '' + State.data['postCode'];
	var number = '' + State.data['number'];

	if (postCode != 'undefined' && number != 'undefined') {
		// Adapt the view
		$("#welcomepage").hide();
		$("#housepage").show();
		$("#form-top").show();

		// Display the house
		displayHouse();
	} else {
		$("#housepage").hide();
		$("#welcomepage").show();
	}

}

/*
 * Is private mode enabled ?
 */
function isPrivateMode() {
	// No code ?
	if (code == null)
		return false;

	// Check the code
	return (code == '1234');
}

/*
 * Get the identifier of the current house
 */
function getIdentifier() {
	var State = History.getState();
	var postCode = State.data['postCode'];
	var number = State.data['number'];
	return postCode + '-' + number;
}

/*
 * Forces the loading of another house
 */
function switchToHouse(postCode, number) {
	// Adapt the view
	$("#welcomepage").hide();
	$("#housepage").show();
	$("#form-top").show();

	// Get the current state to see if the code is there
	var State = History.getState();
	var code = '' + State.data['code'];

	// Push new status
	var url = "?postCode=" + postCode + "&number=" + number;
	var title = "House " + postCode + '-' + number;
	if (code == 'undefined') {
		History.pushState({
			postCode : postCode,
			number : number
		}, title, url);
	} else {
		History.pushState({
			postCode : postCode,
			number : number,
			code : code
		}, title, url + "&code=" + code);
	}
}

/*
 * A form has been used to query for the new house
 */
function switchFromForm(form) {
	var postCode = form[0].postCode.value;
	var number = form[0].number.value;
	switchToHouse(postCode, number);
}