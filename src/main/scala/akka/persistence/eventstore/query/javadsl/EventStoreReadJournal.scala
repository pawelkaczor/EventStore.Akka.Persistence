package akka.persistence.eventstore.query.javadsl

import akka.persistence.eventstore.query.scaladsl
import akka.persistence.query.javadsl._

class EventStoreReadJournal(readJournal: scaladsl.EventStoreReadJournal)
    extends ReadJournal
    with PersistenceIdsQuery
    with CurrentPersistenceIdsQuery
    with EventsByPersistenceIdQuery
    with CurrentEventsByPersistenceIdQuery {

  def persistenceIds() = {
    readJournal.persistenceIds().asJava
  }

  def currentPersistenceIds() = {
    readJournal.currentPersistenceIds().asJava
  }

  def eventsByPersistenceId(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long) = {
    readJournal.eventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr).asJava
  }

  def currentEventsByPersistenceId(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long) = {
    readJournal.currentEventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr).asJava
  }
}
