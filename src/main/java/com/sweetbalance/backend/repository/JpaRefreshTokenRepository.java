package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.RefreshEntity;
import org.springframework.beans.factory.annotation.Autowired;

public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final JpaRefreshRepository jpaRefreshRepository;

    @Autowired
    public JpaRefreshTokenRepository(JpaRefreshRepository jpaRefreshRepository) {
        this.jpaRefreshRepository = jpaRefreshRepository;
    }

    @Override
    public boolean existsByRefresh(String refresh) {
        return jpaRefreshRepository.existsByRefresh(refresh);
    }

    @Override
    public void deleteByRefresh(String refresh) {
        jpaRefreshRepository.deleteByRefresh(refresh);
    }

    @Override
    public void save(RefreshEntity refreshEntity) {
        jpaRefreshRepository.save(refreshEntity);
    }
}
