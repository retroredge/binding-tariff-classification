/*
 * Copyright 2018 HM Revenue & Customs
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
import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.bindingtariffclassification.config.AppConfig
import uk.gov.hmrc.bindingtariffclassification.connector.BankHolidaysConnector
import uk.gov.hmrc.bindingtariffclassification.service.CaseService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.duration.FiniteDuration

@Singleton
class DaysElapsedJob @Inject()(appConfig: AppConfig, caseService: CaseService, bankHolidaysConnector: BankHolidaysConnector) extends ScheduledJob {

  private implicit val carrier: HeaderCarrier = HeaderCarrier()

  private lazy val jobConfig = appConfig.daysElapsed

  override def name: String = "DaysElapsed"

  override def interval: FiniteDuration = FiniteDuration(jobConfig.intervalDays, TimeUnit.DAYS)

  override def execute(): Future[Unit] = {

    val today = LocalDate.now(appConfig.clock)
    val dayOfTheWeek = today.getDayOfWeek
    if (dayOfTheWeek == DayOfWeek.SATURDAY || dayOfTheWeek == DayOfWeek.SUNDAY) {
      Logger.info(s"Scheduled Job [$name]: Skipping today as it is a Weekend")
      successful(())
    } else {
      bankHolidaysConnector.get()
        .map(_.contains(today))
        .flatMap {
          case true =>
            Logger.info(s"Scheduled Job [$name]: Skipping today as it is a Bank Holiday")
            successful(())
          case false =>
            caseService.incrementDaysElapsed(jobConfig.intervalDays)
              .map { modified: Int =>
                Logger.info(s"Scheduled Job [$name]: Incremented the Days Elapsed for [$modified] cases.")
              }
        }
    }
  }

  override def firstRunTime: LocalTime = {
    jobConfig.elapseTime
  }

}
