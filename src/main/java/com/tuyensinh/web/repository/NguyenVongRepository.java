package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.NguyenVongXetTuyen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NguyenVongRepository extends JpaRepository<NguyenVongXetTuyen, Integer> {
    List<NguyenVongXetTuyen> findByCccdOrderByThuTuNguyenVongAsc(String cccd);
}
