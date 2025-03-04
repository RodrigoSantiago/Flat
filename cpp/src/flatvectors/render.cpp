//
// Created by Rodrigo on 25/10/2018.
//
#include "render.h"
#include <glad/glad.h>

#include <iostream>
#include <cmath>

const char *vertexSource =
        "#version 330 core\n"
        "layout (location = 0) in vec2 iPos;\n"
        "layout (location = 1) in vec2 iTex;\n"
        "uniform vec2 view;\n"
        "uniform mat3 mat;\n"
        "out vec2 oPos;\n"
        "out vec2 oTex;\n"
        "void main() {\n"
        "   oPos = iPos;\n"
        "   oTex = iTex;\n"
        "   vec2 pos;\n"
        "   pos.x = iPos.x * mat[0][0] + iPos.y * mat[0][2] + mat[1][1];\n"
        "   pos.y = iPos.x * mat[0][1] + iPos.y * mat[1][0] + mat[1][2];\n"
        "	gl_Position = vec4(pos.x * 2.0 / view.x - 1.0, 1.0 - pos.y * 2.0 / view.y, 0, 1);\n"
        "}\0";

const char *fragmentSource =
        "#version 330 core\n"
        "out vec4 FragColor;\n"
        "layout (std140) uniform Paint {\n"
        "    vec4 data;\n"
        "    mat3 colorMat;\n"
        "    mat3 imageMat;\n"
        "    vec4 shape;\n"
        "    vec4 extra;\n"
        "    vec4 stops[4];\n"
        "    vec4 colors[16];\n"
        "};\n"
        "uniform int stc;\n"
        "uniform int sdf;\n"
        "uniform int dbg;\n"
        "uniform sampler2D tex;\n"
        "uniform sampler2D fnt;\n"
        "uniform vec2 fntSize;\n"
        "in vec2 oPos;\n"
        "in vec2 oTex;\n"
        "float roundrect(vec2 pt, vec2 ext, float rad) {\n"
        "	 vec2 ext2 = ext - vec2(rad,rad);\n"
        "	 vec2 d = abs(pt) - ext2;\n"
        "	 return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;\n"
        "}\n"
        "float focuscircle(vec2 coord, vec2 focus) {\n"
        "    float gradLength = 1.0;\n"
        "    vec2 diff = focus;\n"
        "    vec2 rayDir = normalize(coord - focus);\n"
        "    float a = dot(rayDir, rayDir);\n"
        "    float b = 2.0 * dot(rayDir, diff);\n"
        "    float c = dot(diff, diff) - 1;\n"
        "    float disc = b * b - 4.0 * a * c;\n"
        "    if (disc >= 0.0) {\n"
        "        float t = (-b + sqrt(abs(disc))) / (2.0 * a);\n"
        "        vec2 projection = focus + rayDir * t;\n"
        "        gradLength = distance(projection, focus);\n"
        "    }\n"
        "    return distance(coord, focus) / gradLength;\n"
        "}\n"
        "float expin(float a, float power) {\n"
        "	 return 1 - pow(1 - a, power);\n"
        "}\n"
        "void main() {\n"
        "    if (stc == 1) {\n"
        "        FragColor = vec4(1);\n"
        "    } else {\n"
        "        vec4 color = colors[0], texel = vec4(0);\n"
        "        if (data[3] > 0) {\n"
        "            vec2 cPt = (colorMat * vec3(oPos, 1.0)).xy;\n"
        "            float t;\n"
        "            if (extra[2] == 0) {\n"
        "                t = (roundrect(cPt, shape.xy, shape.z) + shape.w * 0.5) / shape.w;\n"
        "            } else {\n"
        "                t = focuscircle(cPt / (shape.z * 2.0), extra.xy);\n"
        "            }\n"
        "            if (data[2] == 0) {\n"
        "                t = clamp(t, 0.0, 1.0);\n"
        "            } else if (data[2] == 1) {\n"
        "                t = t - floor(t);\n"
        "            } else if (data[2] == 2) {\n"
        "                t = (int(t) % 2 == 0) ? t - floor(t) : 1 - (t - floor(t));\n"
        "            } else {\n"
        "                t = expin(clamp(t, 0.0, 1.0), 2);\n"
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
        "        if (data[0] == 1 || data[0] == 3) {\n"
        "            vec2 tPt = (imageMat * vec3(oPos, 1.0)).xy;\n"
        "            if (data[2] == 0) {\n"
        "                tPt = clamp(tPt, vec2(0.0), vec2(1.0));\n"
        "            } else if (data[2] == 1) {\n"
        "                tPt = tPt - floor(tPt);\n"
        "            } else if (data[2] == 2) {\n"
        "                tPt = vec2((int(tPt.x) % 2 == 0) ? tPt.x - floor(tPt.x) : 1 - (tPt.x - floor(tPt.x)),\n"
        "                           (int(tPt.y) % 2 == 0) ? tPt.y - floor(tPt.y) : 1 - (tPt.y - floor(tPt.y)));\n"
        "            }\n"
        "            color *= texture(tex, tPt);\n"
        "        }\n"
        "        float a = color.a;\n"
        "        if (data[0] > 1) {\n"
        "            ivec2 sz = textureSize(fnt, 0);\n"
        "            float dist = texture(fnt, oTex / sz).r;\n"
        "            if (dbg == 1) {\n"
        "                a = 1;\n"
        "            } else if (sdf == 1) {\n"
        "                float screenSpaceScale = fwidth(oTex).x * 0.05 + extra[3] * 0.5;\n"
        "                float aliasing = smoothstep(0.5 - screenSpaceScale, 0.5 + screenSpaceScale, dist);\n"
        "                a = a * aliasing;\n"
        "            } else {\n"
        "                a = a * dist;\n"
        "            }\n"
        "        }\n"
        "        FragColor = vec4(color.rgb * a, a);\n"
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
    GLint viewID, matID, texID, fntID, sdfID, stcID, dbgID;

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
    ctx->matID = glGetUniformLocation(ctx->shader, "mat");
    ctx->texID = glGetUniformLocation(ctx->shader, "tex");
    ctx->fntID = glGetUniformLocation(ctx->shader, "fnt");
    ctx->sdfID = glGetUniformLocation(ctx->shader, "sdf");
    ctx->stcID = glGetUniformLocation(ctx->shader, "stc");
    ctx->dbgID = glGetUniformLocation(ctx->shader, "dbg");
    ctx->width = 0;
    ctx->height = 0;

    return ctx;
}

int _get_align() {
    GLint align;
    glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, &align);
    return (GLint) ceil(sizeof(fvUniform) / (float)align) * align;
}

int renderAlign() {
    static int align = _get_align();
    return align;
}

void renderAlloc(void * data, int paint, int element, int vertex) {
    fvGLData* ctx = (fvGLData*) data;

    if (ctx->paint != paint) {
        ctx->paint = paint;

        // Uniform Buffer
        glBindBuffer(GL_UNIFORM_BUFFER, ctx->ubo);
        glBufferData(GL_UNIFORM_BUFFER, paint * renderAlign(), NULL, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    glBindVertexArray(ctx->vao);

    // Vertices + UVs
    if (ctx->vertex != vertex) {
        ctx->vertex = vertex;

        glBindBuffer(GL_ARRAY_BUFFER, ctx->vbo);
        glBufferData(GL_ARRAY_BUFFER, vertex * sizeof(float) * 2, NULL, GL_STATIC_DRAW);
    }

    // Elements
    if (ctx->element != element) {
        ctx->element = element;

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ctx->ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, element * sizeof(int), NULL, GL_STATIC_DRAW);

    }

    // pos
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(0);

    // uv
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void *) (vertex * sizeof(float)));
    glEnableVertexAttribArray(1);

    glBindVertexArray(0);
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
    // glUniformMatrix3fv(ctx->matID, 1, 0, *);
    glUniform1i(ctx->texID, 0);
    glUniform1i(ctx->fntID, 1);
    glUniform1i(ctx->sdfID, 0);
    glUniform1i(ctx->stcID, 0);
    glUniform1i(ctx->dbgID, fvIsDebug());

    glBindVertexArray(ctx->vao);

    glBindBuffer(GL_UNIFORM_BUFFER, ctx->ubo);

    glDisable(GL_MULTISAMPLE);
    glDisable(GL_DEPTH_TEST);

    glEnable(GL_STENCIL_TEST);
    glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    glStencilFunc(GL_EQUAL, 0x80, 0xFF);
    glStencilMask(0xFF);

    glEnable(GL_BLEND);
    glBlendFuncSeparate(GL_ONE, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

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
    glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
    glStencilFunc(GL_ALWAYS, 0, 0xFF);
    glClearStencil(clip ? 0x00 : 0x80);
    glClear(GL_STENCIL_BUFFER_BIT);

    glStencilFunc(GL_EQUAL, 0x80, 0xFF);
    glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
}

void renderUnbindImage(void *data) {
    fvGLData *ctx = (fvGLData *) data;
    ctx->image0 = 0;
}

void render__triangles(int pos, int length) {
    glDrawElements(GL_TRIANGLES, (GLsizei) (length), GL_UNSIGNED_INT, (void*) (pos * sizeof(int)));
}

void renderFlush(void *data,
                 fvPaint *paints, void* uniforms, int pSize,
                 int* elements, int eSize,
                 float *vtx, float *uvs, int vSize) {
    fvGLData* ctx = (fvGLData*) data;

    glBufferSubData(GL_UNIFORM_BUFFER, 0, pSize * renderAlign(), uniforms);
    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, eSize * sizeof(int), elements);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vSize * sizeof(float), vtx);
    glBufferSubData(GL_ARRAY_BUFFER, ctx->vertex * sizeof(float), vSize * sizeof(float), uvs);

    GLsizei pos = 0;
    for (int i = 0; i < pSize; i++) {
        // Exclude size, antealiasing and images info
        glBindBufferRange(GL_UNIFORM_BUFFER, 0, ctx->ubo, i * renderAlign(), renderAlign());

        glUniformMatrix3fv(ctx->matID, 1, 0, paints[i].mat);

        fvPaint &p = paints[i];
        int aa = p.aa;
        int sd = p.font == NULL ? 0 : p.font->sdf;
        int cv = p.convex;
        int wr = p.winding;
        int op = p.paintOp;

        // Antialiasing
        if (ctx->aa != aa) {
            ctx->aa = aa;
            if (ctx->aa) {
                glEnable(GL_MULTISAMPLE);
            } else {
                glDisable(GL_MULTISAMPLE);
            }
        }

        if (op == CLIP) {
            glColorMask(0, 0, 0, 0);
            glUniform1i(ctx->stcID, 1);
            glStencilFunc(GL_ALWAYS, 0x80, 0x80);
            glStencilMask(0x80);

            if (cv) {
                glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
                render__triangles(pos, p.size - pos);
            } else {
                glStencilOp(GL_INVERT, GL_INVERT, GL_INVERT);
                render__triangles(pos, p.size - pos);
            }

            glStencilFunc(GL_EQUAL, 0x80, 0xFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            glStencilMask(0xFF);
            glColorMask(1, 1, 1, 1);
            glUniform1i(ctx->stcID, 0);
        } else {
            // Images
            if (ctx->image0 != p.image0) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, ctx->image0 = p.image0);
            }

            // Font
            GLuint fntImg = p.font == NULL ? 0 : p.font->imageID;
            if (ctx->image1 != fntImg) {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, ctx->image1 = fntImg);
                if (ctx->sdf != sd) {
                    ctx->sdf = sd;
                    if (ctx->sdf) {
                        glUniform1i(ctx->sdfID, 1);
                    } else {
                        glUniform1i(ctx->sdfID, 0);
                    }
                }
            }

            if (op == TEXT || (op == FILL && cv)) {
                glStencilFunc(GL_EQUAL, 0x80, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                render__triangles(pos, p.size - pos);
            } else if (op == FILL) {

                if (wr == EVEN_ODD) {
                    // Even-Odd

                    glUniform1i(ctx->stcID, 1);
                    glColorMask(0, 0, 0, 0);
                    glStencilFunc(GL_NOTEQUAL, 0x00, 0xFF);
                    glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
                    render__triangles(pos, p.size - pos);

                    glUniform1i(ctx->stcID, 0);
                    glColorMask(1, 1, 1, 1);
                    glStencilFunc(GL_EQUAL, 0x7F, 0xFF);
                    glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
                    render__triangles(pos, p.size - pos);
                } else {
                    // Non-Zero

                    glUniform1i(ctx->stcID, 1);
                    glColorMask(0, 0, 0, 0);
                    glStencilFuncSeparate(GL_FRONT, GL_NOTEQUAL, 0x00, 0xFF);
                    glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR_WRAP);
                    glStencilFuncSeparate(GL_BACK, GL_NOTEQUAL, 0x00, 0xFF);
                    glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_DECR_WRAP);
                    glStencilMask(0x7F);
                    render__triangles(pos, p.size - pos);

                    glUniform1i(ctx->stcID, 0);
                    glColorMask(1, 1, 1, 1);
                    glStencilFunc(GL_LESS, 0x80, 0xFF);
                    glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
                    glStencilMask(0xFF);
                    render__triangles(pos, p.size - pos);
                }

                glStencilFunc(GL_EQUAL, 0x80, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            } else if (op == STROKE) {
                glUniform1i(ctx->stcID, 1);
                glColorMask(0, 0, 0, 0);
                glStencilFunc(GL_EQUAL, 0x80, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
                render__triangles(pos, p.size - pos);

                glUniform1i(ctx->stcID, 0);
                glColorMask(1, 1, 1, 1);
                glStencilFunc(GL_EQUAL, 0x7F, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
                render__triangles(pos, p.size - pos);

                glStencilFunc(GL_EQUAL, 0x80, 0xFF);
                glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            }
        }

        pos = p.size;
    }
}

unsigned long renderCreateFontTexture(int width, int height) {
    glActiveTexture(GL_TEXTURE0);

    GLuint img;
    glGenTextures(1, &img);
    glBindTexture(GL_TEXTURE_2D, img);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glBindTexture(GL_TEXTURE_2D, 0);
    return img;
}

unsigned long renderResizeFontTexture(unsigned long oldImageID, int oldWidth, int oldHeight, int width, int height) {
    glActiveTexture(GL_TEXTURE0);

    GLuint newImg;
    glGenTextures(1, &newImg);
    glBindTexture(GL_TEXTURE_2D, newImg);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, NULL);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glBindTexture(GL_TEXTURE_2D, oldImageID);

    unsigned char* oldData = (unsigned char *) malloc(oldWidth * oldHeight * sizeof(unsigned char));

    glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_UNSIGNED_BYTE, oldData);

    glBindTexture(GL_TEXTURE_2D, newImg);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, oldWidth, oldHeight, GL_RED, GL_UNSIGNED_BYTE, oldData);

    free(oldData);

    GLuint oldImgID = oldImageID;
    glDeleteTextures(1, &oldImgID);

    glBindTexture(GL_TEXTURE_2D, 0);
    return newImg;
}

void renderUpdateFontTexture(unsigned long imageID, void* data, int x, int y, int width, int height) {
    glActiveTexture(GL_TEXTURE0);

    glBindTexture(GL_TEXTURE_2D, imageID);
    glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_RED, GL_UNSIGNED_BYTE, data);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void renderDestroyFontTexture(unsigned long imageID) {
    GLuint imgID = imageID;
    glDeleteTextures(1, &imgID);
}
