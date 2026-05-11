package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.DiemThiXetTuyen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiemThiRepository extends JpaRepository<DiemThiXetTuyen, Integer> {
    Optional<DiemThiXetTuyen> findByCccd(String cccd);
}
