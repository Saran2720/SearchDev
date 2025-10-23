package com.searchDev.SearchDev.Repository;

import com.searchDev.SearchDev.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
@Repository
public interface MessageRepo extends JpaRepository<Message, UUID> {
    List<Message> findByReceiverIdOrderByCreatedAtDesc(UUID receiverID);
}
