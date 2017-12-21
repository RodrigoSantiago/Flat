package flat.graphics.smart;

import flat.graphics.context.Context;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.Texture;
import flat.graphics.context.VertexArray;
import flat.graphics.context.enuns.AttributeType;
import flat.graphics.context.enuns.BlendFunction;
import flat.graphics.context.enuns.VertexMode;
import flat.graphics.smart.effects.ImageMaterial;
import flat.graphics.smart.effects.ImgMatDefault;
import flat.graphics.smart.effects.MaterialValue;
import flat.graphics.smart.effects.RoundRectShadow;
import flat.graphics.smart.image.Image;
import flat.graphics.smart.mesh.Mesh;
import flat.graphics.smart.mesh.VertexData;
import flat.graphics.svg.Ellipse;
import flat.graphics.svg.Rect;
import flat.graphics.svg.RoundRect;
import flat.graphics.text.Align;
import flat.graphics.context.Font;
import flat.math.Affine;
import flat.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

public class SmartContext {

    private final Context context;

    private int mode;

    private Matrix4 projection3D = new Matrix4();
    private Matrix4 projection2D = new Matrix4();

    // ---- IMAGE ---- //
    private Affine transform2D = new Affine();
    private ImgMatDefault imgMatDefault = new ImgMatDefault();

    private ShaderProgram shader2D;
    private List<MaterialValue> matValues2D = new ArrayList<>();

    private VertexData imageBatch;
    private int imageBatchCount;
    private Texture imageBatchAtlas;
    private float[] parser = new float[24];

    RoundRectShadow rectEffect;

    // ---- MODEL ---- //
    private Matrix4 transform3D = new Matrix4();
    private ShaderProgram shader3D;
    private List<MaterialValue> matValues3D = new ArrayList<>();

    public SmartContext(Context context) {
        this.context = context;
        this.context.setSmartContext(this);
        this.imageBatch = new VertexData();
        imageBatch.setVertexSize(1000 * 24 * 4);

        imageBatch.enableAttribute(0, AttributeType.FLOAT_VEC2, (4 * 2) + (4 * 2), 0);
        imageBatch.enableAttribute(1, AttributeType.FLOAT_VEC2, (4 * 2) + (4 * 2), (4 * 2));

        setImageMaterial(null);

        context.setBlendEnabled(true);
        context.setBlendFunction(BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA,
                BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA);

        rectEffect = new RoundRectShadow();
    }

    // ---- CLEAR ---- //

    private void clearMode() {
        if (mode != 0) {
            if (mode == 1) svgEnd();
            if (mode == 2) imageEnd();
            if (mode == 3) meshEnd();
            mode = 0;
        }
    }

    public void softFlush() {
        clearMode();
        context.softFlush();
    }

    public void setView(int x, int y, int width, int height) {
        clearMode();
        context.setViewPort(x, y, width, height);
        projection2D.setToOrtho(x, width, height, y, 0, 1);
    }

    public void clear(int color) {
        clearMode();
        context.setClearColor(color);
        context.clear(true, true, true);
    }

    public void clear(int color, double depth, int stencil) {
        clearMode();
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    // ---- Properties ---- //

    public void setProjection(Matrix4 projection) {
        if (projection == null) {
            this.projection3D.identity();
        } else {
            this.projection3D.set(projection);
        }
    }

    public void setTransform2D(Affine transform2D) {
        if (transform2D == null) {
            this.transform2D.identity();
        } else {
            this.transform2D.set(transform2D);
        }
        context.svgTransform(transform2D);
    }

    public void setTransform3D(Matrix4 transform3D) {
        if (transform3D == null) {
            this.transform3D.identity();
        } else {
            this.transform3D.set(transform3D);
        }
    }

    public void setColor(int color) {
        context.svgColor(color);
    }

    public void setAlpha(float alpha) {
        context.svgAlpha(alpha);
    }

    public void setTextFont(Font font) {
        context.svgTextFont(font);
    }

    public void setTextSize(float size) {
        context.svgTextSize(size);
    }

    public void setTextVerticalAlign(Align.Vertical align) {
        context.svgTextVerticalAlign(align);
    }

    public void setTextHorizontalAlign(Align.Horizontal align) {
        context.svgTextHorizontalAlign(align);
    }

    // ---- SVG ---- //

    private void svgMode() {
        if (mode != 1) {
            if (mode == 2) imageEnd();
            if (mode == 3) meshEnd();
            mode = 1;
        }
    }

    private void svgEnd() {

    }

    public void drawEllipse(Ellipse ellipse, boolean fill) {
        svgMode();
        context.svgDrawEllipse(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), ellipse.getHeight(), fill);
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        svgMode();
        context.svgDrawEllipse(x, y, width, height, fill);
    }

    public void drawRect(Rect rect, boolean fill) {
        svgMode();
        context.svgDrawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), fill);
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        svgMode();
        context.svgDrawRect(x, y, width, height, fill);
    }

    public void drawRoundRect(RoundRect rect, boolean fill) {
        svgMode();
        context.svgDrawRoundRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
                rect.getCornerTop(), rect.getCornerRight(), rect.getCornerBottom(), rect.getCornerLeft(), fill);
    }

    public void drawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        svgMode();
        context.svgDrawRoundRect(x, y, width, height, cTop, cRight, cBottom, cLeft, fill);
    }

    public void drawText(float x, float y, String text) {
        svgMode();
        context.svgDrawText(x, y, text);
    }

    public void drawTextBox(float x, float y, float maxWidth, String text) {
        svgMode();
        context.svgDrawTextBox(x, y, maxWidth, text);
    }

    public void drawTextSlice(float x, float y, float maxWidth, String text) {
        svgMode();
        context.svgDrawTextSlice(x, y, maxWidth, text);
    }

    // ---- IMAGE ---- //

    private void imageMode() {
        if (mode != 2) {
            if (mode == 1) svgEnd();
            if (mode == 3) meshEnd();
            mode = 2;
        }
    }

    private void imageEnd() {
        imageFlush();
    }

    private void imageFlush() {
        if (imageBatchCount > 0) {

            shader2D.begin();
            shader2D.set("view", projection2D);
            shader2D.set("src", 0);
            for (MaterialValue value : matValues2D) {
                shader2D.set(value.name, value.value);
            }

            if (imageBatchAtlas == null) {
                context.clearBindTexture(0);
            } else {
                imageBatchAtlas.begin(0);
            }

            VertexArray vertexArray = imageBatch.getVertexArray();
            vertexArray.begin();
            context.drawArray(VertexMode.TRIANGLES, 0, imageBatchCount * 6, 1);
            vertexArray.end();

            imageBatchCount = 0;
        }
    }

    public void setImageMaterial(ImageMaterial imageMaterial) {
        imageFlush();

        if (imageMaterial == null) {
            imageMaterial = imgMatDefault;
        }

        shader2D = imageMaterial.getShader();
        matValues2D.clear();
        matValues2D.addAll(imageMaterial.getValues());
    }

    public void drawImage(Image image) {
        imageMode();
        drawImage(image, transform2D);
    }

    public void drawImage(Image image, Affine transform) {
        imageMode();
        drawImage(image, transform, 0, 0, image.getWidth(), image.getHeight());
    }

    public void drawImage(Image image, Affine transform, float x, float y, float width, float height) {
        imageMode();
        drawImage(image.getAtlas(), image.getSrcx(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy() + image.getHeight(),
                x, y, x + width, y + height, transform);
    }

    public void drawImage(Texture texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          Affine tr2) {
        imageMode();
        if (texture != imageBatchAtlas || imageBatchCount >= 1000) {
            imageFlush();
        }

        imageBatchAtlas = texture;
        if (tr2 == null) {
            tr2 = transform2D;
        }

        parser[ 0] = tr2.getPointX(dstX1, dstY1); parser[ 1] = tr2.getPointY(dstX1, dstY1); parser[ 2] = srcX1; parser[ 3] = srcY1;
        parser[ 4] = tr2.getPointX(dstX2, dstY1); parser[ 5] = tr2.getPointY(dstX2, dstY1); parser[ 6] = srcX2; parser[ 7] = srcY1;
        parser[ 8] = tr2.getPointX(dstX2, dstY2); parser[ 9] = tr2.getPointY(dstX2, dstY2); parser[10] = srcX2; parser[11] = srcY2;

        parser[12] = tr2.getPointX(dstX2, dstY2); parser[13] = tr2.getPointY(dstX2, dstY2); parser[14] = srcX2; parser[15] = srcY2;
        parser[16] = tr2.getPointX(dstX1, dstY2); parser[17] = tr2.getPointY(dstX1, dstY2); parser[18] = srcX1; parser[19] = srcY2;
        parser[20] = tr2.getPointX(dstX1, dstY1); parser[21] = tr2.getPointY(dstX1, dstY1); parser[22] = srcX1; parser[23] = srcY1;

        imageBatch.setVertices(imageBatchCount * 24 * 4, parser);

        imageBatchCount ++;
    }

    public void drawRoundRectShadow(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft,
                                    float blur, float alpha) {
        rectEffect.setBox(x, y, width, height);
        rectEffect.setAlpha(alpha);
        rectEffect.setBlur(blur);
        rectEffect.setCorners(cTop, cRight, cBottom, cLeft);
        setImageMaterial(rectEffect);
        drawImage(null, 0, 0, 1, 1, x ,y, width, height, transform2D);
        setImageMaterial(null);
    }

    public void drawRoundRectShadow(RoundRect rect, float blur, float alpha) {
        imageMode();
        drawRoundRectShadow(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
                rect.getCornerTop(), rect.getCornerRight(), rect.getCornerBottom(), rect.getCornerLeft(), blur, alpha);
    }

    // ---- MESH ---- //

    private void meshMode() {
        if (mode != 3) {
            if (mode == 1) svgEnd();
            if (mode == 2) imageEnd();
            mode = 3;
        }
    }

    private void meshEnd() {

    }

    public void setMeshMaterial() {

    }

    public void drawMesh(Mesh mesh) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4 transform) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4 transform, int anim, float frame) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4[] transforms, int offset, int length) {
        meshMode();

    }
}
