package com.houjiang.base.officialdownload;

public interface LeadStore {
    DownloadLead upsert(DownloadLead candidate);
}
