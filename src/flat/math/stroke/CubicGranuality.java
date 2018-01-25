package flat.math.stroke;

import flat.math.Vector2;

import java.util.ArrayList;

public class CubicGranuality {
    private ArrayList<Double> section = new ArrayList<>();
    private ArrayList<Vector2> points = new ArrayList<>();

    private static final int recursion_limit = 32;
    private static final double curve_collinearity_epsilon = 1e-8;
    private static final double curve_angle_tolerance_epsilon = 0.1f;

    private double distance_tolerance;
    private double approximation_scale = 0.1f;
    private double angle_tolerance = 0.0f;
    private double cusp_limit = 0.0f;

    private boolean pointEnabled;
    private boolean splitEnabled;
    private PointAction action;
    private boolean actionFinished;

    public static float[] getSections(double x1, double y1,
                                      double x2, double y2,
                                      double x3, double y3,
                                      double x4, double y4) {
        CubicGranuality worker = new CubicGranuality();
        worker.setSplitEnabled(true);
        worker.init(x1, y1, x2, y2, x3, y3, x4, y4);
        float[] sections = new float[worker.section.size()];
        for (int i = 0; i < worker.section.size(); i++) {
            sections[i] = (float) (double) worker.section.get(i);
        }
        return sections;
    }

    public static Vector2[] getPoints(double x1, double y1,
                                      double x2, double y2,
                                      double x3, double y3,
                                      double x4, double y4) {
        CubicGranuality worker = new CubicGranuality();
        worker.setPointEnabled(true);
        worker.init(x1, y1, x2, y2, x3, y3, x4, y4);
        return worker.points.toArray(new Vector2[worker.points.size()]);
    }

    public static Object[] getAll(double x1, double y1,
                                  double x2, double y2,
                                  double x3, double y3,
                                  double x4, double y4) {
        CubicGranuality worker = new CubicGranuality(true);
        worker.setPointEnabled(true);
        worker.setSplitEnabled(true);
        worker.init(x1, y1, x2, y2, x3, y3, x4, y4);
        float[] sections = new float[worker.section.size()];
        for (int i = 0; i < worker.section.size(); i++) {
            sections[i] = (float) (double) worker.section.get(i);
        }
        Vector2[] points = worker.points.toArray(new Vector2[worker.points.size()]);
        return new Object[]{sections, points};
    }

    public CubicGranuality() {
    }

    public CubicGranuality(boolean pointEnabled) {
        this.pointEnabled = pointEnabled;
    }

    public CubicGranuality(boolean pointEnabled, double approximation_scale, double angle_tolerance, double cusp_limit) {
        this.pointEnabled = pointEnabled;
        this.approximation_scale = approximation_scale;
        this.angle_tolerance = angle_tolerance;
        this.cusp_limit = cusp_limit;
    }

    public CubicGranuality(PointAction action) {
        this.action = action;
    }

    public CubicGranuality(PointAction action, double approximation_scale, double angle_tolerance, double cusp_limit) {
        this.action = action;
        this.approximation_scale = approximation_scale;
        this.angle_tolerance = angle_tolerance;
        this.cusp_limit = cusp_limit;
    }

    public double getApproximation_scale() {
        return approximation_scale;
    }

    public void setApproximation_scale(double approximation_scale) {
        this.approximation_scale = approximation_scale;
    }

    public double getAngle_tolerance() {
        return angle_tolerance;
    }

    public void setAngle_tolerance(double angle_tolerance) {
        this.angle_tolerance = angle_tolerance;
    }

    public double getCusp_limit() {
        return cusp_limit;
    }

    public void setCusp_limit(double cusp_limit) {
        this.cusp_limit = cusp_limit;
    }

    public boolean isPointEnabled() {
        return pointEnabled;
    }

    public void setPointEnabled(boolean pointEnabled) {
        this.pointEnabled = pointEnabled;
    }

    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(boolean splitEnabled) {
        this.splitEnabled = splitEnabled;
    }

    public PointAction getAction() {
        return action;
    }

    public void setAction(PointAction action) {
        this.action = action;
    }

    //------------------------------------------------------------------------
    public void init(double x1, double y1,
                      double x2, double y2,
                      double x3, double y3,
                      double x4, double y4) {
        distance_tolerance = 0.5 / approximation_scale;
        distance_tolerance *= distance_tolerance;
        bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    //------------------------------------------------------------------------
    private void bezier(double x1, double y1,
                        double x2, double y2,
                        double x3, double y3,
                        double x4, double y4) {
        point_type(x1, y1, 0, 0, 0.5);
        recursive_bezier(x1, y1, x2, y2, x3, y3, x4, y4, 0, 0, 1);
        point_type(x4, y4, 0.5, 1, 1);
    }

    //------------------------------------------------------------------------
    private void recursive_bezier(double x1, double y1,
                                  double x2, double y2,
                                  double x3, double y3,
                                  double x4, double y4,
                                  int level, double pt, double nt) {
        if (level > recursion_limit || actionFinished) {
            return;
        }
        double t = (pt + nt) / 2d;

        // Calculate all the mid-points of the line segments
        //----------------------
        double x12 = (x1 + x2) / 2;
        double y12 = (y1 + y2) / 2;
        double x23 = (x2 + x3) / 2;
        double y23 = (y2 + y3) / 2;
        double x34 = (x3 + x4) / 2;
        double y34 = (y3 + y4) / 2;
        double x123 = (x12 + x23) / 2;
        double y123 = (y12 + y23) / 2;
        double x234 = (x23 + x34) / 2;
        double y234 = (y23 + y34) / 2;
        double x1234 = (x123 + x234) / 2;
        double y1234 = (y123 + y234) / 2;

        // Enforce subdivision first time
        if (level > 0) {
            // Try to approximate the full cubic curve by a single straight line
            //------------------
            double dx = x4 - x1;
            double dy = y4 - y1;

            double d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
            double d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));

            double da1, da2;

            if (d2 > curve_collinearity_epsilon && d3 > curve_collinearity_epsilon) {
                // Regular care
                //-----------------
                if ((d2 + d3) * (d2 + d3) <= distance_tolerance * (dx * dx + dy * dy)) {
                    // If the curvature doesn't exceed the distance_tolerance value
                    // we tend to finish subdivisions.
                    //----------------------
                    if (angle_tolerance < curve_angle_tolerance_epsilon) {
                        point_type(x1234, y1234, pt, t, nt);
                        return;
                    }

                    // Angle & Cusp Condition
                    //----------------------
                    double a23 = Math.atan2(y3 - y2, x3 - x2);
                    da1 = Math.abs(a23 - Math.atan2(y2 - y1, x2 - x1));
                    da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - a23);
                    if (da1 >= Math.PI) da1 = 2 * Math.PI - da1;
                    if (da2 >= Math.PI) da2 = 2 * Math.PI - da2;

                    if (da1 + da2 < angle_tolerance) {
                        // Finally we can stop the recursion
                        //----------------------
                        point_type(x1234, y1234, pt, t, nt);
                        return;
                    }

                    if (cusp_limit != 0.0) {
                        if (da1 > cusp_limit) {
                            point_type(x2, y2, pt, t, nt);
                            return;
                        }

                        if (da2 > cusp_limit) {
                            point_type(x3, y3, pt, t, nt);
                            return;
                        }
                    }
                }
            } else {
                if (d2 > curve_collinearity_epsilon) {
                    // p1,p3,p4 are collinear, p2 is considerable
                    //----------------------
                    if (d2 * d2 <= distance_tolerance * (dx * dx + dy * dy)) {
                        if (angle_tolerance < curve_angle_tolerance_epsilon) {
                            point_type(x1234, y1234, pt, t, nt);
                            return;
                        }

                        // Angle Condition
                        //----------------------
                        da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                        if (da1 >= Math.PI) da1 = 2 * Math.PI - da1;

                        if (da1 < angle_tolerance) {
                            point_type(x2, y2, pt, t, nt);
                            point_type(x3, y3, pt, t, nt);
                            return;
                        }

                        if (cusp_limit != 0.0) {
                            if (da1 > cusp_limit) {
                                point_type(x2, y2, pt, t, nt);
                                return;
                            }
                        }
                    }
                } else if (d3 > curve_collinearity_epsilon) {
                    // p1,p2,p4 are collinear, p3 is considerable
                    //----------------------
                    if (d3 * d3 <= distance_tolerance * (dx * dx + dy * dy)) {
                        if (angle_tolerance < curve_angle_tolerance_epsilon) {
                            point_type(x1234, y1234, pt, t, nt);
                            return;
                        }

                        // Angle Condition
                        //----------------------
                        da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
                        if (da1 >= Math.PI) da1 = 2 * Math.PI - da1;

                        if (da1 < angle_tolerance) {
                            point_type(x2, y2, pt, t, nt);
                            point_type(x3, y3, pt, t, nt);
                            return;
                        }

                        if (cusp_limit != 0.0) {
                            if (da1 > cusp_limit) {
                                point_type(x3, y3, pt, t, nt);
                                return;
                            }
                        }
                    }
                } else {
                    // Collinear case
                    //-----------------
                    dx = x1234 - (x1 + x4) / 2;
                    dy = y1234 - (y1 + y4) / 2;
                    if (dx * dx + dy * dy <= distance_tolerance) {
                        point_type(x1234, y1234, pt, t, nt);
                        return;
                    }
                }
            }
        }

        // Continue subdivision
        //----------------------
        recursive_bezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1, pt, t);
        recursive_bezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1, t, nt);
    }

    private void point_type(double x, double y, double pt, double t, double nt) {
        if (action != null) {
            actionFinished = action.onPointFound((float) x, (float) y, (float) pt, (float) t, (float) nt);
        } else {
            if (pointEnabled) {
                points.add(new Vector2((float) x, (float) y));
            }
            if (splitEnabled) {
                section.add(t);
            }
        }
    }

    public static interface PointAction {
        boolean onPointFound(float x, float y, float pt, float t, float nt);
    }
}