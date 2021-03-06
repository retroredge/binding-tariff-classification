/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.bindingtariffclassification.scheduler

import java.time._

import org.mockito.BDDMockito.given
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.bindingtariffclassification.config.AppConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._

class SchedulerDateUtilTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val zone = ZoneOffset.UTC
  private val clock: Clock = Clock.fixed(instant("2019-01-01T12:00:00").plusNanos((Math.random()*1000).toInt), zone)
  private val config: AppConfig = mock[AppConfig]
  private val util = new SchedulerDateUtil(config)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(config.clock) willReturn clock
  }

  "Next Run" should {
    "Calculate the next run date given a run time of now" in {
      util.nextRun(
        time("12:00"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the next run date given a run time in the future" in {
      util.nextRun(
        time("12:00:09"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the next run date given a run time in the future with Offset" in {
      util.nextRun(
        time("12:00:01"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.nextRun(
        time("12:00:10"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")
    }

    "Calculate the next run date given a run time in the past" in {
      util.nextRun(
        time("11:59:51"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the next run date given a run time in the past with Offset" in {
      util.nextRun(
        time("11:59:58"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.nextRun(
        time("11:59:52"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")
    }
  }

  "Closest Run" should {
    "Calculate the closest run date given a run time of now" in {
      util.closestRun(
        time("12:00"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the closest run date given a run time in the future" in {
      util.closestRun(
        time("12:00:09"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the closest run date given a run time in the future with Offset" in {
      util.closestRun(
        time("12:00:01"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.closestRun(
        time("12:00:10"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.closestRun(
        time("12:00:02"),
        3.seconds
      ) shouldBe instant("2019-01-01T11:59:59")

      util.closestRun(
        time("12:00:02"),
        4.seconds
      ) shouldBe instant("2019-01-01T11:59:58")
    }

    "Calculate the closest run date given a run time in the past" in {
      util.closestRun(
        time("11:59:51"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:00")
    }

    "Calculate the closest run date given a run time in the past with Offset" in {
      util.closestRun(
        time("11:59:58"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.closestRun(
        time("11:59:52"),
        3.seconds
      ) shouldBe instant("2019-01-01T12:00:01")

      util.closestRun(
        time("11:59:56"),
        3.seconds
      ) shouldBe instant("2019-01-01T11:59:59")

      util.closestRun(
        time("11:59:58"),
        4.seconds
      ) shouldBe instant("2019-01-01T11:59:58")
    }
  }

  private def instant(datetime: String): Instant = {
    LocalDateTime.parse(datetime).atZone(zone).toInstant
  }

  private def time(datetime: String): LocalTime = {
    LocalTime.parse(datetime)
  }

}
