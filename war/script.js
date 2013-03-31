var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
var osmAttrib = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors';
var osm = L.tileLayer(osmUrl, {
	maxZoom : 18,
	attribution : osmAttrib
});
var map = L.map('map').setView([53.316, 6.873], 15).addLayer(osm);
L.marker([53.316, 6.873]).addTo(map).bindPopup('Hello World').openPopup();
var latlngs = [[53.316, 6.873], [53.315, 6.870], [53.317, 6.870]]
var polyline = L.polyline(latlngs, {color: 'red'}).addTo(map);

