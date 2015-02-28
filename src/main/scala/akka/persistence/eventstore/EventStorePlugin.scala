package akka.persistence.eventstore

import akka.actor.{ ActorLogging, Actor }
import akka.persistence.PersistentRepr
import akka.persistence.eventstore.snapshot.EventStoreSnapshotStore.SnapshotEvent
import akka.serialization.{ SerializationExtension, Serialization }
import eventstore.Content.Empty
import scala.concurrent.Future
import scala.util.Success
import scala.util.control.NonFatal
import eventstore._

trait EventStorePlugin extends ActorLogging { self: Actor =>
  val connection: EsConnection = EventStoreExtension(context.system).connection
  val serialization: Serialization = SerializationExtension(context.system)
  import context.dispatcher

  def deserialize[T](event: Event, clazz: Class[T]): T = {
    val ser = serialization.serializerFor(clazz)
    val res = ser match {
      case ser: EventStoreSerializer if clazz != classOf[PersistentRepr] =>
        ser.fromEvent(event, clazz)
      case _ =>
        val ed = event.data
        val pr = ser.fromBinary(ed.data.value.toArray, clazz).asInstanceOf[PersistentRepr]
        val payload = pr.payload.asInstanceOf[AnyRef]

        serialization.findSerializerFor(pr) match {
          case ser: EventStoreSerializer =>
            val byteString = ed.metadata.value
            val metadata = if (byteString.isEmpty) None else Some(ser.fromBinary(byteString.toArray, None))
            pr.withPayload(ser.fromPayloadAndMetadata(payload, metadata))
          case _ =>
            pr
        }
    }
    res.asInstanceOf[T]
  }

  def serialize(snapshot: SnapshotEvent): EventData = {
    serialization.findSerializerFor(snapshot) match {
      case ser: EventStoreSerializer => ser.toEvent(snapshot)
      case _                         => throw new IllegalStateException()
    }
  }

  def serialize(pr: PersistentRepr): EventData = {
    val prPayload = pr.payload.asInstanceOf[AnyRef]

    serialization.findSerializerFor(pr) match {
      case ser: EventStoreSerializer =>
        val (payload, metadata) = ser.toPayloadAndMetadata(prPayload)
        toEventData(pr.withPayload(payload).asInstanceOf[PersistentRepr], metadata)
      case _ =>
        toEventData(pr, None)
    }
  }

  private def toEventData(pr: PersistentRepr, metadata: Option[AnyRef]) = {
    val ser = serialization.serializerFor(classOf[PersistentRepr])
    EventData(
      eventType = pr.payload.getClass.getName,
      data = Content(ser.toBinary(pr)),
      metadata = metadata.fold(Empty) {
        m => serialization.serialize(m).flatMap(ba => Success(Content(ba))).getOrElse(Empty)
      })
  }

  def asyncUnit(x: => Future[_]): Future[Unit] = async(x).map(_ => Unit)

  def async[T](x: => Future[T]): Future[T] = try x catch {
    case NonFatal(f) => Future.failed(f)
  }

  def asyncSeq[A](x: => Iterable[Future[A]]): Future[Unit] = asyncUnit(Future.sequence(x))
}