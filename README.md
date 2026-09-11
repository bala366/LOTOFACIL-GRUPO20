# Nota Fácil - Grupo 20

Aplicativo Android em Java, tema roxo com trevo amarelo, baseado no motor Lotofácil V2 fornecido pelo usuário.

## Motor mantido

- Último resultado: 15 dezenas.
- Gera C(15,5)=3.003 combinações de cinco.
- Escolhe as 5 geladas pelo score V2 original.
- Retira as 5 e mantém 10 dezenas do último.
- Junta com as 10 dezenas do espelho e forma o Grupo 20.
- Gera 2.100 candidatos finais com exatamente 9 dezenas do último + 6 do espelho.
- Classifica por padrão histórico, movimento, Engrossando o Talo, crescimento e perímetro.

## Compilação

Use GitHub Actions. O workflow está em `.github/workflows/build-apk.yml`.
O APK gerado fica no artefato `NOTA-FACIL-GRUPO20-APK`.
