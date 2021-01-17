# re-frame-highcharts

[![Clojars Project](https://img.shields.io/clojars/v/re-frame-highcharts.svg)](https://clojars.org/re-frame-highcharts)

A little helper utility to make it easier to use Highcharts and Highstock with re-frame and reagent.

## Overview

Highcharts maintain their own chart instance.
So we need to find a method of interacting with the charts so that it works in the world of re-frame and reagent.

This it a small helper utility. Either use it directly or as an example for your own project.

## Running the example

The example is prepared for figwheel, so just run

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).

To clean all compiled files:

    lein clean

To create a production build run (using Java 8):

    lein do clean, cljsbuild once min

To host the production build in a test server run:

    lein do clean, cljsbuild once min, ring server

## Other examples

https://agoraopus.github.io/brownian-motion/

(Let me know of others if you have some)

## License

Copyright © 2019 Christian Felde

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
