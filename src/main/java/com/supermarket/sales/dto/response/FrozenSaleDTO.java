package com.supermarket.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FrozenSaleDTO {
    private Long saleId;
    private LocalDateTime frozenAt;
    private Integer itemCount;
    private BigDecimal total;

    public FrozenSaleDTO() {
    }

    public FrozenSaleDTO(Long saleId, LocalDateTime frozenAt, Integer itemCount, BigDecimal total) {
        this.saleId = saleId;
        this.frozenAt = frozenAt;
        this.itemCount = itemCount;
        this.total = total;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public LocalDateTime getFrozenAt() {
        return frozenAt;
    }

    public void setFrozenAt(LocalDateTime frozenAt) {
        this.frozenAt = frozenAt;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
