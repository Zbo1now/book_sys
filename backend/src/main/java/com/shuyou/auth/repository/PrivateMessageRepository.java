package com.shuyou.auth.repository;

import com.shuyou.auth.entity.PrivateMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
  @Query("select pm from PrivateMessage pm where " +
         "(pm.senderId = :userId1 and pm.receiverId = :userId2) or " +
         "(pm.senderId = :userId2 and pm.receiverId = :userId1) " +
         "order by pm.createdAt desc")
  Page<PrivateMessage> findConversation(@Param("userId1") Long userId1,
                                         @Param("userId2") Long userId2,
                                         Pageable pageable);

  @Query("select pm from PrivateMessage pm where " +
         "(pm.senderId = :userId1 and pm.receiverId = :userId2) or " +
         "(pm.senderId = :userId2 and pm.receiverId = :userId1) " +
         "order by pm.createdAt asc")
  List<PrivateMessage> findConversationAsc(@Param("userId1") Long userId1,
                                           @Param("userId2") Long userId2);

  @Query("select count(pm) from PrivateMessage pm where pm.receiverId = :userId and pm.isRead = false")
  long countUnreadByReceiverId(@Param("userId") Long userId);

  @Query("select pm from PrivateMessage pm where pm.receiverId = :userId and pm.isRead = false order by pm.createdAt desc")
  List<PrivateMessage> findUnreadByReceiverId(@Param("userId") Long userId);

  @Modifying
  @Query("update PrivateMessage pm set pm.isRead = true where pm.receiverId = :receiverId and pm.senderId = :senderId and pm.isRead = false")
  void markAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

  @Query("select distinct case when pm.senderId = :userId then pm.receiverId else pm.senderId end as otherUserId " +
         "from PrivateMessage pm where pm.senderId = :userId or pm.receiverId = :userId")
  List<Long> findConversationPartnerIds(@Param("userId") Long userId);
}
