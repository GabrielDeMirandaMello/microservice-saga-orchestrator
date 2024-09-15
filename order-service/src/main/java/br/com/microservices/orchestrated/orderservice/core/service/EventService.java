package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository repository;

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        this.save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event filterByFilters(EventFilters eventFilters) {
        validateEmptyFilter(eventFilters);
        if (!isEmpty(eventFilters.getOrderId())){
            return findByOrderId(eventFilters.getOrderId());
        } else {
            return findByTransactionId(eventFilters.getTransactionId());
        }
    }

    private Event findByOrderId(String orderId) {
        return this.repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId"));
    }
    private void validateEmptyFilter(EventFilters eventFilters) {
        if (isEmpty(eventFilters.getOrderId()) && isEmpty(eventFilters.getTransactionId())) {
            throw new ValidationException("OrderId or TransactionId must be informed.");
        }
    }

    private Event findByTransactionId(String transactionId) {
        return this.repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionId"));
    }
    public Event save(Event event){
        return this.repository.save(event);
    }
}
