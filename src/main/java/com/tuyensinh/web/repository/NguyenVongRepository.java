package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.NguyenVongXetTuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NguyenVongRepository extends JpaRepository<NguyenVongXetTuyen, Integer> {
    List<NguyenVongXetTuyen> findByCccdOrderByThuTuNguyenVongAsc(String cccd);
}
