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

package uk.gov.hmrc.bindingtariffclassification.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.bindingtariffclassification.config.AppConfig
import uk.gov.hmrc.bindingtariffclassification.model.JsonFormatters.formatNewEventRequest
import uk.gov.hmrc.bindingtariffclassification.model._
import uk.gov.hmrc.bindingtariffclassification.service.{CaseService, EventService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EventController @Inject()(appConfig: AppConfig, eventService: EventService, caseService: CaseService) extends CommonController {

  import JsonFormatters.formatEvent

  lazy private val deleteModeFilter = DeleteMode.actionFilter(appConfig)

  def deleteAll(): Action[AnyContent] = deleteModeFilter.async { implicit request =>
    eventService.deleteAll map ( _ => NoContent ) recover recovery
  }

  def getByCaseReference(caseRef: String): Action[AnyContent] = Action.async { implicit request =>
    eventService.getByCaseReference(caseRef) map {
      events => Ok(Json.toJson(events))
    } recover recovery
  }

  def create(caseRef: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[NewEventRequest] { request =>
      caseService.getByReference(caseRef).flatMap {
        case Some(c: Case) => eventService.insert(request.toEvent(c.reference)).map(e => Created(Json.toJson(e)))
        case _ => Future.successful(NotFound(JsErrorResponse(ErrorCode.NOT_FOUND, "Case not found")))
      }
    }
  }

}
