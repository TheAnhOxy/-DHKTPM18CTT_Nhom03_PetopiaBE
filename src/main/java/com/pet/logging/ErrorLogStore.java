package com.pet.logging;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class ErrorLogStore {

    private static final int DEFAULT_MAX_SIZE = 200;

    private final Deque<ErrorLogEntry> recentLogs = new ArrayDeque<>();

    public synchronized void add(ErrorLogEntry entry) {
        if (entry == null) {
            return;
        }
        recentLogs.addFirst(entry);
        if (recentLogs.size() > DEFAULT_MAX_SIZE) {
            recentLogs.removeLast();
        }
    }

    public synchronized List<ErrorLogEntry> getRecent(int limit) {
        int safeLimit = Math.max(0, Math.min(limit, recentLogs.size()));
        List<ErrorLogEntry> result = new ArrayList<>(safeLimit);
        int count = 0;
        for (ErrorLogEntry entry : recentLogs) {
            if (count >= safeLimit) {
                break;
            }
            result.add(entry);
            count++;
        }
        return result;
    }
}

