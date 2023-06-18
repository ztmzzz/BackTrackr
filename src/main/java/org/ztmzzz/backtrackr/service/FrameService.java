package org.ztmzzz.backtrackr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ztmzzz.backtrackr.dao.FrameRepository;
import org.ztmzzz.backtrackr.entity.Frame;

import java.sql.Timestamp;

@Service
public class FrameService {

    private final FrameRepository frameRepository;

    @Autowired
    public FrameService(FrameRepository frameRepository) {
        this.frameRepository = frameRepository;
    }

    public Frame save(Frame frame) {
        return frameRepository.save(frame);
    }

    public Frame findById(Long id) {
        return frameRepository.findById(id).orElse(null);
    }
}
