package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.util.List;

public interface CreateExpedientBuildDfdRequestUseCase {
    Mono<ExpedientRequest> buildDfdRequest(Tuple4<String, String, String, String> titleAndCenterData, List<ExpedientRequest.Clause> clauses, List<DocumentSignature> documentSignatures, CreateExpedientRequest request, String origin,
            List<ProductDocumentParameters> documentTypes, List<TradeSignerDto> signers, Long originId);
}
