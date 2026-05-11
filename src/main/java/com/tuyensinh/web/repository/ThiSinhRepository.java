package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.ThiSinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThiSinhRepository extends JpaRepository<ThiSinh, Integer> {
    Optional<ThiSinh> findByCccd(String cccd);
}
