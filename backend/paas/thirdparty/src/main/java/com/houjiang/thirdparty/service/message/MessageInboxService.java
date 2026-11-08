package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.entity.message.MessageInbox;
import com.houjiang.thirdparty.mapper.message.MessageInboxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Amos
 */
@Service
public class MessageInboxService {

    private final MessageInboxMapper inboxMapper;
    private final Clock clock;

    @Autowired
    public MessageInboxService(MessageInboxMapper inboxMapper) {
        this(inboxMapper, Clock.systemUTC());
    }

    MessageInboxService(MessageInboxMapper inboxMapper, Clock clock) {
        this.inboxMapper = inboxMapper;
        this.clock = clock;
    }

    public Map<String, Object> list(
            MessageRequestContext context,
            int page,
            int size,
            Integer isRead,
            Integer msgType) {
        Page<MessageInbox> result = inboxMapper.selectOwnedPage(
                new Page<>(page, size), context.userId(), context.orgId(), isRead, msgType);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    public long unreadCount(MessageRequestContext context) {
        Long count = inboxMapper.countOwnedUnread(context.userId(), context.orgId());
        return count == null ? 0 : count;
    }

    public MessageInbox detail(MessageRequestContext context, Long id) {
        MessageInbox inbox = inboxMapper.selectOwnedById(id, context.userId(), context.orgId());
        if (inbox == null) {
            throw notFound();
        }
        return inbox;
    }

    public MessageInbox createIdempotently(MessageInbox inbox) {
        inbox.setCreateTime(now());
        inbox.setIsRead(0);
        try {
            return inboxMapper.insert(inbox) == 1 ? inbox : null;
        } catch (DuplicateKeyException duplicate) {
            if (inbox.getIdempotencyKey() == null || inbox.getIdempotencyKey().isBlank()) {
                throw duplicate;
            }
            return inboxMapper.selectByIdempotencyKey(inbox.getIdempotencyKey());
        }
    }

    public boolean createInbox(MessageInbox inbox) {
        return createIdempotently(inbox) != null;
    }

    public void markAsRead(MessageRequestContext context, Long id) {
        int affected = inboxMapper.markOwnedRead(id, context.userId(), context.orgId(), now());
        if (affected == 0) {
            throw notFound();
        }
    }

    public int markAllAsRead(MessageRequestContext context) {
        return inboxMapper.markAllOwnedRead(context.userId(), context.orgId(), now());
    }

    public void delete(MessageRequestContext context, Long id) {
        int affected = inboxMapper.softDeleteOwned(id, context.userId(), context.orgId(), now());
        if (affected == 0) {
            throw notFound();
        }
    }

    private int now() {
        return Math.toIntExact(clock.instant().getEpochSecond());
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(NOT_FOUND, "消息不存在");
    }
}
