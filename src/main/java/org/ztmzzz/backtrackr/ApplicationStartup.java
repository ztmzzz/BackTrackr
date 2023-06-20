package org.ztmzzz.backtrackr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup {

    private boolean hasRun = false;

    private final OCR ocr;

    @Autowired
    public ApplicationStartup(OCR ocr) {
        this.ocr = ocr;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        if (hasRun) {
            return;
        }
        hasRun = true;

        ocr.startService();

    }
}
