package akka.persistence.eventstore

import akka.serialization.Serializer
import eventstore.{ Event, EventData }

trait EventStoreSerializer extends Serializer {
  def toPayloadAndMetadata(e: AnyRef): (AnyRef, Option[AnyRef]) = (e, None)
  def fromPayloadAndMetadata(payload: AnyRef, metadata: Option[AnyRef]) = payload

  def toEvent(o: AnyRef): EventData
  def fromEvent(event: Event, manifest: Class[_]): AnyRef
}