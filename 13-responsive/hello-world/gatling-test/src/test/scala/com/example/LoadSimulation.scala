package com.example

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

class LoadSimulation extends Simulation {

  val baseUrl = System.getProperty("base.url", "http://localhost:8080")
  val testPath = System.getProperty("test.path", "/hello/200")
  val sim_users = System.getProperty("sim.users", "200").toInt

  val httpConf = http.baseUrl(baseUrl)

  val helloRequest = repeat(100) {
    exec(http("hello-with-latency")
      .get(testPath))
      .pause(0.5 second, 1 seconds)
  }

  val scn = scenario("hello")
    .exec(helloRequest)

  setUp(scn.inject(rampUsers(sim_users).during(10 seconds)).protocols(httpConf))
}
