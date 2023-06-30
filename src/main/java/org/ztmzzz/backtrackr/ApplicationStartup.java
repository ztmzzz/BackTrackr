package org.ztmzzz.backtrackr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup {

    private boolean hasRun = false;

    private final OCRProcessor ocrProcessor;

    @Autowired
    public ApplicationStartup(OCRProcessor ocrProcessor) {
        this.ocrProcessor = ocrProcessor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        if (hasRun) {
            return;
        }
        hasRun = true;
//        ocrProcessor.startService();

    }
}
