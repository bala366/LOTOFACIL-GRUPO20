package com.notafacil.grupo20;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final int PICK_FILE = 1001;
    private static final int ROXO = Color.rgb(91, 26, 168);
    private static final int ROXO_ESCURO = Color.rgb(53, 16, 95);
    private static final int AMARELO = Color.rgb(255, 214, 51);
    private static final int BRANCO = Color.WHITE;

    private Uri arquivoUri;
    private TextView txtArquivo;
    private TextView txtLog;
    private ProgressBar progresso;
    private Button btnRodar;

    private final Set<Integer> PRIMOS = setOf(2,3,5,7,11,13,17,19,23);
    private final Set<Integer> FIB = setOf(1,2,3,5,8,13,21);
    private final Set<Integer> MIOLO = setOf(7,8,9,12,13,14,17,18,19);
    private final Set<Integer> CRUZ = setOf(3,8,11,12,13,14,15,18,23);
    private final Set<Integer> MOLDURA = buildMoldura();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        montarTela();
    }

    private void montarTela() {
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(14), dp(14), dp(14), dp(14));
        raiz.setBackgroundColor(ROXO_ESCURO);

        TextView trevo = new TextView(this);
        trevo.setText("☘");
        trevo.setTextColor(AMARELO);
        trevo.setTextSize(54);
        trevo.setGravity(Gravity.CENTER);
        raiz.addView(trevo, lp(-1, dp(70)));

        TextView titulo = new TextView(this);
        titulo.setText("NOTA FÁCIL");
        titulo.setTextColor(BRANCO);
        titulo.setTextSize(28);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, 1);
        raiz.addView(titulo, lp(-1, -2));

        TextView sub = new TextView(this);
        sub.setText("GRUPO 20 • LOTOFÁCIL");
        sub.setTextColor(AMARELO);
        sub.setTextSize(17);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, dp(12));
        raiz.addView(sub, lp(-1, -2));

        LinearLayout painel = new LinearLayout(this);
        painel.setOrientation(LinearLayout.VERTICAL);
        painel.setPadding(dp(12), dp(12), dp(12), dp(12));
        painel.setBackgroundResource(com.notafacil.grupo20.R.drawable.bg_panel);
        raiz.addView(painel, lp(-1, -2));

        txtArquivo = new TextView(this);
        txtArquivo.setText("Base ainda não selecionada");
        txtArquivo.setTextColor(BRANCO);
        txtArquivo.setTextSize(14);
        txtArquivo.setPadding(0, 0, 0, dp(8));
        painel.addView(txtArquivo, lp(-1, -2));

        Button btnEscolher = new Button(this);
        btnEscolher.setText("ESCOLHER BASE LOTOFÁCIL");
        btnEscolher.setTextColor(ROXO_ESCURO);
        btnEscolher.setTypeface(null, 1);
        btnEscolher.setBackgroundResource(com.notafacil.grupo20.R.drawable.bg_button);
        btnEscolher.setOnClickListener(v -> escolherArquivo());
        painel.addView(btnEscolher, lp(-1, dp(54)));

        btnRodar = new Button(this);
        btnRodar.setText("RODAR ESTUDO V2");
        btnRodar.setTextColor(ROXO_ESCURO);
        btnRodar.setTypeface(null, 1);
        btnRodar.setEnabled(false);
        btnRodar.setBackgroundResource(com.notafacil.grupo20.R.drawable.bg_button);
        LinearLayout.LayoutParams rodarLp = lp(-1, dp(54));
        rodarLp.topMargin = dp(8);
        painel.addView(btnRodar, rodarLp);
        btnRodar.setOnClickListener(v -> iniciarEstudo());

        progresso = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progresso.setMax(100);
        progresso.setProgress(0);
        LinearLayout.LayoutParams prLp = lp(-1, dp(18));
        prLp.topMargin = dp(12);
        painel.addView(progresso, prLp);

        TextView aviso = new TextView(this);
        aviso.setText("Motor exatamente baseado no código V2 escolhido: 5 geladas → sobram 10 → grupo 20 → 9 repetidas + 6 espelho.");
        aviso.setTextColor(BRANCO);
        aviso.setTextSize(13);
        aviso.setPadding(0, dp(8), 0, dp(8));
        painel.addView(aviso, lp(-1, -2));

        txtLog = new TextView(this);
        txtLog.setTextColor(BRANCO);
        txtLog.setTextSize(13);
        txtLog.setPadding(dp(10), dp(10), dp(10), dp(10));
        txtLog.setMovementMethod(new ScrollingMovementMethod());
        txtLog.setBackgroundColor(ROXO);

        ScrollView sc = new ScrollView(this);
        sc.addView(txtLog, lp(-1, -2));
        LinearLayout.LayoutParams scLp = lp(-1, 0);
        scLp.weight = 1;
        scLp.topMargin = dp(12);
        raiz.addView(sc, scLp);

        setContentView(raiz);
    }

    private void escolherArquivo() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/*");
        startActivityForResult(i, PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            arquivoUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(arquivoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            txtArquivo.setText("Base selecionada: " + arquivoUri.getLastPathSegment());
            btnRodar.setEnabled(true);
            txtLog.setText("Base pronta. Toque em RODAR ESTUDO V2.\n");
        }
    }

    private void iniciarEstudo() {
        if (arquivoUri == null) return;

        btnRodar.setEnabled(false);
        progresso.setProgress(0);
        txtLog.setText("");

        new Thread(() -> {
            try {
                log("LOTOFÁCIL - MOTOR INVERTIDO V2\n");
                log("5 GELADAS -> SOBRAM 10 -> GRUPO 20\n");
                log("9 REPETIDAS + 6 ESPELHO\n\n");

                List<int[]> concursos = lerBase(arquivoUri);
                if (concursos.size() < 20) throw new RuntimeException("Poucos concursos identificados na base.");

                int[] ultimo = concursos.get(concursos.size()-1);
                List<int[]> historico = new ArrayList<>(concursos.subList(0, concursos.size()-1));
                List<int[]> ultimos3 = new ArrayList<>();
                ultimos3.add(concursos.get(concursos.size()-4));
                ultimos3.add(concursos.get(concursos.size()-3));
                ultimos3.add(concursos.get(concursos.size()-2));
                int[] espelho = espelho(ultimo);

                log("CONCURSOS CARREGADOS: " + concursos.size() + "\n");
                log("ÚLTIMO RESULTADO: " + fmt(ultimo) + "\n");
                log("ESPELHO: " + fmt(espelho) + "\n\n");
                log("3 CONCURSOS ANTERIORES PARA MEDIR MOVIMENTO:\n");
                for (int[] d : ultimos3) log(fmt(d) + "\n");

                log("\nAprendendo padrão histórico...\n");
                Stats estat = aprenderPadrao(concursos);

                log("Calculando ENGROSSANDO O TALO...\n");
                Map<Integer, TaloInfo> talo = engrossandoTalo(concursos);

                List<FalhaResultado> rankingFalha = achar5Geladas(ultimo, historico, ultimos3);
                FalhaResultado melhorFalha = rankingFalha.get(0);
                int[] falha5 = melhorFalha.combo;
                int[] sobram10 = diferenca(ultimo, falha5);

                log("\n============================================================\n");
                log("5 GELADAS ENCONTRADAS\n");
                log("============================================================\n");
                log("5 RETIRADAS: " + fmt(falha5) + "\n");
                log("MOVIMENTO DE FALHA NOS 3: " + Arrays.toString(melhorFalha.mov3) + "\n");
                log("PERSISTÊNCIA: " + melhorFalha.persistencia + "/5\n");
                log("FALHAS COMPLETAS: " + melhorFalha.completas + "\n");
                log(String.format(Locale.US,"SCORE DE FALHA: %.2f\n", melhorFalha.score));
                log("10 QUE SOBRARAM: " + fmt(sobram10) + "\n");

                List<JogoResultado> ranking = gerarJogos(sobram10, espelho, historico, ultimos3, ultimo, estat, talo);
                JogoResultado melhor = ranking.get(0);

                log("\n============================================================\n");
                log("RESULTADO FINAL\n");
                log("============================================================\n");
                log("5 GELADAS RETIRADAS : " + fmt(falha5) + "\n");
                log("10 QUE SOBRARAM     : " + fmt(sobram10) + "\n");
                log("10 DO ESPELHO       : " + fmt(espelho) + "\n");
                log("GRUPO DE 20         : " + fmt(uniao(sobram10, espelho)) + "\n\n");
                log("PALPITE FINAL       : " + fmt(melhor.jogo) + "\n");
                log("REPETIDAS DO ÚLTIMO : " + melhor.perfil.repetidas + "\n");
                log("DO ESPELHO          : " + intersecao(melhor.jogo, espelho) + "\n");
                log("MOVIMENTO NOS 3     : " + Arrays.toString(melhor.mov3) + "\n");
                log(String.format(Locale.US,"TALO                : %.3f\n", melhor.talo));
                log(String.format(Locale.US,"CRESCIMENTO         : %.4f\n", melhor.crescimento));
                log(String.format(Locale.US,"PERÍMETRO           : %.2f\n", melhor.perimetro));
                log("SOMA                : " + melhor.perfil.soma + "\n");
                log("PARES               : " + melhor.perfil.pares + "\n");
                log("PRIMOS              : " + melhor.perfil.primos + "\n");
                log("FIBONACCI           : " + melhor.perfil.fib + "\n");
                log("MIOLO               : " + melhor.perfil.miolo + "\n");
                log("CRUZ                : " + melhor.perfil.cruz + "\n");
                log("MOLDURA/BORDA       : " + melhor.perfil.moldura + "\n");
                log("MAIOR SEQUÊNCIA     : " + melhor.perfil.sequencia + "\n");
                log("LINHAS L1-L5        : " + Arrays.toString(melhor.perfil.linhas) + "\n");
                log("COLUNAS C1-C5       : " + Arrays.toString(melhor.perfil.colunas) + "\n");
                log(String.format(Locale.US,"SCORE FINAL         : %.2f\n", melhor.score));

                log("\n============================================================\n");
                log("TOP 10 - GRUPOS DE 5 MAIS GELADOS\n");
                log("============================================================\n");
                for (int i=0; i<Math.min(10, rankingFalha.size()); i++) {
                    FalhaResultado r = rankingFalha.get(i);
                    log(String.format(Locale.US,
                            "%02d. %s | falha3=%s | persist=%d | completas=%d | score=%.2f\n",
                            i+1, fmt(r.combo), Arrays.toString(r.mov3), r.persistencia, r.completas, r.score));
                }

                log("\n============================================================\n");
                log("TOP 10 - PALPITES DE 15\n");
                log("============================================================\n");
                for (int i=0; i<Math.min(10, ranking.size()); i++) {
                    JogoResultado r = ranking.get(i);
                    log(String.format(Locale.US,
                            "%02d. %s | mov3=%s | talo=%.2f | per=%.2f | soma=%d | rep=%d | score=%.2f\n",
                            i+1, fmt(r.jogo), Arrays.toString(r.mov3), r.talo, r.perimetro,
                            r.perfil.soma, r.perfil.repetidas, r.score));
                }

                setProgress(100);
                log("\nCONCLUÍDO.\n");
                runOnUiThread(() -> btnRodar.setEnabled(true));

            } catch (Exception e) {
                log("\nERRO: " + e.getMessage() + "\n");
                runOnUiThread(() -> {
                    btnRodar.setEnabled(true);
                    Toast.makeText(this, "Erro no estudo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private List<int[]> lerBase(Uri uri) throws Exception {
        List<Registro> registros = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) throw new RuntimeException("Não consegui abrir o arquivo.");

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String linha;
        int ordem = 0;
        while ((linha = br.readLine()) != null) {
            Matcher m = p.matcher(linha);
            List<Integer> nums = new ArrayList<>();
            while (m.find()) nums.add(Integer.parseInt(m.group()));
            if (nums.size() < 15) continue;

            Integer concurso = null;
            if (nums.size() >= 16 && nums.get(0) > 25) {
                concurso = nums.get(0);
                nums = new ArrayList<>(nums.subList(1, nums.size()));
            }

            List<Integer> validos = new ArrayList<>();
            for (int n : nums) if (n >= 1 && n <= 25) validos.add(n);
            if (validos.size() >= 15) {
                int[] jogo = new int[15];
                int ini = validos.size()-15;
                for (int i=0; i<15; i++) jogo[i] = validos.get(ini+i);
                Arrays.sort(jogo);
                if (unicos(jogo)) {
                    ordem++;
                    registros.add(new Registro(concurso == null ? ordem : concurso, jogo));
                }
            }
        }
        br.close();

        registros.sort(Comparator.comparingInt(a -> a.concurso));
        List<int[]> out = new ArrayList<>();
        for (Registro r : registros) out.add(r.jogo);
        return out;
    }

    private boolean unicos(int[] a) {
        for (int i=1; i<a.length; i++) if (a[i] == a[i-1]) return false;
        return true;
    }

    private Stats aprenderPadrao(List<int[]> resultados) {
        List<Perfil> perfis = new ArrayList<>();
        for (int[] j : resultados) perfis.add(perfil(j, null));

        Stats s = new Stats();
        s.campos.put("soma", faixa(valores(perfis, "soma")));
        s.campos.put("pares", faixa(valores(perfis, "pares")));
        s.campos.put("primos", faixa(valores(perfis, "primos")));
        s.campos.put("fib", faixa(valores(perfis, "fib")));
        s.campos.put("miolo", faixa(valores(perfis, "miolo")));
        s.campos.put("cruz", faixa(valores(perfis, "cruz")));
        s.campos.put("moldura", faixa(valores(perfis, "moldura")));
        s.campos.put("sequencia", faixa(valores(perfis, "sequencia")));

        for (int i=0; i<5; i++) {
            List<Integer> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            for (Perfil pf : perfis) {
                l.add(pf.linhas[i]);
                c.add(pf.colunas[i]);
            }
            s.linhas.add(faixa(l));
            s.colunas.add(faixa(c));
        }
        return s;
    }

    private List<Integer> valores(List<Perfil> ps, String campo) {
        List<Integer> v = new ArrayList<>();
        for (Perfil p : ps) {
            switch (campo) {
                case "soma": v.add(p.soma); break;
                case "pares": v.add(p.pares); break;
                case "primos": v.add(p.primos); break;
                case "fib": v.add(p.fib); break;
                case "miolo": v.add(p.miolo); break;
                case "cruz": v.add(p.cruz); break;
                case "moldura": v.add(p.moldura); break;
                case "sequencia": v.add(p.sequencia); break;
            }
        }
        return v;
    }

    private Faixa faixa(List<Integer> v) {
        return new Faixa(percentil(v,.02), percentil(v,.10), percentil(v,.50), percentil(v,.90), percentil(v,.98));
    }

    private double percentil(List<Integer> valores, double q) {
        List<Integer> v = new ArrayList<>(valores);
        Collections.sort(v);
        double p = (v.size()-1)*q;
        int lo = (int)Math.floor(p);
        int hi = (int)Math.ceil(p);
        if (lo == hi) return v.get(lo);
        return v.get(lo)*(hi-p) + v.get(hi)*(p-lo);
    }

    private Perfil perfil(int[] jogo, int[] ultimo) {
        Perfil p = new Perfil();
        Set<Integer> s = setOf(jogo);
        p.soma = Arrays.stream(jogo).sum();
        for (int n : jogo) {
            if (n % 2 == 0) p.pares++;
            if (PRIMOS.contains(n)) p.primos++;
            if (FIB.contains(n)) p.fib++;
            if (MIOLO.contains(n)) p.miolo++;
            if (CRUZ.contains(n)) p.cruz++;
            if (MOLDURA.contains(n)) p.moldura++;
        }
        p.sequencia = maiorSequencia(jogo);
        for (int i=0; i<5; i++) {
            for (int n=1+i*5; n<=5+i*5; n++) if (s.contains(n)) p.linhas[i]++;
            for (int n=1+i; n<=25; n+=5) if (s.contains(n)) p.colunas[i]++;
        }
        p.repetidas = ultimo == null ? 0 : intersecao(jogo, ultimo);
        return p;
    }

    private int maiorSequencia(int[] a) {
        int best=1, atual=1;
        for (int i=1; i<a.length; i++) {
            if (a[i] == a[i-1]+1) { atual++; if (atual>best) best=atual; }
            else atual=1;
        }
        return best;
    }

    private boolean dentroPadrao(Perfil p, Stats st) {
        if (!entre(p.soma, st.campos.get("soma"))) return false;
        if (!entre(p.pares, st.campos.get("pares"))) return false;
        if (!entre(p.primos, st.campos.get("primos"))) return false;
        if (!entre(p.fib, st.campos.get("fib"))) return false;
        if (!entre(p.miolo, st.campos.get("miolo"))) return false;
        if (!entre(p.cruz, st.campos.get("cruz"))) return false;
        if (!entre(p.moldura, st.campos.get("moldura"))) return false;
        if (!entre(p.sequencia, st.campos.get("sequencia"))) return false;
        for (int i=0; i<5; i++) {
            if (!entre(p.linhas[i], st.linhas.get(i))) return false;
            if (!entre(p.colunas[i], st.colunas.get(i))) return false;
        }
        return true;
    }

    private boolean entre(int v, Faixa f) { return v >= f.min && v <= f.max; }

    private double scorePadrao(Perfil p, Stats st) {
        double sc = 0;
        sc += scoreCampo(p.soma, st.campos.get("soma"));
        sc += scoreCampo(p.pares, st.campos.get("pares"));
        sc += scoreCampo(p.primos, st.campos.get("primos"));
        sc += scoreCampo(p.fib, st.campos.get("fib"));
        sc += scoreCampo(p.miolo, st.campos.get("miolo"));
        sc += scoreCampo(p.cruz, st.campos.get("cruz"));
        sc += scoreCampo(p.moldura, st.campos.get("moldura"));
        sc += scoreCampo(p.sequencia, st.campos.get("sequencia"));
        for (int i=0; i<5; i++) {
            sc -= Math.abs(p.linhas[i] - st.linhas.get(i).med)*3.0;
            sc -= Math.abs(p.colunas[i] - st.colunas.get(i).med)*3.0;
        }
        return sc;
    }

    private double scoreCampo(int valor, Faixa f) {
        double largura = Math.max(1.0, f.p90-f.p10);
        double sc = -(Math.abs(valor-f.med)/largura)*10.0;
        if (valor >= f.p10 && valor <= f.p90) sc += 6.0;
        return sc;
    }

    private List<FalhaResultado> achar5Geladas(int[] ultimo, List<int[]> historico, List<int[]> ultimos3) {
        log("\n============================================================\n");
        log("ETAPA 1 - PROCURANDO AS 5 MAIS GELADAS\n");
        log("============================================================\n");
        log("Testando todas as 3.003 combinações de 5...\n");

        List<int[]> combos = combinacoes(ultimo, 5);
        List<FalhaResultado> ranking = new ArrayList<>();
        int total = combos.size();
        int prox=0;
        long ini=System.currentTimeMillis();

        for (int i=0; i<total; i++) {
            int[] combo = combos.get(i);
            int[] cnt = new int[6];
            double somaFalhas=0;
            for (int[] d : historico) {
                int f = falhas(combo,d);
                cnt[f]++;
                somaFalhas += f;
            }
            double scoreHist = cnt[5]*180.0 + cnt[4]*42.0 + cnt[3]*9.0 + cnt[2]*1.8 + somaFalhas/historico.size();

            int[] mov = new int[]{falhas(combo,ultimos3.get(0)),falhas(combo,ultimos3.get(1)),falhas(combo,ultimos3.get(2))};
            double scoreMov = mov[0]*1.0 + mov[1]*1.6 + mov[2]*2.5;
            int persist=0;
            for (int n : combo) {
                int vezes=0;
                for (int[] d : ultimos3) if (!contains(d,n)) vezes++;
                if (vezes>=2) persist++;
            }
            int completas=0;
            for (int x : mov) if (x==5) completas++;
            int crescimento=mov[2]-mov[0];
            double score = scoreHist + scoreMov*42.0 + persist*18.0 + completas*90.0 + Math.max(0,crescimento)*22.0;
            ranking.add(new FalhaResultado(score,combo,mov,persist,completas,crescimento));

            int pct=(i+1)*100/total;
            if (pct>=prox) {
                long dec=Math.max(1,System.currentTimeMillis()-ini);
                double vel=(i+1)/(dec/1000.0);
                int eta=(int)((total-i-1)/Math.max(.001,vel));
                setProgress(Math.min(45,pct*45/100));
                status("Etapa 1: "+pct+"% • ETA "+eta+"s");
                prox+=5;
            }
        }

        ranking.sort((a,b)->Double.compare(b.score,a.score));
        return ranking;
    }

    private Map<Integer, TaloInfo> engrossandoTalo(List<int[]> resultados) {
        int ini=Math.max(0,resultados.size()-18);
        List<int[]> janela=resultados.subList(ini,resultados.size());
        Map<Integer,TaloInfo> out=new HashMap<>();

        for (int dez=1; dez<=25; dez++) {
            double[] serie=new double[janela.size()];
            double somaP=0, soma=0;
            for (int i=0;i<janela.size();i++) {
                serie[i]=contains(janela.get(i),dez)?1:0;
                double peso=i+1;
                soma += serie[i]*peso;
                somaP += peso;
            }
            double recente=soma/somaP;
            double persist=Arrays.stream(serie).sum()/serie.length;
            double sl=slope(serie);
            int metade=Math.max(1,serie.length/2);
            double m1=media(Arrays.copyOfRange(serie,0,metade));
            double m2=media(Arrays.copyOfRange(serie,metade,serie.length));
            double cresc=m2-m1;
            double[] ult5=Arrays.copyOfRange(serie,Math.max(0,serie.length-5),serie.length);
            double subida=slope(ult5);
            double score=55*recente + 20*persist + 12*Math.max(0,sl) + 8*Math.max(0,cresc) + 5*Math.max(0,subida);
            out.put(dez,new TaloInfo(score,cresc));
        }
        return out;
    }

    private double slope(double[] v) {
        int n=v.length;
        if (n<2) return 0;
        double xm=(n-1)/2.0, ym=media(v), num=0, den=0;
        for (int i=0;i<n;i++) { num+=(i-xm)*(v[i]-ym); den+=(i-xm)*(i-xm); }
        return den==0?0:num/den;
    }

    private double media(double[] v) {
        if (v.length==0) return 0;
        double s=0; for(double x:v)s+=x; return s/v.length;
    }

    private double estudarPerimetro(int[] jogo, List<int[]> historico) {
        int[] geral=new int[16], ult120=new int[16], ult30=new int[16];
        for (int i=0;i<historico.size();i++) {
            int h=intersecao(jogo,historico.get(i));
            geral[h]++;
            if (i>=historico.size()-120) ult120[h]++;
            if (i>=historico.size()-30) ult30[h]++;
        }
        return geral[9]*.05 + geral[10]*.12 + geral[11]*.30 + geral[12]*.75 + geral[13]*1.80 + geral[14]*4.0
                + ult120[9]*.20 + ult120[10]*.55 + ult120[11]*1.30 + ult120[12]*3.0 + ult120[13]*7.0 + ult120[14]*15.0
                + ult30[9]*.60 + ult30[10]*1.50 + ult30[11]*4.0 + ult30[12]*9.0 + ult30[13]*18.0 + ult30[14]*35.0;
    }

    private List<JogoResultado> gerarJogos(int[] sobram10, int[] espelho, List<int[]> historico, List<int[]> ultimos3,
                                            int[] ultimo, Stats estat, Map<Integer,TaloInfo> talo) {
        log("\n============================================================\n");
        log("ETAPA 2 - GRUPO DE 20\n");
        log("============================================================\n");
        log("10 QUE SOBRARAM: " + fmt(sobram10) + "\n");
        log("10 DO ESPELHO  : " + fmt(espelho) + "\n");
        log("GRUPO DE 20    : " + fmt(uniao(sobram10,espelho)) + "\n");
        log("\n============================================================\n");
        log("ETAPA 3 - COMBINAÇÕES DE 15\n");
        log("============================================================\n");
        log("Regra: 9 repetidas + 6 do espelho\n");
        log("Total de candidatos: 2100\n");

        List<int[]> rep9=combinacoes(sobram10,9);
        List<int[]> nov6=combinacoes(espelho,6);
        List<JogoResultado> ranking=new ArrayList<>();
        Set<Integer> antigos=new HashSet<>();
        for(int[] h:historico) antigos.add(mask(h));

        int total=rep9.size()*nov6.size();
        int feito=0, prox=0;
        long ini=System.currentTimeMillis();
        double melhor=-1e99;

        for(int[] a:rep9) {
            for(int[] b:nov6) {
                feito++;
                int[] jogo=uniao(a,b);
                Perfil p=perfil(jogo,ultimo);
                if(p.repetidas!=9) continue;
                if(intersecao(jogo,espelho)!=6) continue;
                if(!dentroPadrao(p,estat)) continue;

                int[] mov=new int[]{intersecao(jogo,ultimos3.get(0)),intersecao(jogo,ultimos3.get(1)),intersecao(jogo,ultimos3.get(2))};
                double scoreMov=mov[0]*1.0+mov[1]*1.7+mov[2]*2.7;
                int crescimentoMov=mov[2]-mov[0];
                double estrut=scorePadrao(p,estat);
                double taloScore=0, crescTalo=0;
                for(int n:jogo){ taloScore+=talo.get(n).score; crescTalo+=Math.max(0,talo.get(n).crescimento); }
                taloScore/=jogo.length;
                crescTalo/=jogo.length;
                double per=estudarPerimetro(jogo,historico);
                double score=scoreMov*45.0 + Math.max(0,crescimentoMov)*20.0 + estrut*3.0 + taloScore*4.0 + crescTalo*40.0 + per*1.15;
                if(antigos.contains(mask(jogo))) score-=5000;
                if(score>melhor) melhor=score;
                ranking.add(new JogoResultado(score,jogo,mov,p,taloScore,crescTalo,per));

                int pct=feito*100/total;
                if(pct>=prox){
                    long dec=Math.max(1,System.currentTimeMillis()-ini);
                    double vel=feito/(dec/1000.0);
                    int eta=(int)((total-feito)/Math.max(.001,vel));
                    setProgress(45 + pct*55/100);
                    status(String.format(Locale.US,"Etapa 3: %d%% • melhor %.2f • ETA %ds",pct,melhor,eta));
                    prox+=5;
                }
            }
        }

        if(ranking.isEmpty()) throw new RuntimeException("Nenhum candidato passou pelos filtros.");
        ranking.sort((x,y)->Double.compare(y.score,x.score));
        return ranking;
    }

    private List<int[]> combinacoes(int[] origem, int k) {
        List<int[]> out=new ArrayList<>();
        gerarComb(origem,k,0,new int[k],0,out);
        return out;
    }

    private void gerarComb(int[] a,int k,int ini,int[] atual,int pos,List<int[]> out){
        if(pos==k){ out.add(Arrays.copyOf(atual,k)); return; }
        for(int i=ini;i<=a.length-(k-pos);i++){
            atual[pos]=a[i];
            gerarComb(a,k,i+1,atual,pos+1,out);
        }
    }

    private int falhas(int[] grupo,int[] sorteio){ return grupo.length-intersecao(grupo,sorteio); }

    private int intersecao(int[] a,int[] b){
        int c=0;
        Set<Integer> s=setOf(b);
        for(int x:a) if(s.contains(x)) c++;
        return c;
    }

    private int[] diferenca(int[] a,int[] tirar){
        Set<Integer> t=setOf(tirar);
        List<Integer> l=new ArrayList<>();
        for(int x:a) if(!t.contains(x)) l.add(x);
        return toArray(l);
    }

    private int[] espelho(int[] ultimo){
        Set<Integer> s=setOf(ultimo);
        List<Integer> l=new ArrayList<>();
        for(int n=1;n<=25;n++) if(!s.contains(n)) l.add(n);
        return toArray(l);
    }

    private int[] uniao(int[] a,int[] b){
        Set<Integer> s=new HashSet<>();
        for(int x:a)s.add(x);
        for(int x:b)s.add(x);
        List<Integer> l=new ArrayList<>(s);
        Collections.sort(l);
        return toArray(l);
    }

    private int[] toArray(List<Integer> l){
        int[] a=new int[l.size()];
        for(int i=0;i<l.size();i++)a[i]=l.get(i);
        return a;
    }

    private boolean contains(int[] a,int n){ for(int x:a)if(x==n)return true; return false; }

    private int mask(int[] a){ int m=0; for(int n:a)m|=1<<(n-1); return m; }

    private String fmt(int[] nums){
        int[] a=Arrays.copyOf(nums,nums.length);
        Arrays.sort(a);
        StringBuilder sb=new StringBuilder();
        for(int n:a){ if(sb.length()>0)sb.append(' '); sb.append(String.format(Locale.US,"%02d",n)); }
        return sb.toString();
    }

    private Set<Integer> setOf(int... a){ Set<Integer>s=new HashSet<>(); for(int x:a)s.add(x); return s; }
    private Set<Integer> buildMoldura(){ Set<Integer>s=new HashSet<>(); for(int n=1;n<=25;n++)if(!MIOLO.contains(n))s.add(n); return s; }

    private void log(String s){ runOnUiThread(() -> txtLog.append(s)); }
    private void setProgress(int p){ runOnUiThread(() -> progresso.setProgress(p)); }
    private void status(String s){ runOnUiThread(() -> txtArquivo.setText(s)); }

    private int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private LinearLayout.LayoutParams lp(int w,int h){ return new LinearLayout.LayoutParams(w,h); }

    static class Registro { int concurso; int[] jogo; Registro(int c,int[]j){concurso=c;jogo=j;} }
    static class Faixa { double min,p10,med,p90,max; Faixa(double a,double b,double c,double d,double e){min=a;p10=b;med=c;p90=d;max=e;} }
    static class Stats { Map<String,Faixa> campos=new HashMap<>(); List<Faixa> linhas=new ArrayList<>(), colunas=new ArrayList<>(); }
    static class Perfil { int soma,pares,primos,fib,miolo,cruz,moldura,sequencia,repetidas; int[] linhas=new int[5], colunas=new int[5]; }
    static class TaloInfo { double score,crescimento; TaloInfo(double s,double c){score=s;crescimento=c;} }
    static class FalhaResultado {
        double score; int[] combo,mov3; int persistencia,completas,crescimento;
        FalhaResultado(double s,int[]c,int[]m,int p,int comp,int cr){score=s;combo=c;mov3=m;persistencia=p;completas=comp;crescimento=cr;}
    }
    static class JogoResultado {
        double score; int[] jogo,mov3; Perfil perfil; double talo,crescimento,perimetro;
        JogoResultado(double s,int[]j,int[]m,Perfil p,double t,double c,double per){score=s;jogo=j;mov3=m;perfil=p;talo=t;crescimento=c;perimetro=per;}
    }
}
