# -*- coding: utf-8 -*-
"""
LOTOFÁCIL - FALHA 5 -> SOBRAM 10 -> GRUPO 20 -> 9 REPETIDAS + 6 ESPELHO
PYDROID V2

ENGRENAGEM
==========
1) Pega o ÚLTIMO resultado da Lotofácil (15 dezenas).

2) Gera TODAS as C(15,5)=3.003 combinações de 5.
   Aqui o estudo é INVERTIDO:
   - procura a combinação de 5 mais "gelada";
   - as 5 que historicamente mais falham juntas;
   - que mais vêm falhando nos 3 concursos anteriores;
   - que apresentam tendência de continuação de falha.
   Essas 5 são RETIRADAS.

3) Sobram 10 dezenas do último resultado.

4) Junta:
      10 que sobraram do último
    + 10 dezenas do ESPELHO do último
    = GRUPO DE 20.

5) Dentro desse grupo de 20 monta jogos de 15 com EXATAMENTE:
      9 dezenas do último resultado
    + 6 dezenas do espelho.
   Como dentro do grupo existem 10 do último e 10 do espelho:
      C(10,9) x C(10,6) = 2.100 jogos válidos.

6) Cada jogo final é classificado por:
   - movimento nos 3 concursos anteriores;
   - padrão histórico;
   - soma;
   - pares;
   - primos;
   - Fibonacci;
   - miolo;
   - cruz;
   - moldura/borda;
   - maior sequência;
   - linhas L1-L5;
   - colunas C1-C5;
   - "ENGROSSANDO O TALO" (janela recente de 18 concursos);
   - crescimento / slope;
   - persistência;
   - perímetro de pontuação do jogo:
       9+, 10+, 11+, 12+, 13+ acertos no histórico e nas janelas recentes.

OBSERVAÇÃO
==========
Os "3 últimos" usados para medir movimento são os 3 concursos imediatamente
ANTERIORES ao último resultado carregado. Isso evita dar a mesma nota para
todos os candidatos que têm exatamente 9 repetidas do último.

Estudo estatístico. Não há garantia de premiação.
"""

import os
import re
import math
import time
import itertools
from collections import Counter

APP = "LOTOFÁCIL - FALHA 5 + GRUPO 20 + ENGROSSANDO O TALO - V2"

DOWNLOADS = [
    "/storage/emulated/0/Download",
    "/storage/emulated/0/Downloads",
    os.path.expanduser("~/Download"),
    ".",
]

UNIVERSO = set(range(1, 26))

PRIMOS = {2,3,5,7,11,13,17,19,23}
FIB = {1,2,3,5,8,13,21}
MIOLO = {7,8,9,12,13,14,17,18,19}
CRUZ = {3,8,11,12,13,14,15,18,23}
MOLDURA = UNIVERSO - MIOLO

JANELA_TALO = 18


# ================================================================
# UTIL
# ================================================================

def hr(c="=", n=92):
    print(c*n)

def fmt(nums):
    return " ".join(f"{n:02d}" for n in sorted(nums))

def pct_bar(done, total, width=28):
    if total <= 0:
        return "[" + "░"*width + "] 0%"
    p = max(0.0, min(1.0, done/total))
    fill = int(round(width*p))
    return "[" + "█"*fill + "░"*(width-fill) + f"] {int(p*100):3d}%"

def pasta_download():
    for p in DOWNLOADS:
        if os.path.isdir(p):
            return p
    return "."

def escolher_arquivo(folder):
    arqs = sorted(
        x for x in os.listdir(folder)
        if x.lower().endswith((".txt",".csv"))
    )

    if not arqs:
        raise RuntimeError("Nenhum TXT/CSV encontrado na pasta Download.")

    hr()
    print("ARQUIVOS EM DOWNLOAD")
    hr()

    for i,a in enumerate(arqs,1):
        print(f"{i:03d} - {a}")

    while True:
        s = input("\nEscolha a base da LOTOFÁCIL pelo número: ").strip()
        if s.isdigit() and 1 <= int(s) <= len(arqs):
            return os.path.join(folder, arqs[int(s)-1])
        print("Número inválido.")


# ================================================================
# LEITURA
# ================================================================

def parse_history(path):
    rows = {}
    text = None

    for enc in ("utf-8-sig","utf-8","latin-1"):
        try:
            with open(path, "r", encoding=enc) as f:
                text = f.read()
            break
        except Exception:
            pass

    if text is None:
        raise RuntimeError("Não consegui abrir o arquivo.")

    for raw in text.splitlines():
        vals = [int(x) for x in re.findall(r"\d+", raw)]

        if len(vals) < 15:
            continue

        contest = None

        # Quando a primeira coluna é concurso (>25), separa.
        if len(vals) >= 16 and vals[0] > 25:
            contest = vals[0]
            vals = vals[1:]

        valid = [x for x in vals if 1 <= x <= 25]

        if len(valid) >= 15:
            draw = tuple(sorted(valid[-15:]))

            if len(set(draw)) == 15:
                if contest is None:
                    contest = len(rows) + 1
                rows[contest] = draw

    if len(rows) < 20:
        raise RuntimeError("Poucos concursos identificados. Confira o TXT.")

    return dict(sorted(rows.items()))


# ================================================================
# PERFIL / PADRÃO HISTÓRICO
# ================================================================

def linhas(game):
    s = set(game)
    return tuple(
        sum(1 for n in range(1+5*i, 6+5*i) if n in s)
        for i in range(5)
    )

def colunas(game):
    s = set(game)
    return tuple(
        sum(1 for n in range(1+j, 26, 5) if n in s)
        for j in range(5)
    )

def maior_seq(game):
    g = sorted(game)
    best = cur = 1

    for i in range(1, len(g)):
        if g[i] == g[i-1] + 1:
            cur += 1
            best = max(best, cur)
        else:
            cur = 1

    return best

def perfil(game, anterior=None):
    s = set(game)

    return {
        "soma": sum(game),
        "pares": sum(1 for n in game if n % 2 == 0),
        "primos": sum(1 for n in game if n in PRIMOS),
        "fib": sum(1 for n in game if n in FIB),
        "miolo": len(s & MIOLO),
        "cruz": len(s & CRUZ),
        "moldura": len(s & MOLDURA),
        "seq": maior_seq(game),
        "linhas": linhas(game),
        "colunas": colunas(game),
        "rep": len(s & set(anterior)) if anterior else None,
    }

def percentile(vals, q):
    vals = sorted(vals)
    p = (len(vals)-1)*q
    lo = int(math.floor(p))
    hi = int(math.ceil(p))

    if lo == hi:
        return vals[lo]

    return vals[lo]*(hi-p) + vals[hi]*(p-lo)

def build_stats(draws):
    ps = []

    for i,d in enumerate(draws):
        ant = draws[i-1] if i > 0 else None
        ps.append(perfil(d, ant))

    st = {}

    for k in ("soma","pares","primos","fib","miolo","cruz","moldura","seq"):
        v = [p[k] for p in ps]
        st[k] = {
            "p02": percentile(v,.02),
            "p10": percentile(v,.10),
            "med": percentile(v,.50),
            "p90": percentile(v,.90),
            "p98": percentile(v,.98),
        }

    st["linhas"] = []
    st["colunas"] = []

    for j in range(5):
        v = [p["linhas"][j] for p in ps]
        st["linhas"].append({
            "p02": percentile(v,.02),
            "med": percentile(v,.50),
            "p98": percentile(v,.98),
        })

        v = [p["colunas"][j] for p in ps]
        st["colunas"].append({
            "p02": percentile(v,.02),
            "med": percentile(v,.50),
            "p98": percentile(v,.98),
        })

    return st

def padrao_ok(p, st):
    for k in ("soma","pares","primos","fib","miolo","cruz","moldura","seq"):
        if not (st[k]["p02"] <= p[k] <= st[k]["p98"]):
            return False

    for j,v in enumerate(p["linhas"]):
        e = st["linhas"][j]
        if not (e["p02"] <= v <= e["p98"]):
            return False

    for j,v in enumerate(p["colunas"]):
        e = st["colunas"][j]
        if not (e["p02"] <= v <= e["p98"]):
            return False

    return True

def score_padrao(p, st):
    sc = 0.0

    for k in ("soma","pares","primos","fib","miolo","cruz","moldura","seq"):
        spread = max(1.0, st[k]["p90"] - st[k]["p10"])

        sc -= abs(p[k] - st[k]["med"]) / spread * 10.0

        if st[k]["p10"] <= p[k] <= st[k]["p90"]:
            sc += 6.0

    for j,v in enumerate(p["linhas"]):
        sc -= abs(v - st["linhas"][j]["med"]) * 3.0

    for j,v in enumerate(p["colunas"]):
        sc -= abs(v - st["colunas"][j]["med"]) * 3.0

    return sc


# ================================================================
# MÉTRICAS BÁSICAS
# ================================================================

def hits(game, draw):
    return len(set(game) & set(draw))

def falhas(combo, draw):
    return len(set(combo) - set(draw))


# ================================================================
# 1ª ETAPA - ACHAR AS 5 MAIS GELADAS DO ÚLTIMO
# ================================================================

def score_falha5(combo, historico, ult3):
    """
    Quanto MAIOR o score, mais forte a combinação como FALHA.

    Mede:
    - histórico das 5 ficando fora juntas;
    - 4/5, 3/5 falhando;
    - movimento de falha nos 3 concursos anteriores;
    - crescimento recente da falha.
    """

    hs = [falhas(combo,d) for d in historico]
    cnt = Counter(hs)

    hist_score = (
        cnt[5]*180.0 +
        cnt[4]*42.0 +
        cnt[3]*9.0 +
        cnt[2]*1.8 +
        sum(hs)/len(hs)
    )

    mov = [falhas(combo,d) for d in ult3]

    # Mais recente dos 3 pesa mais.
    mov_score = mov[0]*1.0 + mov[1]*1.6 + mov[2]*2.5

    slope = mov[-1] - mov[0]

    # Persistência da falha: quantas das 5 falharam em pelo menos 2 dos 3.
    persist = 0

    for n in combo:
        f = sum(1 for d in ult3 if n not in d)
        if f >= 2:
            persist += 1

    # "falha completa": bônus quando o conjunto inteiro ficou fora.
    completas3 = sum(1 for x in mov if x == 5)

    total = (
        hist_score +
        mov_score*42.0 +
        max(0,slope)*22.0 +
        persist*18.0 +
        completas3*90.0
    )

    return {
        "total": total,
        "hist": hist_score,
        "mov3": mov,
        "mov_score": mov_score,
        "slope": slope,
        "persist": persist,
        "completas3": completas3,
    }

def escolher_falha5(ultimo, historico, ult3):
    total = math.comb(15,5)
    ranking = []
    next_pct = 0
    ini = time.time()

    print("\n[1/3] ESTUDANDO AS 3.003 COMBINAÇÕES DE 5 DO ÚLTIMO")
    print("Objetivo: encontrar as 5 mais GELADAS / propensas a falhar.")

    for i,combo in enumerate(itertools.combinations(ultimo,5),1):
        info = score_falha5(combo,historico,ult3)
        ranking.append((info["total"],combo,info))

        pct = int(i*100/total)

        if pct >= next_pct:
            speed = i/max(.001,time.time()-ini)
            eta = int((total-i)/max(.001,speed))

            print(
                "\r"+pct_bar(i,total)+
                f" | {i:,}/{total:,} | ETA {eta}s",
                end="",flush=True
            )

            next_pct += 5

    print()

    ranking.sort(key=lambda x:x[0],reverse=True)
    return ranking


# ================================================================
# ENGROSSANDO O TALO - JANELA 18
# ================================================================

def linear_slope(values):
    n = len(values)

    if n < 2:
        return 0.0

    xm = (n-1)/2.0
    ym = sum(values)/n

    num = sum((i-xm)*(v-ym) for i,v in enumerate(values))
    den = sum((i-xm)**2 for i in range(n))

    return num/den if den else 0.0

def build_talo_scores(draws):
    """
    Para cada dezena 1..25:
    - recente ponderado;
    - persistência;
    - slope;
    - crescimento;
    - subida nos últimos 5.

    Inspirado na ideia "Engrossando o Talo".
    """

    scores = {}

    janela = draws[-min(JANELA_TALO,len(draws)):]

    for n in range(1,26):
        serie = [1 if n in d else 0 for d in janela]

        # Mais peso para os concursos mais recentes.
        pesos = list(range(1,len(serie)+1))
        rec = sum(v*w for v,w in zip(serie,pesos)) / max(1,sum(pesos))

        persist = sum(serie)/max(1,len(serie))

        slope = linear_slope(serie)

        metade = max(1,len(serie)//2)
        antiga = serie[:metade]
        nova = serie[metade:]

        crescimento = (
            sum(nova)/max(1,len(nova)) -
            sum(antiga)/max(1,len(antiga))
        )

        ult5 = serie[-5:]
        subida5 = 0.0

        if len(ult5) >= 2:
            subida5 = linear_slope(ult5)

        # Mesma filosofia do motor "engrossando o talo":
        score = (
            55*rec +
            20*persist +
            12*max(0,slope) +
            8*max(0,crescimento) +
            5*max(0,subida5)
        )

        scores[n] = {
            "score": score,
            "recente": rec,
            "persist": persist,
            "slope": slope,
            "crescimento": crescimento,
            "subida5": subida5,
        }

    return scores


# ================================================================
# PERÍMETRO DE PONTUAÇÃO DO JOGO DE 15
# ================================================================

def perimeter_score(game, historico):
    hs = [hits(game,d) for d in historico]

    allc = Counter(hs)
    r120 = hs[-120:]
    r30 = hs[-30:]

    c120 = Counter(r120)
    c30 = Counter(r30)

    # Não depende de acertar 15 historicamente.
    # Premia principalmente permanência no perímetro 9..13.
    score = (
        allc[9]*0.05 +
        allc[10]*0.12 +
        allc[11]*0.30 +
        allc[12]*0.75 +
        allc[13]*1.80 +
        allc[14]*4.00 +

        c120[9]*0.20 +
        c120[10]*0.55 +
        c120[11]*1.30 +
        c120[12]*3.00 +
        c120[13]*7.00 +
        c120[14]*15.00 +

        c30[9]*0.60 +
        c30[10]*1.50 +
        c30[11]*4.00 +
        c30[12]*9.00 +
        c30[13]*18.00 +
        c30[14]*35.00
    )

    return {
        "score": score,
        "all": allc,
        "w120": c120,
        "w30": c30,
        "recent_hits": hs[-12:],
    }


# ================================================================
# 2ª ETAPA - JOGOS DE 15 DENTRO DO GRUPO 20
# ================================================================

def score_game15(game, historico, ult3, ultimo, st, talo):
    mov = [hits(game,d) for d in ult3]

    mov_score = (
        mov[0]*1.0 +
        mov[1]*1.7 +
        mov[2]*2.7
    )

    slope = mov[-1] - mov[0]

    p = perfil(game,ultimo)

    estrutural = score_padrao(p,st)

    # Engrossando o talo do jogo = soma/média da força das 15 dezenas.
    talo_vals = [talo[n]["score"] for n in game]
    talo_score = sum(talo_vals)/len(talo_vals)

    # Crescimento agregado das dezenas.
    crescimento = sum(
        max(0,talo[n]["crescimento"])
        for n in game
    ) / len(game)

    per = perimeter_score(game,historico)

    total = (
        mov_score*45.0 +
        max(0,slope)*20.0 +
        estrutural*3.0 +
        talo_score*4.0 +
        crescimento*40.0 +
        per["score"]*1.15
    )

    return {
        "total": total,
        "mov3": mov,
        "mov_score": mov_score,
        "slope": slope,
        "perfil": p,
        "estrutural": estrutural,
        "talo": talo_score,
        "crescimento": crescimento,
        "perimetro": per,
    }

def escolher_jogos15(
    sobram10,
    espelho,
    historico,
    ult3,
    ultimo,
    st,
    talo
):
    """
    Grupo 20 = 10 que sobraram do último + 10 do espelho.

    Regra final:
      9 repetidas do último
      6 do espelho

    Como só há 10 dezenas do último dentro do grupo,
    são 2.100 jogos válidos:
      C(10,9) x C(10,6)
    """

    total = math.comb(10,9)*math.comb(10,6)
    ranking = []
    old = set(historico)

    done = 0
    next_pct = 0
    ini = time.time()

    print("\n[2/3] GRUPO DE 20")
    print("10 que sobraram do último:", fmt(sobram10))
    print("10 do espelho            :", fmt(espelho))
    print("GRUPO 20                 :", fmt(set(sobram10)|set(espelho)))

    print("\n[3/3] TESTANDO 2.100 JOGOS DE 15")
    print("Regra: 9 repetidas do último + 6 do espelho.")
    print("Classificação: padrão + movimento + talo + crescimento + perímetro.")

    for rep9 in itertools.combinations(sobram10,9):
        for esp6 in itertools.combinations(espelho,6):
            done += 1

            game = tuple(sorted(rep9 + esp6))

            # Segurança da regra.
            if len(set(game)&set(ultimo)) != 9:
                continue

            if len(set(game)&set(espelho)) != 6:
                continue

            p = perfil(game,ultimo)

            # Elimina jogo estruturalmente fora do padrão.
            if not padrao_ok(p,st):
                continue

            info = score_game15(
                game,
                historico,
                ult3,
                ultimo,
                st,
                talo
            )

            sc = info["total"]

            # Penaliza quinzena já sorteada exatamente.
            if game in old:
                sc -= 5000

            ranking.append((sc,game,info))

            pct = int(done*100/total)

            if pct >= next_pct:
                speed = done/max(.001,time.time()-ini)
                eta = int((total-done)/max(.001,speed))
                best = max((x[0] for x in ranking),default=0)

                print(
                    "\r"+pct_bar(done,total)+
                    f" | {done:,}/{total:,} | melhor={best:.2f} | ETA {eta}s",
                    end="",flush=True
                )

                next_pct += 5

    print()

    if not ranking:
        raise RuntimeError("Nenhum jogo passou pelo padrão histórico.")

    ranking.sort(key=lambda x:x[0],reverse=True)
    return ranking


# ================================================================
# RELATÓRIO
# ================================================================

def build_report(
    history,
    ranking_falha,
    ranking15,
    ultimo,
    espelho,
    sobram10,
    ult3
):
    fscore,falha5,finfo = ranking_falha[0]
    score,game,info = ranking15[0]
    p = info["perfil"]
    per = info["perimetro"]

    L = []
    A = L.append

    A(APP)
    A("="*92)
    A(f"Concursos carregados: {len(history)}")
    A(f"Último resultado: {fmt(ultimo)}")
    A("")

    A("5 RETIRADAS - COMBINAÇÃO MAIS GELADA / FALHANTE")
    A("-"*92)
    A(fmt(falha5))
    A(f"Falhas nos 3 anteriores: {finfo['mov3']}")
    A(f"Persistência de falha: {finfo['persist']}/5")
    A(f"Falhas completas nos 3: {finfo['completas3']}")
    A(f"Slope de falha: {finfo['slope']:+d}")
    A(f"Score de falha: {fscore:.2f}")
    A("")

    A("10 QUE SOBRARAM DO ÚLTIMO")
    A("-"*92)
    A(fmt(sobram10))
    A("")

    A("ESPELHO DO ÚLTIMO")
    A("-"*92)
    A(fmt(espelho))
    A("")

    A("GRUPO DE 20")
    A("-"*92)
    A(fmt(set(sobram10)|set(espelho)))
    A("")

    A("PALPITE FINAL")
    A("-"*92)
    A(fmt(game))
    A(f"Repetidas do último: {p['rep']}")
    A(f"Do espelho: {len(set(game)&set(espelho))}")
    A(f"Movimento nos 3 anteriores: {info['mov3']}")
    A(f"Score talo: {info['talo']:.3f}")
    A(f"Crescimento agregado: {info['crescimento']:.4f}")
    A(f"Score perímetro: {per['score']:.2f}")
    A(f"Score final: {score:.2f}")
    A("")

    A("CLASSIFICAÇÃO ESTRUTURAL")
    A("-"*92)
    A(f"Soma: {p['soma']}")
    A(f"Pares: {p['pares']}")
    A(f"Primos: {p['primos']}")
    A(f"Fibonacci: {p['fib']}")
    A(f"Miolo: {p['miolo']}")
    A(f"Cruz: {p['cruz']}")
    A(f"Moldura/Borda: {p['moldura']}")
    A(f"Maior sequência: {p['seq']}")
    A(f"Linhas: {p['linhas']}")
    A(f"Colunas: {p['colunas']}")
    A("")

    A("PERÍMETRO RECENTE DO PALPITE")
    A("-"*92)
    A("Últimos 12 acertos do candidato: " + str(per["recent_hits"]))
    A(
        "Últimos 30 -> "
        f"9={per['w30'][9]} "
        f"10={per['w30'][10]} "
        f"11={per['w30'][11]} "
        f"12={per['w30'][12]} "
        f"13={per['w30'][13]} "
        f"14={per['w30'][14]}"
    )
    A("")

    A("TOP 10 COMBINAÇÕES DE 5 MAIS GELADAS")
    A("-"*92)

    for i,(sc,c,inf) in enumerate(ranking_falha[:10],1):
        A(
            f"{i:02d}. {fmt(c)} | falha3={inf['mov3']} | "
            f"persist={inf['persist']} | completas={inf['completas3']} | "
            f"score={sc:.2f}"
        )

    A("")
    A("TOP 10 PALPITES DE 15")
    A("-"*92)

    for i,(sc,g,inf) in enumerate(ranking15[:10],1):
        pp = inf["perfil"]

        A(
            f"{i:02d}. {fmt(g)} | mov3={inf['mov3']} | "
            f"talo={inf['talo']:.2f} | per={inf['perimetro']['score']:.2f} | "
            f"soma={pp['soma']} rep={pp['rep']} | score={sc:.2f}"
        )

    A("")
    A("OBSERVAÇÃO")
    A("-"*92)
    A("Estudo estatístico. Não há garantia de premiação.")

    return "\n".join(L),game


# ================================================================
# MAIN
# ================================================================

def main():
    os.system("clear")

    hr()
    print(APP)
    hr()

    print("1) Último resultado -> 3.003 combinações de 5.")
    print("2) Retira as 5 mais geladas / falhantes.")
    print("3) Sobram 10 do último.")
    print("4) 10 restantes + 10 espelho = grupo de 20.")
    print("5) Gera jogos de 15 com 9 repetidas + 6 espelho.")
    print("6) Escolhe por padrão + movimento + talo + perímetro.")
    hr()

    folder = pasta_download()
    path = escolher_arquivo(folder)

    history = parse_history(path)
    draws = list(history.values())

    if len(draws) < 20:
        raise RuntimeError("Base muito pequena.")

    ultimo = draws[-1]
    historico = draws[:-1]

    # 3 imediatamente anteriores ao último.
    ult3 = draws[-4:-1]

    espelho = tuple(sorted(UNIVERSO - set(ultimo)))

    print(f"\nConcursos carregados: {len(draws)}")
    print("Último:", fmt(ultimo))
    print("Espelho:", fmt(espelho))

    print("\n3 CONCURSOS ANTERIORES USADOS NO MOVIMENTO:")
    for d in ult3:
        print(" ",fmt(d))

    print("\nAprendendo padrão histórico...")
    st = build_stats(draws)

    print("Calculando ENGROSSANDO O TALO...")
    talo = build_talo_scores(draws)

    ranking_falha = escolher_falha5(
        ultimo,
        historico,
        ult3
    )

    falha5 = ranking_falha[0][1]

    sobram10 = tuple(
        sorted(set(ultimo) - set(falha5))
    )

    ranking15 = escolher_jogos15(
        sobram10,
        espelho,
        historico,
        ult3,
        ultimo,
        st,
        talo
    )

    report,best = build_report(
        history,
        ranking_falha,
        ranking15,
        ultimo,
        espelho,
        sobram10,
        ult3
    )

    top = ranking15[0]
    score,game,info = top
    p = info["perfil"]

    hr()
    print("RESULTADO FINAL")
    hr()

    print("5 GELADAS RETIRADAS :", fmt(falha5))
    print("10 QUE SOBRARAM     :", fmt(sobram10))
    print("10 DO ESPELHO       :", fmt(espelho))
    print("GRUPO DE 20         :", fmt(set(sobram10)|set(espelho)))
    print("")
    print("PALPITE FINAL - 15  :", fmt(game))
    print("REPETIDAS DO ÚLTIMO :", p["rep"])
    print("DO ESPELHO          :", len(set(game)&set(espelho)))
    print("MOVIMENTO 3         :", info["mov3"])
    print("TALO                :", f"{info['talo']:.3f}")
    print("CRESCIMENTO         :", f"{info['crescimento']:.4f}")
    print("PERÍMETRO           :", f"{info['perimetro']['score']:.2f}")
    print("SOMA                :", p["soma"])
    print("PARES               :", p["pares"])
    print("PRIMOS              :", p["primos"])
    print("FIBONACCI           :", p["fib"])
    print("MIOLO               :", p["miolo"])
    print("CRUZ                :", p["cruz"])
    print("MOLDURA             :", p["moldura"])
    print("LINHAS              :", p["linhas"])
    print("COLUNAS             :", p["colunas"])
    print("SEQUÊNCIA           :", p["seq"])
    print("SCORE FINAL         :", f"{score:.2f}")

    hr()

    print("\nTOP 10 PALPITES")

    for i,(sc,g,inf) in enumerate(ranking15[:10],1):
        print(
            f"{i:02d}. {fmt(g)} | mov3={inf['mov3']} | "
            f"talo={inf['talo']:.2f} | per={inf['perimetro']['score']:.2f} | "
            f"score={sc:.2f}"
        )

    saida = os.path.join(
        folder,
        "LOTOFACIL_FALHA5_GRUPO20_ENGROSSANDO_V2_RESULTADO.txt"
    )

    with open(saida,"w",encoding="utf-8") as f:
        f.write(report)

    print("\nTXT salvo em:")
    print(saida)

    print("\nCONCLUÍDO.")
    input("\nENTER para encerrar...")


if __name__ == "__main__":
    main()
