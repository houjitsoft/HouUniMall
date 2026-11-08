package com.houjiang.goods.vo.stock;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * ERP 物理仓同步请求/响应 DTO（goods 侧）。
 * <p>
 * 与 ERP 侧 {@code ErpStockSyncRequest} 字段保持一致，
 * 但作为跨服务契约各自持有副本，避免编译期依赖。
 * 字段变更必须两侧同步 —— 契约以本文档注释为准。
 *
 * @author Amos
 * @since 2026-10-16
 */
public class ErpStockSyncRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 幂等键：ERP 流水号（行级会拼 #skuId）。 */
    private String erpFlowNo;

    /** 商城仓库ID（由 ERP 的 hou_erp_store_map 映射而来）。 */
    private Long warehouseId;

    private String docNo;
    private String docType;
    private String occurTime;

    /** 1=出库，-1=入库。 */
    private Integer direction;

    private List<Item> items;

    public String getErpFlowNo() {
        return erpFlowNo;
    }

    public void setErpFlowNo(String erpFlowNo) {
        this.erpFlowNo = erpFlowNo;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(String occurTime) {
        this.occurTime = occurTime;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    /** 同步明细行。 */
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long skuId;
        private Integer qty;
        private BigDecimal cost;
        private String batchNo;

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public Integer getQty() {
            return qty;
        }

        public void setQty(Integer qty) {
            this.qty = qty;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }
    }

    /** 同步响应。 */
    public static class Result implements Serializable {

        private static final long serialVersionUID = 1L;

        private Boolean success;
        private String message;
        private Integer affectedRows;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getAffectedRows() {
            return affectedRows;
        }

        public void setAffectedRows(Integer affectedRows) {
            this.affectedRows = affectedRows;
        }
    }
}