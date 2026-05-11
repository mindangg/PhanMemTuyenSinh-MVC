package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.ToHopMonThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToHopRepository extends JpaRepository<ToHopMonThi, Integer> {
    Optional<ToHopMonThi> findByMaToHop(String maToHop);
}
