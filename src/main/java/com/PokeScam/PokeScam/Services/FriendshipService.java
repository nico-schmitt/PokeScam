package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.DTOs.FriendshipStatus;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.FriendshipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepo;

    public FriendshipService(FriendshipRepository friendshipRepo) {
        this.friendshipRepo = friendshipRepo;
    }

    public Page<Friendship> getFriends(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return friendshipRepo.findFriends(user, pageable);
    }

    public Page<Friendship> getincomingRequests(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return friendshipRepo.findByReceiverAndStatus(user, FriendshipStatus.PENDING, pageable);
    }

    public void sendFriendRequest(User from, User to) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("Cannot friend yourself");
        }

        friendshipRepo.findByRequesterAndReceiver(from, to).ifPresent(
                friendship -> {
                    throw new IllegalArgumentException("Request already exists");
                });

        Friendship friendship = new Friendship();
        friendship.setRequester(from);
        friendship.setReceiver(to);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        friendshipRepo.save(friendship);
    }

    public void removeFriendship(User user, int friendshipId) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!friendship.getReceiver().equals(user) || !friendship.getRequester().equals(user)) {
            throw new IllegalArgumentException("Not your friendship");
        }

        friendshipRepo.delete(friendship);
    }

    public void acceptRequest(Integer friendshipId, User receiver) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!friendship.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("Not your request");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
    }

    public void rejectRequest(Integer friendshipId, User receiver) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!friendship.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("Not your request");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
    }
}
