package com.acelera.fx.digitalsignature.application.service;

import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientDocumentStep;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientFinalStep;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientTradeSignatureStep;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.service.CreateTradeSignatureExpedientService;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static com.acelera.fx.digitalsignature.infrastructure.util.TradeSignatureConstants.ORIGIN_EVENT;
import static com.acelera.fx.digitalsignature.infrastructure.util.TradeSignatureConstants.ORIGIN_TRADE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTradeSignatureExpedientServiceImpl implements CreateTradeSignatureExpedientService {

    private final TradeSignerHelper tradeSignerHelper;

    private final CreateExpedientTradeSignatureStep createExpedientTradeSignatureStep;
    private final CreateExpedientDocumentStep createExpedientDocumentStep;
    private final CreateExpedientFinalStep createExpedientFinalStep;

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        String origin = tradeSignerHelper.isEventProduct(request.getProductId()) ? ORIGIN_EVENT : ORIGIN_TRADE;
        return createExpedientTradeSignatureStep.obtainTradeSignature(entity, originId, request)
            .flatMap(tradeSignature -> createExpedientTradeSignatureStep.obtainSigners(locale, entity, tradeSignature, originId, origin)
                    .flatMap(signers -> createExpedientDocumentStep.obtainDocumentTypes(entity, locale, request.getProductId())
                            .flatMap(documentTypes -> createExpedientFinalStep
                                    .createExpedient(documentTypes, locale, entity, originId, request, origin, signers, tradeSignature)
                            )
                    )
            );
    }

}
