package com.PokeScam.PokeScam.Repos;

import com.PokeScam.PokeScam.Model.InboxRequest;
import com.PokeScam.PokeScam.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxRequestRepository extends JpaRepository<InboxRequest, Integer> {
    @Query("""
        SELECT r from InboxRequest r
        WHERE r.receiver = :user AND r.status = 'PENDING'""")
    Page<InboxRequest> findInboxForUser(@Param( "user")User user, Pageable pageable);
}
