package com.tuyensinh.web.service;

import com.tuyensinh.web.dto.KetQuaTraCuuVO;
import com.tuyensinh.web.entity.Nganh;
import com.tuyensinh.web.entity.NguyenVongXetTuyen;
import com.tuyensinh.web.repository.NganhRepository;
import com.tuyensinh.web.repository.NguyenVongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TraCuuKetQuaService {

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Autowired
    private NganhRepository nganhRepository;

    @Transactional(readOnly = true)
    public List<KetQuaTraCuuVO> traKetQua(String cccd) {
        List<NguyenVongXetTuyen> danhSach = nguyenVongRepository.findByCccdOrderByThuTuNguyenVongAsc(cccd);
        if (danhSach.isEmpty()) return List.of();

        // Batch load tất cả ngành cần thiết trong 1 query thay vì N queries
        List<String> maNganhList = danhSach.stream()
                .map(NguyenVongXetTuyen::getMaNganh)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Nganh> nganhMap = nganhRepository.findByMaNganhIn(maNganhList)
                .stream()
                .collect(Collectors.toMap(Nganh::getMaNganh, n -> n));

        List<KetQuaTraCuuVO> result = new ArrayList<>();
        for (NguyenVongXetTuyen nv : danhSach) {
            Nganh nganh = nganhMap.get(nv.getMaNganh());
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
