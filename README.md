# dark-and-stormy

The Insiders: where in the world are my users?

The Undesirables: where in the world are unsuccessful login attempts coming from?

## Usage

* Start up the dark-and-stormy web server:
  * `lein run` if you can take the Leiningen overhead.
  * `lein uberjar && java -jar target/dark-and-stormy-0.1.0-SNAPSHOT-standalone.jar` if you're doing a prod-like run.
* Open [localhost:8080](http://localhost:8080)

## Plan

* Start up ELK
* Start up the dark-and-stormy web server
  * Auth attempt begins
    * Grab IP address
    * Do authentication and return result
    * Geolocate
    * Send geo data + auth result -> ES
* Log in to the dark-and-stormy web client
* Start the traffic simulator
  * Params:
    * authenticated user list (username/password), IPs/Locations
    * Mallory list: number, locations (IPs?)
    * average login rate per user

## License

Copyright © 2015 Ian Truslove

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
