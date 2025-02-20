package com.sweetbalance.backend.repository.refresh;

import com.sweetbalance.backend.entity.RefreshEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Primary;

public class RelationalRefreshTokenRepository implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    public RelationalRefreshTokenRepository(JpaRefreshTokenRepository jpaRefreshTokenRepository) {
        this.jpaRefreshTokenRepository = jpaRefreshTokenRepository;
    }

    @Override
    public boolean existsByRefresh(String refresh) {
        return jpaRefreshTokenRepository.existsByRefresh(refresh);
    }

    @Override
    public void deleteByRefresh(String refresh) {
        jpaRefreshTokenRepository.deleteByRefresh(refresh);
    }

    @Override
    public void save(RefreshEntity refreshEntity) {
        jpaRefreshTokenRepository.save(refreshEntity);
    }
}
