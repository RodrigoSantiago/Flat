package test;

import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static class RectanglePacker<P> {

        public enum Fit {
            FAIL,
            PERFECT,
            FIT
        };

        private Node root;

        /**
         * The border to leave around rectangles
         */
        private int border = 0;

        /**
         * Builds a new {@link RectanglePacker}
         *
         * @param width
         *            The width of the space available to pack into
         * @param height
         *            The height of the space available to pack into
         * @param border
         *            The border to preserve between packed items
         */
        public RectanglePacker(int width, int height, int border) {
            root = new Node(new Rectangle(0, 0, width, height));
            this.border = border;
        }

        /**
         * Builds a list of all {@link Rectangle}s in the tree, for debugging
         * purposes
         *
         * @param rectangles
         *            The list to add the tree's {@link Rectangle}s to
         */
        public void inspectRectangles(List<Rectangle> rectangles) {
            root.getRectangles(rectangles);
        }

        /**
         * Finds the {@link Rectangle} where an item is stored
         *
         * @param item
         *            The item to search for
         * @return The {@link Rectangle} where that item resides, or null if not
         *         found
         */
        public Rectangle findRectangle(P item) {
            return root.findRectange(item);
        }

        /**
         * Clears the packer of all items
         */
        public void clear() {
            root = new Node(root.rect);
        }

        /**
         * Attempts to pack an item of the supplied dimensions
         *
         * @param width
         *            The width of the item
         * @param height
         *            The height of the item
         * @param o
         *            The item to pack
         * @return The packed location, or null if it will not fit.
         */
        public Rectangle insert(int width, int height, P o) {
            Node n = root.insert(width + 2 * border, height + 2 * border, o);

            if (n != null) {
                Rectangle r = new Rectangle(n.rect.x + border, n.rect.y + border,
                        n.rect.width - 2 * border, n.rect.height - 2 * border);
                return r;
            } else {
                return null;
            }
        }

        /**
         * Removes an item from the tree, consolidating the space if possible. The
         * space can easily become fragmented, so don't rely on this to work as
         * cleverly as you would like.
         *
         * @param o
         *            the item to remove
         * @return <code>true</code> if the item was found, false otherwise
         */
        public boolean remove(P o) {
            return root.remove(o);
        }

        /**
         * Gets the width of this packer
         *
         * @return the width of this packer
         */
        public int getWidth() {
            return root.rect.width;
        }

        /**
         * Gets the height of this packer
         *
         * @return The height of this packer
         */
        public int getHeight() {
            return root.rect.height;
        }

        private class Node {
            private Rectangle rect;

            private P occupier = null;

            private Node left = null;

            private Node right = null;

            private Node(Rectangle r) {
                this.rect = r;
            }

            private Rectangle findRectange(P item) {
                if (isLeaf()) {
                    if (item == occupier) {
                        return rect;
                    } else {
                        return null;
                    }
                } else {
                    Rectangle l = left.findRectange(item);

                    if (l != null) {
                        return l;
                    } else {
                        return right.findRectange(item);
                    }
                }
            }

            private Node insert(int width, int height, P o) {
                if (!isLeaf()) {
                    Node r = left.insert(width, height, o);

                    if (r == null) {
                        r = right.insert(width, height, o);
                    }

                    return r;
                } else {
                    if (occupier != null) {
                        return null;
                    }

                    Fit fit = fits(width, height);

                    switch (fit) {
                        case FAIL:
                            return null;
                        case PERFECT:
                            occupier = o;
                            return this;
                        case FIT:
                            split(width, height);
                            break;
                    }

                    return left.insert(width, height, o);
                }
            }

            private boolean isLeaf() {
                return left == null;
            }

            /**
             * Determines if this node contains an item, even many levels below
             *
             * @return <code>true</code> if this node or any of it's descendants
             *         holds an item
             */
            private boolean isOccupied() {
                return occupier != null || !isLeaf();
            }

            /**
             * Removes an item, and consolidates the tree if possible
             *
             * @param o
             *            the item to remove
             * @return <code>true</code> if the item was found, <code>false</code>
             *         otherwise
             */
            private boolean remove(P o) {
                if (isLeaf()) {
                    if (occupier == o) {
                        occupier = null;

                        return true;
                    }
                    return false;
                } else {
                    boolean found = left.remove(o);
                    if (!found) {
                        found = right.remove(o);
                    }

                    if (found) {
                        if (!left.isOccupied() && !right.isOccupied()) {
                            left = null;
                            right = null;
                        }
                    }

                    return found;
                }
            }

            private void split(int width, int height) {
                int dw = rect.width - width;
                int dh = rect.height - height;

                assert dw >= 0;
                assert dh >= 0;

                Rectangle r, l;
                if (dw > dh) {
                    l = new Rectangle(rect.x, rect.y, width, rect.height);

                    r = new Rectangle(l.x + width, rect.y, rect.width - width,
                            rect.height);
                } else {
                    l = new Rectangle(rect.x, rect.y, rect.width, height);

                    r = new Rectangle(rect.x, l.y + height, rect.width, rect.height
                            - height);
                }

                left = new Node(l);
                right = new Node(r);
            }

            private Fit fits(int width, int height) {
                if (width <= rect.width && height <= rect.height) {
                    if (width == rect.width && height == rect.height) {
                        return Fit.PERFECT;
                    } else {
                        return Fit.FIT;
                    }
                }

                return Fit.FAIL;
            }

            private void getRectangles(List<Rectangle> rectangles) {
                rectangles.add(rect);

                if (!isLeaf()) {
                    left.getRectangles(rectangles);
                    right.getRectangles(rectangles);
                }
            }
        }

        /**
         * Yet another Rectangle class. Only here to remove dependencies on
         * awt/lwjgl/etc
         *
         * @author ryanm
         */
        public static class Rectangle {
            /**
             *
             */
            public final int x;

            /**
             *
             */
            public final int y;

            /**
             *
             */
            public final int width;

            /**
             *
             */
            public final int height;

            private Rectangle(int x, int y, int width, int height) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }

            private Rectangle(Rectangle r) {
                this.x = r.x;
                this.y = r.y;
                this.width = r.width;
                this.height = r.height;
            }

            @Override
            public String toString() {
                return "[ " + x + ", " + y + ", " + width + ", " + height + " ]";
            }
        }
    }

    public static class ImagePanel extends JPanel{

        private BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
            setSize(image.getWidth(), image.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }
    }

    public static void main(String... args) {
        Teste2.test();
        BufferedImage img = new BufferedImage(1024, 1024, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        ImagePanel im = new ImagePanel(img);
        im.setSize(1024, 1024);
        JButton btn = new JButton("reload");

        JFrame janela = new JFrame("Meu primeiro frame em Java");
        janela.add(btn);
        janela.add(im);
        janela.pack();
        janela.setSize(1024,1024);
        janela.setVisible(true);
        janela.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        btn.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Graphics2D g = img.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0,0,1024,1024);
                int maxx = 0, maxy = 0;
                int areaMax = 0, areaUsed = 0;
                ArrayList<Rectangle> rectangles = new ArrayList<>();
                for (int i = 0; i < 1024; i++) {
                    int w = (int) (Math.random() * 10 + 10);
                    int h = (int) (Math.random() * 14 + 6);
                    areaMax += w * h;
                    rectangles.add(new Rectangle(0,0,w,h));
                }

                Rectangle rect = new Packer().fit(rectangles);

                for (Rectangle rec : rectangles) {
                    g.setColor(new Color(Float.floatToIntBits((float) Math.random())));
                    g.fillRect((int)rec.x + 128, (int)rec.y, (int)rec.width, (int)rec.height);
                    if (rec.x + rec.width > maxx) maxx = (int)rec.x + (int)rec.width;
                    if (rec.y + rec.height > maxy) maxy = (int)rec.y + (int)rec.height;
                }
                areaUsed = maxx*maxy;

                g.setColor(Color.black);
                g.drawRect(128, 0, maxx, maxy);
                g.drawRect(128, 0, (int)rect.width, (int)rect.height);
                g.drawString("Performance : " + ((areaMax/(float)areaUsed) * 100),0, 64);
                janela.repaint();
            }
        });
        btn.setSize(64,24);
    }

    public static Path rec(Path p, int cx, int cy, int w, int h, boolean rigth) {
        if (rigth) {
            p.moveTo(cx - w / 2f, cy + h / 2f);
            p.lineTo(cx + w / 2f, cy + h / 2f);
            p.lineTo(cx + w / 2f, cy - h / 2f);
            p.lineTo(cx - w / 2f, cy - h / 2f);
            p.closePath();
        } else {
            p.moveTo(cx - w / 2f, cy - h / 2f);
            p.lineTo(cx + w / 2f, cy - h / 2f);
            p.lineTo(cx + w / 2f, cy + h / 2f);
            p.lineTo(cx - w / 2f, cy + h / 2f);
            p.closePath();
        }
        return p;
    }

    public static void printAreas(Shape shape) {
        PathIterator it = shape.pathIterator(null);
        while (!it.isDone()) {
            System.out.println(polygonArea(it));
            it.next();
        }
    }

    public static float polygonArea(PathIterator iterator) {
        float sx = 0, sy = 0, x = 0, y = 0;
        float[] coords = new float[6];
        float area = 0;
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO :
                    sx = x = coords[0];
                    sy = y = coords[1];
                    break;
                case PathIterator.SEG_LINETO :
                    area += (x + coords[0]) * (y - coords[1]);
                    x = coords[0];
                    y = coords[1];
                    break;
                case PathIterator.SEG_CLOSE :
                    area += (x + sx) * (y - sy);
                    return area / 2;
            }
            iterator.next();
        }
        return Float.NaN;
    }

}
