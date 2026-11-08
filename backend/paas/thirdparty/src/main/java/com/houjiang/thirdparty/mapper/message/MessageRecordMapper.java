package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.dto.message.MessageRecordFilter;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 消息发送记录Mapper接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {

    @Select("""
            <script>
            SELECT * FROM hou_message_record WHERE org_id = #{orgId}
            <if test='filter.bizType != null and filter.bizType != ""'>
              <choose>
                <when test='filter.bizType.equals("PM")'>AND biz_type IN ('PM_TASK', 'PM_PROJECT')</when>
                <otherwise>AND biz_type = #{filter.bizType}</otherwise>
              </choose>
            </if>
            <if test='filter.eventType != null and filter.eventType != ""'>AND event_type = #{filter.eventType}</if>
            <if test='filter.channel != null and filter.channel != ""'>AND channel = #{filter.channel}</if>
            <if test='filter.sendStatus != null'>AND send_status = #{filter.sendStatus}</if>
            <if test='filter.receiver != null and filter.receiver != ""'>
              /**
               * TODO 补充方法说明
               * @param CONCAT('%' TODO
               */
              AND (receiver_name LIKE CONCAT('%', #{filter.receiver}, '%')
                   /**
                    * TODO 补充方法说明
                    */
                   OR receiver_contact LIKE CONCAT('%', #{filter.receiver}, '%'))
            </if>
            <if test='filter.startTime != null'>AND create_time &gt;= #{filter.startTime}</if>
            <if test='filter.endTime != null'>AND create_time &lt;= #{filter.endTime}</if>
            ORDER BY create_time DESC
            </script>
            """)
    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    Page<MessageRecord> selectFilteredPage(
            /**
             * TODO 补充方法说明
             */
            Page<MessageRecord> page, @Param("orgId") String orgId,
            @Param("filter") MessageRecordFilter filter);

    @Select("""
            <script>
            /**
             * TODO 补充方法说明
             * @return TODO
             */
            SELECT COUNT(*) total,
              /**
               * TODO 补充方法说明
               * @param 2 TODO
               */
              COALESCE(SUM(send_status = 2), 0) success,
              /**
               * TODO 补充方法说明
               * @param 3 TODO
               */
              COALESCE(SUM(send_status = 3), 0) failed,
              /**
               * TODO 补充方法说明
               * @param 0 TODO
               */
              COALESCE(SUM(send_status = 0), 0) pending
            FROM hou_message_record WHERE org_id = #{orgId}
            <if test='filter.bizType != null and filter.bizType != ""'>
              <choose>
                <when test='filter.bizType.equals("PM")'>AND biz_type IN ('PM_TASK', 'PM_PROJECT')</when>
                <otherwise>AND biz_type = #{filter.bizType}</otherwise>
              </choose>
            </if>
            <if test='filter.eventType != null and filter.eventType != ""'>AND event_type = #{filter.eventType}</if>
            <if test='filter.channel != null and filter.channel != ""'>AND channel = #{filter.channel}</if>
            <if test='filter.sendStatus != null'>AND send_status = #{filter.sendStatus}</if>
            <if test='filter.receiver != null and filter.receiver != ""'>
              /**
               * TODO 补充方法说明
               * @param CONCAT('%' TODO
               */
              AND (receiver_name LIKE CONCAT('%', #{filter.receiver}, '%')
                   /**
                    * TODO 补充方法说明
                    */
                   OR receiver_contact LIKE CONCAT('%', #{filter.receiver}, '%'))
            </if>
            <if test='filter.startTime != null'>AND create_time &gt;= #{filter.startTime}</if>
            <if test='filter.endTime != null'>AND create_time &lt;= #{filter.endTime}</if>
            </script>
            """)
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<String, Object> selectFilteredStatistics(
            @Param("orgId") String orgId, @Param("filter") MessageRecordFilter filter);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_message_record WHERE id = #{id} AND org_id = #{orgId}")
    MessageRecord selectOwnedById(@Param("id") Long id, @Param("orgId") String orgId);

    @Update("UPDATE hou_message_record SET send_status = 1, retry_count = retry_count + 1 "
            + "WHERE id = #{id} AND org_id = #{orgId} AND send_status = 3")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int claimFailedForRetry(@Param("id") Long id, @Param("orgId") String orgId);

    @Update("UPDATE hou_message_record SET send_status = 2, provider_message_id = #{messageId}, "
            + "send_time = #{sendTime} WHERE id = #{id} AND org_id = #{orgId} AND send_status = 1")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int markRetrySucceeded(@Param("id") Long id, @Param("orgId") String orgId,
                           @Param("messageId") String messageId, @Param("sendTime") int sendTime);

    @Update("UPDATE hou_message_record SET send_status = 3, error_code = #{errorCode}, "
            + "fail_reason = #{errorMessage}, send_time = #{sendTime} "
            + "WHERE id = #{id} AND org_id = #{orgId} AND send_status = 1")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int markRetryFailed(@Param("id") Long id, @Param("orgId") String orgId,
                        @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage,
                        @Param("sendTime") int sendTime);
}
