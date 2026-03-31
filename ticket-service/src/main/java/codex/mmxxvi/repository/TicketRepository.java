package codex.mmxxvi.repository;

import codex.mmxxvi.entity.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface TicketRepository extends MongoRepository<Ticket, UUID> {
}
