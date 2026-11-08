package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.app.services.PrintFacadeService;
import java.util.Objects;
import org.openapitools.api.PrintApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Provides PDF representations of persisted execution results. */
@RestController
public class PrintController implements PrintApi {

    private final PrintFacadeService prints;
    private final PrintRequestValidator requests;
    private final PrintResponseAssembler responses;

    PrintController(PrintFacadeService prints, PrintRequestValidator requests, PrintResponseAssembler responses) {
        this.prints = Objects.requireNonNull(prints, "Print facade service is required");
        this.requests = Objects.requireNonNull(requests, "Print request validator is required");
        this.responses = Objects.requireNonNull(responses, "Print response assembler is required");
    }

    @Override
    public ResponseEntity<Resource> printPathResultInTestSet(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId, Long pathResultId) {
        requests.path(applicationId, stageId, testRunId, testSetResultId, pathResultId);
        PrintFacadeService.TestPathPrintData testPathPrintData = new PrintFacadeService.TestPathPrintData(applicationId,
                stageId, testRunId, testSetResultId, pathResultId);
        return prints.printPath(testPathPrintData)
                     .map(responses::pdf)
                     .orElseGet(() -> ResponseEntity.notFound()
                                                    .build());
    }

    @Override
    public ResponseEntity<Resource> printTestSetResultInTestRun(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId) {
        requests.testSet(applicationId, stageId, testRunId, testSetResultId);
        PrintFacadeService.TestSetPrintData testSetPrintData = new PrintFacadeService.TestSetPrintData(applicationId,
                stageId, testRunId, testSetResultId);
        return prints.printTestSet(testSetPrintData)
                     .map(responses::pdf)
                     .orElseGet(() -> ResponseEntity.notFound()
                                                    .build());
    }

    @Override
    public ResponseEntity<Resource> printTestRun(Long applicationId, Long stageId, Long testRunId) {
        requests.testRun(applicationId, stageId, testRunId);
        PrintFacadeService.TestRunPrintData testRunPrintData = new PrintFacadeService.TestRunPrintData(applicationId,
                stageId, testRunId);
        return prints.printTestRun(testRunPrintData)
                     .map(responses::pdf)
                     .orElseGet(() -> ResponseEntity.notFound()
                                                    .build());
    }

}
