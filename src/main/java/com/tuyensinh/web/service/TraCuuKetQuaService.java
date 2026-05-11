package com.tuyensinh.web.service;

import com.tuyensinh.web.dto.KetQuaTraCuuVO;
import com.tuyensinh.web.entity.Nganh;
import com.tuyensinh.web.entity.NguyenVongXetTuyen;
import com.tuyensinh.web.repository.NganhRepository;
import com.tuyensinh.web.repository.NguyenVongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TraCuuKetQuaService {

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Autowired
    private NganhRepository nganhRepository;

    public List<KetQuaTraCuuVO> traKetQua(String cccd) {
        List<NguyenVongXetTuyen> danhSach = nguyenVongRepository.findByCccdOrderByThuTuNguyenVongAsc(cccd);
        List<KetQuaTraCuuVO> result = new ArrayList<>();

        for (NguyenVongXetTuyen nv : danhSach) {
            Nganh nganh = nganhRepository.findByMaNganh(nv.getMaNganh()).orElse(null);
            KetQuaTraCuuVO vo = new KetQuaTraCuuVO();
            vo.setThuTuNguyenVong(nv.getThuTuNguyenVong());
            vo.setMaNganh(nv.getMaNganh());
            vo.setTenNganh(nganh != null ? nganh.getTenNganh() : nv.getMaNganh());
            vo.setToHopMon(nv.getToHopMon());
            vo.setPhuongThuc(nv.getPhuongThuc());
            vo.setDiemXetTuyen(nv.getDiemXetTuyen());
            vo.setKetQua(nv.getKetQua());
            vo.setDiemTrungTuyen(nganh != null ? nganh.getDiemTrungTuyen() : null);
            result.add(vo);
        }
        return result;
    }
}
