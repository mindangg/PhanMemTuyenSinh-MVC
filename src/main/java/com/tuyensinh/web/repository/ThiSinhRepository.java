package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.ThiSinh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThiSinhRepository extends JpaRepository<ThiSinh, Integer> {
    Optional<ThiSinh> findByCccd(String cccd);
}
