package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.NganhToHop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NganhToHopRepository extends JpaRepository<NganhToHop, Integer> {
    List<NganhToHop> findByMaNganh(String maNganh);
}
