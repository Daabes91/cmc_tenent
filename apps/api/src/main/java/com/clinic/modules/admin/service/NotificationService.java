package com.clinic.modules.admin.service;

import com.clinic.modules.admin.dto.AppointmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Create a new SSE connection for a client
     */
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            logger.debug("SSE emitter completed");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            logger.debug("SSE emitter timed out");
            emitter.complete();
            emitters.remove(emitter);
        });

        emitter.onError((ex) -> {
            logger.error("SSE emitter error: {}", ex.getMessage());
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        logger.info("New SSE connection established. Total connections: {}", emitters.size());

        // Send initial connection event asynchronously to avoid blocking
        new Thread(() -> {
            try {
                Thread.sleep(100); // Small delay to ensure connection is ready
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("{\"message\":\"Connected to notification stream\"}"));
                logger.debug("Sent initial connection event");
            } catch (Exception e) {
                logger.warn("Failed to send initial connection event: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }).start();

        return emitter;
    }

    /**
     * Broadcast a new appointment notification to all connected clients
     */
    public void broadcastNewAppointment(AppointmentResponse appointment) {
        logger.info("Broadcasting new appointment notification: {} (Total connections: {})",
                appointment.id(), emitters.size());

        if (emitters.isEmpty()) {
            logger.debug("No active SSE connections to broadcast to");
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-appointment")
                        .data(appointment, org.springframework.http.MediaType.APPLICATION_JSON));
                logger.debug("Sent appointment notification to emitter");
            } catch (Exception e) {
                logger.warn("Failed to send notification: {}, removing dead emitter", e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        // Clean up dead emitters
        emitters.removeAll(deadEmitters);
        if (!deadEmitters.isEmpty()) {
            logger.info("Removed {} dead connections. Active connections: {}",
                    deadEmitters.size(), emitters.size());
        }
    }

    /**
     * Get current number of active connections
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }

    /**
     * Close all connections (useful for shutdown)
     */
    public void closeAllConnections() {
        logger.info("Closing all SSE connections");
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }
}
