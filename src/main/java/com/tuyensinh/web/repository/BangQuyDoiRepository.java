package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.BangQuyDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BangQuyDoiRepository extends JpaRepository<BangQuyDoi, Integer> {
    List<BangQuyDoi> findByPhuongThucAndMonOrderByDiemAAsc(String phuongThuc, String mon);
    List<BangQuyDoi> findByPhuongThuc(String phuongThuc);
}
