from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont
from fontTools.subset import Subsetter, Options

def work(input_path, output_path, fill):
    font = TTFont(input_path)

    # 1. Instancia a fonte variável
    coords = {'FILL': fill, 'GRAD': 0, 'opsz': 24, 'wght': 400}
    font = instantiateVariableFont(font, coords, inplace=False)

    # 2. Substitui mapeamentos por .fill se existir
    cmap = font['cmap'].getBestCmap()
    glyph_set = set(font.getGlyphOrder())

    new_unicode_map = {}
    glyphs_to_keep = set()
    if fill == 0:
        for codepoint, glyph_name in cmap.items():
            new_unicode_map[codepoint] = glyph_name
            glyphs_to_keep.add(glyph_name)
    else:
        for codepoint, glyph_name in cmap.items():
            fill_name = glyph_name + '.fill'
            if fill_name in glyph_set:
                new_unicode_map[codepoint] = fill_name
                glyphs_to_keep.add(fill_name)
            else:
                new_unicode_map[codepoint] = glyph_name
                glyphs_to_keep.add(glyph_name)

    # 3. Subset com esses glifos
    options = Options()
    options.name_IDs = ['*']
    options.name_legacy = True
    options.name_languages = ['*']
    options.layout_features = []  # remove GSUB dependências
    options.glyph_names = False
    options.notdef_glyph = True
    options.retain_gids = False
    options.drop_tables = ['DSIG', 'GSUB']
    options.passthrough_tables = False
    options.hinting = False
    options.desubroutinize = True

    subsetter = Subsetter(options=options)
    subsetter.populate(unicodes=new_unicode_map.keys(), glyphs=glyphs_to_keep)
    subsetter.subset(font)

    # 4. Recria cmap com os nomes .fill
    used_glyphs = set(font.getGlyphOrder())
    for table in font['cmap'].tables:
        table.cmap = {
            cp: new_unicode_map[cp]
            for cp in new_unicode_map
            if new_unicode_map[cp] in used_glyphs
        }

    # 5. Limpa glifos realmente não usados
    glyphs_with_unicode = set()
    for table in font['cmap'].tables:
        glyphs_with_unicode.update(table.cmap.values())

    glyphs_to_preserve = glyphs_with_unicode | {'.notdef'}

    glyf = font['glyf']
    all_glyphs = set(font.getGlyphOrder())
    for glyph_name in all_glyphs:
        if glyph_name not in glyphs_to_preserve:
            del glyf.glyphs[glyph_name]

    final_glyph_order = ['.notdef'] + sorted(glyphs_with_unicode - {'.notdef'})
    font.setGlyphOrder(final_glyph_order)

    font.save(output_path)
    print(f"✅ Fonte final limpa e correta salva como: {output_path}")

if __name__ == "__main__":
    work('MaterialSymbolsSharp[FILL,GRAD,opsz,wght].ttf', 'MaterialSymbolsSharp_Fill.ttf', 1)
    work('MaterialSymbolsSharp[FILL,GRAD,opsz,wght].ttf', 'MaterialSymbolsSharp_Outline.ttf', 0)