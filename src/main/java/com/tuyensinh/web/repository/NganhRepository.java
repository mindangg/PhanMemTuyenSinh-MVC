package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.Nganh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NganhRepository extends JpaRepository<Nganh, Integer> {
    Optional<Nganh> findByMaNganh(String maNganh);
    List<Nganh> findByMaNganhIn(List<String> maNganhList);
    List<Nganh> findAllByOrderByMaNganhAsc();
}
