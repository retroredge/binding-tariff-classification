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

package uk.gov.hmrc.bindingtariffclassification.repository

import play.api.libs.json._
import reactivemongo.api.Cursor
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MongoCrudHelper[T] extends MongoIndexCreator {

  protected val mongoCollection: JSONCollection

  def getOne(selector: JsObject)(implicit r: Reads[T]): Future[Option[T]] = {
    mongoCollection.find(selector).one[T]
  }

  def getMany(filterBy: JsObject, sortBy: JsObject)(implicit r: Reads[T]): Future[List[T]] = {
    mongoCollection.find(filterBy).sort(sortBy).cursor[T]().collect[List](Int.MaxValue, Cursor.FailOnError[List[T]]())
  }

  def createOne(document: T)(implicit w: OWrites[T]): Future[T] = {
    mongoCollection.insert(document).map ( _ => document )
  }

  // TODO: create a single `updateAtomically` method that can update a whole mongo document or a part of it.
  // Remember the D.R.Y. principle

  def updateDocument(selector: JsObject, newDocument: T)
                    (implicit returnFormat: OFormat[T]): Future[Option[T]] = {

    mongoCollection.findAndUpdate(
      selector = selector,
      update = newDocument,
      fetchNewObject = true, // returns the new document
      upsert = false
    ).map( _.value.map(_.as[T]) )

  }

  def updateField[U](selector: JsObject, updatedField: JsObject)
                    (implicit returnFormat: OFormat[T]): Future[Option[T]] = {

    mongoCollection.findAndUpdate(
      selector = selector,
      update = updatedField,
      fetchNewObject = false, // returns the original document
      upsert = false
    ).map( _.value.map(_.as[T]) )

  }

}
