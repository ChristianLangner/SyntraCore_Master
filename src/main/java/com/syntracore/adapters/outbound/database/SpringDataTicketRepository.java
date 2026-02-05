package com.syntracore.adapters.outbound.database;

// Spring Data JPA stellt fertige CRUD-Operationen (save, findAll, findById, delete, ...) bereit
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring Data JPA Repository für Support-Tickets.
 * 
 * <p>Dieses Interface ist Teil des <strong>Adapter-Layers</strong> und nutzt die
 * <strong>Spring Data JPA Magic</strong>, um Datenbankoperationen ohne SQL-Code
 * bereitzustellen.</p>
 * 
 * <h2>Warum ist dieses Repository wichtig?</h2>
 * <ul>
 *   <li><strong>Produktivität:</strong> Keine SQL-Queries schreiben - Spring Data
 *       generiert sie automatisch zur Laufzeit.</li>
 *   <li><strong>Standardoperationen:</strong> CRUD-Operationen (Create, Read, Update, Delete)
 *       sind sofort verfügbar ohne Implementierung.</li>
 *   <li><strong>Typsicherheit:</strong> Compile-Zeit-Prüfung der Methodensignaturen.</li>
 *   <li><strong>Erweiterbarkeit:</strong> Eigene Query-Methoden können durch Namenskonventionen
 *       oder {@code @Query}-Annotationen hinzugefügt werden.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * TicketDatabaseAdapter → SpringDataTicketRepository (hier) → Datenbank
 * </pre>
 * 
 * <h2>Verfügbare Standard-Methoden (von JpaRepository geerbt):</h2>
 * <ul>
 *   <li>{@code save(TicketJpaEntity)} - Speichert oder aktualisiert ein Ticket</li>
 *   <li>{@code findById(UUID)} - Findet ein Ticket anhand der ID</li>
 *   <li>{@code findAll()} - Gibt alle Tickets zurück</li>
 *   <li>{@code deleteById(UUID)} - Löscht ein Ticket anhand der ID</li>
 *   <li>{@code count()} - Zählt alle Tickets</li>
 *   <li>...und viele mehr</li>
 * </ul>
 * 
 * <h2>Beispiele für eigene Query-Methoden:</h2>
 * <pre>
 * // Spring Data generiert automatisch die SQL-Query basierend auf dem Methodennamen:
 * List&lt;TicketJpaEntity&gt; findByCustomerName(String customerName);
 * List&lt;TicketJpaEntity&gt; findByCreatedAtAfter(LocalDateTime date);
 * Optional&lt;TicketJpaEntity&gt; findByIdAndCustomerName(UUID id, String name);
 * 
 * // Oder mit @Query für komplexere Abfragen:
 * {@literal @}Query("SELECT t FROM TicketJpaEntity t WHERE t.aiAnalysis IS NOT NULL")
 * List&lt;TicketJpaEntity&gt; findAllWithAiAnalysis();
 * </pre>
 * 
 * <p><strong>Hinweis:</strong> Dieses Interface benötigt keine Implementierung - Spring Data
 * erstellt zur Laufzeit automatisch eine Proxy-Implementierung.</p>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 1.0
 * 
 * @see TicketJpaEntity
 * @see TicketDatabaseAdapter
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {

    // Aktuell werden nur die Standard-CRUD-Operationen von JpaRepository verwendet.
    // Hier können später eigene Such-Methoden deklariert werden, z.B.:
    // 
    // /**
    //  * Findet alle Tickets eines bestimmten Kunden.
    //  * 
    //  * @param customerName Der Name des Kunden
    //  * @return Liste aller Tickets dieses Kunden
    //  */
    // List<TicketJpaEntity> findByCustomerName(String customerName);
    //
    // /**
    //  * Findet alle Tickets, die nach einem bestimmten Zeitpunkt erstellt wurden.
    //  * 
    //  * @param date Der Zeitpunkt, nach dem gesucht werden soll
    //  * @return Liste aller Tickets nach diesem Zeitpunkt
    //  */
    // List<TicketJpaEntity> findByCreatedAtAfter(LocalDateTime date);
}