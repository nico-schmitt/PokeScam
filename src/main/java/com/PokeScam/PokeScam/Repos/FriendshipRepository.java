package com.PokeScam.PokeScam.Repos;

import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.DTOs.FriendshipStatus;
import com.PokeScam.PokeScam.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    @Query("""
        select f
        from Friendship f
        where f.status = 'ACCEPTED'
            and (f.requester = :user or f.receiver = :user)
    """)
    Page<Friendship> findFriends(User user, Pageable pageable);

    Page<Friendship> findByReceiverAndStatus(
            User receiver,
            FriendshipStatus status,
            Pageable pageable
    );

    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);
}
