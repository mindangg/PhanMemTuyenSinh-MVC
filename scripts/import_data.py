"""
Import dữ liệu từ Excel → SQL cho database xettuyen2026.

Cách dùng:
  python scripts/import_data.py

Output: scripts/output/ — chứa các file .sql chạy theo thứ tự 01 → 05.
Riêng xt_bangquydoi: chạy docs/xt_bachphanvi.sql.txt trực tiếp vào MySQL.

Yêu cầu: pip install pandas openpyxl
"""

import pandas as pd
import re
import os
from pathlib import Path

DOCS = Path(__file__).parent.parent / "docs"
OUT = Path(__file__).parent / "output"
OUT.mkdir(exist_ok=True)

KV_MAP = {"1": "KV1", "2NT": "KV2-NT", "2": "KV2", "3": "KV3"}
DT_MAP = {str(i): f"UT{i}" for i in range(1, 8)}


def v(val):
    """Escape string value → SQL string hoặc NULL."""
    if val is None:
        return "NULL"
    if isinstance(val, float) and pd.isna(val):
        return "NULL"
    s = str(val).strip()
    if s in ("nan", "", "None"):
        return "NULL"
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"


def n(val):
    """Numeric value → SQL number hoặc NULL."""
    if val is None:
        return "NULL"
    if isinstance(val, float) and pd.isna(val):
        return "NULL"
    s = str(val).strip()
    if s in ("nan", "", "None"):
        return "NULL"
    try:
        return str(float(s))
    except ValueError:
        return "NULL"


def parse_tohop(ma_to_hop_full):
    """
    'B03(TO-3,VA-3,SI-1)' → {'mon1':'TO','hs1':3,'mon2':'VA','hs2':3,'mon3':'SI','hs3':1}
    """
    result = {}
    match = re.search(r"\((.+)\)", str(ma_to_hop_full))
    if not match:
        return result
    for i, part in enumerate(match.group(1).split(",")[:3], 1):
        m = re.match(r"(\w+)-(\d+)", part.strip())
        if m:
            result[f"mon{i}"] = m.group(1).upper()
            result[f"hs{i}"] = int(m.group(2))
    return result


def split_ho_ten(full_name):
    """'Nguyễn Văn An' → ('Nguyễn Văn', 'An')"""
    parts = str(full_name).strip().rsplit(" ", 1)
    return (parts[0], parts[1]) if len(parts) == 2 else (parts[0], parts[0])


def compute_password(ngay_sinh):
    """'25/07/2007' → '25072007'"""
    s = str(ngay_sinh).strip()
    if re.match(r"\d{2}/\d{2}/\d{4}", s):
        return s.replace("/", "")
    if re.match(r"\d{4}-\d{2}-\d{2}", s):
        p = s.split("-")
        return p[2] + p[1] + p[0]
    if re.match(r"\d{8}", s):
        return s
    return ""


# ─────────────────────────────────────────────
# 1. xt_nganh
# ─────────────────────────────────────────────
def gen_nganh():
    df_ct = pd.read_excel(DOCS / "Chi tieu 2025.xlsx", dtype=str, header=1)
    df_ct.columns = ["tt", "manganh", "tennganh", "chitieu"]
    df_ct = df_ct.dropna(subset=["manganh"])
    df_ct = df_ct[df_ct["manganh"].str.match(r"^\d+")]

    df_ng = pd.read_excel(DOCS / "Nguong dau vao 2025.xlsx", dtype=str)
    df_ng.columns = ["stt", "manganh", "tennganh", "diemsan"]
    df_ng = df_ng.dropna(subset=["manganh"])
    nguong_map = dict(zip(df_ng["manganh"].str.strip(), df_ng["diemsan"].str.strip()))

    df_th = pd.read_excel(DOCS / "tohopmon.xlsx", dtype=str)
    goc_map = {}
    for _, row in df_th.iterrows():
        if str(row.get("Gốc", "")).strip() == "Gốc":
            goc_map[str(row["MANGANH"]).strip()] = str(row["TEN_TO_HOP"]).strip()

    rows = []
    for _, r in df_ct.iterrows():
        ma = str(r["manganh"]).strip()
        rows.append(
            f"  ({v(ma)}, {v(r['tennganh'])}, {n(r['chitieu'])}, "
            f"{n(nguong_map.get(ma))}, {v(goc_map.get(ma))})"
        )

    return (
        "-- 01: xt_nganh\n"
        "INSERT IGNORE INTO xt_nganh (manganh, tennganh, n_chitieu, n_diemsan, n_tohopgoc) VALUES\n"
        + ",\n".join(rows) + ";"
    )


# ─────────────────────────────────────────────
# 2. xt_nganh_tohop
# ─────────────────────────────────────────────
def gen_nganh_tohop():
    df = pd.read_excel(DOCS / "tohopmon.xlsx", dtype=str)
    SUBJECT_FLAGS = ["N1", "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "KHAC", "KTPL"]

    rows = []
    for _, r in df.iterrows():
        ma_nganh = str(r["MANGANH"]).strip()
        ma_tohop = str(r["TEN_TO_HOP"]).strip()
        tb_keys = str(r["tb_keys"]).strip()
        dolech = n(r.get("Độ lệch"))

        d = parse_tohop(r["MA_TO_HOP"])
        mon1, hs1 = d.get("mon1"), d.get("hs1", 1)
        mon2, hs2 = d.get("mon2"), d.get("hs2", 1)
        mon3, hs3 = d.get("mon3"), d.get("hs3", 1)

        subjects_in = {mon1, mon2, mon3} - {None}
        flags = ", ".join(
            "1" if s in subjects_in else "NULL"
            for s in SUBJECT_FLAGS
        )

        rows.append(
            f"  ({v(ma_nganh)}, {v(ma_tohop)}, "
            f"{v(mon1)}, {n(hs1)}, {v(mon2)}, {n(hs2)}, {v(mon3)}, {n(hs3)}, "
            f"{v(tb_keys)}, {dolech}, {flags})"
        )

    return (
        "-- 02: xt_nganh_tohop\n"
        "INSERT IGNORE INTO xt_nganh_tohop "
        "(manganh, matohop, th_mon1, hsmon1, th_mon2, hsmon2, th_mon3, hsmon3, "
        "tb_keys, dolech, N1, `TO`, LI, HO, SI, VA, SU, DI, TI, KHAC, KTPL) VALUES\n"
        + ",\n".join(rows) + ";"
    )


# ─────────────────────────────────────────────
# 3. xt_thisinhxettuyen25
# ─────────────────────────────────────────────
def gen_thisinhxettuyen():
    df = pd.read_excel(DOCS / "Ds thi sinh.xlsx", sheet_name="Sheet1", dtype=str)
    rows = []
    for _, r in df.iterrows():
        cccd = str(r.get("CCCD", "")).strip()
        if not cccd or cccd == "nan":
            continue
        ho, ten = split_ho_ten(r.get("Họ Tên", ""))
        ngay_sinh = str(r.get("Ngày sinh", "")).strip()
        password = compute_password(ngay_sinh)
        gioi_tinh = str(r.get("Giới tính", "")).strip()
        dt_raw = str(r.get("ĐTƯT", "")).strip()
        kv_raw = str(r.get("KVƯT", "")).strip()
        doi_tuong = DT_MAP.get(dt_raw)
        khu_vuc = KV_MAP.get(kv_raw)
        noi_sinh = str(r.get("Nơi sinh", "")).strip()

        rows.append(
            f"  ({v(cccd)}, {v(ho)}, {v(ten)}, {v(ngay_sinh)}, "
            f"{v(gioi_tinh)}, {v(doi_tuong)}, {v(khu_vuc)}, {v(password)}, {v(noi_sinh)})"
        )

    return (
        "-- 03: xt_thisinhxettuyen25\n"
        "INSERT IGNORE INTO xt_thisinhxettuyen25 "
        "(cccd, ho, ten, ngay_sinh, gioi_tinh, doi_tuong, khu_vuc, password, noi_sinh) VALUES\n"
        + ",\n".join(rows) + ";"
    )


# ─────────────────────────────────────────────
# 4. xt_diemthixettuyen
# ─────────────────────────────────────────────
def gen_diemthi():
    df = pd.read_excel(DOCS / "Ds thi sinh.xlsx", sheet_name="Sheet1", dtype=str)
    rows = []
    for _, r in df.iterrows():
        cccd = str(r.get("CCCD", "")).strip()
        if not cccd or cccd == "nan":
            continue
        to  = n(r.get("TO"));  va  = n(r.get("VA"));  li  = n(r.get("LI"))
        ho  = n(r.get("HO"));  si  = n(r.get("SI"));  su  = n(r.get("SU"))
        di  = n(r.get("DI"));  ti  = n(r.get("TI"));  ktpl = n(r.get("KTPL"))
        cncn = n(r.get("CNCN")); cnnn = n(r.get("CNNN"))
        n1_thi = n(r.get("NN"))

        rows.append(
            f"  ({v(cccd)}, {to}, {va}, {li}, {ho}, {si}, {su}, {di}, {ti}, {ktpl}, "
            f"{cncn}, {cnnn}, {n1_thi})"
        )

    return (
        "-- 04: xt_diemthixettuyen\n"
        "INSERT IGNORE INTO xt_diemthixettuyen "
        "(cccd, `TO`, VA, LI, HO, SI, SU, DI, TI, KTPL, CNCN, CNNN, N1_THI) VALUES\n"
        + ",\n".join(rows) + ";"
    )


# ─────────────────────────────────────────────
# 5. xt_nguyenvongxettuyen
# ─────────────────────────────────────────────
def gen_nguyenvong():
    rows = []
    for sheet in ["Sheet1", "Sheet2"]:
        try:
            df = pd.read_excel(
                DOCS / "Nguyenvong.xlsx", sheet_name=sheet,
                dtype=str, header=3
            )
            df.columns = ["stt", "cccd", "thu_tu", "ma_truong", "ten_truong",
                          "ma_nganh", "ten_nganh", "tuyen_thang"]
            df = df.dropna(subset=["cccd", "ma_nganh"])
            df = df[df["cccd"] != "CCCD"]
            for _, r in df.iterrows():
                cccd = str(r["cccd"]).strip()
                ma_nganh = str(r["ma_nganh"]).strip()
                thu_tu = str(r["thu_tu"]).strip()
                if not cccd or not ma_nganh or cccd == "nan":
                    continue
                nv_keys = f"{cccd}_NV{thu_tu}"
                rows.append(
                    f"  ({v(cccd)}, {v(ma_nganh)}, {n(thu_tu)}, {v(nv_keys)})"
                )
        except Exception as e:
            print(f"  Warning [{sheet}]: {e}")

    return (
        "-- 05: xt_nguyenvongxettuyen\n"
        "INSERT IGNORE INTO xt_nguyenvongxettuyen "
        "(nn_cccd, nv_manganh, nv_tt, nv_keys) VALUES\n"
        + ",\n".join(rows) + ";"
    )


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────
if __name__ == "__main__":
    tasks = [
        ("01_nganh.sql",           gen_nganh),
        ("02_nganh_tohop.sql",     gen_nganh_tohop),
        ("03_thisinhxettuyen.sql", gen_thisinhxettuyen),
        ("04_diemthi.sql",         gen_diemthi),
        ("05_nguyenvong.sql",      gen_nguyenvong),
    ]
    for filename, fn in tasks:
        try:
            sql = fn()
            path = OUT / filename
            path.write_text(sql, encoding="utf-8")
            line_count = sql.count("\n")
            print(f"  OK  {filename}  ({line_count} dòng)")
        except Exception as e:
            print(f"  ERR {filename}: {e}")

    print(f"\nSQL files generated: {OUT}")
    print("Run in order 01 -> 05 in MySQL Workbench.")
    print("xt_bangquydoi: run docs/xt_bachphanvi.sql.txt directly.")
