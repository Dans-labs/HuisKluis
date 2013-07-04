# HuisKluis

Copyright © 2013, Christophe Guéret, DANS (KNAW)/Pilot Linked Open Data project

## About

The government of the Netherlands has a lot of data about
the citizens and basic infrastructure which are contained in
so-called basic registers. Through an open data policy, the
content of these registers is becoming available to the citizens
but having the data open does not implies that it becomes
accessible right away. Those willing to use that data have to go
to different places to grab it and perform some data integration
on their own.

On the other hand, citizens have data that could be useful to the
government, or city related services, and which is not available.
Let us consider, for instance, the case of a fire in a home. The
fire brigade in charge of dealing with the incident would make a
great use of some detailed information about the house (_e.g._
the location of the valuables, the list of sleeping rooms, the
presence of toxic materials) to better focus their efforts. This
data is not open and should not be so to prevent its misuse, still
it would be good to make it available within a given set of
trusted parties

The "HuisKluis" is a concept bringing both points of view
together around a central application. Through this house-centric
application, citizens can:

* Get a single, harmonised, view over all the governmental data about the house
* Provide additional restricted information
* Control who gets access to the restricted information

## What are the data sources ?

The application uses all sort of external open data sets in
addition to its own internal restricted data set

At the moment, the following open data sets are in use:

* The Linked Open Data export from [Netage.nl](http://netage.nl/en/) of the [basic registry of addresses and building (BAG)](http://bag.vrom.nl/)
* The map data from [Open Street Map](http://www.openstreetmap.org/)
* The images from the [city archives](http://www.beeldbank.amsterdam.nl/)

## How does it work?

The HuisKluis is implemented as two different parts, a
server and a client applications:

* Server: the server implements a RESTful API that redirect calls to other APIs. 
This server is implemented in Java with the RestLet and Jena libraries, it runs
on the AppEngine platform from Google

* Client: the client is a Web application wrote in HTML5/CSS3/Javascript using 
[Bootstrap](http://twitter.github.io/bootstrap/index.html),
[JQuery](http://jquery.com/) and [Leaflet](http://leafletjs.com/). The application is
designed to work fine on any kind of Web-enabled device (desktop,
phone, tablet, ...) running modern Web browsers

The source code of both the server and client part are open source
and [available on GitHub](https://github.com/Dans-labs/HuisKluis).

## Who is behind that ?

This application has been developed in the context of the [Pilot Linked Open Data project](http://www.geonovum.nl/dossiers/linkedopendata)
(see the [list of participants](http://www.geonovum.nl/dossiers/linkedopendata/deelnemers)) with financial and R&D support from the 
[Network Institute](http://www.networkinstitute.org/) of the [VU University of Amsterdam](http://www.vu.nl/)
along with [Data Archiving and Networking Services (DANS)](http://www.dans.knaw.nl/), an institute from the 
[Royal Netherlands Academy of Sciences (KNAW)](http://www.knaw.nl/).

## Contributors

Many people contributed to the source code and/or the design of the site:

* Ingrid Arcas
* [Wouter Beek](https://github.com/wouterbeek)
* [Marat Charlaganov](https://github.com/cmarat/)
* [Albert Meroño Peñuela](https://github.com/albertmeronyo)

