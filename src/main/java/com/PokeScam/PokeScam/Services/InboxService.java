package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.DTOs.RequestDTO;
import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.Model.TradeRequest;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.InboxRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class InboxService {
    private final InboxRequestRepository inboxRequestRepo;

    public InboxService(InboxRequestRepository inboxRequestRepo) {
        this.inboxRequestRepo = inboxRequestRepo;
    }

    public Page<RequestDTO> getInbox(User user, int page, int size, String sortBy, String dir) {
        Sort sort = Sort.by(dir.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        return inboxRequestRepo.findInboxForUser(user, pageable)
                .map(req -> {
                    if (req instanceof Friendship f) {
                        return f.convertToRequestDTO();
                    } else if (req instanceof TradeRequest t) {
                        return t.convertToDTO();
                    }
                    throw  new IllegalStateException("Unknown inbox request type");
                });
    }
}
