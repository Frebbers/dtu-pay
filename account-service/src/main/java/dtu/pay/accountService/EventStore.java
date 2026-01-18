package dtu.pay.accountService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import dtu.pay.accountService.models.events.AccountCreated;
import dtu.pay.accountService.models.events.AccountDeregistered;
import dtu.pay.accountService.models.events.AccountEvent;
import messaging.Event;
import lombok.NonNull;
import messaging.MessageQueue;


/**
 * @author Fadl Matar
 * In-memory event store for account aggregates.
 * Stores {@link dtu.pay.accountService.models.events.AccountEvent}s per account id and
 * publishes corresponding integration events on the provided {@link messaging.MessageQueue}.
 * Used to reconstruct aggregates by replaying their event stream.
 */
public class EventStore {

	private Map<UUID, List<AccountEvent>> store = new ConcurrentHashMap<>();

	private MessageQueue eventBus;

	public EventStore(MessageQueue bus) {
		this.eventBus = bus;
	}

	public void addEvent(UUID id, AccountEvent e) {
		if (!store.containsKey(id)) {
			store.put(id, new ArrayList<AccountEvent>());
		}
		store.get(id).add(e);

		Event event = null;

		if (e instanceof AccountCreated) {
			event = new Event("AccountCreated", e);
		} else if (e instanceof AccountDeregistered) {
			event = new Event("AccountDeregistered", e);
		}

		eventBus.publish(event);
	}

	public Stream<AccountEvent> getEventsFor(UUID id) {
		if (!store.containsKey(id)) {
			store.put(id, new ArrayList<AccountEvent>());
		}
		return store.get(id).stream();
	}

	public void addEvents(@NonNull UUID id, List<AccountEvent> appliedEvents) {
		appliedEvents.stream().forEach(e -> addEvent(id, e));
	}

}
