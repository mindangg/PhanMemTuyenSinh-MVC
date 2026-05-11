package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.NganhToHop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NganhToHopRepository extends JpaRepository<NganhToHop, Integer> {
    List<NganhToHop> findByMaNganh(String maNganh);
}
