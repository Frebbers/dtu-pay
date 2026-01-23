package dtu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import messaging.Event;
import dtu.tokens.AccountCreated;
import dtu.tokens.AccountDeregistered;
import dtu.tokens.AccountEvent;
import lombok.NonNull;
import messaging.MessageQueue;

/// @author Elias Mortensen - s235109

public class EventStore {

	private Map<String, List<AccountEvent>> store = new ConcurrentHashMap<>();

	private MessageQueue eventBus;

	public EventStore(MessageQueue bus) {
		this.eventBus = bus;
	}

	public void addEvent(String id, AccountEvent e) {
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

	public Stream<AccountEvent> getEventsFor(String id) {
		if (!store.containsKey(id)) {
			store.put(id, new ArrayList<AccountEvent>());
		}
		return store.get(id).stream();
	}

	public void addEvents(@NonNull String id, List<AccountEvent> appliedEvents) {
		appliedEvents.stream().forEach(e -> addEvent(id, e));
	}

}
