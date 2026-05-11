package com.tuyensinh.web.repository;

import com.tuyensinh.web.entity.BangQuyDoi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BangQuyDoiRepository extends JpaRepository<BangQuyDoi, Integer> {
    List<BangQuyDoi> findByPhuongThucAndMonOrderByDiemAAsc(String phuongThuc, String mon);
    List<BangQuyDoi> findByPhuongThuc(String phuongThuc);
}
