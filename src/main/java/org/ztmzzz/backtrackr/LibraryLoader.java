package org.ztmzzz.backtrackr;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

@Component
public class LibraryLoader {

    @PostConstruct
    public void loadLibraries() {
        OpenCV.loadLocally();
    }
}

