package com.sweetbalance.backend.repository.refresh;

import com.sweetbalance.backend.entity.RefreshEntity;

public interface RefreshTokenRepository {

    boolean existsByRefresh(String refresh);

    void deleteByRefresh(String refresh);

    void save(RefreshEntity refreshEntity);
}
