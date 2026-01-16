package dtu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import messaging.Event;
import dtu.Event.AccountCreated;
import dtu.Event.AccountEvent;
import lombok.NonNull;
import messaging.MessageQueue;


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
    eventBus.publish(e);

    // Event event = null;

    //     if(e instanceof AccountCreated) {
    //         event = new Event("AccountRegisteredEvent", new Object[] { event });
    //     }

    //     eventBus.publish(event);
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
