package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.DiemThiXetTuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiemThiRepository extends JpaRepository<DiemThiXetTuyen, Integer> {
    Optional<DiemThiXetTuyen> findByCccd(String cccd);
}
