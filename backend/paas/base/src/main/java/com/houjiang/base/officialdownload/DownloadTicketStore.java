package com.houjiang.base.officialdownload;

public interface DownloadTicketStore {
    void save(DownloadTicket ticket);
    DownloadTicket consume(String tokenHash, String ipHash, java.time.Instant now);
}
