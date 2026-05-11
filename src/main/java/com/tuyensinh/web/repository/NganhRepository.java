package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.Nganh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NganhRepository extends JpaRepository<Nganh, Integer> {
    Optional<Nganh> findByMaNganh(String maNganh);
    List<Nganh> findAllByOrderByMaNganhAsc();
}
