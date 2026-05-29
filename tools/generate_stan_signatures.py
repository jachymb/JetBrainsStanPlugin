#!/usr/bin/env python3
"""
Generate Stan function signature database from stanc3 Generate.ml.

Usage (from project root):
    python3 tools/generate_stan_signatures.py [path/to/stanc3/src]

Output: src/main/resources/stan_signatures.json
Re-run whenever stanc3 is updated.
"""

import re
import json
import sys
from itertools import product
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
PROJECT_DIR = SCRIPT_DIR.parent
STANC3_SRC = (Path(sys.argv[1]) if len(sys.argv) > 1
              else PROJECT_DIR.parent / "stanc3" / "src")
GENERATE_ML = STANC3_SRC / "stan_math_signatures" / "Generate.ml"
OUTPUT_JSON = PROJECT_DIR / "src" / "main" / "resources" / "stan_signatures.json"

# ── Type string helpers ────────────────────────────────────────────────────────

def arr(t, depth=0):
    for _ in range(depth):
        t = f"a[{t}]"
    return t

INT = "int";    REAL = "real";   COMPLEX = "complex"
VEC = "vector"; RVEC = "row_vector"; MAT = "matrix"
CVEC = "complex_vector"; CRVEC = "complex_row_vector"; CMAT = "complex_matrix"

TYPE_MAP = {
    "UInt": INT, "UReal": REAL, "UComplex": COMPLEX,
    "UVector": VEC, "URowVector": RVEC, "UMatrix": MAT,
    "UComplexVector": CVEC, "UComplexRowVector": CRVEC, "UComplexMatrix": CMAT,
}
BARE_TYPES = [INT, REAL, COMPLEX, VEC, RVEC, MAT, CVEC, CRVEC, CMAT]
DEEP_BASE         = [INT, REAL, RVEC, VEC, MAT]
DEEP_COMPLEX_BASE = [COMPLEX, CVEC, CRVEC, CMAT]

def expand_dim(d):
    if d == "DInt":       return [INT]
    if d == "DReal":      return [REAL]
    if d == "DVector":    return [VEC]
    if d == "DMatrix":    return [MAT]
    if d == "DIntArray":  return [arr(INT, 1)]
    if d == "DVInt":      return [INT, arr(INT, 1)]
    if d == "DVReal":     return [REAL, arr(REAL, 1), VEC, RVEC]
    if d == "DVComplex":  return [COMPLEX, arr(COMPLEX, 1), CVEC, CRVEC]
    if d == "DIntAndReals": return expand_dim("DVReal") + expand_dim("DVInt")
    if d == "DVectors":   return [VEC, arr(VEC, 1), RVEC, arr(RVEC, 1)]
    if d == "DComplexVectors": return [CVEC, arr(CVEC, 1), CRVEC, arr(CRVEC, 1)]
    if d == "DDeepVectorized":
        return [arr(b, depth) for b in DEEP_BASE for depth in range(8)]
    if d == "DDeepComplexVectorized":
        return [arr(b, depth) for b in DEEP_COMPLEX_BASE for depth in range(8)]
    return [d]

def ints_to_real(t):
    if t == INT: return REAL
    if t.startswith("a["): return "a[" + ints_to_real(t[2:-1]) + "]"
    return t

def complex_to_real(t):
    m = {COMPLEX: REAL, CVEC: VEC, CRVEC: RVEC, CMAT: MAT}
    if t in m: return m[t]
    if t.startswith("a["): return "a[" + complex_to_real(t[2:-1]) + "]"
    return t

def real_to_complex(t):
    m = {REAL: COMPLEX, VEC: CVEC, RVEC: CRVEC, MAT: CMAT}
    if t in m: return m[t]
    if t.startswith("a["): return "a[" + real_to_complex(t[2:-1]) + "]"
    return t

def is_primitive(t): return t in (INT, REAL)

def rng_return(rt, args):
    return rt if all(is_primitive(a) for a in args) else arr(rt, 1)

def all_combos(dims):
    return list(product(*[expand_dim(d) for d in dims]))

MISSING = {"beta_proportion_cdf", "loglogistic_lcdf"}
FULL_LPDF = ["Lpdf", "Rng", "Ccdf", "Cdf"]
FULL_LPMF = ["Lpmf", "Rng", "Ccdf", "Cdf"]

def fkind_suffixes(fk):
    return {"Lpmf": ["_lpmf"], "Lpdf": ["_lpdf"], "Rng": ["_rng"],
            "Cdf": ["_cdf", "_lcdf"], "Ccdf": ["_lccdf"]}.get(fk, [""])

# ── Declarative signature generators ──────────────────────────────────────────

def process_dist(fkinds, name, dims):
    out = []
    for fk in fkinds:
        if fk == "Rng":
            rt_dim, arg_dims = dims[0], dims[1:]
            promoted_rt = INT if rt_dim in ("DInt", "DIntArray", "DVInt") else REAL
            edims = ["DIntAndReals" if d == "DVReal" else d for d in arg_dims]
            for args in all_combos(edims):
                fn = name + "_rng"
                if fn not in MISSING:
                    out.append((fn, list(args), rng_return(promoted_rt, args)))
        else:
            for suffix in fkind_suffixes(fk):
                fn = name + suffix
                if fn not in MISSING:
                    for args in all_combos(dims):
                        out.append((fn, list(args), REAL))
    return out

def process_math(fkinds, name, dims):
    out = []
    for fk in fkinds:
        behavior = fk.replace("UnaryVectorized ", "")
        for args in all_combos(dims):
            first = args[0]
            if   behavior == "SameAsArg":      rt = first
            elif behavior == "IntsToReals":    rt = ints_to_real(first)
            elif behavior == "ComplexToReals": rt = complex_to_real(first)
            else:                              rt = REAL
            out.append((name, list(args), rt))
    return out

# ── Generate.ml parser ────────────────────────────────────────────────────────

def parse_utype(s):
    s = s.strip()
    # bare_array_type (UType, N) — literal numeric depth, e.g. bare_array_type (UReal, 2)
    m = re.match(r'bare_array_type\s*\(\s*(U\w+)\s*,\s*(\d+)\s*\)$', s)
    if m:
        base = TYPE_MAP.get(m.group(1))
        return arr(base, int(m.group(2))) if base else None
    depth = 0
    while s.startswith("UArray"):
        depth += 1
        s = s[6:].strip()
    base = TYPE_MAP.get(s)
    return None if base is None else arr(base, depth)

def parse_arg_list(s):
    if any(kw in s for kw in ("UFun", "UMathLibraryFunction")):
        return None
    parts = [p.strip() for p in re.split(r';', s) if p.strip()]
    result = []
    for p in parts:
        t = parse_utype(p)
        if t is None: return None
        result.append(t)
    return result

def parse_distributions(text):
    m = re.search(r'let distributions\s*=\s*\[(.*?)\](?=\s*\n\s*let\s)', text, re.DOTALL)
    if not m: return []
    block = m.group(1)
    out = []
    for em in re.finditer(r'\(\s*((?:full_lpdf|full_lpmf|\[[^\]]*\]))\s*,\s*"([^"]+)"\s*,\s*\[([^\]]*)\]', block, re.DOTALL):
        raw_fkinds, name, raw_dims = em.group(1).strip(), em.group(2), em.group(3)
        if   raw_fkinds == "full_lpdf": fkinds = FULL_LPDF
        elif raw_fkinds == "full_lpmf": fkinds = FULL_LPMF
        else: fkinds = re.findall(r'(?:Lpmf|Lpdf|Rng|Cdf|Ccdf)', raw_fkinds)
        dims = re.findall(r'D\w+', raw_dims)
        if dims: out.extend(process_dist(fkinds, name, dims))
    return out

def parse_math_sigs(text):
    m = re.search(r'let math_sigs\s*=\s*\[(.*?)\](?=\s*\n\s*let\s)', text, re.DOTALL)
    if not m: return []
    block = m.group(1)
    out = []
    for em in re.finditer(r'\(\s*\[([^\]]*)\]\s*,\s*"([^"]+)"\s*,\s*\[([^\]]*)\]', block, re.DOTALL):
        raw_fkinds, name, raw_dims = em.group(1).strip(), em.group(2), em.group(3)
        if   "SameAsArg"    in raw_fkinds: fkinds = ["UnaryVectorized SameAsArg"]
        elif "ComplexToReals" in raw_fkinds: fkinds = ["UnaryVectorized ComplexToReals"]
        else:                              fkinds = ["UnaryVectorized IntsToReals"]
        dims = re.findall(r'D\w+', raw_dims)
        if dims: out.extend(process_math(fkinds, name, dims))
    return out

def parse_explicit(text, sigs):
    """Parse add_unqualified and helper calls from the imperative section."""

    def add(name, args, ret):
        if name not in sigs: sigs[name] = set()
        sigs[name].add((tuple(args), ret))

    # Simple add_unqualified("name", ReturnType UType, [...], mem)
    for m in re.finditer(
        r'add_unqualified\s*\(\s*"([^"]+)"\s*,\s*'
        r'(?:ReturnType\s+(U\w+(?:\s+U\w+)*)|Void)\s*,\s*'
        r'\[([^\]]*)\]\s*,\s*(?:SoA|AoS)\s*\)',
        text, re.DOTALL
    ):
        name = m.group(1)
        raw_ret = m.group(2)
        raw_args = m.group(3)
        ret = "void" if raw_ret is None else parse_utype(raw_ret)
        if ret is None: continue
        args = parse_arg_list(raw_args)
        if args is None: continue
        add(name, args, ret)

    # add_binary_vec "name" mem  → scalars+vectors, ints_to_real return
    for m in re.finditer(r'add_binary_vec\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        scalars  = [INT, REAL]
        vectors  = [VEC, RVEC, MAT, arr(INT, 1), arr(REAL, 1)]
        for s in scalars:
            for t in scalars: add(name, [s, t], ints_to_real(s))
        for v in vectors:
            add(name, [v, v], ints_to_real(v))
            for s in scalars:
                add(name, [v, s], ints_to_real(v))
                add(name, [s, v], ints_to_real(v))

    # add_binary_vec_real_real "name" mem
    for m in re.finditer(r'add_binary_vec_real_real\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        for t in [REAL, VEC, RVEC, MAT, arr(REAL, 1)]:
            add(name, [t, t], t)
            if t != REAL:
                add(name, [t, REAL], t)
                add(name, [REAL, t], t)

    # add_binary_vec_complex_complex "name" mem
    for m in re.finditer(r'add_binary_vec_complex_complex\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        for t in [COMPLEX, CVEC, CRVEC, CMAT, arr(COMPLEX, 1)]:
            add(name, [t, t], t)
            if t != COMPLEX:
                add(name, [t, COMPLEX], t)
                add(name, [COMPLEX, t], t)

    # add_binary_vec_int_real "name" mem
    for m in re.finditer(r'add_binary_vec_int_real\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        for t in [VEC, RVEC, MAT, arr(REAL, 1)]:
            for d in range(8):
                add(name, [INT, arr(t if d == 0 else REAL, d)], arr(t if d == 0 else REAL, d))

    # add_binary_vec_real_int "name" mem
    for m in re.finditer(r'add_binary_vec_real_int\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        for t in [VEC, RVEC, MAT, arr(REAL, 1)]:
            add(name, [t, INT], t)

    # add_binary_vec_int_int "name" mem
    for m in re.finditer(r'add_binary_vec_int_int\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        for d in range(8):
            t = arr(INT, d)
            add(name, [t, INT], t)
            if d > 0: add(name, [INT, t], t)
            if d > 0: add(name, [t, t], t)

    # add_first_arg_vector_binary "name" mem
    for m in re.finditer(r'add_first_arg_vector_binary\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        add(name, [REAL, REAL], REAL)
        for t in [VEC, RVEC, MAT, arr(REAL, 1)]:
            for d in range(8):
                ty = arr(t if d == 0 else REAL, d) if d > 0 else t
                add(name, [ty, ty], ty)
                add(name, [ty, REAL], ty)

    # add_first_arg_vector_ternary "name" mem
    for m in re.finditer(r'add_first_arg_vector_ternary\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        add(name, [REAL, REAL, REAL], REAL)
        for t in [VEC, RVEC, MAT, arr(REAL, 1)]:
            add(name, [t, t, t], t); add(name, [t, t, REAL], t)
            add(name, [t, REAL, t], t); add(name, [t, REAL, REAL], t)

    # add_ternary_vec "name" mem
    for m in re.finditer(r'add_ternary_vec\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        name = m.group(1)
        add(name, [REAL, REAL, REAL], REAL)
        for t in [VEC, RVEC, MAT]:
            for a in [t, REAL]:
                for b in [t, REAL]:
                    add(name, [t, a, b], t)
                    add(name, [REAL, t, b] if b != REAL else [REAL, t, b], t)
                    add(name, [REAL, a, t] if a != REAL else [REAL, a, t], t)

    # add_nested_unary UType "name" mem
    for m in re.finditer(r'add_nested_unary\s+(\w+)\s+"([^"]+)"\s+(?:SoA|AoS)', text):
        base_raw, name = m.group(1), m.group(2)
        base = TYPE_MAP.get(base_raw)
        if base:
            for d in range(8):
                t = arr(base, d)
                add(name, [t], t)

    # add_nullary "name"
    for m in re.finditer(r'add_nullary\s+"([^"]+)"', text):
        add(m.group(1), [], REAL)

    # for_all_vector_types / for_vector_types patterns — skip (complex expansion)

def main():
    print(f"Reading {GENERATE_ML} ...")
    text = GENERATE_ML.read_text(encoding="utf-8")

    # Collect into dict: name → set of (args_tuple, ret)
    sigs: dict[str, set] = {}

    def add_list(entries):
        for name, args, ret in entries:
            if name not in sigs: sigs[name] = set()
            sigs[name].add((tuple(args), ret))

    print("Processing distributions ...")
    add_list(parse_distributions(text))

    print("Processing math signatures ...")
    add_list(parse_math_sigs(text))

    print("Processing explicit add_* calls ...")
    parse_explicit(text, sigs)

    # Serialise: name → list of [args_list, ret_str]
    out = {name: [[list(args), ret] for args, ret in sorted(entries)]
           for name, entries in sorted(sigs.items())}

    OUTPUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_JSON.write_text(json.dumps({"v": 1, "f": out}, separators=(",", ":")))

    total = sum(len(v) for v in out.values())
    print(f"Wrote {total} signatures for {len(out)} functions → {OUTPUT_JSON}")

if __name__ == "__main__":
    main()
