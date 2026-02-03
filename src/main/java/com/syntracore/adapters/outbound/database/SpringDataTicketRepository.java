package com.syntracore.adapters.outbound.database;

// Spring Data JPA stellt fertige CRUD-Operationen (save, findAll, findById, delete, ...) bereit
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring-Data-Repository für `TicketJpaEntity`.
 * Durch das Erben von `JpaRepository` bekommen wir alle Standard-Datenbankoperationen,
 * ohne selber SQL schreiben zu müssen.
 */
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {

    // Hier können wir später eigene Such-Methoden deklarieren,
    // z.B. `List<TicketJpaEntity> findByCustomerName(String customerName);`
}