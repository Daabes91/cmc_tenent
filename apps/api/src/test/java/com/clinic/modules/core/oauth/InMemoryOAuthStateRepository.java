package com.clinic.modules.core.oauth;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * In-memory implementation of OAuthStateRepository for testing purposes.
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class InMemoryOAuthStateRepository implements OAuthStateRepository {
    
    private final Map<Long, OAuthStateEntity> storage = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<OAuthStateEntity> findByStateToken(String stateToken) {
        Long id = tokenIndex.get(stateToken);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<OAuthStateEntity> findByTenantSlug(String tenantSlug) {
        List<OAuthStateEntity> result = new ArrayList<>();
        for (OAuthStateEntity state : storage.values()) {
            if (state.getTenantSlug().equals(tenantSlug)) {
                result.add(state);
            }
        }
        return result;
    }

    @Override
    public int deleteExpiredStates(Instant now) {
        int count = 0;
        Iterator<Map.Entry<Long, OAuthStateEntity>> iterator = storage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, OAuthStateEntity> entry = iterator.next();
            if (entry.getValue().getExpiresAt().isBefore(now)) {
                tokenIndex.remove(entry.getValue().getStateToken());
                iterator.remove();
                count++;
            }
        }
        return count;
    }

    @Override
    public int deleteConsumedStates(Instant cutoffTime) {
        int count = 0;
        Iterator<Map.Entry<Long, OAuthStateEntity>> iterator = storage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, OAuthStateEntity> entry = iterator.next();
            OAuthStateEntity state = entry.getValue();
            if (state.isConsumed() && state.getCreatedAt().isBefore(cutoffTime)) {
                tokenIndex.remove(state.getStateToken());
                iterator.remove();
                count++;
            }
        }
        return count;
    }

    @Override
    public <S extends OAuthStateEntity> S save(S entity) {
        // Use reflection to set the ID if it's null
        try {
            java.lang.reflect.Field idField = OAuthStateEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            if (idField.get(entity) == null) {
                idField.set(entity, idGenerator.getAndIncrement());
            }
            
            // Trigger @PrePersist if createdAt is null
            if (entity.getCreatedAt() == null) {
                entity.onCreate();
            }
            
            Long id = (Long) idField.get(entity);
            storage.put(id, entity);
            tokenIndex.put(entity.getStateToken(), id);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    @Override
    public <S extends OAuthStateEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<OAuthStateEntity> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public List<OAuthStateEntity> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<OAuthStateEntity> findAllById(Iterable<Long> ids) {
        List<OAuthStateEntity> result = new ArrayList<>();
        for (Long id : ids) {
            OAuthStateEntity entity = storage.get(id);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(Long id) {
        OAuthStateEntity entity = storage.remove(id);
        if (entity != null) {
            tokenIndex.remove(entity.getStateToken());
        }
    }

    @Override
    public void delete(OAuthStateEntity entity) {
        try {
            java.lang.reflect.Field idField = OAuthStateEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            Long id = (Long) idField.get(entity);
            if (id != null) {
                deleteById(id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends OAuthStateEntity> entities) {
        for (OAuthStateEntity entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        storage.clear();
        tokenIndex.clear();
    }

    // Unsupported operations for testing
    @Override
    public void flush() {
        // No-op for in-memory
    }

    @Override
    public <S extends OAuthStateEntity> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends OAuthStateEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<OAuthStateEntity> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public OAuthStateEntity getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public OAuthStateEntity getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public OAuthStateEntity getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends OAuthStateEntity> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity> long count(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public <S extends OAuthStateEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public List<OAuthStateEntity> findAll(Sort sort) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public Page<OAuthStateEntity> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented for testing");
    }
}
