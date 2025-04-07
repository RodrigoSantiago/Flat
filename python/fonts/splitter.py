import os
import subprocess
from pathlib import Path

# Caminho para a fonte variável original
SOURCE_FONT = "MaterialSymbolsSharp[FILL,GRAD,opsz,wght].ttf"

# Eixos desejados
FILL_VALUES = [0, 1]
WEIGHT_VALUES = [400]

# Pasta de saída
OUTPUT_DIR = Path("generated_fonts")
OUTPUT_DIR.mkdir(exist_ok=True)

def generate_static_instance(fill, weight):
    # Nome do novo arquivo
    out_name = f"MaterialSymbols_Fill{fill}_Wght{weight}.ttf"
    out_path = OUTPUT_DIR / out_name

    # Comando usando fonttools
    cmd = [
        "python",
        "-m", "fontTools.varLib.instancer",
        SOURCE_FONT,
        f"FILL={fill}",
        f"wght={weight}",
        f"GRAD=0",
        f"opsz=1",
        "-o", str(out_path)
    ]

    print(f"→ Gerando: {out_name}")
    subprocess.run(cmd, check=True)

# Loop sobre as combinações
for fill in FILL_VALUES:
    for weight in WEIGHT_VALUES:
        generate_static_instance(fill, weight)

print("✅ Fontes geradas com sucesso em:", OUTPUT_DIR.resolve())