package test;

import flat.math.shapes.Rectangle;

import java.util.ArrayList;

public class Packer {
    static class Node {
        int x, y, w, h;
        boolean used;
        Node right, down;

        public Node(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    Node root;

    public Rectangle fit(ArrayList<Rectangle> blocks) {
        blocks.sort((o1, o2) -> Double.compare(o2.height, o1.height));

        int w = blocks.size() > 0 ? nextPower(1, (int)blocks.get(0).width) : 1;
        int h = blocks.size() > 0 ? nextPower(1, (int)blocks.get(0).height) : 1;
        this.root = new Node(0, 0, w, h);
        Node node;
        for (int n = 0; n < blocks.size(); n++) {
            Rectangle block = blocks.get(n);
            Node splited;
            if ((node = this.findNode(this.root, (int)block.width, (int)block.height)) != null) {
                splited = this.splitNode(node, (int)block.width, (int)block.height);
            } else {
                splited = this.growNode((int)block.width, (int)block.height);
            }
            block.x = splited.x;
            block.y = splited.y;
        }

        return new Rectangle(0,0,root.w, root.h);
    }

    public Node findNode(Node root, int w, int h) {
        Node node;
        if (root.used) {
            node = this.findNode(root.right, w, h);
            if (node == null) {
                node = this.findNode(root.down, w, h);
            }
            return node;
        } else if ((w <= root.w) && (h <= root.h)) {
            return root;
        } else {
            return null;
        }
    }

    public Node splitNode(Node node, int w, int h) {
        node.used = true;
        node.down = new Node(node.x, node.y + h, node.w, node.h - h);
        node.right = new Node(node.x + w, node.y, node.w - w, h);
        return node;
    }

    public Node growNode(int w, int h) {
        boolean canGrowDown = (w <= this.root.w);
        boolean canGrowRight = (h <= this.root.h);

        boolean shouldGrowRight = canGrowRight && (this.root.h >= (this.root.w + w));   // attempt to keep square-ish by growing right when height is much greater than width
        boolean shouldGrowDown = canGrowDown && (this.root.w >= (this.root.h + h));     // attempt to keep square-ish by growing down  when width  is much greater than height

        if (shouldGrowRight) {
            return this.growRight(w, h);
        } else if (shouldGrowDown) {
            return this.growDown(w, h);
        } else if (canGrowRight) {
            return this.growRight(w, h);
        } else if (canGrowDown) {
            return this.growDown(w, h);
        } else {
            return null; // need to ensure sensible root starting size to avoid this happening
        }
    }

    public Node growRight(int w, int h) {
        int nw = nextPower(root.w, root.w + w) - root.w;

        Node root = this.root;
        this.root = new Node(0, 0, root.w + nw, root.h);
        this.root.used = true;
        this.root.down = root;
        this.root.right = new Node(root.w, 0, nw, root.h);
        Node node  = this.findNode(this.root, w, h);
        if (node != null) {
            return this.splitNode(node, w, h);
        } else {
            return null;
        }
    }

    public Node growDown(int w, int h) {
        int nh = nextPower(root.h, root.h + h) - root.h;

        Node root = this.root;
        this.root = new Node(0, 0, root.w, root.h + nh);
        this.root.used = true;
        this.root.down = new Node(0, root.h, root.w, nh);
        this.root.right = root;
        Node node = this.findNode(this.root, w, h);
        if (node != null) {
            return this.splitNode(node, w, h);
        } else {
            return null;
        }
    }

    private static int nextPower(int val, int next) {
        if (val <= 0) val = 1;
        while (val < next) {
            val += val;
        }
        return val;
    }
}