
package com.acelera.fx.digitalsignature.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.domain.port.client.ExternalSsccStatusClient;
import com.acelera.fx.digitalsignature.domain.port.repository.OperationFwdRepository;
import com.acelera.fx.digitalsignature.domain.port.repository.SignatureExpedientRepository;
import com.acelera.fx.digitalsignature.domain.port.service.UpdateSignatureExpedientStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class UpdateSignatureExpedientStatusServiceImpl implements UpdateSignatureExpedientStatusService {

  private final SignatureExpedientRepository signatureRepository;
  private final OperationFwdRepository operationRepository;
  private final ExternalSsccStatusClient ssccClient;

  @Override
  public Mono<Void> updateStatus(String entity, Long expedientId, String status) {
    return signatureRepository.findByExpedientId(entity, expedientId)
        .switchIfEmpty(Mono.error(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró expediente con id " + expedientId)))
        .flatMap(tradeSignature -> {

          if ("OK".equals(tradeSignature.getValidatedBo())) {
            log.info("Expediente {} ya tiene validatedBo=OK, saltando procesamiento", expedientId);
            return Mono.empty();
          }

          if (tradeSignature.getIndicatorSSCC() != null && "Y".equals(tradeSignature.getIndicatorSSCC())) {
            log.info("Expediente con id {} tiene INDICATORSSCC=Y, procesando actualización", expedientId);
            Long originId = tradeSignature.getOriginId();
            if (originId == null) {
              return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                  "No se encontro ORIGINID para el expediente " + expedientId));
            }

            return operationRepository.findContratoPartenon(originId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró CONTRATOPARTENON para ORIGINID " + originId)))
                .flatMap(contratoPartenon -> {
                  if (contratoPartenon.isEmpty()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "CONTRATOPARTENON vacío para ORIGINID " + originId));
                  }

                  log.info("Llamando a servicio SSCC para contrato {} y expediente {}", contratoPartenon, expedientId);

                  return ssccClient.notifySignatureCompletion(contratoPartenon)
                      .then(actualizarExpediente(tradeSignature, "OK")).onErrorResume(e -> {
                        log.error("Error al notificar a SSCC o guardar el expediente: {}", e.getMessage());
                        return actualizarExpediente(tradeSignature, "KO")
                            .then(Mono.error(new RuntimeException("Error al notificar a SSCC: " + e.getMessage())));
                      });
                });

          } else {
            log.info("No se requiere actualización SSCC para expediente con id {} (INDICATORSSCC≠Y)", expedientId);
            return Mono.empty();
          }
        }).onErrorResume(e -> {
          log.error("Error en updateStatus para expediente {}: {}", expedientId, e.getMessage());
          if (e instanceof ResponseStatusException) {
            return Mono.error(e);
          }
          return Mono.error(new RuntimeException("Error procesando expediente: " + e.getMessage()));
        });
  }

  private Mono<Void> actualizarExpediente(TradeSignature tradeSignature, String validatedBO) {
    tradeSignature.setValidatedBo(validatedBO);
    return signatureRepository.updateExpedientValidatedBo(tradeSignature).then()
        .doOnSubscribe(
            subscription -> log.info("Iniciando actualización expediente {}", tradeSignature.getTradeSignatureId()))
        .doOnSuccess(saved -> log.info("Expediente actualizado exitosamente: {}", tradeSignature.getTradeSignatureId()))
        .doOnError(e -> log.error("Error al actualizar el expediente: {}", tradeSignature.getTradeSignatureId(), e));
  }
}
