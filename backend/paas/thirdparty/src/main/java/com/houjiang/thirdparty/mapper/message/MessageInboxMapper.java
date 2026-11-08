package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.entity.message.MessageInbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户消息收件箱Mapper接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Mapper
public interface MessageInboxMapper extends BaseMapper<MessageInbox> {

    @Select("""
            <script>
            SELECT * FROM hou_message_inbox
            WHERE user_id = #{userId} AND org_id = #{orgId} AND delete_time IS NULL
            <if test='isRead != null'>AND is_read = #{isRead}</if>
            <if test='msgType != null'>AND msg_type = #{msgType}</if>
            ORDER BY create_time DESC
            </script>
            """)
    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    Page<MessageInbox> selectOwnedPage(
            Page<MessageInbox> page,
            @Param("userId") String userId,
            @Param("orgId") String orgId,
            @Param("isRead") Integer isRead,
            @Param("msgType") Integer msgType);

    @Select("SELECT * FROM hou_message_inbox WHERE id = #{id} AND user_id = #{userId} "
            + "AND org_id = #{orgId} AND delete_time IS NULL")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    MessageInbox selectOwnedById(
            @Param("id") Long id, @Param("userId") String userId, @Param("orgId") String orgId);

    @Select("SELECT COUNT(*) FROM hou_message_inbox WHERE user_id = #{userId} AND org_id = #{orgId} "
            + "AND is_read = 0 AND delete_time IS NULL")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Long countOwnedUnread(@Param("userId") String userId, @Param("orgId") String orgId);

    @Update("UPDATE hou_message_inbox SET is_read = 1, read_time = #{readTime} WHERE id = #{id} "
            + "AND user_id = #{userId} AND org_id = #{orgId} AND delete_time IS NULL")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int markOwnedRead(@Param("id") Long id, @Param("userId") String userId,
                      @Param("orgId") String orgId, @Param("readTime") int readTime);

    @Update("UPDATE hou_message_inbox SET is_read = 1, read_time = #{readTime} WHERE user_id = #{userId} "
            + "AND org_id = #{orgId} AND is_read = 0 AND delete_time IS NULL")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int markAllOwnedRead(@Param("userId") String userId, @Param("orgId") String orgId,
                         @Param("readTime") int readTime);

    @Update("UPDATE hou_message_inbox SET delete_time = #{deleteTime} WHERE id = #{id} "
            + "AND user_id = #{userId} AND org_id = #{orgId} AND delete_time IS NULL")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int softDeleteOwned(@Param("id") Long id, @Param("userId") String userId,
                        @Param("orgId") String orgId, @Param("deleteTime") int deleteTime);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_message_inbox WHERE idempotency_key = #{idempotencyKey}")
    MessageInbox selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
