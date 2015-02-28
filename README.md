### Event Store Plugin for Akka Persistence

[Akka Persistence](http://doc.akka.io/docs/akka/2.3.9/scala/persistence.html) journal and snapshot-store backed by [Event Store](http://geteventstore.com/).

**This is a fork of [https://github.com/EventStore/EventStore.Akka.Persistence](https://github.com/EventStore/EventStore.Akka.Persistence)**

### Changes to original serializer

Two methods have been added to original ``EventStoreSerializer``:

```scala
trait EventStoreSerializer extends Serializer {
  def toEvent(o: AnyRef): EventData
  def fromEvent(event: Event, manifest: Class[_]): AnyRef

  // new methods
  def toPayloadAndMetadata(e: AnyRef): (AnyRef, Option[AnyRef]) = (e, None)
  def fromPayloadAndMetadata(payload: AnyRef, metadata: Option[AnyRef]) = payload
}
```

``EventStorePlugin`` has been adjusted to use these new methods:

Serialization part:

```scala
  def serialize(pr: PersistentRepr): EventData = {
    val prPayload = pr.payload.asInstanceOf[AnyRef]

    serialization.findSerializerFor(prPayload) match {
      case ser: EventStoreSerializer =>
        val (payload, metadata) = ser.toPayloadAndMetadata(prPayload)
        toEventData(pr.withPayload(payload).asInstanceOf[PersistentRepr], metadata)
      case _ =>
        toEventData(pr, None)
    }
  }
```

During serialization, original payload of ``PersistentRepr`` is replaced with payload returned by toPayloadAndMetadata method.
This mechanism is used by [json serializer] (https://github.com/pawelkaczor/akka-ddd/blob/master/eventstore-akka-persistence/src/main/scala/pl/newicom/eventstore/Json4sEsSerializer.scala)
from [akka-ddd](https://github.com/pawelkaczor/akka-ddd) project to break down EventMessage into event payload and metadata
before storing in EventStore journal. In result business data (business event, its metadata and its type) are mapped to EventStore's eventData, metadata and eventType
without being wrapped into PersistentRepr/EventMessage envelope (business event, metadata) or being lost (eventType).
This in turn allows easy debugging and especially easy creation of user projections.