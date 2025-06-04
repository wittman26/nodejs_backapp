package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.FxTradeSignatureExpedientView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FxTradeSignatureExpedientViewRepository extends JpaRepository<FxTradeSignatureExpedientView, Long> {
    List<FxTradeSignatureExpedientView> findByEntity(String entity);
    List<FxTradeSignatureExpedientView> findByExpedientId(Long expedientId);
}

package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.FxTradeSignerDocumentStatusView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FxTradeSignerDocumentStatusViewRepository extends JpaRepository<FxTradeSignerDocumentStatusView, Long> {
    List<FxTradeSignerDocumentStatusView> findByTradeSignatureId(Long tradeSignatureId);
    List<FxTradeSignerDocumentStatusView> findBySignerId(String signerId);
}