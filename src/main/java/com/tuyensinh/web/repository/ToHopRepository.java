package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.ToHopMonThi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToHopRepository extends JpaRepository<ToHopMonThi, Integer> {
    Optional<ToHopMonThi> findByMaToHop(String maToHop);
}
