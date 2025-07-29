package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureServicePostImpl implements TradeSignatureServicePost {

    private final ProductDocumentsService productDocumentsService;

    @Override
    public Mono<StartSignatureResponseDto> startSignatureWorkflow(String entity, Locale locale, Long originId,
            StartSignatureRequestDto dto) {

        // 1. documentos del producto:
        var documents = productDocumentsService.findProductDocumentType(entity, locale, dto.getProductId());

        Flux<String> resul= documents.map(this::get4676);

        //TODO Implement method
        return Mono.just(StartSignatureResponseDto.builder().expedientId(originId).build());
    }

    private String get4676(ProductDocumentParameters document) {
        log.info("Documento: {} - {}", document.getDocumentType(), document.getDocumentalTypeDoc());

        return "KD111727202503071120Pzb.pdf";
    }
}
