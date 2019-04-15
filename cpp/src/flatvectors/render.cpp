//
// Created by Rodrigo on 25/10/2018.
//
#include "render.h"
#include <glad/glad.h>

#include <iostream>

const char *vertexSource =
        "#version 330 core\n"
        "layout (location = 0) in vec2 iPos;\n"
        "layout (location = 1) in vec2 iTex;\n"
        "uniform vec2 view;\n"
        "out vec2 oPos;\n"
        "out vec2 oTex;\n"
        "void main() {\n"
        "   oPos = iPos;\n"
        "   oTex = iTex;\n"
        "	gl_Position = vec4(iPos.x * 2.0 / view.x - 1.0, 1.0 - iPos.y * 2.0 / view.y, 0, 1);\n"
        "}\0";

const char *fragmentSource =
        "#version 330 core\n"
        "out vec4 FragColor;\n"
        "layout (std140) uniform Paint {\n"
        "    vec4 data;\n"
        "    mat3 colorMat;\n"
        "    mat3 imageMat;\n"
        "    vec4 shape;\n"
        "    vec4 stops[4];\n"
        "    vec4 colors[16];\n"
        "};\n"
        "uniform int stc;\n"
        "uniform int sdf;\n"
        "uniform sampler2D tex;\n"
        "uniform sampler2D fnt;\n"
        "in vec2 oPos;\n"
        "in vec2 oTex;\n"
        "float roundrect(vec2 pt, vec2 ext, float rad) {\n"
        "	vec2 ext2 = ext - vec2(rad,rad);\n"
        "	vec2 d = abs(pt) - ext2;\n"
        "	return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;\n"
        "}\n"
        "void main() {\n"
        "    if (stc == 1) {\n"
        "        FragColor = vec4(1);\n"
        "    } else {\n"
        "        vec4 color = colors[0], texel = vec4(0);\n"
        "        if (data[3] > 0) {\n"
        "            vec2 cPt = (colorMat * vec3(oPos, 1.0)).xy;\n"
        "            float t = (roundrect(cPt, shape.xy, shape.z) + shape.w * 0.5) / shape.w;\n"
        "            if (data[2] == 1) {\n"
        "                t = t - floor(t);\n"
        "            } else if (data[2] == 2) {\n"
        "                t = (int(t) % 2 == 0) ? t - floor(t) : 1 - (t - floor(t));\n"
        "            } else {\n"
        "                t = clamp(t, 0.0, 1.0);\n"
        "            }\n"
        "            color = mix(color, colors[1], clamp((t - stops[0].x) / (stops[0].y - stops[0].x), 0.0, 1.0));\n"
        "            if(data[3] > 1) color = mix(color, colors[2], clamp((t - stops[0].y) / (stops[0].z - stops[0].y), 0.0, 1.0));\n"
        "            if(data[3] > 2) color = mix(color, colors[3], clamp((t - stops[0].z) / (stops[0].w - stops[0].z), 0.0, 1.0));\n"
        "            if(data[3] > 3) color = mix(color, colors[4], clamp((t - stops[0].w) / (stops[1].x - stops[0].w), 0.0, 1.0));\n"
        "            if(data[3] > 4) color = mix(color, colors[5], clamp((t - stops[1].x) / (stops[1].y - stops[1].x), 0.0, 1.0));\n"
        "            if(data[3] > 5) color = mix(color, colors[6], clamp((t - stops[1].y) / (stops[1].z - stops[1].y), 0.0, 1.0));\n"
        "            if(data[3] > 6) color = mix(color, colors[7], clamp((t - stops[1].z) / (stops[1].w - stops[1].z), 0.0, 1.0));\n"
        "            if(data[3] > 7) color = mix(color, colors[8], clamp((t - stops[1].w) / (stops[2].x - stops[1].w), 0.0, 1.0));\n"
        "            if(data[3] > 8) color = mix(color, colors[9], clamp((t - stops[2].x) / (stops[2].y - stops[2].x), 0.0, 1.0));\n"
        "            if(data[3] > 9) color = mix(color, colors[10], clamp((t - stops[2].y) / (stops[2].z - stops[2].y), 0.0, 1.0));\n"
        "            if(data[3] >10) color = mix(color, colors[11], clamp((t - stops[2].z) / (stops[2].w - stops[2].z), 0.0, 1.0));\n"
        "            if(data[3] >11) color = mix(color, colors[12], clamp((t - stops[2].w) / (stops[3].x - stops[2].w), 0.0, 1.0));\n"
        "            if(data[3] >12) color = mix(color, colors[13], clamp((t - stops[3].x) / (stops[3].y - stops[3].x), 0.0, 1.0));\n"
        "            if(data[3] >13) color = mix(color, colors[14], clamp((t - stops[3].y) / (stops[3].z - stops[3].y), 0.0, 1.0));\n"
        "            if(data[3] >14) color = mix(color, colors[15], clamp((t - stops[3].z) / (stops[3].w - stops[3].z), 0.0, 1.0));\n"
        "        }\n"
        "        if (data[0] > 0 && data[0] <= 2) {\n"
        "            vec2 tPt = (imageMat * vec3(oPos, 1.0)).xy;\n"
        "            texel = texture(tex, tPt);\n"
        "        }\n"
        "        float a = color.a + texel.a * (1 - color.a);\n"
        "        if (data[0] >= 2) {\n"
        "            if (sdf == 1) {\n"
        "                float d = texture(fnt, oTex).r - 0.5;\n"
        "                float width = d / fwidth(d);"
        //"                a = clamp(width + 0.5, 0.0, 1.0);\n" GLES2.0
        "                ivec2 sz = textureSize(fnt, 0);"
        "                float dx = dFdx(oTex.x) * sz.x;\n"
        "                float dy = dFdy(oTex.y) * sz.y;\n"
        "                float toPixels = 8.0 * inversesqrt(dx * dx + dy * dy);"
        "                a = clamp(d * toPixels + 0.5, 0.0, 1.0);"
        "            } else {\n"
        "                a = texture(fnt, oTex).r;\n"
        "            }\n"
        "        }\n"
        "        FragColor = vec4(color.rgb * color.a + texel.rgb * texel.a * (1 - color.a), a);\n"
        "    }\n"
        "}\0";

typedef struct fvGLData {
    GLuint vao;
    GLuint vbo;
    GLuint ebo;
    GLuint ubo;
    int paint, vertex, element;

    GLuint image0, image1;

    GLuint shader;
    GLint viewID, texID, fntID, sdfID, stcID;

    int aa, sdf;
    unsigned int width, height;
} fvGLData;

void* renderCreate() {
// todo - single buffer for vertex, uv, elements

    fvGLData* ctx = (fvGLData*) malloc(sizeof(fvGLData));
    memset(ctx, 0, sizeof(fvGLData));

    glGenVertexArrays(1, &ctx->vao);
    glGenBuffers(1, &ctx->vbo);
    glGenBuffers(1, &ctx->ebo);
    glGenBuffers(1, &ctx->ubo);

    // Vertex Shader
    int success;
    char infoLog[512];
    GLuint vS = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vS, 1, &vertexSource, NULL);
    glCompileShader(vS);
    glGetShaderiv(vS, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(vS, 512, NULL, infoLog);
        std::cout << "ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    // Fragment Shader
    GLuint fS = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fS, 1, &fragmentSource, NULL);
    glCompileShader(fS);
    glGetShaderiv(fS, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(fS, 512, NULL, infoLog);
        std::cout << "ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    // Shader Program
    ctx->shader = glCreateProgram();
    glAttachShader(ctx->shader, vS);
    glAttachShader(ctx->shader, fS);
    glLinkProgram(ctx->shader);
    glGetProgramiv(ctx->shader, GL_LINK_STATUS, &success);
    if (!success) {
        glGetProgramInfoLog(ctx->shader, 512, NULL, infoLog);
        std::cout << "ERROR::SHADER::PROGRAM::LINKING_FAILED\n" << infoLog << std::endl;
    }
    glDeleteShader(vS);
    glDeleteShader(fS);

    GLuint paintIndex = glGetUniformBlockIndex(ctx->shader, "Paint");
    glUniformBlockBinding(ctx->shader, paintIndex, 0);
    ctx->viewID = glGetUniformLocation(ctx->shader, "view");
    ctx->texID = glGetUniformLocation(ctx->shader, "tex");
    ctx->fntID = glGetUniformLocation(ctx->shader, "fnt");
    ctx->sdfID = glGetUniformLocation(ctx->shader, "sdf");
    ctx->stcID = glGetUniformLocation(ctx->shader, "stc");
    ctx->width = 0;
    ctx->height = 0;

    return ctx;
}

void renderAlloc(void * data, int paint, int element, int vertex) {
    fvGLData* ctx = (fvGLData*) data;

    if (ctx->paint != paint) {
        ctx->paint = paint;

        // Uniform Buffer
        glBindBuffer(GL_UNIFORM_BUFFER, ctx->ubo);
        glBufferData(GL_UNIFORM_BUFFER, paint * sizeof(fvPaint), NULL, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    if (ctx->vertex != vertex || ctx->element != element) {
        ctx->vertex = vertex;
        ctx->element = element;

        glBindVertexArray(ctx->vao);

        // Vertices + UVs
        glBindBuffer(GL_ARRAY_BUFFER, ctx->vbo);
        glBufferData(GL_ARRAY_BUFFER, vertex * sizeof(float) * 2, NULL, GL_STATIC_DRAW);

        // Elements
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ctx->ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, element * sizeof(short), NULL, GL_STATIC_DRAW);

        // pos
        glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void *) 0);
        glEnableVertexAttribArray(0);

        // uv
        glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void *) (vertex * sizeof(float)));
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}

void renderDestroy(void *data) {
    fvGLData* ctx = (fvGLData*) data;

    glDeleteBuffers(1, &ctx->vao);
    glDeleteBuffers(1, &ctx->vbo);
    glDeleteBuffers(1, &ctx->ebo);
    glDeleteBuffers(1, &ctx->ubo);
    glDeleteShader(ctx->shader);
    free(ctx);
}

void renderBegin(void *data, unsigned int width, unsigned int height) {
    fvGLData* ctx = (fvGLData*) data;
    ctx->width = width;
    ctx->height = height;
    ctx->image0 = 0;
    ctx->image1 = 0;
    ctx->aa = 0;
    ctx->sdf = 0;

    glUseProgram(ctx->shader);
    glUniform2f(ctx->viewID, width, height);
    glUniform1i(ctx->texID, 0);
    glUniform1i(ctx->fntID, 1);
    glUniform1i(ctx->sdfID, 0);
    glUniform1i(ctx->stcID, 0);

    glBindVertexArray(ctx->vao);

    glBindBuffer(GL_UNIFORM_BUFFER, ctx->ubo);

    glDisable(GL_MULTISAMPLE);

    glEnable(GL_STENCIL_TEST);
    glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
    glStencilFunc(GL_ALWAYS, 0, 0xFF);
    glStencilMask(0xFF);

    glEnable(GL_BLEND);
    glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, 0);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
}

void renderEnd(void *data) {
    fvGLData *ctx = (fvGLData *) data;

    glUseProgram(0);
    glBindVertexArray(0);

    glBindBuffer(GL_UNIFORM_BUFFER, 0);

    if (ctx->image1 != 0) {
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void renderClearClip(void* data, int clip) {
    glClearStencil(clip ? 0x00 : 0x01);
    glClear(GL_STENCIL_BUFFER_BIT);
}

void render__triangles(int pos, int length) {
    glDrawElements(GL_TRIANGLES, (GLsizei) (length), GL_UNSIGNED_SHORT, (void*) (pos * sizeof(short)));
}

void renderFlush(void *data,
                 fvPaint *paints, int pSize,
                 short* elements, int eSize,
                 float *vtx, float *uvs, int vSize) {
    fvGLData* ctx = (fvGLData*) data;

    glBufferSubData(GL_UNIFORM_BUFFER, 0, pSize * sizeof(fvPaint), paints);
    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, eSize * sizeof(short), elements);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vSize * sizeof(float), vtx);
    glBufferSubData(GL_ARRAY_BUFFER, ctx->vertex * sizeof(float), vSize * sizeof(float), uvs);

    GLsizei pos = 0;
    for (int i = 0; i < pSize; i++) {
        // Exclude size, antealiasing and images info
        glBindBufferRange(GL_UNIFORM_BUFFER, 0, ctx->ubo,
                          i * sizeof(fvPaint) + sizeof(unsigned long) * 4,
                          sizeof(fvPaint) - sizeof(unsigned long) * 4);
        fvPaint &p = paints[i];
        int op = (int) (p.edgeAA >> 2);

        // Antialiasing
        if (ctx->aa != ((p.edgeAA & 1) == 1)) {
            ctx->aa = ((p.edgeAA & 1) == 1);
            if (ctx->aa) {
                glEnable(GL_MULTISAMPLE);
            } else {
                glDisable(GL_MULTISAMPLE);
            }
        }

        if (op == CLIP || op == UNCLIP) {
            glColorMask(0, 0, 0, 0);
            glUniform1i(ctx->stcID, 1);
            glStencilFunc(GL_ALWAYS, op == CLIP ? 0x00 : 0x01, 0xFF);
            render__triangles(pos, p.size - pos);

            glColorMask(1, 1, 1, 1);
            glUniform1i(ctx->stcID, 0);
        } else {
            // Images
            if (ctx->image0 != p.image0) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, ctx->image0 = p.image0);
            }

            if (ctx->image1 != p.image1) {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, ctx->image1 = p.image1);
                if (ctx->sdf != ((p.edgeAA & 2) == 2)) {
                    ctx->sdf = ((p.edgeAA & 2) == 2);
                    if (ctx->sdf) {
                        glUniform1i(ctx->sdfID, 1);
                    } else {
                        glUniform1i(ctx->sdfID, 0);
                    }
                }
            }

            if (op == FILL || op == TEXT) {
                glStencilFunc(GL_EQUAL, 0x01, 0xFF);
                render__triangles(pos, p.size - pos);
            } else if (op == STROKE) {
                glUniform1i(ctx->stcID, 1);
                glColorMask(0, 0, 0, 0);
                glStencilFunc(GL_EQUAL, 0x01, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_INCR);
                render__triangles(pos, p.size - pos);

                glUniform1i(ctx->stcID, 0);
                glColorMask(1, 1, 1, 1);
                glStencilFunc(GL_LESS, 0x01, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
                render__triangles(pos, p.size - pos);
            }
        }

        pos = p.size;
    }
}

unsigned long renderCreateFontTexture(void* data, int width, int height) {
    GLint prev;
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &prev);

    GLuint img;
    glGenTextures(1, &img);
    glBindTexture(GL_TEXTURE_2D, img);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, data);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glBindTexture(GL_TEXTURE_2D, prev);
    return img;
}

void renderUpdateFontTexture(unsigned long imageID, void* data, int x, int y, int width, int height) {
    GLint  prev;
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &prev);

    glBindTexture(GL_TEXTURE_2D, imageID);

    glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_RED, GL_UNSIGNED_BYTE, data);

    glBindTexture(GL_TEXTURE_2D, prev);
}

void renderDestroyFontTexture(unsigned long imageID) {
    GLuint imgID = imageID;
    glDeleteTextures(1, &imgID);
}
