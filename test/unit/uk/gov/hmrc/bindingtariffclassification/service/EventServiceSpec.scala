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

package uk.gov.hmrc.bindingtariffclassification.service

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.bindingtariffclassification.model.{Event, EventSearch, Paged, Pagination}
import uk.gov.hmrc.bindingtariffclassification.repository.EventRepository
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future.successful

class EventServiceSpec extends UnitSpec with MockitoSugar {

  final private val e1 = mock[Event]
  final private val e2 = mock[Event]

  private val repository = mock[EventRepository]
  private val service = new EventService(repository)

  final val emulatedFailure = new RuntimeException("Emulated failure.")

  "deleteAll()" should {

    "return () and clear the database collection" in {
      when(repository.deleteAll()).thenReturn(successful(()))
      await(service.deleteAll()) shouldBe ((): Unit)
    }

    "propagate any error" in {
      when(repository.deleteAll()).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        await(service.deleteAll())
      }
      caught shouldBe emulatedFailure
    }
  }

  "insert" should {

    "return the expected event after it is inserted in the database collection" in {
      when(repository.insert(e1)).thenReturn(successful(e1))
      val result = await(service.insert(e1))
      result shouldBe e1
    }

    "propagate any error" in {
      when(repository.insert(e1)).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        await(service.insert(e1))
      }
      caught shouldBe emulatedFailure
    }
  }

  "search" should {

    "return the expected events" in {
      when(repository.search(EventSearch(), Pagination())).thenReturn(successful(Paged(Seq(e1, e2))))
      val result = await(service.search(EventSearch(), Pagination()))
      result shouldBe Paged(Seq(e1, e2))
    }

    "propagate any error" in {
      when(repository.search(EventSearch(), Pagination())).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        await(service.search(EventSearch(), Pagination()))
      }
      caught shouldBe emulatedFailure
    }
  }

}
