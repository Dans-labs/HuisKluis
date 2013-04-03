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

function go(xml) {
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
}

$().ready(function() {
	// Configure Ajax queries
	$.ajaxSetup({
		datatype : "xml"
	});

	$.ajax({
		url : "http://localhost:8888/data/1055HB/20-3",
		success : function(xml) {
			go(xml);
		},
	});
});
